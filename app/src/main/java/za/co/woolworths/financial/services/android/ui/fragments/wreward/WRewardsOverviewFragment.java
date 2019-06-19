package za.co.woolworths.financial.services.android.ui.fragments.wreward;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse;
import za.co.woolworths.financial.services.android.models.dto.PromotionsResponse;
import za.co.woolworths.financial.services.android.models.dto.TierInfo;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.WRewardsMembersInfoActivity;
import za.co.woolworths.financial.services.android.ui.adapters.FeaturedPromotionsAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.CardType;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.Utils;
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
	private WTextView barCodeNumber;
	private ImageView bardCodeImage;
	private View flipCardFrontLayout;
	private View flipCardBackLayout;
	private AnimatorSet mSetRightOut;
	private AnimatorSet mSetLeftIn;
	private boolean mIsBackVisible = false;
	private boolean isStarted = false;
	public CardDetailsResponse cardDetailsResponse;
	;
	private ScrollView scrollWRewardsOverview;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.wrewards_overview_fragment, container, false);
		Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSOVERVIEW);
		noTireHistory = (WTextView) view.findViewById(R.id.noTireHistory);
		overviewLayout = (LinearLayout) view.findViewById(R.id.overviewLayout);
		infoImage = (ImageView) view.findViewById(R.id.infoImage);
		scrollWRewardsOverview = view.findViewById(R.id.scrollWRewardsOverview);
		tireStatus = (WTextView) view.findViewById(R.id.tireStatus);
		savings = (WTextView) view.findViewById(R.id.savings);
		mRlConnect = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), mRlConnect);
		mErrorHandlerView.setMargin(mRlConnect, 0, 0, 0, 0);
		toNextTire = (WTextView) view.findViewById(R.id.toNextTire);
		toNextTireLayout = (RelativeLayout) view.findViewById(R.id.toNextTireLayout);
		promotionViewPager = (ViewPager) view.findViewById(R.id.promotionViewPager);
		barCodeNumber = (WTextView) view.findViewById(R.id.barCodeNumber);
		bardCodeImage = (ImageView) view.findViewById(R.id.barCodeImage);
		flipCardFrontLayout = view.findViewById(R.id.flipCardFrontLayout);
		flipCardBackLayout = view.findViewById(R.id.flipCardBackLayout);
		loadDefaultCardType();
		Bundle bundle = getArguments();
		voucherResponse = new Gson().fromJson(bundle.getString("WREWARDS"), VoucherResponse.class);

		if (voucherResponse.tierInfo != null) {
			handleTireHistoryView(voucherResponse.tierInfo);
			if (bundle.containsKey("CARD_DETAILS")) {
				cardDetailsResponse = new Gson().fromJson(bundle.getString("CARD_DETAILS"), CardDetailsResponse.class);
				handleCard(cardDetailsResponse);
			}
		} else {
			handleNoTireHistoryView();
		}
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
					loadPromotionsAPI();
				}
			}

		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_OVERVIEW);
	}


	public void loadPromotionsAPI() {
		mErrorHandlerView.hideErrorHandlerLayout();
		Call<PromotionsResponse> promotionsResponseCall = OneAppService.INSTANCE.getPromotions();
		promotionsResponseCall.enqueue(new CompletionHandler<>(new RequestListener<PromotionsResponse>() {
			@Override
			public void onSuccess(PromotionsResponse promotionsResponse) {
				handlePromotionResponse(promotionsResponse);
			}

			@Override
			public void onFailure(Throwable error) {
				if (error.getMessage() == null) return;
				mErrorHandlerView.networkFailureHandler(error.getMessage());
			}
		},PromotionsResponse.class));

	}

	public void handlePromotionResponse(PromotionsResponse promotionsResponse) {
		try {
			if (promotionsResponse.httpCode == 200) {
				if (promotionsResponse.promotions.size() > 0) {
					promotionViewPager.setAdapter(new FeaturedPromotionsAdapter(getActivity(), promotionsResponse.promotions));
				}
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
		flipCardFrontLayout.setOnClickListener(this);
		flipCardBackLayout.setOnClickListener(this);
		if (currentStatus.equals(getString(R.string.valued)) || currentStatus.equals(getString(R.string.loyal))) {
			toNextTireLayout.setVisibility(View.VISIBLE);
			toNextTire.setText(WFormatter.formatAmount(tireInfo.toSpend));
		}
		loadPromotionsAPI();

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
		/*else if(v.getId() == R.id.flipCardBackLayout || v.getId()==R.id.flipCardFrontLayout)
		{
			flipCard();
		}*/
	}

	private void changeCameraDistance() {
		int distance = 8000;
		float scale = getResources().getDisplayMetrics().density * distance;
		flipCardFrontLayout.setCameraDistance(scale);
		flipCardBackLayout.setCameraDistance(scale);
	}

	private void loadAnimations() {
		mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.card_flip_out);
		mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.card_flip_in);
	}

	public void flipCard() {
		Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSFLIP);
		if (!mIsBackVisible) {
			mSetRightOut.setTarget(flipCardFrontLayout);
			mSetLeftIn.setTarget(flipCardBackLayout);
			mSetRightOut.start();
			mSetLeftIn.start();
			mIsBackVisible = true;
		} else {
			mSetRightOut.setTarget(flipCardBackLayout);
			mSetLeftIn.setTarget(flipCardFrontLayout);
			mSetRightOut.start();
			mSetLeftIn.start();
			mIsBackVisible = false;
		}
	}

	/*@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isStarted) {
			if(isVisibleToUser && voucherResponse.tierInfo!=null && !mIsBackVisible )
			{
				flipCard();
			}
		}

	}*/

	@Override
	public void onStart() {
		super.onStart();
		isStarted = true;
	}

	public void handleCard(CardDetailsResponse cardDetailsResponse) {
		if (cardDetailsResponse.cardType != null && cardDetailsResponse.cardNumber != null) {
			if (cardDetailsResponse.cardType.equalsIgnoreCase(CardType.WREWARDS.getType())) {
				flipCardFrontLayout.setBackgroundResource(R.drawable.wrewards_card);
				flipCardBackLayout.setBackgroundResource(R.drawable.wrewards_card_flipped);
			} else if (cardDetailsResponse.cardType.equalsIgnoreCase(CardType.MYSCHOOL.getType())) {
				flipCardFrontLayout.setBackgroundResource(R.drawable.myschool_card);
				flipCardBackLayout.setBackgroundResource(R.drawable.myschool_card_flipped);
			} else {
				return;
			}
			barCodeNumber.setText(WFormatter.formatVoucher(cardDetailsResponse.cardNumber));
			try {
				bardCodeImage.setImageBitmap(Utils.encodeAsBitmap(cardDetailsResponse.cardNumber, BarcodeFormat.CODE_128, bardCodeImage.getWidth(), 60));
			} catch (WriterException e) {
				e.printStackTrace();
			}
			loadAnimations();
			changeCameraDistance();
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					flipCard();
				}
			}, 1000);
		}
	}

	public void loadDefaultCardType() {
		flipCardFrontLayout.setBackgroundResource(R.drawable.wrewards_card);
		flipCardBackLayout.setBackgroundResource(R.drawable.wrewards_card_flipped);
	}

	public void scrollToTop() {
		ObjectAnimator anim = ObjectAnimator.ofInt(scrollWRewardsOverview, "scrollY", scrollWRewardsOverview.getScrollY(), 0);
		anim.setDuration(500).start();
	}
}