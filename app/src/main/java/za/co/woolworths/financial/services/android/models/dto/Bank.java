package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bank {
	@SerializedName("bankName")
	@Expose
	public String bankName;

	public String bankImage;

	public int id;

	public Bank(String bankName,int id) {
		this.bankName = bankName;
		this.id=id;
	}
}
