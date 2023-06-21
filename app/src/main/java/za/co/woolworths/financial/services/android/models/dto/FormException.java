package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FormException {
	@SerializedName("id")
	@Expose
	public String id;
	@SerializedName("message")
	@Expose
	public String message;
}
