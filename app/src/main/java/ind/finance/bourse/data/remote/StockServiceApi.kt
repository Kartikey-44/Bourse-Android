package ind.finance.bourse.data.remote

import ind.finance.bourse.data.model.PriceHistoryPoint
import ind.finance.bourse.data.model.StockResponse
import ind.finance.bourse.data.model.StockTrendResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StockServiceApi {
    @GET("api/stocks")
    suspend fun getStocks(): Response<List<StockResponse>>

    @GET("api/stocks/{symbol}")
    suspend fun getStock(@Path("symbol") symbol: String): Response<StockResponse>

    @GET("market/trend/{symbol}")
    suspend fun getStockTrend(@Path("symbol") symbol: String): Response<StockTrendResponse>

    @GET("api/stocks/{symbol}/history")
    suspend fun getStockPriceHistory(
        @Path("symbol") symbol: String,
        @Query("limit") limit: Int = 100
    ): Response<List<PriceHistoryPoint>>

    @GET("market/history/{symbol}")
    suspend fun getMarketHistory(
        @Path("symbol") symbol: String,
        @Query("limit") limit: Int = 100
    ): Response<List<PriceHistoryPoint>>

    @GET("api/market/history/{symbol}")
    suspend fun getApiMarketHistory(
        @Path("symbol") symbol: String,
        @Query("limit") limit: Int = 100
    ): Response<List<PriceHistoryPoint>>

    @GET("market/stocks/{symbol}/history")
    suspend fun getMarketStockHistory(
        @Path("symbol") symbol: String,
        @Query("limit") limit: Int = 100
    ): Response<List<PriceHistoryPoint>>

    @GET("api/stocks/history/{symbol}")
    suspend fun getStocksHistorySymbol(
        @Path("symbol") symbol: String,
        @Query("limit") limit: Int = 100
    ): Response<List<PriceHistoryPoint>>
}
