package ind.finance.bourse.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.UserDto
import ind.finance.bourse.data.remote.ApiClient
import ind.finance.bourse.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserDto) : ProfileState()
    data class Error(val message: String) : ProfileState()
    object Unauthorized : ProfileState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val repository = UserRepository(ApiClient.getInstance(application), tokenManager)

    private val _state = MutableLiveData<ProfileState>()
    val state: LiveData<ProfileState> = _state

    fun loadProfile() {
        _state.value = ProfileState.Loading
        viewModelScope.launch {
            val result = repository.getProfile()
            if (result.exceptionOrNull()?.message == "UNAUTHORIZED") {
                _state.value = ProfileState.Unauthorized
            } else if (result.isSuccess) {
                _state.value = ProfileState.Success(result.getOrThrow())
            } else {
                _state.value = ProfileState.Error("Unable to load profile")
            }
        }
    }
}
