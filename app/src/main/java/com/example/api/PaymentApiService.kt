package com.example.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class CreatePaymentIntentRequest(
    val packName: String,
    val points: Int,
    val priceLabel: String
)

data class CreatePaymentIntentResponse(
    val clientSecret: String
)

interface PaymentApiService {
    @POST("create-payment-intent")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequest
    ): CreatePaymentIntentResponse
}

object PaymentApi {
    fun create(baseUrl: String): PaymentApiService {
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PaymentApiService::class.java)
    }
}
