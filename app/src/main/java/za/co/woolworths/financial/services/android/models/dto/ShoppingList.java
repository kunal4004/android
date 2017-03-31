package za.co.woolworths.financial.services.android.models.dto;

public class ShoppingList {

    private String product_id;
    private String product_name;
    private boolean ProductIsChecked;

    public ShoppingList(String product_id, String product_name, boolean productIsChecked) {
        this.product_id = product_id;
        this.product_name = product_name;
        ProductIsChecked = productIsChecked;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public boolean isProductIsChecked() {
        return ProductIsChecked;
    }
}
