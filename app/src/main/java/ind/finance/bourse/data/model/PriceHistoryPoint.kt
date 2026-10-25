package ind.finance.bourse.data.model

import com.google.gson.annotations.JsonAdapter
import ind.finance.bourse.data.remote.TimestampDeserializer

data class PriceHistoryPoint(
    @JsonAdapter(TimestampDeserializer::class)
    val timestamp: String,
    val price: Double
)
