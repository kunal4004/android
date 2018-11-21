package za.co.woolworths.financial.services.android.ui.fragments.wreward;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsVoucherDetailsActivity;
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVoucherListAdapter;
import za.co.woolworths.financial.services.android.ui.views.ScrollingLinearLayoutManager;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.RecycleViewClickListner;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsVouchersFragment extends Fragment {
	private ScrollingLinearLayoutManager mLayoutManager;
	private WRewardsVoucherListAdapter mAdapter;
	private RecyclerView recyclerView;
	public VoucherResponse voucherResponse;
	private ErrorHandlerView mErrorHandlerView;
	public int selectedVoucherPosition;
	public static final int LOCK_REQUEST_CODE_WREWARDS = 111;
	private CompositeDisposable mDisposables = new CompositeDisposable();
	private Activity mActivity;
	private boolean isAuthenticated;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.wrewards_vouchers_fragment, container, false);
		Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSVOUCHERS);
		Bundle bundle = getArguments();
		voucherResponse = new Gson().fromJson(bundle.getString("WREWARDS"), VoucherResponse.class);
		recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

		mErrorHandlerView = new ErrorHandlerView(getActivity(),
				(RelativeLayout) view.findViewById(R.id.relEmptyStateHandler),
				(ImageView) view.findViewById(R.id.imgEmpyStateIcon),
				(WTextView) view.findViewById(R.id.txtEmptyStateTitle),
				(WTextView) view.findViewById(R.id.txtEmptyStateDesc));

		mLayoutManager = new ScrollingLinearLayoutManager(
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
				.subscribe(new Consumer<Object>() {
					@Override
					public void accept(Object object) throws Exception {
						if (object != null) {
							if (object instanceof WRewardsVouchersFragment) {
								if (!isAuthenticated) {
									AuthenticateUtils.getInstance(getActivity()).enableBiometricForCurrentSession(false);
									startVoucherDetailsActivity();
									isAuthenticated = true;
								}
							}
						}
					}
				}));
		return view;
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
		mAdapter = new WRewardsVoucherListAdapter(getActivity(), vResponse.voucherCollection.vouchers);
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