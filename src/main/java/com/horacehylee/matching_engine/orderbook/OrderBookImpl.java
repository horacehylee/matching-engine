package com.horacehylee.matching_engine.orderbook;

import com.horacehylee.matching_engine.domain.Order;
import com.horacehylee.matching_engine.orderbook.exception.DuplicateOrderIdException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OrderBookImpl implements IOrderBook {

    private final Map<Long, Order> orderIdMap;

    /** Key by price, value of list of orders */
    private final NavigableMap<Long, OrdersBucket> bidOrdersBuckets;

    /** Key by price, value of list of orders */
    private final NavigableMap<Long, OrdersBucket> askOrdersBuckets;

    private OrderBookImpl() {
        this.orderIdMap = new HashMap<>();
        this.bidOrdersBuckets = new TreeMap<>();
        this.askOrdersBuckets = new TreeMap<>();
    }

    public static IOrderBook of() {
        return new OrderBookImpl();
    }

    @Override
    public void addOrder(Order order) throws DuplicateOrderIdException {
        if (orderIdMap.containsKey(order.getOrderId())) {
            throw new DuplicateOrderIdException(order.getOrderId());
        }
        NavigableMap<Long, OrdersBucket> ordersBuckets = bidOrdersBuckets;
        if (order.isAsk()) {
            ordersBuckets = askOrdersBuckets;
        }
        ordersBuckets.compute(
                order.getPrice(),
                (price, prevBucket) -> {
                    OrdersBucket bucket = prevBucket;
                    if (bucket == null) {
                        bucket = new OrdersBucket(order.getPrice());
                    }
                    bucket.addOrder(order);
                    orderIdMap.put(order.getOrderId(), order);
                    return bucket;
                });
    }

    @Override
    public void cancelOrder(long orderId) {}

    @Override
    public void changeOrderPrice(long orderId, long price) {}

    @Override
    public void changeOrderQuantity(long orderId, long quantity) {}

    @Override
    public List<Order> getAskOrders() {
        return askOrdersBuckets.values().stream()
                .flatMap(bucket -> bucket.getOrders().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getBidOrders() {
        return bidOrdersBuckets.values().stream()
                .flatMap(bucket -> bucket.getOrders().stream())
                .collect(Collectors.toList());
    }

    private class OrdersBucket implements Comparable<OrdersBucket> {

        private final long price;
        private final LinkedHashMap<Long, Order> orders;

        private OrdersBucket(long price) {
            this.price = price;
            orders = new LinkedHashMap<>();
        }

        @Override
        public int compareTo(@NotNull OrderBookImpl.OrdersBucket o) {
            return Long.compare(this.price, o.price);
        }

        public long getPrice() {
            return price;
        }

        public void addOrder(Order order) {
            orders.put(order.getOrderId(), order);
        }

        @TestOnly
        public List<Order> getOrders() {
            return List.copyOf(orders.values());
        }
    }
}
