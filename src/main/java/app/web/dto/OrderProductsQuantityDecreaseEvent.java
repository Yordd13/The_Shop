package app.web.dto;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class OrderProductsQuantityDecreaseEvent {
    private Map<UUID, Integer> productQuantityMap;

    public OrderProductsQuantityDecreaseEvent(Map<UUID, Integer> productQuantityMap) {
        this.productQuantityMap = productQuantityMap;
    }
}
