package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.orderbook.exception.UnknownOrderIdException;

import java.util.List;

public interface IReadOnlyOrderBook {

    /**
     * Get list of ask orders
     * @return List of order in ascending prices
     */
    List<Order> getAskOrders();

    /**
     * Get list of bid orders
     * @return List of order in descending prices
     */
    List<Order> getBidOrders();

    Order getOrder(long orderId) throws UnknownOrderIdException;
}
