package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

/**
 * Created by W7099877 on 12/10/2016.
 */

public class LocationResponse {

    public int httpCode;
    public Response response;
    public Boolean inGeofence;
    public List<StoreDetails> Locations;
}
