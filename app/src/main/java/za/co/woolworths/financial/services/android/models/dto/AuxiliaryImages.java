package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2017/01/30.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuxiliaryImages {

    @Expose
    public ColourBACK colourBACK;
    @SerializedName("colour_SIDE")
    @Expose
    public ColourSIDE colourSIDE;
}