package ind.finance.bourse.data.remote

import ind.finance.bourse.data.model.LoginRequest
import ind.finance.bourse.data.model.LoginResponse
import ind.finance.bourse.data.model.RegisterRequest
import ind.finance.bourse.data.model.RegisterResponse
import ind.finance.bourse.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

interface UserServiceApi {
    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/users/profile")
    suspend fun getProfile(): Response<UserDto>
}
