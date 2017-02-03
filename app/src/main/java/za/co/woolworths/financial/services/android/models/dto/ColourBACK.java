package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/30.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ColourBACK {

    @SerializedName("externalImageRef")
    @Expose
    public String externalImageRef;
    @SerializedName("imagePath")
    @Expose
    public String imagePath;
    @SerializedName("mimeType")
    @Expose
    public String mimeType;

}