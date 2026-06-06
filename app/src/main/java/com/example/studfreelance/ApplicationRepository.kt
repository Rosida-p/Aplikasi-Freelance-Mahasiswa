package com.example.studfreelance

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

enum class ApplicationStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class JobApplication(
    val id: String,
    val jobId: Int,
    val jobTitle: String,
    val freelancerId: String,
    val freelancerName: String,
    val freelancerMajor: String,
    val freelancerUniversity: String,
    val freelancerRating: Float,
    val freelancerSkills: List<String>,
    val freelancerBio: String,
    val freelancerAvatarInitial: String,
    val freelancerAvatarColor: Long,
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val appliedAt: String = ""
)

enum class NotificationType {
    APPLICATION_SENT,
    APPLICATION_ACCEPTED,
    APPLICATION_REJECTED,
    NEW_APPLICANT,
    WORK_SUBMITTED,
    WORK_APPROVED,
    WORK_REVISION,
    PAYMENT_RECEIVED,
    RATING_GIVEN
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: String = ""
)

object ApplicationRepository {

    private const val PREF_NAME        = "studfreelance_applications"
    private const val KEY_APPLICATIONS = "applications"
    private const val KEY_FL_NOTIFS    = "freelancer_notifications"
    private const val KEY_CL_NOTIFS    = "client_notifications"

    private lateinit var prefs: SharedPreferences

    private val _applications            = mutableListOf<JobApplication>()
    private val _freelancerNotifications = mutableListOf<AppNotification>()
    private val _clientNotifications     = mutableListOf<AppNotification>()
    private val listeners                = mutableListOf<() -> Unit>()

