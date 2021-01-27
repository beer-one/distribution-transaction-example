package com.trx.errors.exception

import com.trx.errors.CustomException

class ProductNotFoundException(productId: Int): CustomException() {
    override val message: String = "(productId = $productId): 상품이 없습니다."
}
class ProductOutOfStockException(current: Int, required: Int): CustomException() {
    override val message: String = "상품의 재고가 부족합니다. current: $current, required: $required"
}