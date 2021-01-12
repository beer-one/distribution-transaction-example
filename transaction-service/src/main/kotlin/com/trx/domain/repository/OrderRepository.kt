package com.trx.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.trx.domain.entity.Order

interface OrderRepository : JpaRepository<Order, Int>