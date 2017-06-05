package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.PromotionsResponse;
import za.co.woolworths.financial.services.android.models.dto.TierInfo;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsMembersInfoActivity;
import za.co.woolworths.financial.services.android.ui.adapters.FeaturedPromotionsAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WFormatter;

/**
 * Created by W7099877 on 05/01/2017.
 */


public class WRewardsOverviewFragment extends Fragment implements View.OnClickListener {
	public ImageView infoImage;
	public WTextView tireStatus;
	public WTextView savings;
	public WTextView toNextTire;
	public RelativeLayout toNextTireLayout;
	public VoucherResponse voucherResponse;
	public ViewPager promotionViewPager;
	public String currentStatus;
	public LinearLayout overviewLayout;
	public WTextView noTireHistory;
	private RelativeLayout mRlConnect;
	private ErrorHandlerView mErrorHandlerView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.wrewards_overview_fragment, container, false);
		noTireHistory = (WTextView) view.findViewById(R.id.noTireHistory);
		overviewLayout = (LinearLayout) view.findViewById(R.id.overviewLayout);
		infoImage = (ImageView) view.findViewById(R.id.infoImage);
		tireStatus = (WTextView) view.findViewById(R.id.tireStatus);
		savings = (WTextView) view.findViewById(R.id.savings);
		mRlConnect = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), mRlConnect);
		mErrorHandlerView.setMargin(mRlConnect, 0, 0, 0, 0);
		toNextTire = (WTextView) view.findViewById(R.id.toNextTire);
		toNextTireLayout = (RelativeLayout) view.findViewById(R.id.toNextTireLayout);
		promotionViewPager = (ViewPager) view.findViewById(R.id.promotionViewPager);
		Bundle bundle = getArguments();
		voucherResponse = new Gson().fromJson(bundle.getString("WREWARDS"), VoucherResponse.class);
		if (voucherResponse.tierInfo != null) {
			handleTireHistoryView(voucherResponse.tierInfo);
		} else {
			handleNoTireHistoryView();
		}
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline()) {
					loadPromotions();
				}
			}

		});

		return view;
	}

	public void loadPromotions() {
		loadPromotionsAPI().execute();
	}

	public HttpAsyncTask<String, String, PromotionsResponse> loadPromotionsAPI() {
		return new HttpAsyncTask<String, String, PromotionsResponse>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mErrorHandlerView.hideErrorHandlerLayout();
			}

			@Override
			protected PromotionsResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getPromotions();
			}

			@Override
			protected Class<PromotionsResponse> httpDoInBackgroundReturnType() {
				return PromotionsResponse.class;
			}

			@Override
			protected PromotionsResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				mErrorHandlerView.networkFailureHandler(errorMessage);
				return new PromotionsResponse();
			}

			@Override
			protected void onPostExecute(PromotionsResponse promotionsResponse) {
				super.onPostExecute(promotionsResponse);
				handlePromotionResponse(promotionsResponse);
			}
		};
	}

	public void handlePromotionResponse(PromotionsResponse promotionsResponse) {
		try {
			switch (promotionsResponse.httpCode) {
				case 200:
					if (promotionsResponse.promotions.size() > 0) {
						promotionViewPager.setAdapter(new FeaturedPromotionsAdapter(getActivity(), promotionsResponse.promotions));
					}
					break;
				default:
					break;
			}
		} catch (NullPointerException ignored) {
		}
	}

	public void redirectToWRewardsMemberActivity(int type) {
		startActivity(new Intent(getActivity(), WRewardsMembersInfoActivity.class).putExtra("type", type));
		getActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
	}

	public void handleNoTireHistoryView() {
		overviewLayout.setVisibility(View.GONE);
		noTireHistory.setVisibility(View.VISIBLE);
	}

	public void handleTireHistoryView(TierInfo tireInfo) {
		overviewLayout.setVisibility(View.VISIBLE);
		noTireHistory.setVisibility(View.GONE);
		currentStatus = tireInfo.currentTier.toUpperCase();
		tireStatus.setText(tireInfo.currentTier);
		savings.setText(WFormatter.formatAmount(tireInfo.earned));
		infoImage.setOnClickListener(this);
		if (currentStatus.equals(getString(R.string.valued)) || currentStatus.equals(getString(R.string.loyal))) {
			toNextTireLayout.setVisibility(View.VISIBLE);
			toNextTire.setText(WFormatter.formatAmount(tireInfo.toSpend));
		}
		loadPromotions();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.infoImage) {
			if (currentStatus.equals(getString(R.string.valued))) {
				redirectToWRewardsMemberActivity(0);
			} else if (currentStatus.equals(getString(R.string.loyal))) {
				redirectToWRewardsMemberActivity(1);
			} else if (currentStatus.equals(getString(R.string.vip))) {
				redirectToWRewardsMemberActivity(2);
			}
		}
	}
}