package com.trx.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.account.AccountCommandService
import com.trx.application.event.TransactionEventPublisher
import com.trx.coroutine.boundedElasticScope
import com.trx.errors.CustomException
import com.trx.topic.Topic.APPLY_PAYMENT
import com.trx.topic.Topic.APPLY_PAYMENT_RESULT
import com.trx.topic.Topic.CHECK_PRODUCT
import com.trx.topic.event.ApplyPaymentEvent
import com.trx.topic.event.ApplyPaymentResultEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class AccountEventListener(
    private val objectMapper: ObjectMapper,
    private val accountCommandService: AccountCommandService,
    private val transactionEventPublisher: TransactionEventPublisher
) : AcknowledgingMessageListener<String, String> {

    @KafkaListener(topics = [APPLY_PAYMENT], containerFactory = "kafkaListenerContainerFactory")
    override fun onMessage(data: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val (key, event) = (data.key() to objectMapper.readValue(data.value(), ApplyPaymentEvent::class.java))

        try {
            accountCommandService.applyPayment(event)
            transactionEventPublisher.publishEvent(
                topic = APPLY_PAYMENT_RESULT,
                key = key,
                event = ApplyPaymentResultEvent.success()
            )
        } catch (e: CustomException) {
            transactionEventPublisher.publishEvent(
                topic = APPLY_PAYMENT_RESULT,
                key = key,
                event = ApplyPaymentResultEvent.fail(e.errorCode.message)
            )
        }.let {
            boundedElasticScope.launch {
                it.awaitFirstOrNull()
            }
        }
    }

}
