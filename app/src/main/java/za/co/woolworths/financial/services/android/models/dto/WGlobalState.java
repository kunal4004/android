package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;
import java.util.List;

public class WGlobalState {

	private boolean toolbarIsDisplayed;
	private int mSaveButtonClick;
	private boolean determineLocationPopUpEnabled;
	private String section;
	private int navigateFromQuantity;

	public static final String EMPTY_FIELD = "";

	public static final int SYNC_FIND_IN_STORE = 3401;

	private boolean clothingProducts, foodProducts;
	private int startRadius, endRadius, mLatestSelectedPicker;

	private boolean onBackPressed,
			colorWasPopup, sizeWasPopup;
	private String creditLimit;
	private List<StoreDetails> storeDetailsArrayList;
	private ArrayList<OtherSkus> colourSKUArrayList;
	private OtherSkus colorPopUpValue, sizePopUpValue;
	private CLIOfferDecision mDeclineDecision;
	private OtherSkus selectedSKUId;
	private List<ShoppingList> shoppingListRequest;

	public boolean getOnBackPressed() {
		return onBackPressed;
	}

	public void setOnBackPressed(boolean pOnBackPressed) {
		onBackPressed = pOnBackPressed;
	}

	public boolean clothingIsEnabled() {
		return clothingProducts;
	}

	public void setClothingProducts(boolean clothingProducts) {
		this.clothingProducts = clothingProducts;
	}

	public boolean isFoodProducts() {
		return foodProducts;
	}

	public void setFoodProducts(boolean foodProducts) {
		this.foodProducts = foodProducts;
	}

	public int getStartRadius() {
		return startRadius;
	}

	public void setStartRadius(int startRadius) {
		this.startRadius = startRadius;
	}

	public int getEndRadius() {
		return endRadius;
	}

	public void setEndRadius(int endRadius) {
		this.endRadius = endRadius;
	}

	public OtherSkus getSelectedSKUId() {
		return selectedSKUId;
	}

	public void setSelectedSKUId(OtherSkus selectedSKUId) {
		this.selectedSKUId = selectedSKUId;
	}

	public List<StoreDetails> getStoreDetailsArrayList() {
		return storeDetailsArrayList;
	}

	public void setStoreDetailsArrayList(List<StoreDetails> storeDetailsArrayList) {
		this.storeDetailsArrayList = storeDetailsArrayList;
	}

	public ArrayList<OtherSkus> getColourSKUArrayList() {
		return colourSKUArrayList;
	}

	public void setColourSKUArrayList(ArrayList<OtherSkus> colourSKUArrayList) {
		this.colourSKUArrayList = colourSKUArrayList;
	}

	public boolean colorWasPopup() {
		return colorWasPopup;
	}

	public void setColorWasPopup(boolean colorWasPopup) {
		this.colorWasPopup = colorWasPopup;
	}

	public boolean sizeWasPopup() {
		return sizeWasPopup;
	}

	public void setSizeWasPopup(boolean sizeWasPopup) {
		this.sizeWasPopup = sizeWasPopup;
	}

	public OtherSkus getColorPickerSku() {
		return colorPopUpValue;
	}

	public void setColorPickerSku(OtherSkus colorPopUpValue) {
		this.colorPopUpValue = colorPopUpValue;
		this.mLatestSelectedPicker = 1;
	}

	public OtherSkus getSizePickerSku() {
		return sizePopUpValue;
	}

	public int getLatestSelectedPicker() {
		return mLatestSelectedPicker;
	}

	public void setSizePickerSku(OtherSkus sizePopUpValue) {
		this.sizePopUpValue = sizePopUpValue;
		this.mLatestSelectedPicker = 2;
	}

	public CLIOfferDecision getDeclineDecision() {
		return mDeclineDecision;
	}

	public void setDecisionDeclineOffer(CLIOfferDecision createOfferDecision) {
		this.mDeclineDecision = createOfferDecision;
	}

	public String getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(String creditLimit) {
		this.creditLimit = creditLimit;
	}

	public boolean toolbarIsShown() {
		return toolbarIsDisplayed;
	}

	public void setToolbarIsDisplayed(boolean toolbarIsDisplayed) {
		this.toolbarIsDisplayed = toolbarIsDisplayed;
	}

	public void saveButtonClicked(int saveButtonClick) {
		this.mSaveButtonClick = saveButtonClick;
	}

	public int getSaveButtonClick() {
		return mSaveButtonClick;
	}

	public boolean determineLocationPopUpEnabled() {
		return determineLocationPopUpEnabled;
	}

	public void setDetermineLocationPopUpEnabled(boolean determineLocationPopUpEnabled) {
		this.determineLocationPopUpEnabled = determineLocationPopUpEnabled;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getSection() {
		return section;
	}

	public void navigateFromQuantity(int screen) {
		this.navigateFromQuantity = screen;
	}

	public int getNavigateFromQuantity() {
		return navigateFromQuantity;
	}

	public List<ShoppingList> getShoppingListRequest() {
		return shoppingListRequest;
	}

	public void setShoppingListRequest(List<ShoppingList> shoppingListRequest) {
		this.shoppingListRequest = shoppingListRequest;
	}
}