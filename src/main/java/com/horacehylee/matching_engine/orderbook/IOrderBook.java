package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.orderbook.exception.DuplicateOrderIdException;
import com.horacehylee.matching_engine.orderbook.exception.UnknownOrderIdException;

public interface IOrderBook extends IReadOnlyOrderBook {
    void addOrder(Order order) throws DuplicateOrderIdException;

    void cancelOrder(long orderId) throws UnknownOrderIdException;

    void changeOrderPrice(long orderId, long price) throws UnknownOrderIdException;

    void changeOrderQuantity(long orderId, long quantity) throws UnknownOrderIdException;
}
