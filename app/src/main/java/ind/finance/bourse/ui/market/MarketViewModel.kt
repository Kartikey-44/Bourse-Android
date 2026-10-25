package ind.finance.bourse.ui.market

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.StockResponse
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.StockRepository
import kotlinx.coroutines.launch

sealed class MarketState {
    object Loading : MarketState()
    data class Success(val stocks: List<StockResponse>) : MarketState()
    data class Error(val message: String) : MarketState()
    object Unauthorized : MarketState()
}

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.getStockService(application)
    private val repository = StockRepository(api, tokenManager)

    private val _marketState = MutableLiveData<MarketState>()
    val marketState: LiveData<MarketState> = _marketState

    fun loadStocks() {
        _marketState.value = MarketState.Loading

        viewModelScope.launch {
            val result = repository.getStocks()
            if (result.exceptionOrNull()?.message == "UNAUTHORIZED") {
                tokenManager.clearToken()
                _marketState.value = MarketState.Unauthorized
                return@launch
            }

            val stocks = result.getOrNull() ?: emptyList()
            if (result.isSuccess) {
                _marketState.value = MarketState.Success(stocks)
            } else {
                _marketState.value = MarketState.Error("Unable to load market")
            }
        }
    }
}
