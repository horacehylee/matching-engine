package com.horacehylee.matching_engine.domain;

import org.jetbrains.annotations.TestOnly;

import java.util.Objects;

public class Order {
    private final long orderId;
    private final long price;
    private final long quantity;
    private final Side side;
    private final long filled;

    private Order(long orderId, long price, long quantity, Side side, long filled) {
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.filled = filled;
    }

    public static Order of(long orderId, long price, long quantity, Side side, long filled) {
        return new Order(orderId, price, quantity, side, filled);
    }

    public static Order copyOfWithPrice(Order other, long price) {
        return new Order(other.orderId, price, other.quantity, other.side, other.filled);
    }

    public static Order copyOfWithQuantity(Order other, long quantity) {
        return new Order(other.orderId, other.price, quantity, other.side, other.filled);
    }

    public static Order copyOfWithFilled(Order other, long filled) {
        return new Order(other.orderId, other.price, other.quantity, other.side, filled);
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
                + ", filled="
                + filled
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
                && filled == order.filled
                && side == order.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, price, quantity, side, filled);
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

    public Side getSide() {
        return side;
    }

    public long getFilled() {
        return filled;
    }

    public long getRemainingQuantity() {
        return quantity - filled;
    }

    @TestOnly
    public static final class Builder {
        private long orderId;
        private long price;
        private long quantity;
        private Side side;
        private long filled;

        private Builder() {}

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

        public Builder withFilled(long filled) {
            this.filled = filled;
            return this;
        }

        public Order build() {
            return new Order(orderId, price, quantity, side, filled);
        }
    }
}
