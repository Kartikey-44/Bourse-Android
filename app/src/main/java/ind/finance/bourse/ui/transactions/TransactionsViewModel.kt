package ind.finance.bourse.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.TransactionDto
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.PortfolioRepository
import kotlinx.coroutines.launch

sealed class TransactionsState {
    object Loading : TransactionsState()
    data class Success(val transactions: List<TransactionDto>) : TransactionsState()
    data class Error(val message: String) : TransactionsState()
    object Unauthorized : TransactionsState()
}

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = PortfolioRepository(
        ApiClient.getPortfolioService(application),
        tokenManager
    )

    private val _state = MutableLiveData<TransactionsState>()
    val state: LiveData<TransactionsState> = _state

    fun loadTransactions() {
        _state.value = TransactionsState.Loading
        viewModelScope.launch {
            val result = repository.getTransactions()
            if (result.exceptionOrNull()?.message == "UNAUTHORIZED") {
                _state.value = TransactionsState.Unauthorized
            } else if (result.isSuccess) {
                _state.value = TransactionsState.Success(result.getOrDefault(emptyList()))
            } else {
                _state.value = TransactionsState.Error("Unable to load transactions")
            }
        }
    }
}
