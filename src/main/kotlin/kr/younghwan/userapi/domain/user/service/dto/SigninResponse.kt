package kr.younghwan.userapi.domain.user.service.dto

class SigninResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
)