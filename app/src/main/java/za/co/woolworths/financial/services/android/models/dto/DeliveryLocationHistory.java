package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

public class DeliveryLocationHistory {
    public Province province;
    public Suburb suburb;

    public DeliveryLocationHistory(Province province, Suburb suburb) {
        this.province = province;
        this.suburb = suburb;
    }
}
