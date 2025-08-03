package kr.younghwan.userapi.domain.user.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.younghwan.userapi.domain.user.controller.dto.SignupRequest
import kr.younghwan.userapi.domain.user.exception.EmailAlreadyExistsException
import kr.younghwan.userapi.domain.user.service.UserService
import kr.younghwan.userapi.helper.BaseControllerTest
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@WebMvcTest(SignupController::class)
class SignupControllerTest : BaseControllerTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var userService: UserService

    @Test
    fun `POST signup`() {
        // given
        val request = validRequest()
        val content = jacksonObjectMapper().writeValueAsString(request)

        // when
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            // then
            .andExpect(status().isCreated)
    }

    @Test
    fun `POST signup - already exists email`() {
        // given
        val request = validRequest()
        val content = jacksonObjectMapper().writeValueAsString(request)

        given { userService.signup(any()) }
            .willThrow(EmailAlreadyExistsException("test@example.com"))

        // when
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            // then
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("USER_EMAIL_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.message").value("Email already exists: test@example.com"))
    }

    private fun validRequest(): SignupRequest =
        SignupRequest(
            email = "test@example.com",
            password = "secure123!",
            name = "홍길동"
        )

    @Test
    fun `POST signup - invalid email`() {
        // given
        val request = SignupRequest(
            email = "invalid-email",
            password = "secure123!",
            name = "홍길동"
        )
        val content = jacksonObjectMapper().writeValueAsString(request)

        // when
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            // then
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("[email] 유효하지 않은 이메일 형식입니다."))
    }

    @Test
    fun `POST signup - password is null`() {
        // given
        val request = SignupRequest(
            email = "test@example.com",
            password = null,
            name = "홍길동"
        )
        val content = jacksonObjectMapper().writeValueAsString(request)

        // when
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            // then
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("[password] 비밀번호는 필수입니다."))
    }

    @Test
    fun `POST signup - name is empty`() {
        // given
        val request = SignupRequest(
            email = "test@example.com",
            password = "secure123!",
            name = ""
        )
        val content = jacksonObjectMapper().writeValueAsString(request)

        // when
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            // then
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("[name] 이름은 필수입니다."))
    }
}