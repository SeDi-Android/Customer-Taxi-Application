package ru.sedi.customerclient.Otto;

public class OrderCancelEvent {
    private int orderId;

    public OrderCancelEvent(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
