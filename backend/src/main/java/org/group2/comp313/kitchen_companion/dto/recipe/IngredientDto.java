package org.group2.comp313.kitchen_companion.dto.recipe;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.group2.comp313.kitchen_companion.utility.ValidationGroups;

public record IngredientDto(
        @NotNull(groups = ValidationGroups.Update.class) Integer id,
        @NotNull @Min(1) int ingredientOrder,
        @Size(max = 500) String imageUrl,
        @NotNull @Size(min = 1, max = 255) String label
) {}