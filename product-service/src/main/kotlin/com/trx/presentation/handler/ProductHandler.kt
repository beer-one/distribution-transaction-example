package com.trx.presentation.handler

import com.trx.domain.entity.Product
import com.trx.domain.repository.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Component
class ProductHandler (
    private val repository: ProductRepository
) {

    suspend fun add(request: ServerRequest): ServerResponse {
        return request.awaitBodyOrNull<Product>()?.let {
            ok().bodyValueAndAwait(repository.save(it))
        }?: badRequest().bodyValueAndAwait("")
    }

    suspend fun updateCount(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()
        val count = request.pathVariable("count").toInt()

        val product = repository.findByIdOrNull(id)?: throw Exception()
        product.count -= count

        return ok().bodyValueAndAwait(product)
    }
}