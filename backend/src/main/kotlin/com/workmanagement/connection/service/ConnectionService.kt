package com.workmanagement.backend.connection.service

import com.workmanagement.backend.connection.Connection
import com.workmanagement.backend.connection.dto.AcceptConnectionRequest
import com.workmanagement.backend.connection.dto.ConnectionRequestDto
import com.workmanagement.backend.connection.dto.ConnectionResponseDto
import com.workmanagement.backend.connection.dto.ConnectUserRequest
import com.workmanagement.backend.connection.dto.ConnectedUserDto
import com.workmanagement.backend.connection.repository.ConnectionRepository
import com.workmanagement.backend.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ConnectionService(
    private val connectionRepository: ConnectionRepository,
    private val userRepository: UserRepository
) {
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun sendRequest(actorId: Long, request: ConnectionRequestDto): ConnectionResponseDto {
        requireActor(actorId, request.requesterId)
        val pair = lockPair(actorId, request.receiverId)
        if (pair.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Connection request already exists")
        }
        return connectionRepository.save(
            Connection(requesterId = actorId, receiverId = request.receiverId)
        ).toResponse()
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun connect(actorId: Long, request: ConnectUserRequest): ConnectionResponseDto {
        val pair = lockPair(actorId, request.receiverId)
        pair.firstOrNull { it.status == "ACCEPTED" }?.let { return it.toResponse() }

        val connection = pair.firstOrNull { it.status == "PENDING" }
            ?: pair.firstOrNull()
            ?: Connection(requesterId = actorId, receiverId = request.receiverId)
        connection.status = "ACCEPTED"
        return connectionRepository.save(connection).toResponse()
    }

    @Transactional
    fun acceptRequest(actorId: Long, connectionId: Long, request: AcceptConnectionRequest): ConnectionResponseDto {
        requireActor(actorId, request.receiverId)
        val connection = connectionRepository.findForUpdate(connectionId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Connection request not found")
        requireActor(actorId, connection.receiverId)
        if (connection.status != "PENDING") {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Connection request is not pending")
        }
        connection.status = "ACCEPTED"
        return connection.toResponse()
    }

    @Transactional(readOnly = true)
    fun getConnectedUsers(actorId: Long, userId: Long): List<ConnectedUserDto> {
        requireActor(actorId, userId)
        return userRepository.findAllById(connectionRepository.findConnectedUserIds(userId))
            .map { ConnectedUserDto(requireNotNull(it.id), it.name) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    private fun lockPair(requesterId: Long, receiverId: Long): List<Connection> {
        if (requesterId == receiverId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot connect with yourself")
        }

        // Both creation routes lock the same user rows in the same order, even for reversed requests.
        val first = minOf(requesterId, receiverId)
        val second = maxOf(requesterId, receiverId)
        if (userRepository.lockById(first) == null || userRepository.lockById(second) == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
        return connectionRepository.findPairForUpdate(first, second)
    }

    private fun requireActor(actorId: Long, userId: Long) {
        if (actorId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User does not match the signed-in account")
        }
    }

    private fun Connection.toResponse() = ConnectionResponseDto(requireNotNull(id), requesterId, receiverId, status)
}
