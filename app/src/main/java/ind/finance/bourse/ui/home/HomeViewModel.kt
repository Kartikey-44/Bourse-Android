package ind.finance.bourse.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.PortfolioSummaryResponse
import ind.finance.bourse.data.model.TransactionDto
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.PortfolioRepository
import ind.finance.bourse.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class HomeState {
    object Loading : HomeState()
    data class Success(
        val summary: PortfolioSummaryResponse?,
        val transactions: List<TransactionDto>,
        val userName: String
    ) : HomeState()
    data class Error(val message: String) : HomeState()
    object Unauthorized : HomeState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.getPortfolioService(application)
    private val repository = PortfolioRepository(api, tokenManager)
    private val userRepository = UserRepository(ApiClient.getInstance(application), tokenManager)

    private val _homeState = MutableLiveData<HomeState>()
    val homeState: LiveData<HomeState> = _homeState

    fun loadHomeData() {
        _homeState.value = HomeState.Loading

        viewModelScope.launch {
            val summaryResult = repository.getPortfolioSummary()
            val txResult = repository.getTransactions()
            val profileResult = userRepository.getProfile()

            if (summaryResult.exceptionOrNull()?.message == "UNAUTHORIZED" ||
                txResult.exceptionOrNull()?.message == "UNAUTHORIZED" ||
                profileResult.exceptionOrNull()?.message == "UNAUTHORIZED"
            ) {
                tokenManager.clearToken()
                _homeState.value = HomeState.Unauthorized
                return@launch
            }

            val summary = summaryResult.getOrNull()
            val transactions = txResult.getOrNull() ?: emptyList()
            val userName = profileResult.getOrNull()?.name ?: ""

            if (summary == null && summaryResult.isFailure) {
                _homeState.value = HomeState.Error("Unable to load your portfolio")
            } else {
                _homeState.value = HomeState.Success(summary, transactions, userName)
            }
        }
    }
}
