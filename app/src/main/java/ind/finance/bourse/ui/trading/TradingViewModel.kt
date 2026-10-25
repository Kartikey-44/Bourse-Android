package ind.finance.bourse.ui.trading

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.TradeRequest
import ind.finance.bourse.data.model.TransactionDto
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.TradingRepository
import kotlinx.coroutines.launch

sealed class TradingState {
    object Idle : TradingState()
    object Loading : TradingState()
    data class Success(val transaction: TransactionDto) : TradingState()
    data class Failure(val message: String) : TradingState()
    object Unauthorized : TradingState()
}

class TradingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TradingRepository(
        ApiClient.getTradingService(application),
        TokenManager(application)
    )

    private val _state = MutableLiveData<TradingState>(TradingState.Idle)
    val state: LiveData<TradingState> = _state

    fun submit(isBuy: Boolean, symbol: String, quantity: Int) {
        if (_state.value is TradingState.Loading) return
        _state.value = TradingState.Loading
        viewModelScope.launch {
            val request = TradeRequest(symbol, quantity)
            val result = if (isBuy) repository.buy(request) else repository.sell(request)
            if (result.exceptionOrNull()?.message == "UNAUTHORIZED") {
                _state.value = TradingState.Unauthorized
            } else {
                _state.value = result.fold(
                    onSuccess = { TradingState.Success(it) },
                    onFailure = { TradingState.Failure(it.message ?: "Unable to complete trade") }
                )
            }
        }
    }

    fun reset() {
        if (_state.value !is TradingState.Loading) _state.value = TradingState.Idle
    }
}
