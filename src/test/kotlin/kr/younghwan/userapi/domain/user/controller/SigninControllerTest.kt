package kr.younghwan.userapi.domain.user.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.younghwan.userapi.config.SecurityConfig
import kr.younghwan.userapi.domain.user.controller.dto.SigninRequest
import kr.younghwan.userapi.domain.user.exception.InvalidCredentialsException
import kr.younghwan.userapi.domain.user.service.UserService
import kr.younghwan.userapi.domain.user.service.dto.SigninResponse
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@Import(SecurityConfig::class)
@WebMvcTest(SigninController::class)
class SigninControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var userService: UserService

    @Test
    fun `POST signin`() {
        val request = request()
        val content = jacksonObjectMapper().writeValueAsString(request)

        given { userService.signin(any()) }.willReturn(response())

        mockMvc.perform(
            post("/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("accessToken"))
            .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
    }

    @Test
    fun `POST signin - invalid credentials`() {
        val request = request()
        val content = jacksonObjectMapper().writeValueAsString(request)
        given { userService.signin(any()) }.willThrow(InvalidCredentialsException())

        mockMvc.perform(
            post("/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    private fun request() = SigninRequest(
        email = "test@example.com",
        password = "secure123!"
    )

    private fun response() = SigninResponse(
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        tokenType = "Bearer",
    )
}