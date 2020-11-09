package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DebitOrder implements Serializable {
    @SerializedName("debitOrderActive")
    @Expose
    public boolean debitOrderActive;
    @SerializedName("debitOrderDeductionDay")
    @Expose
    public String debitOrderDeductionDay;
    @SerializedName("debitOrderProjectedAmount")
    @Expose
    public float debitOrderProjectedAmount;
}
