package app.web.dto;

import app.order.model.Order;
import app.user.model.User;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserOrderItemsToOrderEvent {
    private final User user;
    private final Order order;

    public UserOrderItemsToOrderEvent(User user, Order order) {
        this.user = user;
        this.order = order;
    }
}
