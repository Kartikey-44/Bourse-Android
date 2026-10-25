package ind.finance.bourse.data.model

import com.google.gson.annotations.JsonAdapter
import ind.finance.bourse.data.remote.TimestampDeserializer

data class TransactionDto(
    val tradeId: Long,
    val symbol: String,
    val action: String,
    val quantity: Int,
    val executionPrice: Double,
    val totalAmount: Double,
    val status: String,
    @JsonAdapter(TimestampDeserializer::class)
    val timestamp: String
)
