package com.horacehylee.matching_engine.orderbook.exception;

public class DuplicateOrderIdException extends Exception {

    public DuplicateOrderIdException(long orderId) {
        super("Duplicate order id \"" + orderId + "\" is found");
    }
}
