package za.co.woolworths.financial.services.android.models.dto;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.util.analytics.dto.AnalyticProductItem;

public class AddItemToCart {

	private String productId;
	private String catalogRefId;
	private int quantity;
	private String substitutionSelection;
	private String substitutionId;

	public AddItemToCart(String productId, String catalogRefId, int quantity) {
		this.productId = productId;
		this.catalogRefId = catalogRefId;
		this.quantity = quantity;
	}

	public AddItemToCart(String productId, String catalogRefId, int quantity, String substitutionSelection, String substitutionId) {
		this.productId = productId;
		this.catalogRefId = catalogRefId;
		this.quantity = quantity;
		this.substitutionSelection = substitutionSelection;
		this.substitutionId = substitutionId;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getProductId() {
		return productId;
	}

	public String getCatalogRefId() {
		return catalogRefId;
	}

    public AnalyticProductItem toAnalyticItem(String category) {
        return new AnalyticProductItem(
                productId,
                "",
                category,
                "",
                category,
                "",
                quantity,
                0.0,
                FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
                Integer.parseInt(FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE),
                ""
        );
    }
}
