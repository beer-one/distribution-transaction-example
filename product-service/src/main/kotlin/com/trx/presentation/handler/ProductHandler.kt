package com.trx.presentation.handler

import com.trx.application.product.ProductCommandService
import com.trx.domain.repository.ProductRepository
import com.trx.errors.exception.IncorrectFormatBodyException
import com.trx.errors.exception.ProductNotFoundException
import com.trx.presentation.request.ProductCreateRequest
import com.trx.presentation.request.ProductIncrementRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Component
class ProductHandler (
    private val repository: ProductRepository,
    private val commandService: ProductCommandService
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

    suspend fun create(request: ServerRequest): ServerResponse {
        val createRequest = request.awaitBodyOrNull<ProductCreateRequest>()
            ?: throw IncorrectFormatBodyException()

        commandService.create(createRequest)
        return noContent().buildAndAwait()
    }

    suspend fun getOne(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()

        return repository.findByIdOrNull(id)
            ?.let { ok().bodyValueAndAwait(it) }
            ?: throw ProductNotFoundException(id)
    }

    suspend fun add(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()
        val incrementRequest = request.awaitBodyOrNull<ProductIncrementRequest>()
            ?: throw Exception()

        commandService.incrementProductCount(id, incrementRequest.count)
        return noContent().buildAndAwait()
    }
}