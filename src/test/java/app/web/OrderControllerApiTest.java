package app.web;

import app.order.service.OrderService;
import app.orderItem.service.OrderItemService;
import app.products.model.Product;
import app.security.AuthenticationDetails;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.user.model.User;
import app.order.model.Order;
import app.orderItem.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static app.TestBuilder.aRandomProduct;
import static app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OrderItemService orderItemService;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getOrderPage_shouldReturnOrderView() throws Exception {

        User user = aRandomUser();
        Product product = aRandomProduct();
        OrderItem orderItem = OrderItem.builder()
                .user(user)
                .product(product)
                .quantity(1)
                .build();
        user.setOrderItems(List.of(orderItem));

        when(userService.getById(any())).thenReturn(user);
        when(orderItemService.getTotalPrice(any())).thenReturn(new BigDecimal("10"));
        when(userService.getOrderQuantity(any())).thenReturn(1);

        UUID userId = UUID.randomUUID();
        AuthenticationDetails principal = new AuthenticationDetails(userId, "User123", "123456789", List.of(UserRole.USER), true);

        MockHttpServletRequestBuilder request = get("/order")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("orderItems"))
                .andExpect(model().attributeExists("totalPrice"))
                .andExpect(model().attributeExists("cartQuantity"));
    }

    @Test
    void increaseQuantity_shouldRedirectToOrder() throws Exception {
        UUID orderItemId = UUID.randomUUID();

        doNothing().when(orderItemService).increaseQuantity(orderItemId);

        MockHttpServletRequestBuilder request = get("/order/" + orderItemId + "/increase")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(UserRole.USER), true)));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order"));

        verify(orderItemService, times(1)).increaseQuantity(orderItemId);
    }


    @Test
    void decreaseQuantity_shouldRedirectToOrder() throws Exception {
        UUID orderItemId = UUID.randomUUID();

        doNothing().when(orderItemService).decreaseQuantity(orderItemId);

        MockHttpServletRequestBuilder request = get("/order/" + orderItemId + "/decrease")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(UserRole.USER), true)));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order"));

        verify(orderItemService, times(1)).decreaseQuantity(orderItemId);
    }


    @Test
    void purchaseOrder_shouldRedirectToCategories() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = aRandomUser();
        Product product = aRandomProduct();
        OrderItem orderItem = OrderItem.builder()
                .user(user)
                .product(product)
                .quantity(1)
                .build();
        user.setOrderItems(List.of(orderItem));
        BigDecimal totalPrice = BigDecimal.TEN;

        when(userService.getById(userId)).thenReturn(user);
        when(orderItemService.getTotalPrice(List.of(orderItem))).thenReturn(totalPrice);

        MockHttpServletRequestBuilder request = get("/order/purchase")
                .with(user(new AuthenticationDetails(userId, "User123", "password", List.of(UserRole.USER), true)));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(orderService, times(1)).makeAnOrder(List.of(orderItem), user, totalPrice);
    }


    @Test
    void removeOrderItem_shouldRedirectToOrder() throws Exception {
        UUID orderItemId = UUID.randomUUID();

        doNothing().when(orderItemService).deleteOrderItem(orderItemId);

        MockHttpServletRequestBuilder request = get("/order/" + orderItemId + "/remove")
                .with(user(new AuthenticationDetails(UUID.randomUUID(), "User123", "password", List.of(UserRole.USER), true)));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order"));

        verify(orderItemService, times(1)).deleteOrderItem(orderItemId);
    }


    @Test
    void orderPreview_shouldReturnPreviewView() throws Exception {
        User user = aRandomUser();
        Product product = aRandomProduct();
        OrderItem orderItem = OrderItem.builder()
                .user(user)
                .product(product)
                .quantity(1)
                .build();
        user.setOrderItems(List.of(orderItem));
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .orderItems(List.of(orderItem))
                .buyer(user)
                .totalPrice(BigDecimal.TEN)
                .build();
        user.setOrders(List.of(order));
        int cartQuantity = 3;

        when(userService.getById(user.getId())).thenReturn(user);
        when(userService.getOrderQuantity(user)).thenReturn(cartQuantity);
        when(orderService.getById(order.getId())).thenReturn(order);
        when(orderItemService.getOrderItemsByOrderId(order)).thenReturn(List.of(orderItem));

        MockHttpServletRequestBuilder request = get("/order/" + order.getId() + "/preview")
                .with(user(new AuthenticationDetails(user.getId(), "User123", "password", List.of(UserRole.USER), true)));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("order-preview"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("orderItems"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("cartQuantity"));

        verify(orderService, times(1)).getById(order.getId());
        verify(orderItemService, times(1)).getOrderItemsByOrderId(order);
    }

}
