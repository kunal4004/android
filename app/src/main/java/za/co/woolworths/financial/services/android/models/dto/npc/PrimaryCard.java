package za.co.woolworths.financial.services.android.models.dto.npc;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PrimaryCard {

    @SerializedName("cardBlocked")
    @Expose
    public Boolean cardBlocked;
    @SerializedName("cards")
    @Expose
    public List<Card> cards = null;

}

