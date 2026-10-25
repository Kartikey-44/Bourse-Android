package ind.finance.bourse.data.remote

import android.content.Context
import ind.finance.bourse.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL =
        "http://bourse-alb-546387840.ap-south-1.elb.amazonaws.com/"

    private var retrofit: Retrofit? = null

    fun getInstance(context: Context): UserServiceApi {

        if (retrofit == null) {

            val tokenManager =
                TokenManager(context.applicationContext)

            val authInterceptor =
                AuthInterceptor(tokenManager)

            val logging =
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.NONE
                }

            val client =
                OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

            retrofit =
                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(
                        GsonConverterFactory.create()
                    )
                    .build()
        }

        return retrofit!!.create(UserServiceApi::class.java)
    }

    fun getPortfolioService(context: Context): PortfolioServiceApi {

        if (retrofit == null) {
            getInstance(context)
        }

        return retrofit!!.create(
            PortfolioServiceApi::class.java
        )
    }

    fun getStockService(context: Context): StockServiceApi {

        if (retrofit == null) {
            getInstance(context)
        }

        return retrofit!!.create(
            StockServiceApi::class.java
        )
    }

    fun getTradingService(context: Context): TradingServiceApi {

        if (retrofit == null) {
            getInstance(context)
        }

        return retrofit!!.create(
            TradingServiceApi::class.java
        )
    }
}