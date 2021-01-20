package com.trx.domain.repository

import com.trx.domain.entity.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Int> {
    fun findByName(name: String): Product?
}