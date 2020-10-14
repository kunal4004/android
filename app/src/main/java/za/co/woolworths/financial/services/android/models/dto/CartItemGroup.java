package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class CartItemGroup {

	public String type;

	public ArrayList<CommerceItem> commerceItems;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<CommerceItem> getCommerceItems() {
		return commerceItems;
	}

	public void setCommerceItems(ArrayList<CommerceItem> commerceItems) {
		this.commerceItems = commerceItems;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CartItemGroup cartItemGroup = (CartItemGroup) o;
		return this.type.equals(cartItemGroup.type);
	}
}
