package crm.strategy;

import crm.model.entity.Course;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategie de pricing pentru clienți corporate.
 * Discount-uri progresive pe baza numărului de participanți.
 */
public class CorporatePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Course course, int participants) {
        if (course.getPriceCorporatePerDay() == null) return BigDecimal.ZERO;

        // Calcul preț: zile_training * preț_pe_zi
        int days = (course.getDurationHours() != null) 
                ? Math.max(1, course.getDurationHours() / 8) : 1;

        BigDecimal totalPrice = course.getPriceCorporatePerDay()
                .multiply(BigDecimal.valueOf(days));

        BigDecimal discount = calculateDiscount(totalPrice, participants);
        return totalPrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal basePrice, int participants) {
        if (basePrice == null) return BigDecimal.ZERO;

        BigDecimal discountPercent;
        if (participants >= 50) {
            discountPercent = new BigDecimal("0.25");  // 25%
        } else if (participants >= 25) {
            discountPercent = new BigDecimal("0.20");  // 20%
        } else if (participants >= 15) {
            discountPercent = new BigDecimal("0.15");  // 15%
        } else if (participants >= 10) {
            discountPercent = new BigDecimal("0.10");  // 10%
        } else if (participants >= 5) {
            discountPercent = new BigDecimal("0.05");  // 5%
        } else {
            discountPercent = BigDecimal.ZERO;
        }

        return basePrice.multiply(discountPercent).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getStrategyName() {
        return "CorporatePricing";
    }
}
