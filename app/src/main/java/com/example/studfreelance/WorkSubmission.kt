package com.example.studfreelance

data class WorkSubmission(
    val id: String,
    val jobId: Int,
    val jobTitle: String,
    val freelancerId: String,
    val freelancerName: String,
    val description: String,
    val fileUrl: String = "",
    val fileName: String = "",
    val submittedAt: String = "",
    val status: String = "pending",
    val rating: Int = 0,
    val review: String = ""
)