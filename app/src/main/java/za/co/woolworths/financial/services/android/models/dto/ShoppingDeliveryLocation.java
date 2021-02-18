package za.co.woolworths.financial.services.android.models.dto;

public class ShoppingDeliveryLocation {
    public Province province;
    public Suburb suburb;
    public Store store;
    public boolean storePickup;

    public ShoppingDeliveryLocation(Province province, Suburb suburb, Store store) {
        this.province = province;
        this.suburb = suburb;
        this.store = store;
    }
}
