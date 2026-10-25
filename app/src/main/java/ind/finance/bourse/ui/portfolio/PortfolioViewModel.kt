package ind.finance.bourse.ui.portfolio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.HoldingResponse
import ind.finance.bourse.data.model.PortfolioResponse
import ind.finance.bourse.data.model.PortfolioSummaryResponse
import ind.finance.bourse.data.model.RecommendationResponse
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.PortfolioRepository
import kotlinx.coroutines.launch

sealed class PortfolioState {
    object Loading : PortfolioState()
    data class Success(
        val portfolio: PortfolioResponse?,
        val summary: PortfolioSummaryResponse?,
        val holdings: List<HoldingResponse>,
        val recommendations: List<RecommendationResponse>
    ) : PortfolioState()
    data class Error(val message: String) : PortfolioState()
    object Unauthorized : PortfolioState()
}

class PortfolioViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = PortfolioRepository(
        ApiClient.getPortfolioService(application),
        tokenManager
    )
    private val _state = MutableLiveData<PortfolioState>()
    val state: LiveData<PortfolioState> = _state

    fun loadPortfolio() {
        _state.value = PortfolioState.Loading
        viewModelScope.launch {
            val portfolio = repository.getPortfolio()
            val summary = repository.getPortfolioSummary()
            val holdings = repository.getHoldings()
            val recommendations = repository.getRecommendations()
            val results = listOf(portfolio, summary, holdings, recommendations)

            if (results.any { it.exceptionOrNull()?.message == "UNAUTHORIZED" }) {
                _state.value = PortfolioState.Unauthorized
            } else if (results.any { it.isFailure }) {
                _state.value = PortfolioState.Error("Unable to load portfolio")
            } else {
                _state.value = PortfolioState.Success(
                    portfolio.getOrNull(),
                    summary.getOrNull(),
                    holdings.getOrDefault(emptyList()),
                    recommendations.getOrDefault(emptyList())
                )
            }
        }
    }
}
