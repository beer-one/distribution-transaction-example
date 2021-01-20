package com.trx.presentation.router

import com.trx.presentation.handler.ProductHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class ProductRouter (
    private val handler: ProductHandler
) {
    @Bean
    fun productRoute(): RouterFunction<ServerResponse> {
        return coRouter {
            "/products".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", handler::search)
                    POST("", handler::create)
                    GET("/{id}", handler::getOne)
                    PUT("/{id}", handler::add)
                }
            }
        }
    }
}