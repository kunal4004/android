package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;

import za.co.woolworths.financial.services.android.models.dto.voucher_redemption.DiscountDetails;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class OrderSummary {

	public int totalItemsCount;
	public double total;
	public double estimatedDelivery;
	public double basketTotal;
	public boolean shippingAdjusted;
	public double savedAmount;
	public double totalStaffDiscount;
	public Suburb suburb;
	public String state;
	public String submittedDate;
	public JsonElement deliveryDates;
	public DiscountDetails discountDetails;

	public int getTotalItemsCount() {
		return totalItemsCount;
	}

	public void setTotalItemsCount(int totalItemsCount) {
		this.totalItemsCount = totalItemsCount;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public double getEstimatedDelivery() {
		return estimatedDelivery;
	}

	public void setEstimatedDelivery(double estimatedDelivery) {
		this.estimatedDelivery = estimatedDelivery;
	}

	public double getBasketTotal() {
		return basketTotal;
	}

	public void setBasketTotal(double basketTotal) {
		this.basketTotal = basketTotal;
	}

	public double getSavedAmount() {
		return savedAmount;
	}

	public void setSavedAmount(double savedAmount) {
		this.savedAmount = savedAmount;
	}

	public double getStaffDiscount() {
		return totalStaffDiscount;
	}

	public void setStaffDiscount(double staffDiscount) {
		this.totalStaffDiscount = staffDiscount;
	}
}