    // ── INIT (dipanggil dari StudFreelanceApp) ──────────────────────────────
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadFromDisk()
    }

    fun clearAll() {
        _applications.clear()
        _freelancerNotifications.clear()
        _clientNotifications.clear()
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
    }

    // ── LISTENERS ───────────────────────────────────────────────────────────
    fun addListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
    private fun notifyListeners() { listeners.forEach { it() } }

    // ── LAMARAN ─────────────────────────────────────────────────────────────
    fun applyJob(application: JobApplication): Boolean {
        val alreadyApplied = _applications.any {
            it.jobId == application.jobId && it.freelancerId == application.freelancerId
        }
        if (alreadyApplied) return false

        _applications.add(application)
        JobRepository.incrementPelamar(application.jobId.toString())

        addFreelancerNotification(AppNotification(
            id        = "notif_fl_${System.currentTimeMillis()}",
            type      = NotificationType.APPLICATION_SENT,
            title     = "Lamaran Terkirim! 🎉",
            message   = "Lamaranmu untuk \"${application.jobTitle}\" telah dikirim. Tunggu konfirmasi dari penyedia.",
            timestamp = getCurrentTime()
        ))
        addClientNotification(AppNotification(
            id        = "notif_cl_${System.currentTimeMillis()}",
            type      = NotificationType.NEW_APPLICANT,
            title     = "Pelamar Baru!",
            message   = "${application.freelancerName} melamar pekerjaan \"${application.jobTitle}\".",
            timestamp = getCurrentTime()
        ))

        saveToDisk()
        notifyListeners()
        return true
    }

    fun acceptApplication(applicationId: String) {
        val idx = _applications.indexOfFirst { it.id == applicationId }
        if (idx < 0) return
        val app = _applications[idx]
        _applications[idx] = app.copy(status = ApplicationStatus.ACCEPTED)

        addFreelancerNotification(AppNotification(
            id        = "notif_fl_acc_${System.currentTimeMillis()}",
            type      = NotificationType.APPLICATION_ACCEPTED,
            title     = "Lamaran Diterima! 🥳",
            message   = "Selamat! Lamaranmu untuk \"${app.jobTitle}\" diterima.",
            timestamp = getCurrentTime()
        ))

        saveToDisk()
        notifyListeners()
    }

    fun rejectApplication(applicationId: String) {
        val idx = _applications.indexOfFirst { it.id == applicationId }
        if (idx < 0) return
        val app = _applications[idx]
        _applications[idx] = app.copy(status = ApplicationStatus.REJECTED)

        addFreelancerNotification(AppNotification(
            id        = "notif_fl_rej_${System.currentTimeMillis()}",
            type      = NotificationType.APPLICATION_REJECTED,
            title     = "Lamaran Tidak Diterima",
            message   = "Lamaran untuk \"${app.jobTitle}\" belum berhasil. Coba job lainnya!",
            timestamp = getCurrentTime()
        ))

        saveToDisk()
        notifyListeners()
    }

    // ── QUERY ───────────────────────────────────────────────────────────────
    fun getAllApplications(): List<JobApplication> = _applications.toList()

    fun getApplicationsByJob(jobId: Int): List<JobApplication> =
        _applications.filter { it.jobId == jobId }

    fun getApplicationsByFreelancer(freelancerId: String): List<JobApplication> =
        _applications.filter { it.freelancerId == freelancerId }

    fun hasApplied(jobId: Int, freelancerId: String): Boolean =
        _applications.any { it.jobId == jobId && it.freelancerId == freelancerId }

    fun getApplicationStatus(jobId: Int, freelancerId: String): ApplicationStatus? =
        _applications.find { it.jobId == jobId && it.freelancerId == freelancerId }?.status

    // ── NOTIFIKASI ──────────────────────────────────────────────────────────
    fun getFreelancerNotifications(): List<AppNotification> =
        _freelancerNotifications.toList().sortedByDescending { it.id }

    fun getClientNotifications(): List<AppNotification> =
        _clientNotifications.toList().sortedByDescending { it.id }

    fun unreadFreelancerCount(): Int =
        _freelancerNotifications.count { !it.isRead }

    fun markFreelancerNotificationsRead() {
        val updated = _freelancerNotifications.map { it.copy(isRead = true) }
        _freelancerNotifications.clear()
        _freelancerNotifications.addAll(updated)
        saveToDisk()
    }

    fun addFreelancerNotification(notif: AppNotification) {        _freelancerNotifications.add(0, notif)
        saveToDisk()
    }

    fun addClientNotification(notif: AppNotification) {        _clientNotifications.add(0, notif)
        saveToDisk()
    }

    // ── SAVE TO DISK ────────────────────────────────────────────────────────
    private fun saveToDisk() {
        if (!::prefs.isInitialized) return
        prefs.edit().apply {
            putString(KEY_APPLICATIONS, applicationsToJson())
            putString(KEY_FL_NOTIFS,    notifsToJson(_freelancerNotifications))
            putString(KEY_CL_NOTIFS,    notifsToJson(_clientNotifications))
            apply()
        }
    }

    // ── LOAD FROM DISK ──────────────────────────────────────────────────────
    private fun loadFromDisk() {
        if (!::prefs.isInitialized) return

        // Load applications
        val appsJson = prefs.getString(KEY_APPLICATIONS, null)
        if (appsJson != null) {
            _applications.clear()
            _applications.addAll(applicationsFromJson(appsJson))
        }

        // Load notifications
        val flNotifsJson = prefs.getString(KEY_FL_NOTIFS, null)
        if (flNotifsJson != null) {
            _freelancerNotifications.clear()
            _freelancerNotifications.addAll(notifsFromJson(flNotifsJson))
        }

        val clNotifsJson = prefs.getString(KEY_CL_NOTIFS, null)
        if (clNotifsJson != null) {
            _clientNotifications.clear()
            _clientNotifications.addAll(notifsFromJson(clNotifsJson))
        }
    }

    // ── JSON SERIALIZATION ──────────────────────────────────────────────────
    private fun applicationsToJson(): String {
        val arr = JSONArray()
        _applications.forEach { app ->
            val obj = JSONObject().apply {
                put("id",                   app.id)
                put("jobId",                app.jobId)
                put("jobTitle",             app.jobTitle)
                put("freelancerId",         app.freelancerId)
                put("freelancerName",       app.freelancerName)
                put("freelancerMajor",      app.freelancerMajor)
                put("freelancerUniversity", app.freelancerUniversity)
                put("freelancerRating",     app.freelancerRating)
                put("freelancerBio",        app.freelancerBio)
                put("freelancerAvatarInitial", app.freelancerAvatarInitial)
                put("freelancerAvatarColor",   app.freelancerAvatarColor)
                put("status",               app.status.name)
                put("appliedAt",            app.appliedAt)
                val skillsArr = JSONArray()
                app.freelancerSkills.forEach { skillsArr.put(it) }
                put("freelancerSkills", skillsArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun applicationsFromJson(json: String): List<JobApplication> {
        val result = mutableListOf<JobApplication>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val skillsArr = obj.getJSONArray("freelancerSkills")
                val skills = (0 until skillsArr.length()).map { skillsArr.getString(it) }
                result.add(JobApplication(
                    id                    = obj.getString("id"),
                    jobId                 = obj.getInt("jobId"),
                    jobTitle              = obj.getString("jobTitle"),
                    freelancerId          = obj.getString("freelancerId"),
                    freelancerName        = obj.getString("freelancerName"),
                    freelancerMajor       = obj.getString("freelancerMajor"),
                    freelancerUniversity  = obj.getString("freelancerUniversity"),
                    freelancerRating      = obj.getDouble("freelancerRating").toFloat(),
                    freelancerSkills      = skills,
                    freelancerBio         = obj.getString("freelancerBio"),
                    freelancerAvatarInitial = obj.getString("freelancerAvatarInitial"),
                    freelancerAvatarColor = obj.getLong("freelancerAvatarColor"),
                    status                = ApplicationStatus.valueOf(obj.getString("status")),
                    appliedAt             = obj.getString("appliedAt")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun notifsToJson(notifs: List<AppNotification>): String {
        val arr = JSONArray()
        notifs.forEach { notif ->
            arr.put(JSONObject().apply {
                put("id",        notif.id)
                put("type",      notif.type.name)
                put("title",     notif.title)
                put("message",   notif.message)
                put("isRead",    notif.isRead)
                put("timestamp", notif.timestamp)
            })
        }
        return arr.toString()
    }

    private fun notifsFromJson(json: String): List<AppNotification> {
        val result = mutableListOf<AppNotification>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(AppNotification(
                    id        = obj.getString("id"),
                    type      = NotificationType.valueOf(obj.getString("type")),
                    title     = obj.getString("title"),
                    message   = obj.getString("message"),
                    isRead    = obj.getBoolean("isRead"),
                    timestamp = obj.getString("timestamp")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // ── HELPER ──────────────────────────────────────────────────────────────
    private fun getCurrentTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm, dd MMM", java.util.Locale("id"))
        return sdf.format(java.util.Date())
    }
}