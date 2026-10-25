package ind.finance.bourse

data class TransactionModel(
    val id: String,
    val type: String, // "BUY" or "SELL"
    val symbol: String,
    val shares: Int,
    val pricePerShare: Double,
    val totalAmount: Double,
    val dateTime: String,
    val status: String // "COMPLETED"
)
