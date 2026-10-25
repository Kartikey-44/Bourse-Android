package ind.finance.bourse.data.model

data class PortfolioSummaryResponse(
    val cashBalance: Double,
    val totalInvestedValue: Double,
    val totalCurrentValue: Double,
    val totalPortfolioValue: Double,
    val totalProfitLoss: Double
)
