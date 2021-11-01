package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.domain.Side;
import com.horacehylee.matching_engine.orderbook.exception.DuplicateOrderIdException;
import com.horacehylee.matching_engine.orderbook.exception.UnknownOrderIdException;
import com.horacehylee.matching_engine.orderbook.exception.UnknownPriceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderBookImpl implements IOrderBook {

    private final Map<Long, Order> orderIdMap;

    /** Key by price, value of list of orders */
    private final NavigableMap<Long, OrdersBucket> bidOrdersBuckets;

    /** Key by price, value of list of orders */
    private final NavigableMap<Long, OrdersBucket> askOrdersBuckets;

    private OrderBookImpl() {
        this.orderIdMap = new HashMap<>();
        this.bidOrdersBuckets = new TreeMap<>(Collections.reverseOrder());
        this.askOrdersBuckets = new TreeMap<>();
    }

    public static IOrderBook of() {
        return new OrderBookImpl();
    }

    @Override
    public void addOrder(Order order) throws DuplicateOrderIdException {
        final long orderId = order.getOrderId();
        if (orderIdMap.containsKey(orderId)) {
            throw new DuplicateOrderIdException(orderId);
        }

        final long price = order.getPrice();
        final Side side = order.getSide();
        final long quantity = order.getQuantity();

        // try matching
        // TODO: refactor this
        long quantityLeft = quantity;
        final NavigableMap<Long, OrdersBucket> oppositeSubBuckets =
                getOrdersBucketBySide(side.getOpposite()).headMap(price, true);
        for (Iterator<Map.Entry<Long, OrdersBucket>> bucketsIterator =
                        oppositeSubBuckets.entrySet().iterator();
                bucketsIterator.hasNext(); ) {
            Map.Entry<Long, OrdersBucket> bucketsEntry = bucketsIterator.next();
            OrdersBucket ordersBucket = bucketsEntry.getValue();

            for (Iterator<Map.Entry<Long, Order>> it = ordersBucket.getIterator(); it.hasNext(); ) {
                final Map.Entry<Long, Order> entry = it.next();
                final Order oppositeOrder = entry.getValue();
                final long filled = Math.min(quantityLeft, oppositeOrder.getRemainingQuantity());
                fillOrder(entry, ordersBucket, it, filled);
                if (ordersBucket.getVolume() == 0) {
                    bucketsIterator.remove();
                }
                quantityLeft -= filled;
                if (quantityLeft == 0) {
                    // TODO: trade event
                    return;
                }
            }
        }
        if (quantityLeft != quantity) {
            order = Order.copyOfWithFilled(order, quantity - quantityLeft);
        }

        getOrdersBucketBySide(side).computeIfAbsent(price, OrdersBucket::new).add(order);
        orderIdMap.put(order.getOrderId(), order);
    }

    @Override
    public void cancelOrder(final long orderId) throws UnknownOrderIdException {
        final Order order = getOrderById(orderId);
        final Side side = order.getSide();
        final long price = order.getPrice();

        orderIdMap.remove(orderId);

        NavigableMap<Long, OrdersBucket> ordersBuckets = getOrdersBucketBySide(side);
        OrdersBucket ordersBucket = ordersBuckets.get(price);
        if (ordersBucket == null) {
            throw new IllegalStateException(
                    "Orders bucket could not be found for "
                            + price
                            + " price for cancelling order "
                            + orderId);
        }
        ordersBucket.remove(order);
        if (ordersBucket.getVolume() == 0) {
            ordersBuckets.remove(price);
        }
    }

    @Override
    public void changeOrderPrice(long orderId, long price) throws UnknownOrderIdException {
        final Order originalOrder = getOrderById(orderId);
        cancelOrder(orderId);

        final Order order = Order.copyOfWithPrice(originalOrder, price);
        try {
            addOrder(order);
        } catch (DuplicateOrderIdException e) {
            throw new IllegalStateException(
                    "Duplicate order id \"" + orderId + "\" for changing order price", e);
        }
    }

    @Override
    public void changeOrderQuantity(long orderId, long quantity) throws UnknownOrderIdException {
        final Order originalOrder = getOrderById(orderId);
        final Side side = originalOrder.getSide();
        final long price = originalOrder.getPrice();

        final Order order = Order.copyOfWithQuantity(originalOrder, quantity);

        NavigableMap<Long, OrdersBucket> ordersBuckets = getOrdersBucketBySide(side);
        OrdersBucket ordersBucket = ordersBuckets.get(price);
        if (ordersBucket == null) {
            throw new IllegalStateException(
                    "Orders bucket could not be found for "
                            + price
                            + " price for changing quantity for order "
                            + orderId);
        }

        orderIdMap.replace(orderId, order);

        ordersBucket.replace(originalOrder, order);
        if (ordersBucket.getVolume() == 0) {
            ordersBuckets.remove(price);
        }
    }

    @Override
    public List<Order> getAskOrders() {
        return askOrdersBuckets.values().stream()
                .flatMap(OrdersBucket::getOrders)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getBidOrders() {
        return bidOrdersBuckets.values().stream()
                .flatMap(OrdersBucket::getOrders)
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrder(long orderId) throws UnknownOrderIdException {
        return getOrderById(orderId);
    }

    @Override
    public boolean containsOrder(long orderId) {
        return orderIdMap.containsKey(orderId);
    }

    @Override
    public IOrderBookSlice getSlice(long price) throws UnknownPriceException {
        OrdersBucket ordersBucket;
        Side side;
        if (bidOrdersBuckets.containsKey(price)) {
            ordersBucket = bidOrdersBuckets.get(price);
            side = Side.BID;
        } else if (askOrdersBuckets.containsKey(price)) {
            ordersBucket = askOrdersBuckets.get(price);
            side = Side.ASK;
        } else {
            throw new UnknownPriceException(price);
        }
        return new OrderBookSlice(
                side,
                price,
                ordersBucket.getVolume(),
                ordersBucket.getOrders().collect(Collectors.toUnmodifiableList()));
    }

    private void fillOrder(
            Map.Entry<Long, Order> entry,
            OrdersBucket ordersBucket,
            Iterator<Map.Entry<Long, Order>> iterator,
            long filled) {
        final Order order = entry.getValue();
        // TODO: have trade event
        final long orderId = order.getOrderId();
        final long remainingQuantity = order.getRemainingQuantity();
        if (filled == remainingQuantity) {
            orderIdMap.remove(orderId);
            ordersBucket.remove(iterator, order);

        } else if (filled < remainingQuantity) {
            Order newOrder = Order.copyOfWithFilled(order, filled);
            orderIdMap.replace(orderId, newOrder);
            ordersBucket.replace(entry, newOrder);
        } else {
            throw new IllegalStateException(
                    "Fill order should not have filled ("
                            + filled
                            + ") > remaining quantity ("
                            + remainingQuantity
                            + ") for order "
                            + orderId);
        }
    }

    private NavigableMap<Long, OrdersBucket> getOrdersBucketBySide(Side side) {
        return side == Side.BID ? bidOrdersBuckets : askOrdersBuckets;
    }

    private Order getOrderById(long orderId) throws UnknownOrderIdException {
        if (!orderIdMap.containsKey(orderId)) {
            throw new UnknownOrderIdException(orderId);
        }
        return orderIdMap.get(orderId);
    }

    private static class OrdersBucket implements Comparable<OrdersBucket> {
        private final long price;
        private long volume = 0;

        // Key by price to value of order
        private final LinkedHashMap<Long, Order> orders;

        private OrdersBucket(long price) {
            this.price = price;
            orders = new LinkedHashMap<>();
        }

        @Override
        public int compareTo(@NotNull OrderBookImpl.OrdersBucket o) {
            return Long.compare(this.price, o.price);
        }

        public void add(Order order) {
            orders.put(order.getOrderId(), order);
            volume += order.getRemainingQuantity();
        }

        public void remove(Order order) {
            orders.remove(order.getOrderId());
            volume -= order.getRemainingQuantity();
        }

        public void remove(Iterator<Map.Entry<Long, Order>> iterator, Order order) {
            iterator.remove();
            volume -= order.getRemainingQuantity();
        }

        public void replace(Order originalOrder, Order newOrder) {
            orders.replace(originalOrder.getOrderId(), newOrder);
            volume += newOrder.getRemainingQuantity() - originalOrder.getRemainingQuantity();
        }

        public void replace(Map.Entry<Long, Order> entry, Order newOrder) {
            final Order originalOrder = entry.getValue();
            entry.setValue(newOrder);
            volume += newOrder.getRemainingQuantity() - originalOrder.getRemainingQuantity();
        }

        public Stream<Order> getOrders() {
            return orders.values().stream();
        }

        public Iterator<Map.Entry<Long, Order>> getIterator() {
            return orders.entrySet().iterator();
        }

        public long getVolume() {
            return volume;
        }
    }

    private static class OrderBookSlice implements IOrderBookSlice {

        private final Side side;
        private final long price;
        private final long volume;
        private final List<Order> orders;

        private OrderBookSlice(Side side, long price, long volume, List<Order> orders) {
            this.side = side;
            this.price = price;
            this.volume = volume;
            this.orders = orders;
        }

        @Override
        public Side getSide() {
            return side;
        }

        @Override
        public long getPrice() {
            return price;
        }

        @Override
        public List<Order> getOrders() {
            return orders;
        }

        @Override
        public long getVolume() {
            return volume;
        }

        @TestOnly
        @Override
        public String toString() {
            return "OrderBookSlice{"
                    + "side="
                    + side
                    + ", price="
                    + price
                    + ", volume="
                    + volume
                    + ", orders="
                    + orders
                    + '}';
        }
    }
}
