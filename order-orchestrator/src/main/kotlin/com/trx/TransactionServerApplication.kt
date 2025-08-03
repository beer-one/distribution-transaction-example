package com.trx

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@EnableKafka
@ConfigurationPropertiesScan
@SpringBootApplication
class TransactionServerApplication

fun main(args: Array<String>) {
    runApplication<TransactionServerApplication>(*args)
}