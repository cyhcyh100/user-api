package kr.younghwan.userapi.domain.user.entity

import jakarta.persistence.*
import kr.younghwan.userapi.domain.user.enums.UserRole

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.MEMBER
)