package za.co.woolworths.financial.services.android.models.dto;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.util.Utils;


public class WGlobalState {

	private final Context mContext;

	public WGlobalState(Context context) {
		this.mContext = context;
	}

	public static final String EMPTY_FIELD = "";
	public static final String ON_CANCEL = "CANCEL";
	public static final String ON_SIGN_IN = "SIGNIN";

	public static final int SYNC_FIND_IN_STORE = 3401;

	private boolean clothingProducts, foodProducts;
	private int startRadius, endRadius, mLatestSelectedPicker;

	private boolean cardGestureIsEnabled, onBackPressed, accountHasExpired, rewardHasExpired,
			FragmentIsReward, defaultPopupState, colorWasPopup, sizeWasPopup;
	private String pressState, newSTSParams, storeLocatorJson, selectedSKUId;
	private List<StoreDetails> storeDetailsArrayList;
	private ArrayList<OtherSku> colourSKUArrayList;
	private OtherSku colorPopUpValue, sizePopUpValue;
	public CreateOfferDecision mDeclineDecision;

	public void setAccountSignInState(boolean accountSignInState) {
		setPersistentValue(SessionDao.KEY.ACCOUNT_IS_ACTIVE, accountSignInState);
	}

	public boolean getAccountSignInState() {
		return getPersistentValue(SessionDao.KEY.ACCOUNT_IS_ACTIVE);
	}

	public void setRewardSignInState(boolean rewardSignInState) {
		setPersistentValue(SessionDao.KEY.REWARD_IS_ACTIVE, rewardSignInState);
	}

	public boolean getRewardSignInState() {
		return getPersistentValue(SessionDao.KEY.REWARD_IS_ACTIVE);
	}

	public boolean getOnBackPressed() {
		return onBackPressed;
	}

	public void setOnBackPressed(boolean pOnBackPressed) {
		onBackPressed = pOnBackPressed;
	}

	public boolean cardGestureIsEnabled() {
		return cardGestureIsEnabled;
	}

	public void setCardGestureIsEnabled(boolean pCardGestureIsEnabled) {
		cardGestureIsEnabled = pCardGestureIsEnabled;
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

	public boolean accountHasExpired() {
		return accountHasExpired;
	}

	public void setAccountHasExpired(boolean pAccountHasExpired) {
		accountHasExpired = pAccountHasExpired;
	}

	public String getPressState() {
		return pressState;
	}

	public void setPressState(String pPressState) {
		pressState = pPressState;
	}

	public boolean rewardHasExpired() {
		return rewardHasExpired;
	}

	public void setRewardHasExpired(boolean pRewardHasExpired) {
		rewardHasExpired = pRewardHasExpired;
	}

	public boolean fragmentIsReward() {
		return FragmentIsReward;
	}

	public void setFragmentIsReward(boolean pFragmentIsReward) {
		FragmentIsReward = pFragmentIsReward;
	}

	public String getNewSTSParams() {
		return newSTSParams;
	}

	public void setNewSTSParams(String pNewSTSParams) {
		newSTSParams = pNewSTSParams;
	}

	public void resetStsParams() {
		setNewSTSParams(EMPTY_FIELD);
	}

	public void resetPressState() {
		setPressState(EMPTY_FIELD);
	}

	public boolean getDefaultPopupState() {
		return defaultPopupState;
	}

	public void setDefaultPopupState(boolean pDefaultPopupState) {
		defaultPopupState = pDefaultPopupState;
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

	public ArrayList<OtherSku> getColourSKUArrayList() {
		return colourSKUArrayList;
	}

	public void setColourSKUArrayList(ArrayList<OtherSku> colourSKUArrayList) {
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

	public OtherSku getColorPickerSku() {
		return colorPopUpValue;
	}

	public void setColorPickerSku(OtherSku colorPopUpValue) {
		this.colorPopUpValue = colorPopUpValue;
		this.mLatestSelectedPicker = 1;
	}

	public OtherSku getSizePickerSku() {
		return sizePopUpValue;
	}

	public int getLatestSelectedPicker() {
		return mLatestSelectedPicker;
	}

	public void setSizePickerSku(OtherSku sizePopUpValue) {
		this.sizePopUpValue = sizePopUpValue;
		this.mLatestSelectedPicker = 2;
	}

	public CreateOfferDecision getDeclineDecision() {
		return mDeclineDecision;
	}

	public void setDecisionDeclineOffer(CreateOfferDecision createOfferDecision) {
		this.mDeclineDecision = createOfferDecision;
	}
}