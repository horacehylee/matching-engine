package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.OrderIdCounter;
import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.domain.Side;
import com.horacehylee.matching_engine.orderbook.exception.UnknownPriceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderBookImplMatchingTest {

    private IOrderBook orderBook;

    @BeforeEach
    public void setup() {
        OrderIdCounter.reset();
        orderBook = OrderBookImpl.of();
    }

    @Test
    public void testFullyFillWithAddingAskAndBidOrder() throws Exception {
        final long price = 100L;
        final long quantity = 10L;

        final Order askOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(quantity)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(askOrder);

        assertIterableEquals(List.of(askOrder), orderBook.getAskOrders());

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(quantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder);

        assertIterableEquals(List.of(), orderBook.getAskOrders());

        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(price),
                "Unknown price \"100\" is given");
    }

    @Test
    public void testPartialFillFirstWithSecondOrder() throws Exception {
        final long price = 100L;
        final long askQuantity = 10L;
        final long bidQuantity = 5L;

        final Order askOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(askQuantity)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(askOrder);

        assertIterableEquals(List.of(askOrder), orderBook.getAskOrders());

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder);

        final Order expectedRemainingAskOrder =
                Order.Builder.anOrder()
                        .withOrderId(askOrder.getOrderId())
                        .withPrice(price)
                        .withQuantity(askQuantity)
                        .withSide(Side.ASK)
                        .withFilled(bidQuantity)
                        .build();

        assertIterableEquals(List.of(expectedRemainingAskOrder), orderBook.getAskOrders());

        IOrderBookSlice orderBookSlice = orderBook.getSlice(price);
        assertIterableEquals(List.of(expectedRemainingAskOrder), orderBookSlice.getOrders());
        assertEquals(Side.ASK, orderBookSlice.getSide());
        assertEquals(askQuantity - bidQuantity, orderBookSlice.getVolume());
    }

    @Test
    public void testPartialFillSecondOrder() throws Exception {
        final long price = 100L;
        final long askQuantity = 10L;
        final long bidQuantity = 15L;

        final Order askOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(askQuantity)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(askOrder);

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder);

        final Order expectedRemainingBidOrder =
                Order.Builder.anOrder()
                        .withOrderId(bidOrder.getOrderId())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .withFilled(askQuantity)
                        .build();

        assertIterableEquals(List.of(expectedRemainingBidOrder), orderBook.getBidOrders());

        IOrderBookSlice orderBookSlice = orderBook.getSlice(price);
        assertIterableEquals(List.of(expectedRemainingBidOrder), orderBookSlice.getOrders());
        assertEquals(Side.BID, orderBookSlice.getSide());
        assertEquals(Math.abs(askQuantity - bidQuantity), orderBookSlice.getVolume());
    }

    @Test
    public void testOneOrderToBeFilledWithMultipleOrders() throws Exception {
        final long price = 100L;
        final long askQuantity = 10L;
        final long bidQuantity = 5L;

        final Order askOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(askQuantity)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(askOrder);

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder);

        final Order bidOrder2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder2);

        assertIterableEquals(List.of(), orderBook.getAskOrders());
        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(price),
                "Unknown price \"100\" is given");
    }

    @Test
    public void testMultipleOrdersToBeFilledWithOneOrder() throws Exception {
        final long price = 100L;
        final long askQuantity = 10L;
        final long bidQuantity = 15L;

        for (int i = 0; i < 2; i++) {
            final Order askOrder =
                    Order.Builder.anOrder()
                            .withOrderId(OrderIdCounter.get())
                            .withPrice(price)
                            .withQuantity(askQuantity)
                            .withSide(Side.ASK)
                            .build();

            orderBook.addOrder(askOrder);
        }

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder);

        final Order expectedRemainingAskOrder =
                Order.Builder.anOrder()
                        .withOrderId(2L)
                        .withPrice(price)
                        .withQuantity(askQuantity)
                        .withSide(Side.ASK)
                        .withFilled(5L)
                        .build();

        assertIterableEquals(List.of(expectedRemainingAskOrder), orderBook.getAskOrders());

        IOrderBookSlice orderBookSlice = orderBook.getSlice(price);
        assertIterableEquals(List.of(expectedRemainingAskOrder), orderBookSlice.getOrders());
        assertEquals(Side.ASK, orderBookSlice.getSide());
        assertEquals(5L, orderBookSlice.getVolume());
    }

    @Test
    public void testCancelOrderAfterPartialFilled() throws Exception {
        final long price = 100L;
        final long askQuantity = 10L;
        final long bidQuantity = 5L;

        final Order askOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(askQuantity)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(askOrder);

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder);

        orderBook.cancelOrder(askOrder.getOrderId());

        assertIterableEquals(List.of(), orderBook.getAskOrders());
        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(price),
                "Unknown price \"100\" is given");
    }

    @Test
    public void testChangeOrderQuantityAfterPartialFilled() throws Exception {
        final long price = 100L;
        final long askQuantity = 10L;
        final long bidQuantity = 5L;

        final Order askOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(askQuantity)
                        .withSide(Side.ASK)
                        .build();

        orderBook.addOrder(askOrder);

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(price)
                        .withQuantity(bidQuantity)
                        .withSide(Side.BID)
                        .build();

        orderBook.addOrder(bidOrder);

        orderBook.changeOrderQuantity(askOrder.getOrderId(), askQuantity - bidQuantity);

        assertIterableEquals(List.of(), orderBook.getAskOrders());
        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(price),
                "Unknown price \"100\" is given");
    }

    @Test
    public void testMatchingWithMultiplePrices() throws Exception {
        final Order askOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(100L)
                        .withQuantity(10L)
                        .withSide(Side.ASK)
                        .build();
        orderBook.addOrder(askOrder);

        final Order askOrder2 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(150L)
                        .withQuantity(15L)
                        .withSide(Side.ASK)
                        .build();
        orderBook.addOrder(askOrder2);

        final Order askOrder3 =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(200L)
                        .withQuantity(20L)
                        .withSide(Side.ASK)
                        .build();
        orderBook.addOrder(askOrder3);

        final Order bidOrder =
                Order.Builder.anOrder()
                        .withOrderId(OrderIdCounter.get())
                        .withPrice(180L)
                        .withQuantity(30L)
                        .withSide(Side.BID)
                        .build();
        orderBook.addOrder(bidOrder);

        final Order expectedRemainingBidOrder = Order.copyOfWithFilled(bidOrder, 25L);

        assertIterableEquals(List.of(askOrder3), orderBook.getAskOrders());
        assertIterableEquals(List.of(expectedRemainingBidOrder), orderBook.getBidOrders());

        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(100L),
                "Unknown price \"100\" is given");

        assertThrows(
                UnknownPriceException.class,
                () -> orderBook.getSlice(150L),
                "Unknown price \"150\" is given");

        {
            IOrderBookSlice orderBookSlice = orderBook.getSlice(180L);
            assertIterableEquals(List.of(expectedRemainingBidOrder), orderBookSlice.getOrders());
            assertEquals(Side.BID, orderBookSlice.getSide());
            assertEquals(5L, orderBookSlice.getVolume());
        }
        {
            IOrderBookSlice orderBookSlice = orderBook.getSlice(200L);
            assertIterableEquals(List.of(askOrder3), orderBookSlice.getOrders());
            assertEquals(Side.ASK, orderBookSlice.getSide());
            assertEquals(20L, orderBookSlice.getVolume());
        }
    }
}
