package com.trx.application.product

import com.trx.domain.repository.ProductRepository
import com.trx.errors.exception.ProductNotFoundException
import com.trx.topic.event.CheckProductEvent
import com.trx.topic.event.ProductRollBackEvent
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductCommandService (
    private val productRepository: ProductRepository
) {

    @Transactional
    fun checkAndSubtractProduct(event: CheckProductEvent): Int {
        return productRepository.findByIdOrNull(event.productId)
            ?.subtract(event.count)
            ?: throw ProductNotFoundException()
    }

    @Transactional
    fun incrementProductCount(event: ProductRollBackEvent) {
        productRepository.findByIdOrNull(event.productId)
            ?.increment(event.count)
            ?: throw ProductNotFoundException()
    }
}