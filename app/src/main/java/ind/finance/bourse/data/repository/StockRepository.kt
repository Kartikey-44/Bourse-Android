package ind.finance.bourse.data.repository

import android.util.Log
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.PriceHistoryPoint
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

    suspend fun getStockPriceHistory(symbol: String, limit: Int = 100): Result<List<PriceHistoryPoint>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("BOURSE_HISTORY", "HISTORY: CALLING API for symbol=$symbol, limit=$limit")
                var response = api.getStockPriceHistory(symbol, limit)
                if (response.code() == 404) {
                    response = api.getMarketHistory(symbol, limit)
                }
                if (response.code() == 404) {
                    response = api.getApiMarketHistory(symbol, limit)
                }
                if (response.code() == 404) {
                    response = api.getMarketStockHistory(symbol, limit)
                }
                if (response.code() == 404) {
                    response = api.getStocksHistorySymbol(symbol, limit)
                }

                Log.d("BOURSE_HISTORY", "HISTORY: HTTP CODE=${response.code()}")
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("BOURSE_HISTORY", "HISTORY: RESPONSE BODY SIZE=${body.size}")
                    Result.success(body)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("BOURSE_HISTORY", "HISTORY: ERROR BODY=$errorBody")
                    Result.success(emptyList())
                }
            } catch (e: Exception) {
                Log.e("BOURSE_HISTORY", "HISTORY: EXCEPTION=${e.javaClass.name}: ${e.message}", e)
                Result.success(emptyList())
            }
        }
    }
}
