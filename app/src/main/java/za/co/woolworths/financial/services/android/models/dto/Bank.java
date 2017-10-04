package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bank {
	@SerializedName("bankName")
	@Expose
	public String bankName;

	public Bank(String bankName) {
		this.bankName = bankName;
	}
}
