package kr.younghwan.userapi.helper

import kr.younghwan.userapi.config.SecurityConfig
import kr.younghwan.userapi.global.jwt.JwtAuthenticationFilter
import kr.younghwan.userapi.global.jwt.JwtTokenProvider
import org.springframework.context.annotation.Import

@Import(SecurityConfig::class, JwtTokenProvider::class, JwtAuthenticationFilter::class)
open class BaseControllerTest