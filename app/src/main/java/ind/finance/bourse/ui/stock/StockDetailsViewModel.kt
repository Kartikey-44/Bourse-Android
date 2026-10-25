package ind.finance.bourse.ui.stock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.StockResponse
import ind.finance.bourse.data.model.StockTrendResponse
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.StockRepository
import kotlinx.coroutines.launch

sealed class StockDetailsState {
    object Loading : StockDetailsState()
    data class Success(
        val stock: StockResponse,
        val trend: StockTrendResponse?
    ) : StockDetailsState()
    data class Error(val message: String) : StockDetailsState()
    object Unauthorized : StockDetailsState()
}

class StockDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repository = StockRepository(ApiClient.getStockService(application), tokenManager)

    private val _state = MutableLiveData<StockDetailsState>()
    val state: LiveData<StockDetailsState> = _state

    fun loadStock(symbol: String) {
        _state.value = StockDetailsState.Loading

        viewModelScope.launch {
            val stockResult = repository.getStock(symbol)
            if (stockResult.exceptionOrNull()?.message == "UNAUTHORIZED") {
                _state.value = StockDetailsState.Unauthorized
                return@launch
            }

            val stock = stockResult.getOrNull()
            if (stock == null) {
                _state.value = StockDetailsState.Error("Unable to load stock details")
                return@launch
            }

            val trendResult = repository.getStockTrend(symbol)
            if (trendResult.exceptionOrNull()?.message == "UNAUTHORIZED") {
                _state.value = StockDetailsState.Unauthorized
                return@launch
            }

            _state.value = StockDetailsState.Success(stock, trendResult.getOrNull())
        }
    }
}
