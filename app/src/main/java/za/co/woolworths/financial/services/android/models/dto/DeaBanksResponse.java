package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/20.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeaBanksResponse {
	@SerializedName("code")
	@Expose
	public String code;
	@SerializedName("desc")
	@Expose
	public String desc;
	@SerializedName("stsParams")
	@Expose
	public String stsParams;
}