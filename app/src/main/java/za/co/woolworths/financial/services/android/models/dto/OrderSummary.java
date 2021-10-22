package za.co.woolworths.financial.services.android.models.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;

import java.io.Serializable;

import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.DiscountDetails;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class OrderSummary implements Parcelable {

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
	public Store store;

	protected OrderSummary(Parcel in) {
		totalItemsCount = in.readInt();
		total = in.readDouble();
		estimatedDelivery = in.readDouble();
		basketTotal = in.readDouble();
		shippingAdjusted = in.readByte() != 0;
		savedAmount = in.readDouble();
		totalStaffDiscount = in.readDouble();
		state = in.readString();
		submittedDate = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(totalItemsCount);
		dest.writeDouble(total);
		dest.writeDouble(estimatedDelivery);
		dest.writeDouble(basketTotal);
		dest.writeByte((byte) (shippingAdjusted ? 1 : 0));
		dest.writeDouble(savedAmount);
		dest.writeDouble(totalStaffDiscount);
		dest.writeString(state);
		dest.writeString(submittedDate);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<OrderSummary> CREATOR = new Creator<OrderSummary>() {
		@Override
		public OrderSummary createFromParcel(Parcel in) {
			return new OrderSummary(in);
		}

		@Override
		public OrderSummary[] newArray(int size) {
			return new OrderSummary[size];
		}
	};

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
