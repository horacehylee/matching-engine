package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.OrderIdCounter;
import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.domain.Side;
import com.horacehylee.matching_engine.orderbook.exception.DuplicateOrderIdException;
import com.horacehylee.matching_engine.orderbook.exception.UnknownOrderIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);

        assertIterableEquals(Collections.singletonList(order), orderBook.getAskOrders());
        assertIterableEquals(Collections.emptyList(), orderBook.getBidOrders());
    }

    @Test
    public void testAddAskOrderInOrder() throws Exception {
        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        final Order order2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(20L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);
        orderBook.addOrder(order2);

        assertIterableEquals(Arrays.asList(order, order2), orderBook.getAskOrders());
    }

    @Test
    public void testInvalidAddWithSameOrderId() throws Exception {
        final long id = OrderIdCounter.get();
        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        final Order order2 =
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
        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(200L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        final Order order2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(20L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);
        orderBook.addOrder(order2);

        assertIterableEquals(Arrays.asList(order2, order), orderBook.getAskOrders());
    }

    @Test
    public void testAddBidOrderOfDiffPrices_OrdersInDescendingOrder() throws Exception {
        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.BID)
                        .build();

        final Order order2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(200L)
                        .withQuantity(20L)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(order);
        orderBook.addOrder(order2);

        assertIterableEquals(Arrays.asList(order2, order), orderBook.getBidOrders());
    }

    @Test
    public void testCancelOrder() throws Exception {
        final long id = OrderIdCounter.get();
        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);
        orderBook.cancelOrder(id);

        assertEquals(0, orderBook.getAskOrders().size());
    }

    @Test
    public void testInvalidCancelOrderWithUnknownOrderId() {
        final long id = OrderIdCounter.get();
        assertThrows(
                UnknownOrderIdException.class,
                () -> orderBook.cancelOrder(id),
                "Unknown order id \"" + id + "\" is given");
    }

    @Test
    public void testCancelAndAddOrderWithSameId() throws Exception {
        final long id = OrderIdCounter.get();
        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(order);

        orderBook.cancelOrder(id);
        orderBook.addOrder(order);

        assertIterableEquals(Collections.singletonList(order), orderBook.getAskOrders());
    }
}
