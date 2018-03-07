package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class OrderSummary {

	public int totalItemsCount;
	public double total;
	public double estimatedDelivery;
	public double basketTotal;

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
}
