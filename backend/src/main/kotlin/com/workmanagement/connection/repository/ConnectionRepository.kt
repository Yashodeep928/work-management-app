package com.workmanagement.backend.connection.repository

import com.workmanagement.backend.connection.Connection
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ConnectionRepository : JpaRepository<Connection, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select c from Connection c
        where (c.requesterId = :first and c.receiverId = :second)
           or (c.requesterId = :second and c.receiverId = :first)
        order by c.id
    """)
    fun findPairForUpdate(@Param("first") first: Long, @Param("second") second: Long): List<Connection>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Connection c where c.id = :id")
    fun findForUpdate(@Param("id") id: Long): Connection?

    @Query("""
        select distinct case when c.requesterId = :userId then c.receiverId else c.requesterId end
        from Connection c
        where c.status = 'ACCEPTED' and (c.requesterId = :userId or c.receiverId = :userId)
    """)
    fun findConnectedUserIds(@Param("userId") userId: Long): List<Long>
}
