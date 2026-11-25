
package com.workmanagement.backend.connection.repository

import com.workmanagement.backend.connection.Connection
import org.springframework.data.jpa.repository.JpaRepository

interface ConnectionRepository : JpaRepository<Connection, Long> {

    fun existsByRequesterIdAndReceiverId(
        requesterId: Long,
        receiverId: Long
    ): Boolean
}
