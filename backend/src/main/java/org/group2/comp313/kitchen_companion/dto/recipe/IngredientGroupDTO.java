package org.group2.comp313.kitchen_companion.dto.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IngredientGroupDTO(
        @NotNull @Min(1) int ingredientGroupOrder,
        @Size(max = 255) String label,
        @NotNull @Size(min = 1) List<@Valid IngredientDTO> ingredients
) {
}
