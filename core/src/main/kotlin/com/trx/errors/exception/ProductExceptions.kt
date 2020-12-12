package com.trx.errors.exception

import com.trx.errors.CustomException
import com.trx.errors.code.ProductErrorCode

class ProductNotFoundException: CustomException(ProductErrorCode.NOT_FOUND)
class ProductOutOfStockException: CustomException(ProductErrorCode.OUT_OF_STOCK)