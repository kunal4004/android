package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

/**
 * Created by W7099877 on 2018/02/08.
 */

public class CartResponse {
	public Response response;
	public int httpCode;
	public int suburbId;
	public ArrayList<CartItemGroup> cartItems;
	//public ArrayList<OrderSummary> orderSummaries;
	public OrderSummary orderSummary;
}
