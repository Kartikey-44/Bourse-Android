package ind.finance.bourse.data.model

import com.google.gson.annotations.JsonAdapter
import ind.finance.bourse.data.remote.TimestampDeserializer

data class PortfolioTransactionDto(
    val symbol: String,
    val companyName: String,
    val type: String,
    val quantity: Int,
    val price: Double,
    @JsonAdapter(TimestampDeserializer::class)
    val timestamp: String,
    val status: String?
)
