package za.co.woolworths.financial.services.android.models.dto;

import android.content.Context;
import android.text.TextUtils;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.util.Utils;

public class Counter {

    private final Context mContext;

    public Counter(Context context) {
        this.mContext = context;
    }

    public int getActiveVoucher() {
        String activeVoucher = Utils.getSessionDaoValue(mContext,
                SessionDao.KEY.STORE_VOUCHER_COUNT);
        if (TextUtils.isEmpty(activeVoucher)) {
            return 0;
        } else {
            return Integer.valueOf(activeVoucher);
        }
    }

    public void setActiveVoucher(int activeVoucher) {
        Utils.sessionDaoSave(mContext,
                SessionDao.KEY.STORE_VOUCHER_COUNT,
                String.valueOf(activeVoucher));
    }

    public boolean accountIsActive() {
        return Boolean.valueOf(Utils.getSessionDaoValue(mContext,
                SessionDao.KEY.ACCOUNT_IS_ACTIVE));
    }

    public void setAccountIsActive(boolean accountIsActive) {
        Utils.sessionDaoSave(mContext,
                SessionDao.KEY.ACCOUNT_IS_ACTIVE,
                String.valueOf(accountIsActive));
    }
}
