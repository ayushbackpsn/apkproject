package com.shoecatalog.app.data.model

import com.google.gson.annotations.SerializedName

data class Brand(
    @SerializedName("_id") val id: String,
    @SerializedName("brand_name") val brandName: String
)

