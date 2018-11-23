package za.co.woolworths.financial.services.android.models.dto;

import java.io.Serializable;

public class DebitOrder implements Serializable {
    public boolean debitOrderActive;
    public String debitOrderDeductionDay;
    public float debitOrderProjectedAmount;
}
