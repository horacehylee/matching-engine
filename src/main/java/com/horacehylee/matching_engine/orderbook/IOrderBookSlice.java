package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.domain.Side;

import java.util.List;

public interface IOrderBookSlice {

    Side getSide();

    long getPrice();

    List<Order> getOrders();

    long getVolume();
}
