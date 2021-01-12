package com.trx.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.account.AccountCommandService
import com.trx.application.event.TransactionEventPublisher
import com.trx.coroutine.boundedElasticScope
import com.trx.errors.CustomException
import com.trx.topic.Topic.APPLY_PAYMENT
import com.trx.topic.Topic.PAYMENT_FAILED
import com.trx.topic.Topic.PAYMENT_SUCCEED
import com.trx.topic.event.ApplyPaymentEvent
import com.trx.topic.event.PaymentFailed
import com.trx.topic.event.PaymentSucceed
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class AccountEventListener(
    private val objectMapper: ObjectMapper,
    private val accountCommandService: AccountCommandService,
    private val transactionEventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [APPLY_PAYMENT], groupId = "account-consumer", containerFactory = "accountEventListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = (data.key() to objectMapper.readValue(data.value(), ApplyPaymentEvent::class.java))

        logger.info("Topic: $APPLY_PAYMENT, key: $key, event: $event")

        try {
            val restBalance = accountCommandService.applyPayment(event)
            transactionEventPublisher.publishEvent(
                topic = PAYMENT_SUCCEED,
                key = key,
                event = PaymentSucceed(restBalance)
            )
        } catch (e: CustomException) {
            transactionEventPublisher.publishEvent(
                topic = PAYMENT_FAILED,
                key = key,
                event = PaymentFailed(e.errorCode.message)
            )
        }.let {
            boundedElasticScope.launch {
                it.awaitFirstOrNull()
            }
        }
    }
}
