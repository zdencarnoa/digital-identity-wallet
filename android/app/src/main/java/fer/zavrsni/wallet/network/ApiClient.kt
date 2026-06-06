package fer.zavrsni.wallet.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory


/**
 Objekt zaduzen za konfiguraciju klijenta i pruzane pristupa API sucelju aplikacije
 **/
object ApiClient {

    //Adresa backend posluzitelja
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    //Logging interceptor koji omogucava da HTTP zahtjevi i odgovori budu vidljivi pri debugiranju aplikacije
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    /**
      Konfiguracija retrofit instance koja sadrzi osnovni URL backenda, OkHttp klijent i serialization konverter za JSON
     **/
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    //Implementacija wallet api sucelja
    val walletApi: WalletApi by lazy {
        retrofit.create(WalletApi::class.java)
    }
}