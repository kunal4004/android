package za.co.woolworths.financial.services.android.models.dto;

import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails;

public class ShoppingDeliveryLocation {
    public FulfillmentDetails fulfillmentDetails;

    public ShoppingDeliveryLocation(FulfillmentDetails fulfillmentDetails) {
        this.fulfillmentDetails = fulfillmentDetails;
    }
}
