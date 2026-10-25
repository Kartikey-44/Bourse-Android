package ind.finance.bourse

data class StockModel(
    val symbol: String,
    val companyName: String,
    val price: String,
    val change: String,
    val isPositive: Boolean
)
