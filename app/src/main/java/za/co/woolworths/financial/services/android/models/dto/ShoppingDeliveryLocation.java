package za.co.woolworths.financial.services.android.models.dto;

public class ShoppingDeliveryLocation {
    public Province province;
    public Suburb suburb;

    public ShoppingDeliveryLocation(Province province, Suburb suburb) {
        this.province = province;
        this.suburb = suburb;
    }
}
