package ind.finance.bourse.data.repository

import android.util.Log
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.data.model.LoginRequest
import ind.finance.bourse.data.model.LoginResponse
import ind.finance.bourse.data.model.RegisterRequest
import ind.finance.bourse.data.model.RegisterResponse
import ind.finance.bourse.data.model.UserDto
import ind.finance.bourse.data.remote.UserServiceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val api: UserServiceApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body.token
                    if (!token.isBlank()) {
                        tokenManager.saveToken(token)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception("Invalid email or password"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<RegisterResponse> {
        Log.d("BOURSE_REGISTER", "REPOSITORY REGISTER STARTED")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("BOURSE_REGISTER", "CALLING API")
                val response = api.register(RegisterRequest(name, email, password))
                Log.d("BOURSE_REGISTER", "API RETURNED")
                Log.d("BOURSE_REGISTER", "HTTP CODE = ${response.code()}")
                if (response.isSuccessful) {
                    Log.d("BOURSE_REGISTER", "SUCCESS BODY = ${response.body()}")
                    Result.success(response.body() ?: RegisterResponse("Registration successful", null, true))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("BOURSE_REGISTER", "ERROR BODY = $errorBody")
                    val errorMsg = when {
                        errorBody?.contains("already", ignoreCase = true) == true -> "Email already registered"
                        else -> "Invalid registration details"
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Throwable) {
                Log.e("BOURSE_REGISTER", "EXCEPTION = ${e.javaClass.name}", e)
                Log.e("BOURSE_REGISTER", "EXCEPTION MESSAGE = ${e.message}")
                Result.failure(Exception("Unable to connect to Bourse"))
            }
        }
    }

    suspend fun getProfile(): Result<UserDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getProfile()
                if (response.code() == 401) {
                    tokenManager.clearToken()
                    return@withContext Result.failure(Exception("UNAUTHORIZED"))
                }
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Unable to load profile"))
                }
            } catch (_: Exception) {
                Result.failure(Exception("Unable to load profile"))
            }
        }
    }
}
