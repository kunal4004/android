package za.co.woolworths.financial.services.android.ui.fragments.wreward;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsVoucherDetailsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVoucherListAdapter;
import za.co.woolworths.financial.services.android.ui.views.ScrollingLinearLayoutManager;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsVouchersFragment extends Fragment {
    private RecyclerView recyclerView;
    public VoucherResponse voucherResponse;
    private ErrorHandlerView mErrorHandlerView;
    public int selectedVoucherPosition;
    public static final int LOCK_REQUEST_CODE_WREWARDS = 111;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Activity mActivity;
    private boolean isAuthenticated;
    private RelativeLayout relEmptyStateHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrewards_vouchers_fragment, container, false);
        Bundle bundle = getArguments();
        voucherResponse = new Gson().fromJson(bundle.getString("WREWARDS"), VoucherResponse.class);
        recyclerView = view.findViewById(R.id.recycler_view);
        relEmptyStateHandler = view.findViewById(R.id.relEmptyStateHandler);
        mErrorHandlerView = new ErrorHandlerView(getActivity(),
                relEmptyStateHandler,
                view.findViewById(R.id.imgEmpyStateIcon),
                view.findViewById(R.id.txtEmptyStateTitle),
                view.findViewById(R.id.txtEmptyStateDesc));

        ScrollingLinearLayoutManager mLayoutManager = new ScrollingLinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false, 1500
        );

        recyclerView.setLayoutManager(mLayoutManager);

        if (voucherResponse.voucherCollection.vouchers == null || voucherResponse.voucherCollection.vouchers.size() == 0) {
            displayNoVouchersView();
        } else {
            displayVouchers(voucherResponse);
        }

        mDisposables.add(WoolworthsApplication.getInstance()
                .bus()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object -> {
                    if (object != null) {
                        if (object instanceof WRewardsVouchersFragment) {
                            if (!isAuthenticated) {
                                AuthenticateUtils.getInstance(getActivity()).enableBiometricForCurrentSession(false);
                                startVoucherDetailsActivity();
                                isAuthenticated = true;
                            }
                        }
                    }
                }));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        uniqueIdsForRewardVoucherAutomation();
    }

    private void uniqueIdsForRewardVoucherAutomation() {
        Activity activity = getActivity();
        if (activity != null && activity.getResources() != null) {
            recyclerView.setContentDescription(getString(R.string.vouchersLayout));
            relEmptyStateHandler.setContentDescription(getString(R.string.voucher_empty_state));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_VOUCHERS);
    }

    public void displayNoVouchersView() {
        mErrorHandlerView.showEmptyState(0);
        mErrorHandlerView.hideIcon();
        mErrorHandlerView.hideTitle();
        mErrorHandlerView.textDescription(getActivity().getResources().getString(R.string.no_vouchers));
        recyclerView.setVisibility(View.GONE);
    }


    public void displayVouchers(final VoucherResponse vResponse) {
        mErrorHandlerView.hideEmpyState();
        recyclerView.setVisibility(View.VISIBLE);
        WRewardsVoucherListAdapter mAdapter = new WRewardsVoucherListAdapter();
        mAdapter.setItem(vResponse.voucherCollection.vouchers);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecycleViewClickListner(getActivity(), recyclerView, new RecycleViewClickListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectedVoucherPosition = position;
                if (AuthenticateUtils.getInstance(getActivity()).isBiometricAuthenticationRequired()) {
                    try {
                        AuthenticateUtils.getInstance(getActivity()).startAuthenticateApp(LOCK_REQUEST_CODE_WREWARDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    startVoucherDetailsActivity();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    public void startVoucherDetailsActivity() {
        Intent intent = new Intent(mActivity, WRewardsVoucherDetailsActivity.class);
        intent.putExtra("VOUCHERS", Utils.objectToJson(voucherResponse.voucherCollection));
        intent.putExtra("POSITION", selectedVoucherPosition);
        mActivity.startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }
}