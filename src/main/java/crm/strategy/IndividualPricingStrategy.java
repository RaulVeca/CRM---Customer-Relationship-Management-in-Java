package crm.strategy;

import crm.model.entity.Course;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategie de pricing pentru cursanți individuali.
 * Aplică discount-uri pentru plată anticipată sau early-bird.
 */
public class IndividualPricingStrategy implements PricingStrategy {

    private final boolean earlyBird;
    private final boolean fullPaymentUpfront;

    public IndividualPricingStrategy(boolean earlyBird, boolean fullPaymentUpfront) {
        this.earlyBird = earlyBird;
        this.fullPaymentUpfront = fullPaymentUpfront;
    }

    @Override
    public BigDecimal calculatePrice(Course course, int participants) {
        BigDecimal basePrice = course.getPriceIndividual();
        if (basePrice == null) return BigDecimal.ZERO;

        BigDecimal discount = calculateDiscount(basePrice, participants);
        return basePrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal basePrice, int participants) {
        if (basePrice == null) return BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;

        // Discount Early Bird: 15%
        if (earlyBird) {
            discount = discount.add(basePrice.multiply(new BigDecimal("0.15")));
        }

        // Discount plată integrală: 10%
        if (fullPaymentUpfront) {
            discount = discount.add(basePrice.multiply(new BigDecimal("0.10")));
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getStrategyName() {
        return "IndividualPricing";
    }
}
