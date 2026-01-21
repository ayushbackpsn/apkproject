package com.shoecatalog.app.data.model

import com.google.gson.annotations.SerializedName

data class PdfGenerateResponse(
    val message: String,
    @SerializedName("pdf_id") val pdfId: String,
    val filename: String,
    @SerializedName("download_url") val downloadUrl: String
)

