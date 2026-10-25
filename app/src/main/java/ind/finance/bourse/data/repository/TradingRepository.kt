package ind.finance.bourse.data.repository

import com.google.gson.JsonParser
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.TradeRequest
import ind.finance.bourse.data.model.TransactionDto
import android.util.Log
import android.util.Log.e
import ind.finance.bourse.data.remote.TradingServiceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TradingRepository(
    private val api: TradingServiceApi,
    private val tokenManager: TokenManager
) {
    suspend fun buy(request: TradeRequest): Result<TransactionDto> =
        execute { api.buy(request) }

    suspend fun sell(request: TradeRequest): Result<TransactionDto> =
        execute { api.sell(request) }

    private suspend fun execute(
        call: suspend () -> retrofit2.Response<TransactionDto>
    ): Result<TransactionDto> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (response.code() == 401) {
                tokenManager.clearToken()
                return@withContext Result.failure(Exception("UNAUTHORIZED"))
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(safeErrorMessage(response)))
            }
        } catch (e: Exception) {
            Log.e("BOURSE_TRADE", "Trade API exception", e)
            Result.failure(Exception("Unable to complete trade"))
        }
    }

    private fun safeErrorMessage(response: retrofit2.Response<TransactionDto>): String {
        val body = response.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            try {
                val json = JsonParser.parseString(body)
                if (json.isJsonObject) {
                    val message = json.asJsonObject.get("message")?.asString
                    if (!message.isNullOrBlank()) return message
                }
            } catch (_: Exception) {
                // Use the generic message for non-JSON error bodies.
            }
        }
        return "Unable to complete trade"
    }
}
