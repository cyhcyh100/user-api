package kr.younghwan.userapi.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import kr.younghwan.userapi.domain.user.entity.UserEntity
import kr.younghwan.userapi.domain.user.enums.UserRole
import kr.younghwan.userapi.domain.user.event.UserDeletionProducer
import kr.younghwan.userapi.domain.user.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @MockitoBean
    lateinit var userDeletionProducer: UserDeletionProducer

    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `POST signup and signin`() {
        val email = "test@example.com"
        val password = "secure123!"
        val name = "홍길동"

        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "email" to email,
                            "password" to password,
                            "name" to name
                        )
                    )
                )
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "email" to email,
                            "password" to password
                        )
                    )
                )
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    @Test
    fun `GET user`() {
        val email = "user@example.com"
        val password = "password1!"
        val name = "사용자"

        val userId = signup(email, password, name)
        val token = signin(email, password)

        mockMvc.perform(
            get("/users/$userId")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.name").value(name))
    }

    @Test
    fun `PUT user`() {
        val email = "update@example.com"
        val password = "password1!"
        val name = "업데이트 전"

        val userId = signup(email, password, name)
        val token = signin(email, password)

        val newEmail = "updated@example.com"
        val newName = "업데이트 후"

        mockMvc.perform(
            put("/users/$userId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "email" to newEmail,
                            "name" to newName
                        )
                    )
                )
        ).andExpect(status().isOk)
            .andExpect(content().string("OK"))

        val updated = userRepository.findById(userId).orElseThrow()
        newEmail shouldBe updated.email
        newName shouldBe updated.name
    }

    @Test
    fun `DELETE user`() {
        val email = "delete@example.com"
        val password = "password1!"
        val name = "삭제 유저"

        val userId = signup(email, password, name)
        val token = signin(email, password)

        mockMvc.perform(
            delete("/users/$userId")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isNoContent)

        userRepository.findById(userId).isEmpty shouldBe true
    }

    @Test
    fun `GET users by admin`() {
        val adminEmail = "admin@example.com"
        val adminPassword = "adminPass1!"

        userRepository.save(
            UserEntity(
                email = adminEmail,
                password = passwordEncoder.encode(adminPassword),
                name = "관리자",
                role = UserRole.ADMIN
            )
        )

        signup("user1@example.com", "password1!", "유저1")
        signup("user2@example.com", "password1!", "유저2")

        val adminToken = signin(adminEmail, adminPassword)

        mockMvc.perform(
            get("/users")
                .header("Authorization", "Bearer $adminToken")
                .param("page", "0")
                .param("size", "10")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(3))
    }

    private fun signup(email: String, password: String, name: String): Long {
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "email" to email,
                            "password" to password,
                            "name" to name
                        )
                    )
                )
        ).andExpect(status().isCreated)

        return userRepository.findByEmail(email)!!.id
    }

    private fun signin(email: String, password: String): String {
        val result = mockMvc.perform(
            post("/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "email" to email,
                            "password" to password
                        )
                    )
                )
        ).andExpect(status().isOk)
            .andReturn()

        val json = objectMapper.readTree(result.response.contentAsString)
        return json.get("accessToken").asText()
    }
}