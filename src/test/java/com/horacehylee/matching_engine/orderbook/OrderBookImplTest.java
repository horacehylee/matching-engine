package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.OrderIdCounter;
import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.domain.Side;
import com.horacehylee.matching_engine.orderbook.exception.DuplicateOrderIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderBookImplTest {

    private IOrderBook orderBook;

    @BeforeEach
    public void setup() {
        OrderIdCounter.reset();
        orderBook = OrderBookImpl.of();
    }

    @Test
    public void testAddAskOrder() throws Exception {
        Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);

        List<Order> askOrders = orderBook.getAskOrders();
        List<Order> bidOrders = orderBook.getBidOrders();

        assertIterableEquals(askOrders, Collections.singletonList(order));
        assertIterableEquals(bidOrders, Collections.emptyList());
    }

    @Test
    public void testAddAskOrderInOrder() throws Exception {
        Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        Order order2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(20L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);
        orderBook.addOrder(order2);

        List<Order> askOrders = orderBook.getAskOrders();
        assertIterableEquals(askOrders, Arrays.asList(order, order2));
    }

    @Test
    public void testInvalidAddWithSameOrderId() throws Exception {
        long id = OrderIdCounter.get();
        Order order =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        Order order2 =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(100L)
                        .withQuantity(20L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);

        assertThrows(
                DuplicateOrderIdException.class,
                () -> orderBook.addOrder(order2),
                "Duplicate order id \"" + id + "\" is found");
    }

    @Test
    public void testAddAskOrderOfDiffPrices_OrdersInAscendingOrder() throws Exception {
        Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(200L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        Order order2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(20L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);
        orderBook.addOrder(order2);

        List<Order> askOrders = orderBook.getAskOrders();
        assertIterableEquals(askOrders, Arrays.asList(order2, order));
    }

    @Test
    public void testAddBidOrderOfDiffPrices_OrdersInDescendingOrder() throws Exception {
        Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.BID)
                        .build();

        Order order2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(200L)
                        .withQuantity(20L)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(order);
        orderBook.addOrder(order2);

        List<Order> bidOrders = orderBook.getBidOrders();
        assertIterableEquals(bidOrders, Arrays.asList(order2, order));
    }
}
