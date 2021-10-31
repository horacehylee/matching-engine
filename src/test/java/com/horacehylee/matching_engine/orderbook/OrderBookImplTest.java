package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.OrderIdCounter;
import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.domain.Side;
import com.horacehylee.matching_engine.orderbook.exception.DuplicateOrderIdException;
import com.horacehylee.matching_engine.orderbook.exception.UnknownOrderIdException;
import com.horacehylee.matching_engine.orderbook.exception.UnknownPriceException;
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

        assertIterableEquals(List.of(order), orderBook.getAskOrders());
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

        assertIterableEquals(List.of(order, order2), orderBook.getAskOrders());

        final IOrderBookSlice slice = orderBook.getSlice(100L);
        assertEquals(30L, slice.getVolume());
        assertEquals(Side.ASK, slice.getSide());
        assertEquals(100L, slice.getPrice());
        assertIterableEquals(List.of(order, order2), slice.getOrders());
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

        assertIterableEquals(List.of(order2, order), orderBook.getAskOrders());
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

        assertIterableEquals(List.of(order2, order), orderBook.getBidOrders());
        assertIterableEquals(Collections.emptyList(), orderBook.getAskOrders());

        final IOrderBookSlice slice = orderBook.getSlice(100L);
        assertEquals(10L, slice.getVolume());
        assertEquals(Side.BID, slice.getSide());
        assertEquals(100L, slice.getPrice());
        assertIterableEquals(List.of(order), slice.getOrders());
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

        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(100L),
                "Unknown price is \"" + 100L + "\"given");
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

        assertIterableEquals(List.of(order), orderBook.getAskOrders());
    }

    @Test
    public void testChangeOrderPrice() throws Exception {
        final long id = OrderIdCounter.get();
        final long originalPrice = 100L;
        final long newPrice = 200L;
        final long quantity = 10L;

        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(originalPrice)
                        .withQuantity(quantity)
                        .withSide(Side.ASK)
                        .build();
        orderBook.addOrder(order);
        orderBook.changeOrderPrice(id, newPrice);

        final Order expectedOrder =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(newPrice)
                        .withQuantity(quantity)
                        .withSide(Side.ASK)
                        .build();
        assertIterableEquals(List.of(expectedOrder), orderBook.getAskOrders());

        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(originalPrice),
                "Unknown price is \"" + originalPrice + "\"given");

        final IOrderBookSlice slice = orderBook.getSlice(newPrice);
        assertEquals(quantity, slice.getVolume());
    }

    @Test
    public void testChangeOrderQuantityIncrease() throws Exception {
        final long id = OrderIdCounter.get();
        final long originalQuantity = 10L;
        final long newQuantity = 20L;
        final long price = 100L;

        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(price)
                        .withQuantity(originalQuantity)
                        .withSide(Side.ASK)
                        .build();
        orderBook.addOrder(order);

        orderBook.changeOrderQuantity(id, newQuantity);

        final Order expectedOrder =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(price)
                        .withQuantity(newQuantity)
                        .withSide(Side.ASK)
                        .build();
        assertIterableEquals(List.of(expectedOrder), orderBook.getAskOrders());
        assertEquals(expectedOrder, orderBook.getOrder(id));

        final IOrderBookSlice slice = orderBook.getSlice(price);
        assertEquals(newQuantity, slice.getVolume());
    }

    @Test
    public void testChangeOrderQuantityDecrease() throws Exception {
        final long id = OrderIdCounter.get();
        final long originalQuantity = 10L;
        final long newQuantity = 5L;
        final long price = 100L;

        final Order order =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(price)
                        .withQuantity(originalQuantity)
                        .withSide(Side.ASK)
                        .build();
        orderBook.addOrder(order);

        orderBook.changeOrderQuantity(id, newQuantity);

        final Order expectedOrder =
                Order.Builder.anOrder()
                        .withOrderId(id)
                        .withPrice(price)
                        .withQuantity(newQuantity)
                        .withSide(Side.ASK)
                        .build();
        assertIterableEquals(List.of(expectedOrder), orderBook.getAskOrders());
        assertEquals(expectedOrder, orderBook.getOrder(id));

        final IOrderBookSlice slice = orderBook.getSlice(price);
        assertEquals(newQuantity, slice.getVolume());
    }
}
