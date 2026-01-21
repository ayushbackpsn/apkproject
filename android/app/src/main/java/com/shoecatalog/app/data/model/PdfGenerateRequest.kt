package com.shoecatalog.app.data.model

import com.google.gson.annotations.SerializedName

data class PdfGenerateRequest(
    @SerializedName("product_ids") val productIds: List<String>
)

