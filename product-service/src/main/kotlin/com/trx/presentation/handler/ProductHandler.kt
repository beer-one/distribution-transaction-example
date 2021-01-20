package com.trx.presentation.handler

import com.trx.domain.repository.ProductRepository
import com.trx.errors.exception.ProductNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull

@Component
class ProductHandler (
    private val repository: ProductRepository
) {

    suspend fun search(request: ServerRequest): ServerResponse {
        val name = request.queryParamOrNull("name")

        val products =  name?.let { name ->
            repository.findByName(name)?.let {
                listOf(it)
            }?: emptyList()
        }?: repository.findAll()

        return ok().bodyValueAndAwait(products)
    }

    suspend fun getOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()

        return repository.findByIdOrNull(id)
            ?.let { ok().bodyValueAndAwait(it) }
            ?: throw ProductNotFoundException(id)
    }
}