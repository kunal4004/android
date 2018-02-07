package za.co.woolworths.financial.services.android.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Product {

    @SerializedName("products")
    @Expose
    public ArrayList<ProductDetail> products = null;
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