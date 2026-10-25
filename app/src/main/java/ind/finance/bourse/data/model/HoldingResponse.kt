package ind.finance.bourse.data.model

data class HoldingResponse(
    val symbol: String?,
    val companyName: String?,
    val quantity: Int?,
    val averageBuyPrice: Double?,
    val currentPrice: Double?,
    val investedValue: Double?,
    val currentValue: Double?,
    val profitLoss: Double?,
    val profitLossPercentage: Double?
)
