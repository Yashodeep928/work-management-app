
package com.workmanagement.backend.connection.dto

data class ConnectionRequestDto(
    val requesterId: Long,
    val receiverId: Long
)

data class ConnectionResponseDto(
    val id: Long,
    val requesterId: Long,
    val receiverId: Long,
    val status: String
)


data class AcceptConnectionRequest(
    val receiverId: Long
)

data class ConnectUserRequest(val receiverId: Long)

data class ConnectedUserDto(val id: Long, val name: String)
