package ind.finance.bourse.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("jwt") val jwt: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("user") val user: UserDto?
) {
    val authToken: String?
        get() = token ?: accessToken ?: jwt
}

data class UserDto(
    @SerializedName("id") val id: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?
)
