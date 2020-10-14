package com.trx.presentation.handler

import com.trx.domain.model.DistributedTransaction
import com.trx.domain.model.DistributedTransactionParticipant
import com.trx.domain.model.DistributedTransactionStatus
import com.trx.domain.repository.TransactionRepository
import com.trx.infrastructure.kafka.KafkaProducer
import com.trx.infrastructure.kafka.TopicConstant.TRANSACTION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Component
class TransactionHandler (
    private val transactionRepository: TransactionRepository,
    private val kafkaProducer: KafkaProducer
) {
    suspend fun add(request: ServerRequest): ServerResponse {
        val transaction = request.awaitBodyOrNull<DistributedTransaction>()
            ?: throw Exception()

        return ok().bodyValueAndAwait(
            transactionRepository.save(transaction)
        )
    }

    suspend fun findById(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id")

        val transaction = transactionRepository.findById(id)
            ?: throw Exception()

        return ok().bodyValueAndAwait(transaction)
    }

    suspend fun finish(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id")
        val status = request.pathVariable("status")
            .let { DistributedTransactionStatus.valueOf(it) }

        val transaction = transactionRepository.findById(id)?.let {
            it.status = status
            kafkaProducer.sendMessage(
                TRANSACTION,
                DistributedTransaction(id = id, status = status)
            )
        }

        return noContent().buildAndAwait()
    }

    suspend fun addParticipant(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id")
        val participant = request.awaitBodyOrNull<DistributedTransactionParticipant>()
            ?: throw Exception()

        transactionRepository.findById(id)?.apply {
            participants.add(participant)
        }?: throw Exception()

        return noContent().buildAndAwait()
    }

    suspend fun updateParticipant(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id")
        val serviceId = request.pathVariable("serviceId")
        val status = request.pathVariable("status")
            .let { DistributedTransactionStatus.valueOf(it) }

        val transaction = transactionRepository.findById(id)?: throw Exception()
        transaction.participants.find { it.serviceId == serviceId }
            ?.let {
                it.status = status
                kafkaProducer.sendMessage(
                    TRANSACTION,
                    DistributedTransaction(id = id, status = status)
                )
            }

        return noContent().buildAndAwait()
    }
}