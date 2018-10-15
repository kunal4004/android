package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ProductView {

    @SerializedName("products")
    @Expose
    public ArrayList<ProductList> products = null;
    @SerializedName("response")
    @Expose
    public Response response;
    @SerializedName("pagingResponse")
    @Expose
    public PagingResponse pagingResponse;
    @SerializedName("httpCode")
    @Expose
    public Integer httpCode;
    public ArrayList<SortOption> sortOptions;
    public ArrayList<RefinementNavigation> navigation;
    public RefinementHistory history;

}