package com.example.studfreelance

data class Job(
    val judul: String,
    val deskripsi: String,
    val budget: String,
    val deadline: String,
    val status: String // "open" atau "done"
)