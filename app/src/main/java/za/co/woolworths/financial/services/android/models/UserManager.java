package za.co.woolworths.financial.services.android.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;

public class UserManager {

	private static final String SESSION = "Session";
	public static final String STORE_CARD = "store_card";
	public static final String CREDIT_CARD = "credit_card";
	public static final String PERSONAL_LOAN = "personal_loan";
	private static final String ACCOUNT = "product_list";
	private static final String W_REWARDS = "w_rewards";
	private static final String ACCOUNT_CACHE_TIME = "account_cache_time";
	private static final String W_REWARDS_CACHE_TIME = "w_rewards_cache_time";
	private final Context mContext;

	UserManager(Context context) {
		mContext = context;
	}

	public String getSession() {
		return getSharedPreferences().getString(SESSION, "");
	}

	private SharedPreferences getSharedPreferences() {
		return mContext.getSharedPreferences("User", Context.MODE_PRIVATE);
	}

	public void setSession(String session) {
		getSharedPreferences().edit().putString(SESSION, session).commit();
	}

	public String getAccount(String account) {
		return getSharedPreferences().getString(account, "");
	}

	public String getWRewards() {
		if (getSharedPreferences().getLong(W_REWARDS_CACHE_TIME, 0l) + mContext.getResources().getInteger(R.integer.account_time_to_live) < System.currentTimeMillis()) {
			return "";
		} else {
			return getSharedPreferences().getString(W_REWARDS, "");
		}
	}

	public void setWRewards(VoucherResponse voucherResponse) {
		getSharedPreferences().edit().putString(W_REWARDS, new Gson().toJson(voucherResponse)).commit();
		getSharedPreferences().edit().putLong(W_REWARDS_CACHE_TIME, System.currentTimeMillis()).commit();
		WoolworthsApplication.setNumVouchers(voucherResponse.voucherCollection == null || voucherResponse.voucherCollection.vouchers == null ? 0 : voucherResponse.voucherCollection.vouchers.size());
	}

	public String getAccounts() {
		if (getSharedPreferences().getLong(ACCOUNT_CACHE_TIME, 0l) + mContext.getResources().getInteger(R.integer.account_time_to_live) < System.currentTimeMillis()) {
			return "";
		} else {
			return getSharedPreferences().getString(ACCOUNT, "");
		}
	}

	public void invalidateCache() {
		getSharedPreferences().edit().putLong(ACCOUNT_CACHE_TIME, 0).commit();
	}

	public void setAccounts(AccountsResponse s) {
		getSharedPreferences().edit().putLong(ACCOUNT_CACHE_TIME, System.currentTimeMillis()).apply();
		getSharedPreferences().edit().putString(ACCOUNT, new Gson().toJson(s)).apply();
		for (Account a : s.accountList) {
			if ("SC".equals(a.productGroupCode)) {
				setAccount(STORE_CARD, a);
			} else if ("CC".equals(a.productGroupCode)) {
				setAccount(CREDIT_CARD, a);
			} else if ("PL".equals(a.productGroupCode)) {
				setAccount(PERSONAL_LOAN, a);
			}
		}
	}

	public void setAccount(String account, Account a) {
		getSharedPreferences().edit().putString(account, new Gson().toJson(a)).commit();
	}

	public String getLandingScreen() {
		return getSharedPreferences().getString(String.format("LANDING-%s", getSession()), "");
	}

	public void setLandingScreen(String string) {
		getSharedPreferences().edit().putString(String.format("LANDING-%s", getSession()), string).commit();
	}

}
