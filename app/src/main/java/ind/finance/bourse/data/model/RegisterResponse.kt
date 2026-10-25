package ind.finance.bourse.data.model

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("success") val success: Boolean?
)
