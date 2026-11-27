
package com.workmanagement.backend.user.service

import com.workmanagement.backend.user.User
import com.workmanagement.backend.connection.repository.ConnectionRepository
import com.workmanagement.backend.user.dto.*
import com.workmanagement.backend.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val connectionRepository: ConnectionRepository
) {
    fun createUser(request: CreateUserRequest): UserResponse {
        val name = request.name.trim()
        val email = request.email.trim().lowercase()

        if (name.isBlank() || email.isBlank() ||
            request.password.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "All fields are required"
            )
        }

        if (userRepository.existsByEmail(email)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT, "Email already registered"
            )
        }

        val user = User(
            name = name,
            email = email,
            passwordHash = requireNotNull(passwordEncoder.encode(request.password)) { "Password encoder returned null" }
        )

        return userRepository.save(user).toResponse()
    }

    fun getAllUsers(): List<UserResponse> =
        userRepository.findAll().map { it.toResponse() }

    fun getCurrentUser(email: String): UserResponse =
        userRepository.findByEmail(email)?.toResponse()
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found")

    fun searchUsers(email: String, query: String): List<UserSearchResult> {
        val prefix = query.trim()
        if (prefix.isEmpty()) return emptyList()

        val userId = getCurrentUser(email).id
        val connectedIds = connectionRepository.findConnectedUserIds(userId).toSet()
        return userRepository.findByNameStartingWithIgnoreCaseAndIdNotOrderByNameAsc(prefix, userId)
            .map { user ->
                val id = requireNotNull(user.id)
                UserSearchResult(id, user.name, id in connectedIds)
            }
    }

    private fun User.toResponse() = UserResponse(
        id = requireNotNull(id),
        name = name,
        email = email
    )
}
