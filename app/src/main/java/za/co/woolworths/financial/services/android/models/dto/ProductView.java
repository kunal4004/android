package za.co.woolworths.financial.services.android.models.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner;

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
    @SerializedName("sortOptions")
    @Expose
    public ArrayList<SortOption> sortOptions;
    @SerializedName("navigation")
    @Expose
    public ArrayList<RefinementNavigation> navigation;
    @SerializedName("history")
    @Expose
    public RefinementHistory history;
    @SerializedName("isBanners")
    @Expose
    public Boolean isBanners = false;
    @SerializedName("pageHeading")
    @Expose
    public String pageHeading;
    @SerializedName("dynamicBanners")
    @Expose
    public List<DynamicBanner> dynamicBanners;

}