package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.account.Products;

public class AccountsResponse {

    public int httpCode;
    public Account account; //Required to get account product by product group code
    public ArrayList<Account> accountList;
    public ArrayList<Products> products;
    public Response response;
}
