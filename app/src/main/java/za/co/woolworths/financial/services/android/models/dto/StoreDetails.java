package za.co.woolworths.financial.services.android.models.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by W7099877 on 12/10/2016.
 */

public class StoreDetails implements Serializable {
    public int id;
    public String name;
    public double latitude;
    public double longitude;
    public double distance;
    public String address;
    public String phoneNumber;
    public String status;
    public int isHeader;
    public List<StoreOfferings> offerings;
    public List<StoreTimeings> times;
    public boolean npcAvailable;
}
