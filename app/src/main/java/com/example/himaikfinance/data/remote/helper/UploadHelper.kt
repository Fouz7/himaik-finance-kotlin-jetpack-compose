package com.example.himaikfinance.data.remote.helper

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun buildEvidenceFilePart(file: File): MultipartBody.Part {
    val mediaType = when (file.extension.lowercase()) {
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        else -> "application/octet-stream"
    }.toMediaType()

    val requestBody = file.asRequestBody(mediaType)
    return MultipartBody.Part.createFormData("file", file.name, requestBody)
}