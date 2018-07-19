package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/20.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeaBanks {
	@SerializedName("banks")
	@Expose
	public List<Bank> banks = null;
	@SerializedName("response")
	@Expose
	public DeaBanksResponse response;
	@SerializedName("httpCode")
	@Expose
	public Integer httpCode;
	@SerializedName("stsParams")
	@Expose
	public String stsParams;
}
