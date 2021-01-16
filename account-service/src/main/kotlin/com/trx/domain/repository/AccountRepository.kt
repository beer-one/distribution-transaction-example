package com.trx.domain.repository

import com.trx.domain.entity.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository: JpaRepository<Account, Int> {
    fun findByCustomerId(customerId: Int): Account?
}