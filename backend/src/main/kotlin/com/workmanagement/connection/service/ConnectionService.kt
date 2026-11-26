
package com.workmanagement.backend.connection.service
import org.springframework.transaction.annotation.Transactional
import com.workmanagement.backend.connection.Connection
import com.workmanagement.backend.connection.dto.*
import com.workmanagement.backend.connection.repository.ConnectionRepository
import com.workmanagement.backend.user.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.dao.DataIntegrityViolationException

@Service
class ConnectionService(
    private val connectionRepository: ConnectionRepository,
    private val userRepository: UserRepository
) {

    fun sendRequest(
        request: ConnectionRequestDto
    ): ConnectionResponseDto {

        val requesterId = request.requesterId
        val receiverId = request.receiverId

        if (requesterId == receiverId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot connect with yourself"
            )
        }

        if (!userRepository.existsById(requesterId) ||
            !userRepository.existsById(receiverId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            )
        }

        val alreadyExists =
            connectionRepository.existsByRequesterIdAndReceiverId(
                requesterId, receiverId
            ) ||
            connectionRepository.existsByRequesterIdAndReceiverId(
                receiverId, requesterId
            )

        if (alreadyExists) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Connection request already exists"
            )
        }

        val connection = Connection(
            requesterId = requesterId,
            receiverId = receiverId
        )

        val saved = try {
            connectionRepository.saveAndFlush(connection)
        } catch (ex: DataIntegrityViolationException) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Connection could not be created; it may already exist"
            )
        }

        return ConnectionResponseDto(
            id = requireNotNull(saved.id),
            requesterId = saved.requesterId,
            receiverId = saved.receiverId,
            status = saved.status
        )
    }




@Transactional
fun acceptRequest(
    connectionId: Long,
    request: AcceptConnectionRequest
): ConnectionResponseDto {

    val connection = connectionRepository
        .findById(connectionId)
        .orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Connection request not found"
            )
        }

    if (connection.receiverId != request.receiverId) {
        throw ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Only the receiver can accept this request"
        )
    }

    if (connection.status != "PENDING") {
        throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Connection request is not pending"
        )
    }

    connection.status = "ACCEPTED"

    val saved = connectionRepository.save(connection)

    return ConnectionResponseDto(
        id = requireNotNull(saved.id),
        requesterId = saved.requesterId,
        receiverId = saved.receiverId,
        status = saved.status
    )
}

}
