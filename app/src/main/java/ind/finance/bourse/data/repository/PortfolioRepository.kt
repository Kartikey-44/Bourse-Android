package ind.finance.bourse.data.repository

import com.google.gson.Gson
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.PortfolioSummaryResponse
import ind.finance.bourse.data.model.PortfolioResponse
import ind.finance.bourse.data.model.HoldingResponse
import ind.finance.bourse.data.model.RecommendationResponse
import ind.finance.bourse.data.model.TransactionDto
import ind.finance.bourse.data.remote.PortfolioServiceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PortfolioRepository(
    private val api: PortfolioServiceApi,
    private val tokenManager: TokenManager
) {
    suspend fun getPortfolio(): Result<PortfolioResponse> = execute { api.getPortfolio() }

    suspend fun getHoldings(): Result<List<HoldingResponse>> = execute { api.getHoldings() }

    suspend fun getRecommendations(): Result<List<RecommendationResponse>> =
        execute { api.getRecommendations() }

    suspend fun getPortfolioSummary(): Result<PortfolioSummaryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPortfolioSummary()
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Unable to load your portfolio"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTransactions(): Result<List<TransactionDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTransactions()

                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }

                if (response.isSuccessful && response.body() != null) {

                    val transactions = response.body()!!.map { tx ->
                        TransactionDto(
                            tradeId = 0L,
                            symbol = tx.symbol,
                            action = tx.type,
                            quantity = tx.quantity,
                            executionPrice = tx.price,
                            totalAmount = tx.price * tx.quantity,
                            status = tx.status ?: "UNKNOWN",
                            timestamp = tx.timestamp
                        )
                    }

                    Result.success(transactions)

                } else {
                    Result.success(emptyList())
                }

            } catch (e: Exception) {
                Result.success(emptyList())
            }
        }
    }
    private suspend fun <T> execute(
        call: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Unable to load portfolio"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
