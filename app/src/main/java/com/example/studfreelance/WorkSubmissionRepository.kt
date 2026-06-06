package com.example.studfreelance

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object WorkSubmissionRepository {

    private const val PREF_NAME = "studfreelance_submissions"
    private const val KEY_SUBMISSIONS = "submissions"

    private lateinit var prefs: SharedPreferences
    private val _submissions = mutableListOf<WorkSubmission>()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadFromDisk()
    }

    fun addSubmission(submission: WorkSubmission) {
        _submissions.removeAll { it.jobId == submission.jobId && it.freelancerId == submission.freelancerId }
        _submissions.add(0, submission)
        saveToDisk()
    }

    fun getSubmissionByJob(jobId: Int): WorkSubmission? =
        _submissions.find { it.jobId == jobId }

    fun getSubmissionsByFreelancer(freelancerId: String): List<WorkSubmission> =
        _submissions.filter { it.freelancerId == freelancerId }

    fun updateRating(jobId: Int, rating: Int, review: String) {
        val idx = _submissions.indexOfFirst { it.jobId == jobId }
        if (idx >= 0) {
            _submissions[idx] = _submissions[idx].copy(rating = rating, review = review)
            saveToDisk()
        }
    }
    fun updateStatus(jobId: Int, status: String) {
        val idx = _submissions.indexOfFirst { it.jobId == jobId }
        if (idx >= 0) {
            _submissions[idx] = _submissions[idx].copy(status = status)
            saveToDisk()
        }
    }
    fun clearAll() {
        _submissions.clear()
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
        // Dummy submission untuk Edit Video Dokumentasi (id=3)
        _submissions.add(
            WorkSubmission(
                id             = "sub_dummy_3",
                jobId          = 3,
                jobTitle       = "Edit Video Dokumentasi",
                freelancerId   = "dummy",
                freelancerName = "Dummy Freelancer",
                description    = "Video sudah diedit sesuai permintaan.",
                submittedAt    = "10:00, 01 Jun",
                status         = "paid",
                rating         = 4,
                review         = "Hasil edit bagus dan tepat waktu!"
            )
        )
        saveToDisk()
    }

    private fun saveToDisk() {
        if (!::prefs.isInitialized) return
        val arr = JSONArray()
        _submissions.forEach { s ->
            arr.put(JSONObject().apply {
                put("id",             s.id)
                put("jobId",          s.jobId)
                put("jobTitle",       s.jobTitle)
                put("freelancerId",   s.freelancerId)
                put("freelancerName", s.freelancerName)
                put("description",    s.description)
                put("fileUrl",        s.fileUrl)
                put("fileName",       s.fileName)
                put("submittedAt",    s.submittedAt)
                put("status",         s.status)
                put("rating", s.rating)
                put("review", s.review)
            })
        }
        prefs.edit().putString(KEY_SUBMISSIONS, arr.toString()).apply()
    }

    private fun loadFromDisk() {
        if (!::prefs.isInitialized) return
        val json = prefs.getString(KEY_SUBMISSIONS, null) ?: return
        try {
            val arr = JSONArray(json)
            _submissions.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                _submissions.add(WorkSubmission(
                    id             = o.getString("id"),
                    jobId          = o.getInt("jobId"),
                    jobTitle       = o.getString("jobTitle"),
                    freelancerId   = o.getString("freelancerId"),
                    freelancerName = o.getString("freelancerName"),
                    description    = o.getString("description"),
                    fileUrl        = o.getString("fileUrl"),
                    fileName       = o.getString("fileName"),
                    submittedAt    = o.getString("submittedAt"),
                    status         = o.getString("status"),
                    rating = o.optInt("rating", 0),
                    review = o.optString("review", "")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}