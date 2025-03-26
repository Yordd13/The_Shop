package app.web.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
public class UpdateQuantityRequest {

    @Min(value=1,message = "The added quantity should be no less tha 1!")
    int quantity;
}
