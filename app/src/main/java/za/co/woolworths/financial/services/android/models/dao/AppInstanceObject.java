package za.co.woolworths.financial.services.android.models.dao;

import android.app.Activity;

import com.google.gson.Gson;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.OrderSummary;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigChatEnabledForProductFeatures;
import za.co.woolworths.financial.services.android.models.dto.chat.InAppChatTipAcknowledgements;
import za.co.woolworths.financial.services.android.ui.fragments.shop.domain.TooltipData;
import za.co.woolworths.financial.services.android.util.SessionUtilities;

/**
 * Created by w7099877 on 2018/06/12.
 */

public class AppInstanceObject {

    public ArrayList<User> users;

    public static final int MAX_DELIVERY_LOCATION_HISTORY = 5;
    public static final int MAX_USERS = 1;
    public boolean biometric;
    public FeatureWalkThrough featureWalkThrough;
    private InAppChatTipAcknowledgements inAppChatTipAcknowledgements;


    public AppInstanceObject() {
        users = new ArrayList<>();
        featureWalkThrough =  new FeatureWalkThrough();
        setDefaultInAppChatTipAcknowledgements();
    }

    public void setDefaultInAppChatTipAcknowledgements() {
        inAppChatTipAcknowledgements = new InAppChatTipAcknowledgements(false, new ConfigChatEnabledForProductFeatures(false, false, false, false), new ConfigChatEnabledForProductFeatures(false, false, false, false), new ConfigChatEnabledForProductFeatures(false, false, false, false), false);
    }

    public static AppInstanceObject get() {
        try {
            SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
            if (sessionDao.value != null) {
                return new Gson().fromJson(sessionDao.value, AppInstanceObject.class);
            }
        } catch (Exception e) {

        }

        return new AppInstanceObject();
    }

    public User getCurrentUserObject() {

        if (this.users.size() == 0) {
            return new User();
        } else {
            for (User user : this.users) {
                if (user.id.equalsIgnoreCase(getCurrentUsersID())) {
                    return user;
                }
            }
        }

        return new User();
    }

    public void save() {
        SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.APP_INSTANCE_OBJECT);
        sessionDao.value = new Gson().toJson(this);
        try {
            sessionDao.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class User {
        public String id;
        public ShoppingDeliveryLocation preferredShoppingDeliveryLocation;
        public ArrayList<ShoppingDeliveryLocation> shoppingDeliveryLocationHistory;
        public OrderSummary[] myOrdersPendingPicking;
        public SessionDao.BIOMETRIC_AUTHENTICATION_STATE biometricAuthenticationState;
        public boolean kmsi;
        public String absaLoginAliasID;
        public String absaDeviceID;
        public boolean isVirtualTemporaryStoreCardPopupShown;
        public boolean didShowDeliverySelectionModal;
        public boolean isLinkConfirmationScreenShown;
        public boolean isCheckedSubstituteAwarenessModal;
        public Long linkedDeviceIdentityId;
        public String mId;

        public String serverDyId;
        public String sessionDyId;

        public boolean enhanceSubstitutionFeatureShown;
        public String deliveryDetails;


        public User() {
            id = AppInstanceObject.getCurrentUsersID();
            shoppingDeliveryLocationHistory = new ArrayList<>();
            myOrdersPendingPicking = new OrderSummary[0];
        }

        public void save() {
            AppInstanceObject appInstanceObject = AppInstanceObject.get();
            if (appInstanceObject.users.size() == 0) {
                appInstanceObject.users.add(this);
            } else {
                int index = -1;
                for (int i = 0; i < appInstanceObject.users.size(); i++) {
                    if (appInstanceObject.users.get(i).id.equalsIgnoreCase(this.id)) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    appInstanceObject.users.add(this);
                    if (appInstanceObject.users.size() > AppInstanceObject.MAX_USERS)
                        appInstanceObject.users.remove(0);
                } else
                    appInstanceObject.users.set(index, this);

            }
            appInstanceObject.save();
        }

    }

    public static String getCurrentUsersID() {
        if (!SessionUtilities.getInstance().isUserAuthenticated())
            return "";
        ArrayList<String> arrEmail = SessionUtilities.getInstance().getJwt().email;
        return arrEmail == null ? "" : arrEmail.get(0);
    }

    public boolean isBiometricWalkthroughPresented() {
        return biometric;
    }

    public void setBiometricWalkthroughPresented(boolean biometric) {
        this.biometric = biometric;
    }

    public class FeatureWalkThrough {
        //Show Tutorials
        public TooltipData tooltipData = null;
        public boolean showTutorials = true; // Default to show

        //features
        public boolean barcodeScan;
        public boolean findInStore;
		public boolean deliveryLocation;
		public boolean vouchers;
		public boolean refineProducts;
		public boolean account;
		public boolean shoppingList;
		public boolean statements;
		public boolean cartRedeemVoucher;
		public boolean creditScore;
        public boolean isTryItOn;
        public boolean shopping;
        public boolean dash;
        public boolean delivery_details;
        public boolean my_lists;
        public boolean pargo_store;
        public boolean new_fbh_cnc;
        public boolean plp_add_to_list;

    }

    public InAppChatTipAcknowledgements getInAppChatTipAcknowledgements() {
        return inAppChatTipAcknowledgements;
    }
}
