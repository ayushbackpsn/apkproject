package com.shoecatalog.app.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("message") val message: String,
    @SerializedName("product") val product: Product
)

