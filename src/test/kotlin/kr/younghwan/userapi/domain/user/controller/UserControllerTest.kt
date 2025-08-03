package kr.younghwan.userapi.domain.user.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kr.younghwan.userapi.domain.user.controller.dto.UserUpdateRequest
import kr.younghwan.userapi.domain.user.service.UserService
import kr.younghwan.userapi.domain.user.service.dto.UserResponse
import kr.younghwan.userapi.helper.BaseControllerTest
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@WebMvcTest(UserController::class)
class UserControllerTest : BaseControllerTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

    @Test
    @WithMockUser(username = "1", roles = ["MEMBER"])
    fun `GET users`() {
        // given
        val userId = "1"
        val mockResponse = UserResponse(id = 1L, email = "test@example.com", name = "테스트 유저")
        whenever(userService.getUser(userId.toLong())).thenReturn(mockResponse)

        // when & then
        mockMvc.perform(get("/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("테스트 유저"))
    }

    @Test
    @WithMockUser(username = "2", roles = ["MEMBER"])
    fun `GET users - member can not access other user`() {
        val userId = "1"

        mockMvc.perform(get("/users/$userId"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "999", roles = ["ADMIN"])
    fun `GET users - admin can access other user`() {
        // given
        val userId = "123"
        val mockResponse = UserResponse(id = 123L, email = "admin@example.com", name = "관리자")
        whenever(userService.getUser(userId.toLong())).thenReturn(mockResponse)

        // when & then
        mockMvc.perform(get("/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(123L))
            .andExpect(jsonPath("$.email").value("admin@example.com"))
            .andExpect(jsonPath("$.name").value("관리자"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["MEMBER"])
    fun `PUT users`() {
        val userId = "1"
        val request = UserUpdateRequest(email = "test2@example.com", name = "변경된 유저")

        mockMvc.perform(
            put("/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("OK"))
    }

    @Test
    @WithMockUser(username = "2", roles = ["MEMBER"])
    fun `PUT users - member can NOT update other user`() {
        val userId = "1"
        val request = UserUpdateRequest(name = "변경된 유저")

        mockMvc.perform(
            put("/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "999", roles = ["ADMIN"])
    fun `PUT users - admin can update any user`() {
        val userId = "123"
        val request = UserUpdateRequest(name = "관리자가 수정")

        mockMvc.perform(
            put("/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("OK"))
    }
}