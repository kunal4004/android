package za.co.woolworths.financial.services.android.models.dto;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.util.Utils;


public class WGlobalState {

	private final Context mContext;
	private boolean toolbarIsDisplayed;
	private int mSaveButtonClick;
	private boolean determineLocationPopUpEnabled;
	private boolean fragmentIsReward;
	private String section;

	public WGlobalState(Context context) {
		this.mContext = context;
	}

	public static final String EMPTY_FIELD = "";
	public static final String ON_CANCEL = "CANCEL";
	public static final String ON_SIGN_IN = "SIGNIN";

	public static final int SYNC_FIND_IN_STORE = 3401;

	private boolean clothingProducts, foodProducts;
	private int startRadius, endRadius, mLatestSelectedPicker;

	private boolean onBackPressed, rewardHasExpired,
			colorWasPopup, sizeWasPopup;
	private String newSTSParams, storeLocatorJson, selectedSKUId, creditLimit;
	private List<StoreDetails> storeDetailsArrayList;
	private ArrayList<OtherSkus> colourSKUArrayList;
	private OtherSkus colorPopUpValue, sizePopUpValue;
	private CLIOfferDecision mDeclineDecision;

	public boolean getOnBackPressed() {
		return onBackPressed;
	}

	public void setOnBackPressed(boolean pOnBackPressed) {
		onBackPressed = pOnBackPressed;
	}

	private void setPersistentValue(SessionDao.KEY key, boolean value) {
		Utils.sessionDaoSave(mContext,
				key, String.valueOf(value));
	}

	private boolean getPersistentValue(SessionDao.KEY key) {
		String value = Utils.getSessionDaoValue(mContext, key);
		if (TextUtils.isEmpty(value)) {
			return false;
		}
		return Boolean.valueOf(value);
	}

	public boolean rewardHasExpired() {
		return rewardHasExpired;
	}

	public void setRewardHasExpired(boolean pRewardHasExpired) {
		rewardHasExpired = pRewardHasExpired;
	}

	public String getNewSTSParams() {
		return newSTSParams;
	}

	public void setNewSTSParams(String pNewSTSParams) {
		newSTSParams = pNewSTSParams;
	}

	public String getStoreLocatorJson() {
		return storeLocatorJson;
	}

	public void setStoreLocatorJson(String storeLocatorJson) {
		this.storeLocatorJson = storeLocatorJson;
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

	public String getSelectedSKUId() {
		return selectedSKUId;
	}

	public void setSelectedSKUId(String selectedSKUId) {
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

	public boolean fragmentIsReward() {
		return fragmentIsReward;
	}

	public void setFragmentIsReward(boolean fragmentIsReward) {
		this.fragmentIsReward = fragmentIsReward;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getSection() {
		return section;
	}
}