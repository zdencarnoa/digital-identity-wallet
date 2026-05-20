package fer.zavrsni.wallet.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    //Logging interceptor samo da se json vidi pri debagiranju
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val walletApi: WalletApi by lazy {
        retrofit.create(WalletApi::class.java)
    }
}