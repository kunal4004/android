package za.co.woolworths.financial.services.android.models.dto;

public class LoginRequest {

    public String idNumber;
    public String cardNumber;

    public LoginRequest(String idNumber, String cardNumber) {
        this.idNumber = idNumber;
        this.cardNumber = cardNumber;
    }
}
