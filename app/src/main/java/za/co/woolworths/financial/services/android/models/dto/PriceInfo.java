package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class PriceInfo {

	public double amount;
	public double listPrice;
	public double rawTotalPrice;
	public double salePrice;
	public boolean onSale;
	public double discountedAmount;

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getListPrice() {
		return listPrice;
	}

	public void setListPrice(double listPrice) {
		this.listPrice = listPrice;
	}

	public double getRawTotalPrice() {
		return rawTotalPrice;
	}

	public void setRawTotalPrice(double rawTotalPrice) {
		this.rawTotalPrice = rawTotalPrice;
	}

	public double getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(double salePrice) {
		this.salePrice = salePrice;
	}

	public boolean isOnSale() {
		return onSale;
	}

	public void setOnSale(boolean onSale) {
		this.onSale = onSale;
	}

	public double getDiscountedAmount() {
		if(discountedAmount==0 && amount != rawTotalPrice)
			return rawTotalPrice-amount;
		else
		    return discountedAmount;
	}

	public void setDiscountedAmount(double discountedAmount) {
		this.discountedAmount = discountedAmount;
	}
}
