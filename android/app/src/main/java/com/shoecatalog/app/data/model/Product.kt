package com.shoecatalog.app.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("_id") val id: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("brand_name") val brandName: String,
    @SerializedName("product_image") val productImage: String
)

