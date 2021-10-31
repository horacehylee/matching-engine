package com.horacehylee.matching_engine.domain;

import org.jetbrains.annotations.TestOnly;

import java.util.Objects;

public class Order {
    private final long orderId;
    private final long price;
    private final long quantity;
    private final Side side;

    private Order(long orderId, long price, long quantity, Side side) {
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
    }

    public static Order of(long orderId, long price, long quantity, Side side) {
        return new Order(orderId, price, quantity, side);
    }

    @Override
    public String toString() {
        return "Order{"
                + "orderId="
                + orderId
                + ", price="
                + price
                + ", quantity="
                + quantity
                + ", side="
                + side
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId
                && price == order.price
                && quantity == order.quantity
                && side == order.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, price, quantity, side);
    }

    public long getOrderId() {
        return orderId;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public boolean isBid() {
        return Side.BID.equals(side);
    }

    public boolean isAsk() {
        return Side.ASK.equals(side);
    }

    @TestOnly
    public static final class Builder {
        private long orderId;
        private long price;
        private long quantity;
        private Side side;

        private Builder() {
        }

        public static Builder anOrder() {
            return new Builder();
        }

        public Builder withOrderId(long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder withPrice(long price) {
            this.price = price;
            return this;
        }

        public Builder withQuantity(long quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder withSide(Side side) {
            this.side = side;
            return this;
        }

        public Order build() {
            return new Order(orderId, price, quantity, side);
        }
    }
}
