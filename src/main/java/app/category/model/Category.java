package app.category.model;

import app.products.model.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String categoryPictureUrl;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.EAGER)
    private List<Product> products = new ArrayList<>();
}
