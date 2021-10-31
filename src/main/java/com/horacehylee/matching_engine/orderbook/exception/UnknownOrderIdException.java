package com.horacehylee.matching_engine.orderbook.exception;

public class UnknownOrderIdException extends Exception {
    public UnknownOrderIdException(long orderId) {
        super("Unknown order id \"" + orderId + "\" is given");
    }
}
