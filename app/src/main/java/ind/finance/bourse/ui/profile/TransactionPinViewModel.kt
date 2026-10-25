package ind.finance.bourse.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ind.finance.bourse.data.local.TransactionPinStore

sealed class TransactionPinState {
    object Idle : TransactionPinState()
    object Success : TransactionPinState()
    data class Error(val message: String) : TransactionPinState()
}

class TransactionPinViewModel(application: Application) : AndroidViewModel(application) {
    private val pinStore = TransactionPinStore(application)
    private val _state = MutableLiveData<TransactionPinState>(TransactionPinState.Idle)
    val state: LiveData<TransactionPinState> = _state

    fun hasPin(): Boolean = pinStore.hasPin()

    fun savePin(currentPin: String, newPin: String, confirmation: String) {
        if (pinStore.hasPin() && currentPin != pinStore.getPin()) {
            _state.value = TransactionPinState.Error("Current PIN is incorrect")
            return
        }
        if (!newPin.matches(Regex("\\d{4}"))) {
            _state.value = TransactionPinState.Error("New PIN must be exactly 4 digits")
            return
        }
        if (newPin != confirmation) {
            _state.value = TransactionPinState.Error("PINs do not match")
            return
        }
        pinStore.savePin(newPin)
        _state.value = TransactionPinState.Success
    }

    fun reset() {
        _state.value = TransactionPinState.Idle
    }
}
