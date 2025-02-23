package org.group2.comp313.kitchen_companion.repository;

import org.group2.comp313.kitchen_companion.domain.MealPlanDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MealPlanDayRepository extends JpaRepository<MealPlanDay, Integer>, JpaSpecificationExecutor<MealPlanDay> {
}