package com.trx.domain.repository

import com.trx.domain.model.DistributedTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository: JpaRepository<DistributedTransaction, Int> {
    fun findById(id: String): DistributedTransaction?
}