package ind.finance.bourse.data.remote

import ind.finance.bourse.data.model.StockResponse
import ind.finance.bourse.data.model.StockTrendResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StockServiceApi {
    @GET("api/stocks")
    suspend fun getStocks(): Response<List<StockResponse>>

    @GET("api/stocks/{symbol}")
    suspend fun getStock(@Path("symbol") symbol: String): Response<StockResponse>

    @GET("market/trend/{symbol}")
    suspend fun getStockTrend(@Path("symbol") symbol: String): Response<StockTrendResponse>
}
