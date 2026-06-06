package crm.strategy;

import crm.model.entity.Course;

import java.math.BigDecimal;

/**
 * STRATEGY PATTERN - Pricing
 * 
 * Permite calcul preț diferit în funcție de tipul cursantului
 * sau de promoții active.
 */
public interface PricingStrategy {

    /**
     * Calculează prețul final pentru un curs.
     */
    BigDecimal calculatePrice(Course course, int participants);

    /**
     * Calculează discount-ul aplicabil.
     */
    BigDecimal calculateDiscount(BigDecimal basePrice, int participants);

    String getStrategyName();
}
