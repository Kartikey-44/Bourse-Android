package ind.finance.bourse.data.remote

import com.google.gson.JsonElement
import ind.finance.bourse.data.model.PortfolioSummaryResponse
import ind.finance.bourse.data.model.PortfolioResponse
import ind.finance.bourse.data.model.HoldingResponse
import ind.finance.bourse.data.model.RecommendationResponse
import retrofit2.Response
import retrofit2.http.GET

interface PortfolioServiceApi {
    @GET("api/portfolio")
    suspend fun getPortfolio(): Response<PortfolioResponse>

    @GET("api/portfolio/holdings")
    suspend fun getHoldings(): Response<List<HoldingResponse>>

    @GET("api/portfolio/recommendations")
    suspend fun getRecommendations(): Response<List<RecommendationResponse>>

    @GET("api/portfolio/summary")
    suspend fun getPortfolioSummary(): Response<PortfolioSummaryResponse>

    @GET("api/portfolio/transactions")
    suspend fun getRawTransactions(): Response<JsonElement>
}
