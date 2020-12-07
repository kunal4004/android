package za.co.woolworths.financial.services.android.models.dto.npc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Card  implements Serializable {

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
    private String openedDate;

    public Date openedDate() {
        if (openedDate == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            return new Date(sdf.parse(openedDate).getTime());
        } catch (ParseException e) {
            return null;
        }

    }
}