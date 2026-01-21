package com.shoecatalog.app.api

import com.shoecatalog.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("brands")
    suspend fun getBrands(): Response<List<Brand>>

    @POST("brands")
    suspend fun createBrand(@Body request: Map<String, String>): Response<BrandResponse>

    @GET("brands/{id}/products")
    suspend fun getProductsByBrand(@Path("id") brandId: String): Response<List<Product>>

    @Multipart
    @POST("products")
    suspend fun uploadProduct(
        @Part("product_name") productName: RequestBody,
        @Part("brand_name") brandName: RequestBody,
        @Part product_image: MultipartBody.Part
    ): Response<ProductResponse>

    @POST("pdf/generate")
    suspend fun generatePdf(@Body request: PdfGenerateRequest): Response<PdfGenerateResponse>
}

