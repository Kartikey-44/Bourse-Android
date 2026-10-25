package ind.finance.bourse.data.model

data class RecommendationResponse(
    val symbol: String?,
    val stockName: String?,
    val recommendation: String?,
    val action: String?,
    val currentPrice: Double?,
    val reason: String?,
    val trend: String?
)
