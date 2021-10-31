package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.orderbook.exception.DuplicateOrderIdException;

public interface IOrderBook extends IReadOnlyOrderBook {
    void addOrder(Order order) throws DuplicateOrderIdException;

    void cancelOrder(long orderId);

    void changeOrderPrice(long orderId, long price);

    void changeOrderQuantity(long orderId, long quantity);
}
