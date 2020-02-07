package za.co.woolworths.financial.services.android.util;

import java.util.List;
import java.util.Observable;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.dto.CartSummary;
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;

import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_ACCOUNT;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART;
import static za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_REWARD;

public class QueryBadgeCounter extends Observable {

    private int cartCount;
    private int voucherCount;
    private int messageCount;
    private int updateAtPosition;

    private Call<MessageResponse> mGetMessage;
    private Call<VoucherResponse> mGetVoucher;
    private Call<CartSummaryResponse> mGetCartCount;

    private static QueryBadgeCounter INSTANCE = null;

    // Returns a single instance of this class, creating it if necessary.
    public static QueryBadgeCounter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QueryBadgeCounter();
        }
        return INSTANCE;
    }

    public void setCartCount(int count, int updateAtPosition) {
        this.cartCount = count;
        this.updateAtPosition = updateAtPosition;
        setChanged();
        notifyObservers();
    }

    private void setVoucherCount(int count, int updateAtPosition) {
        this.voucherCount = count;
        this.updateAtPosition = updateAtPosition;
        setChanged();
        notifyObservers();
    }

    private void setMessageCount(int count, int updateAtPosition) {
        this.messageCount = count;
        this.updateAtPosition = updateAtPosition;
        setChanged();
        notifyObservers();
    }

    public void notifyBadgeCounterUpdate(int updateAtPosition) {
        this.updateAtPosition = updateAtPosition;
        setChanged();
        notifyObservers();
    }

    public int getCartCount() {
        return cartCount;
    }

    public int getVoucherCount() {
        return voucherCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getUpdateAtPosition() {
        return updateAtPosition;
    }

    public void queryAllBadgeCounters() {
        queryVoucherCount();
        queryCartSummaryCount();
        queryMessageCount();
    }

    public void queryMessageCount() {
        if (isUserAuthenticated()) return;
        if (isC2User()) return;
        mGetMessage = loadMessageCount();
    }

    private boolean isC2User() {
        return !SessionUtilities.getInstance().isC2User();
    }

    public void queryVoucherCount() {
        if (isUserAuthenticated()) return;
        if (isC2User()) return;
        mGetVoucher = loadVoucherCount();
    }

    private boolean isUserAuthenticated() {
        return !SessionUtilities.getInstance().isUserAuthenticated();
    }

    public void queryCartSummaryCount() {
        if (isUserAuthenticated()) return;
        mGetCartCount = loadShoppingCartCount();
    }

    private Call<VoucherResponse> loadVoucherCount() {
        Call<VoucherResponse> voucherCall = OneAppService.INSTANCE.getVouchers();
        voucherCall.enqueue(new CompletionHandler<>(new IResponseListener<VoucherResponse>() {
            @Override
            public void onSuccess(VoucherResponse voucherResponse) {
                switch (voucherResponse.httpCode) {
                    case 200:
                        VoucherCollection voucherCollection = voucherResponse.voucherCollection;
                        if (voucherCollection != null) {
                            if (voucherCollection.vouchers != null) {
                                setVoucherCount(voucherCollection.vouchers.size(), INDEX_REWARD);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Throwable error) {

            }
        }, VoucherResponse.class));

        return voucherCall;
    }

    private Call<CartSummaryResponse> loadShoppingCartCount() {
        GetCartSummary cartSummary = new GetCartSummary();
        return cartSummary.getCartSummary(new IResponseListener<CartSummaryResponse>() {
            @Override
            public void onSuccess(CartSummaryResponse cartSummaryResponse) {
                if (cartSummaryResponse.httpCode == 200) {
                    if (cartSummaryResponse.data == null) return;
                    List<CartSummary> cartSummary = cartSummaryResponse.data;
                    if (cartSummary.get(0) != null) {
                        setCartCount(cartSummary.get(0).totalItemsCount, INDEX_CART);
                    }
                }
            }

            @Override
            public void onFailure(Throwable error) {

            }
        });
    }

    private Call<MessageResponse> loadMessageCount() {
        Call<MessageResponse> messageResponseCall = OneAppService.INSTANCE.getMessagesResponse(5, 1);
        messageResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse messageResponse) {
                setMessageCount(messageResponse.unreadCount, INDEX_ACCOUNT);
            }

            @Override
            public void onFailure(Throwable error) {

            }
        }, MessageResponse.class));
        return messageResponseCall;
    }

    public void clearBadge() {
        setCartCount(0, INDEX_CART);
        setMessageCount(0, INDEX_ACCOUNT);
        setVoucherCount(0, INDEX_REWARD);
    }

    public void cancelCounterRequest() {
        cancelRequest(mGetMessage);
        cancelRequest(mGetVoucher);
        cancelRequest(mGetCartCount);
    }

    private void cancelRequest(Call call) {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    @Override
    public boolean hasChanged() {
        return true; //super.hasChanged();
    }

    public void queryBadgeCount() {
        updateAtPosition = 10;
        setChanged();
        notifyObservers();
    }
}
