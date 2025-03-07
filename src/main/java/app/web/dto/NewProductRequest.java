package app.web.dto;

import app.category.model.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
public class NewProductRequest {

    @Size(min = 2,max = 20, message = "Name length must be between 2 and 20 characters!")
    private String name;

    @Size(min = 10,max = 200, message = "Description length must be between 10 and 200 characters!")
    private String description;

    @NotNull(message = "The price should be no less than 1")
    @Min(value=1,message = "The price should be no less than 1")
    private BigDecimal price;

    @NotNull(message = "The quantity should be no less tha 1")
    @Min(value=1,message = "The quantity should be no less tha 1")
    private int quantity;

    @NotNull
    private Category category;

    @URL(message = "Enter a valid Url")
    @NotNull(message = "Image Url is mandatory")
    private String image;
}
