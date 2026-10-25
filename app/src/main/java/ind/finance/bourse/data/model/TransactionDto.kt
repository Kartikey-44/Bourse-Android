package ind.finance.bourse.data.model

import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("id", alternate = ["transactionId", "_id"])
    val id: String?,

    @SerializedName("type", alternate = ["action", "tradeType"])
    val type: String?,

    @SerializedName("symbol", alternate = ["stockSymbol", "ticker"])
    val symbol: String?,

    @SerializedName("shares", alternate = ["quantity", "units"])
    val shares: Int?,

    @SerializedName("pricePerShare", alternate = ["price", "executionPrice"])
    val pricePerShare: Double?,

    @SerializedName("totalAmount", alternate = ["total", "amount"])
    val totalAmount: Double?,

    @SerializedName("dateTime", alternate = ["date", "timestamp", "createdAt"])
    val dateTime: String?,

    @SerializedName("status")
    val status: String?
)
