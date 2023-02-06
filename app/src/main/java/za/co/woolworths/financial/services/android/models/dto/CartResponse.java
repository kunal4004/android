package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap;
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails;

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
	public VoucherDetails voucherDetails;
	public ProductCountMap productCountMap;
	public boolean liquorOrder;
	public String noLiquorImageUrl;
	public GlobalMessages globalMessages;
	public String jSessionId;
}
