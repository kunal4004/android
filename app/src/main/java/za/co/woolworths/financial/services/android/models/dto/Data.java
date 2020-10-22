package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.JsonElement;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails;
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherErrorMessage;

/**
 * Created by w7099877 on 2018/02/28.
 */

public class Data {
	public JsonElement items;

	public String suburbName;

	public String provinceName;

	public OrderSummary orderSummary;

	public String suburbId;

	public GlobalMessages globalMessages;

	public String jSessionId;

	public VoucherDetails voucherDetails;

	public ArrayList<VoucherErrorMessage> messages;

	public String provinceId;

}
