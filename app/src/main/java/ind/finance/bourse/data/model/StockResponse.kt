package ind.finance.bourse.data.model

data class StockResponse(
    val id: Int,
    val symbol: String,
    val companyName: String,
    val currentPrice: Double
)
