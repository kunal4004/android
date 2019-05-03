package za.co.woolworths.financial.services.android.models.dto.npc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Card {

    @SerializedName("cardNumber")
    @Expose
    public String cardNumber;
    @SerializedName("sequenceNumber")
    @Expose
    public Integer sequenceNumber;
    @SerializedName("embossingName")
    @Expose
    public String embossingName;
    @SerializedName("idRequired")
    @Expose
    public Boolean idRequired;
    @SerializedName("cardHolderType")
    @Expose
    public String cardHolderType;
    @SerializedName("openedDate")
    @Expose
    public String openedDate;

}