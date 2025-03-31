package app.user.model;

import app.order.model.Order;
import app.orderItem.model.OrderItem;
import app.products.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false,unique = true)
    private String email;

    private String firstName;

    private String lastName;

    private String profilePictureUrl;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private List<UserRole> roles;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isBannedFromSelling;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "seller")
    private List<Product> productsForSale = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "buyer")
    private List<Order> orders = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<OrderItem> orderItems = new ArrayList<>();
}
