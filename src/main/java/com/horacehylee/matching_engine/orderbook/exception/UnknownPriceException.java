package com.horacehylee.matching_engine.orderbook.exception;

public class UnknownPriceException extends Exception {
    public UnknownPriceException(long price) {
        super("Unknown price \"" + price + "\" is given");
    }
}
