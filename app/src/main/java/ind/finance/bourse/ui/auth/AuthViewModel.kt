package ind.finance.bourse.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.getInstance(application)
    private val repository = UserRepository(api, tokenManager)

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = LoginState.Error("Please enter a valid email address")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _loginState.value = LoginState.Success
            } else {
                val err = result.exceptionOrNull()
                val msg = if (err?.message?.contains("Invalid") == true) {
                    "Invalid email or password"
                } else {
                    "Unable to connect to Bourse\nPlease try again."
                }
                _loginState.value = LoginState.Error(msg)
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _registerState.value = RegisterState.Error("All fields are required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerState.value = RegisterState.Error("Please enter a valid email address")
            return
        }

        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            val result = repository.register(name, email, password)
            if (result.isSuccess) {
                _registerState.value = RegisterState.Success
            } else {
                val err = result.exceptionOrNull()
                val msg = err?.message ?: "Unable to connect to Bourse\nPlease try again."
                _registerState.value = RegisterState.Error(msg)
            }
        }
    }
}
