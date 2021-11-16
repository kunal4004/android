
package za.co.woolworths.financial.services.android.checkout.service.network;

import com.google.gson.annotations.SerializedName;

public class DeliveryStatus {

    @SerializedName("01")
    private Boolean _01;

    @SerializedName("02")
    private Boolean _02;

    @SerializedName("04")
    private Boolean _04;

    @SerializedName("07")
    private Boolean _07;

    public Boolean get01() {
        return _01;
    }

    public void set01(Boolean _01) {
        this._01 = _01;
    }

    public Boolean get02() {
        return _02;
    }

    public void set02(Boolean _02) {
        this._02 = _02;
    }

    public Boolean get04() {
        return _04;
    }

    public void set04(Boolean _04) {
        this._04 = _04;
    }

    public Boolean get07() {
        return _07;
    }

    public void set07(Boolean _07) {
        this._07 = _07;
    }

}
