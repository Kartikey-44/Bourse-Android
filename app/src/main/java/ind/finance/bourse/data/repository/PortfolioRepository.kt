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
                val response = api.getRawTransactions()
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    val element = response.body()!!
                    val list = mutableListOf<TransactionDto>()
                    val gson = Gson()

                    if (element.isJsonArray) {
                        val jsonArray = element.asJsonArray
                        for (item in jsonArray) {
                            try {
                                val dto = gson.fromJson(item, TransactionDto::class.java)
                                list.add(dto)
                            } catch (_: Exception) {}
                        }
                    } else if (element.isJsonObject) {
                        val jsonObj = element.asJsonObject
                        val array = jsonObj.getAsJsonArray("transactions")
                            ?: jsonObj.getAsJsonArray("data")
                            ?: jsonObj.getAsJsonArray("result")
                        if (array != null) {
                            for (item in array) {
                                try {
                                    val dto = gson.fromJson(item, TransactionDto::class.java)
                                    list.add(dto)
                                } catch (_: Exception) {}
                            }

                        }
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Unable to load transactions"))
                }
            } catch (e: Exception) {
                Result.failure(e)
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
