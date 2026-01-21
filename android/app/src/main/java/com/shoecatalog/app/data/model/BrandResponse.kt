package com.shoecatalog.app.data.model

import com.google.gson.annotations.SerializedName

data class BrandResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("brand") val brand: Brand
)

