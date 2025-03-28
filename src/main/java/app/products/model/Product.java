package app.products.model;

import app.category.model.Category;
import app.orderItem.model.OrderItem;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
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
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    private String description;

    @Column(nullable = false)
    private String image; //url

    @ManyToOne
    private User seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime listedOn;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    private boolean isVisible;

    @Column(nullable = false)
    private boolean isRemoved;
}
