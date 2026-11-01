package com.vitalbite.documental.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class DietPdfRequestDTO {

    // ── Identificadores ─────────────────────────────
    private String id;
    private String tenantId;
    private String patientId;
    private String nutritionistId;

    // ── Datos de la dieta ───────────────────────────
    private String name;
    private String objective;
    private String phase;
    private String approach;
    private String startDate;
    private String endDate;
    private String status;
    private Integer mealsPerDay;
    private String mainRestriction;
    private String notes;
    private Integer estimatedCalories;
    private Double adherencePercent;

    // ── Nombres resueltos desde el Core ─────────────
    private String patientFullName;
    private String nutritionistFullName;

    // ── Plan de días ─────────────────────────────────
    private List<DayDTO> days;

    @Data
    public static class DayDTO {
        private String id;
        private String dietPlanId;
        private String dayLabel;  // "Lunes", "Martes", etc.
        private Integer dayOrder;
        private String createdAt;
        private String updatedAt;
        private List<MealDTO> meals;
    }

    @Data
    public static class MealDTO {
        private String id;
        private String dietPlanDayId;
        private String name;       // "Desayuno", "Almuerzo", etc.
        private Integer mealOrder;
        private Integer targetCalories;
        private String notes;
        private String createdAt;
        private String updatedAt;
        private List<ItemDTO> items;
    }

    @Data
    public static class ItemDTO {
        private String id;
        private String dietMealId;
        private String name;       // "Avena 80g"
        private String portion;    // "1 taza"
        private Integer calories;
        private Integer itemOrder;
        private String notes;
        private String createdAt;
        private String updatedAt;
    }
}