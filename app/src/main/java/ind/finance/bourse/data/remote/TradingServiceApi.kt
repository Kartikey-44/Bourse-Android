package ind.finance.bourse.data.remote

import ind.finance.bourse.data.model.TradeRequest
import ind.finance.bourse.data.model.TransactionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TradingServiceApi {
    @POST("api/trading/buy")
    suspend fun buy(@Body request: TradeRequest): Response<TransactionDto>

    @POST("api/trading/sell")
    suspend fun sell(@Body request: TradeRequest): Response<TransactionDto>
}
