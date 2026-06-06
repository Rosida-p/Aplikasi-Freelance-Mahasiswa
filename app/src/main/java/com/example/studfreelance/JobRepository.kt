package com.example.studfreelance

import android.content.Context
import android.content.SharedPreferences
import com.example.studfreelance.Client.PostedJob
import org.json.JSONArray
import org.json.JSONObject

object JobRepository {

    private const val PREF_NAME  = "studfreelance_jobs"
    private const val KEY_JOBS   = "jobs"

    private lateinit var prefs: SharedPreferences

    // Data awal — hanya dipakai kalau belum ada data tersimpan di disk
    private val defaultJobs = listOf(
        PostedJob(1, "Desain Brosur Kampus",     "Rp200.000",   "3 Hari", "Desain",    "active", 0),
        PostedJob(2, "Pembuatan Website Toko",   "Rp1.500.000", "7 Hari", "Web Dev",   "review", 0),
        PostedJob(3, "Edit Video Dokumentasi",   "Rp350.000",   "2 Hari", "Video",     "done",   0),
        PostedJob(4, "Terjemahan Dokumen EN-ID", "Rp120.000",   "1 Hari", "Penulisan", "active", 0)
    )

    private val _jobs  = mutableListOf<PostedJob>()
    private var nextId = 5

    // ── INIT (dipanggil dari StudFreelanceApp) ──────────────────────────────
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadFromDisk()
    }

    fun clearAll() {
        _jobs.clear()
        nextId = 5
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
        _jobs.addAll(defaultJobs)
        saveToDisk()
    }
    // ── PUBLIC API ──────────────────────────────────────────────────────────
    fun getAllJobs(): List<PostedJob> = _jobs.toList()

    fun addJob(judul: String, budget: String, deadline: String, kategori: String) {
        _jobs.add(0, PostedJob(
            id            = nextId++,
            judul         = judul,
            budget        = budget,
            deadline      = deadline,
            kategori      = kategori,
            status        = "active",
            jumlahPelamar = 0
        ))
        saveToDisk()
    }

    fun incrementPelamar(jobId: String) {
        val idx = _jobs.indexOfFirst { it.id.toString() == jobId }
        if (idx >= 0) {
            _jobs[idx] = _jobs[idx].copy(jumlahPelamar = _jobs[idx].jumlahPelamar + 1)
            saveToDisk()
        }
    }

    fun getJobById(jobId: String): PostedJob? =
        _jobs.find { it.id.toString() == jobId }

    fun countActive()  = _jobs.count { it.status == "active" }
    fun countDone()    = _jobs.count { it.status == "done" }
    fun totalPelamar() = _jobs.sumOf { it.jumlahPelamar }

    fun updateJobStatus(jobId: Int, status: String) {
        val idx = _jobs.indexOfFirst { it.id == jobId }
        if (idx >= 0) {
            _jobs[idx] = _jobs[idx].copy(status = status)
            saveToDisk()
        }
    }

    // ── SAVE TO DISK ────────────────────────────────────────────────────────
    private fun saveToDisk() {
        if (!::prefs.isInitialized) return
        val arr = JSONArray()
        _jobs.forEach { job ->
            arr.put(JSONObject().apply {
                put("id",            job.id)
                put("judul",         job.judul)
                put("budget",        job.budget)
                put("deadline",      job.deadline)
                put("kategori",      job.kategori)
                put("status",        job.status)
                put("jumlahPelamar", job.jumlahPelamar)
            })
        }
        prefs.edit().putString(KEY_JOBS, arr.toString()).apply()
    }

    // ── LOAD FROM DISK ──────────────────────────────────────────────────────
    private fun loadFromDisk() {
        if (!::prefs.isInitialized) return
        val json = prefs.getString(KEY_JOBS, null)
        if (json == null) {
            // Pertama kali — pakai data default
            _jobs.clear()
            _jobs.addAll(defaultJobs)
            saveToDisk()
            return
        }
        try {
            val arr = JSONArray(json)
            _jobs.clear()
            var maxId = 4
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getInt("id")
                if (id > maxId) maxId = id
                _jobs.add(PostedJob(
                    id            = id,
                    judul         = obj.getString("judul"),
                    budget        = obj.getString("budget"),
                    deadline      = obj.getString("deadline"),
                    kategori      = obj.getString("kategori"),
                    status        = obj.getString("status"),
                    jumlahPelamar = obj.getInt("jumlahPelamar")
                ))
            }
            nextId = maxId + 1
        } catch (e: Exception) {
            // Kalau corrupt, fallback ke default
            _jobs.clear()
            _jobs.addAll(defaultJobs)
            e.printStackTrace()
        }
    }
}