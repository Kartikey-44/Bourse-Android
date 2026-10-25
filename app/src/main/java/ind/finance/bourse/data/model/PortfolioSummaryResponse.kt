package ind.finance.bourse.data.model

import com.google.gson.annotations.SerializedName

data class PortfolioSummaryResponse(
    @SerializedName("portfolioValue", alternate = ["totalValue", "value", "totalPortfolioValue"])
    val portfolioValue: Double?,

    @SerializedName("returnAmount", alternate = ["profitAmount", "changeAmount", "totalReturnAmount"])
    val returnAmount: Double?,

    @SerializedName("returnPercentage", alternate = ["profitPercentage", "changePercentage", "totalReturnPercentage"])
    val returnPercentage: Double?,

    @SerializedName("investedAmount", alternate = ["invested", "totalInvested"])
    val investedAmount: Double?,

    @SerializedName("availableBalance", alternate = ["available", "balance", "cash", "availableCash"])
    val availableBalance: Double?
)
