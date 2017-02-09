package za.co.woolworths.financial.services.android.models.dto;


import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

}