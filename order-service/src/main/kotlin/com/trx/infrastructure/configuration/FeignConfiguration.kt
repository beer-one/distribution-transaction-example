package com.trx.infrastructure.configuration

import com.trx.infrastructure.feign.FeignClientBase
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import kotlin.streams.toList


@Configuration
@EnableFeignClients(basePackageClasses = [FeignClientBase::class])
class FeignConfiguration {

    @Bean
    fun messageConverters(converters: ObjectProvider<HttpMessageConverter<*>>): HttpMessageConverters {
        return HttpMessageConverters(true, converters.orderedStream().toList())
    }
}