package ind.finance.bourse.data.model

data class TransactionDto(
    val tradeId: Long,
    val symbol: String,
    val action: String,
    val quantity: Int,
    val executionPrice: Double,
    val totalAmount: Double,
    val status: String,
    val timestamp: String
)
