package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Bank {


	@SerializedName("id")
	@Expose
	public int id;

	@SerializedName("bankName")
	@Expose
	public String bankName;


	@SerializedName("bankImage")
	@Expose
	public String bankImage;

	public Bank(int id, String bankName, String bankImage) {
		this.id = id;
		this.bankName = bankName;
		this.bankImage = bankImage;
	}
}
