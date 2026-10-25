package ind.finance.bourse.data.repository

import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.StockResponse
import ind.finance.bourse.data.model.StockTrendResponse
import ind.finance.bourse.data.remote.StockServiceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockRepository(
    private val api: StockServiceApi,
    private val tokenManager: TokenManager
) {
    suspend fun getStocks(): Result<List<StockResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStocks()
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Unable to load market"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getStock(symbol: String): Result<StockResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStock(symbol)
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Unable to load stock details"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getStockTrend(symbol: String): Result<StockTrendResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStockTrend(symbol)
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Unable to load stock trend"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
