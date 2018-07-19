package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by jean on 1/24/18.
 */

public class CartPriceValues {
    public int basketItemCount;
    public double basketItems, estimatedDelivery, discounts, companyDiscounts, wRewardsSavings, otherDiscount, total;

    public CartPriceValues (int basketItemCount, double basketItems, double estimatedDelivery, double discounts, double companyDiscounts, double wRewardsSavings, double otherDiscount, double total) {
        this.basketItemCount = basketItemCount;
        this.basketItems = basketItems;
        this.estimatedDelivery = estimatedDelivery;
        this.discounts = discounts;
        this.companyDiscounts = companyDiscounts;
        this.wRewardsSavings = wRewardsSavings;
        this.otherDiscount = otherDiscount;
        this.total = total;
    }
}