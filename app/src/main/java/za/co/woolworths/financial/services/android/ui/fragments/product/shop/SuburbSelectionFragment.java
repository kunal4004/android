package za.co.woolworths.financial.services.android.ui.fragments.product.shop;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.ui.adapters.SuburbSelectionAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

public class SuburbSelectionFragment extends Fragment implements SuburbSelectionAdapter.SuburbSelectionCallback, View.OnTouchListener {

	public Province selectedProvince;

	public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;

	private ErrorHandlerView mErrorHandlerView;
	private View btnRetry;

	private RelativeLayout suburbContentLayout;
	private ProgressBar loadingProgressBar;
	private RecyclerView suburbList;
	private LinearLayout scrollbarLayout;
	private SuburbSelectionAdapter suburbAdapter;
	private Call<SuburbsResponse>  getSuburbsAsync;
	private Call<SetDeliveryLocationSuburbResponse> setDeliveryLocationSuburb;

	private SuburbAdapterAsyncTask listConfiguration;

	private int scrollbarHeight, scrollbarItemHeight;

	public SuburbSelectionFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_suburb_selection, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RelativeLayout relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		btnRetry = view.findViewById(R.id.btnRetry);

		suburbContentLayout = view.findViewById(R.id.suburbContentLayout);
		loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

		suburbList = view.findViewById(R.id.suburbList);
		scrollbarLayout = view.findViewById(R.id.scrollbarLayout);

		loadSuburbItems();
	}

	private void configureSuburbList(List<Suburb> suburbItems) {
		listConfiguration = new SuburbAdapterAsyncTask();
		listConfiguration.execute(suburbItems);
	}

	private void toggleLoading(boolean show) {
		suburbContentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
		if (show) {
			// show progress
			loadingProgressBar.getIndeterminateDrawable().setColorFilter(null);
			loadingProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
			loadingProgressBar.setVisibility(View.VISIBLE);
		} else {
			// hide progress
			loadingProgressBar.setVisibility(View.GONE);
			loadingProgressBar.getIndeterminateDrawable().setColorFilter(null);
		}
	}

	@Override
	public void setScrollbarVisibility(boolean visible) {
		scrollbarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void loadSuburbItems() {
		toggleLoading(true);
		mErrorHandlerView.hideErrorHandler();
		getSuburbsAsync = getSuburbs(selectedProvince.id);
	}

	private Call<SuburbsResponse>  getSuburbs(final String locationId) {

	Call<SuburbsResponse> suburbsResponseCall =  OneAppService.INSTANCE.getSuburbs(locationId);
		suburbsResponseCall.enqueue(new CompletionHandler<>(new RequestListener<SuburbsResponse>() {
			@Override
			public void onSuccess(SuburbsResponse suburbsResponse) {
				handleSuburbsResponse(suburbsResponse);
			}

			@Override
			public void onFailure(final Throwable error) {
				Activity activity = getActivity();
				if (error == null && activity == null) return;
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
									loadSuburbItems();
								}
							}
						});
						mErrorHandlerView.networkFailureHandler(error.getMessage());
					}
				});
			}
		},SuburbsResponse.class));

		return suburbsResponseCall;
	}

	public void handleSuburbsResponse(SuburbsResponse response) {
		try {
			switch (response.httpCode) {
				case 200:
					configureSuburbList(response.suburbs);
					break;
				case 440:

					SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams);
					SessionExpiredUtilities.getInstance().showSessionExpireDialog((AppCompatActivity) getActivity());

					// hide loading
					toggleLoading(false);
					btnRetry.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
								loadSuburbItems();
							}
						}

					});
					mErrorHandlerView.networkFailureHandler("");
					break;
				default:
					if (response.response != null) {
						Utils.alertErrorMessage(getActivity(), response.response.desc);

						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
									loadSuburbItems();
								}
							}

						});
						mErrorHandlerView.networkFailureHandler("");
					}
					break;
			}
		} catch (Exception ignored) {
		}
	}

	private void configureSectionScrollbar() {
		scrollbarLayout.removeAllViews();
		for (final SuburbSelectionAdapter.HeaderPosition header : suburbAdapter.getHeaderItems()) {
			WTextView tvHeaderItem = (WTextView) getLayoutInflater().inflate(R.layout.suburb_scrollbar_item, null);
			tvHeaderItem.setText(header.title);
			tvHeaderItem.setTag(header.position); // store position in view's tag
			scrollbarLayout.addView(tvHeaderItem);
		}

		scrollbarLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		scrollbarHeight = scrollbarLayout.getMeasuredHeight() - 20; // 20 = top and bottom padding
		if (!suburbAdapter.getHeaderItems().isEmpty()) {
			scrollbarItemHeight = scrollbarHeight / suburbAdapter.getHeaderItems().size();
		}
		scrollbarLayout.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.scrollbarLayout) {
			// Use the event's touch position to figure out the child view touched
			// Then, get the child view's tag, which is the position, and scroll to that position
			if (event.getY() > 0 && event.getY() <= scrollbarHeight) {
				int viewIndex = (int) (event.getY() / scrollbarItemHeight);
				if (viewIndex < scrollbarLayout.getChildCount()) {
					View childView = scrollbarLayout.getChildAt(viewIndex);
					int scrollPosition = (int) childView.getTag();
					if (scrollPosition == 1) {
						scrollPosition = 0;
					}
					suburbList.scrollToPosition(scrollPosition);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(Suburb suburb) {
		setSuburbRequest(selectedProvince, suburb);
	}

	private void setSuburbRequest(final Province province, final Suburb suburb) {
		// TODO: confirm loading when doing this request
		mErrorHandlerView.hideErrorHandlerLayout();
		toggleLoading(true);
		setDeliveryLocationSuburb =  OneAppService.INSTANCE.setSuburb(suburb.id);
		setDeliveryLocationSuburb.enqueue(new CompletionHandler<>(new RequestListener<SetDeliveryLocationSuburbResponse>() {
			@Override
			public void onSuccess(SetDeliveryLocationSuburbResponse setDeliveryLocationSuburbResponse) {
				handleSetSuburbResponse(setDeliveryLocationSuburbResponse, province, suburb);
			}

			@Override
			public void onFailure(final Throwable error) {
				Activity activity = getActivity();
				if (activity == null || error == null) return;
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
									setSuburbRequest(province, suburb);
								}
							}

						});
						mErrorHandlerView.networkFailureHandler(error.getMessage());
					}
				});
			}
		},SetDeliveryLocationSuburbResponse.class));
	}

	private void handleSetSuburbResponse(SetDeliveryLocationSuburbResponse response, final Province province, final Suburb suburb) {
		try {
			switch (response.httpCode) {
				case 200:
					Activity activity = getActivity();
					if (activity == null) return;
					Utils.sendBus(new CartState(suburb.name + ", " + province.name));
					Utils.savePreferredDeliveryLocation(new ShoppingDeliveryLocation(province, suburb));
					Utils.addToShoppingDeliveryLocationHistory(new ShoppingDeliveryLocation(province, suburb));
					Map<String, String> arguments = new HashMap<>();
					arguments.put(FirebaseManagerAnalyticsProperties.PropertyNames.SUBURBNAME, suburb.name);
					Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYCARTDELIVERY, arguments);
					activity.setResult(Activity.RESULT_OK);
					closeActivity();
					break;
				case 440:
					SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams);
					SessionExpiredUtilities.getInstance().showSessionExpireDialog((AppCompatActivity) getActivity());

					// hide loading
					toggleLoading(false);
					btnRetry.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
								setSuburbRequest(province, suburb);
							}
						}

					});
					mErrorHandlerView.networkFailureHandler("");
					break;
				default:
					if (response.response != null) {
						Utils.alertErrorMessage(getActivity(), response.response.desc);

						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
									setSuburbRequest(province, suburb);
								}
							}

						});
						mErrorHandlerView.networkFailureHandler("");
					}
					break;
			}
		} catch (Exception ignored) {
		}
	}

	private void closeActivity() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.finish();
			activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
		}
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			deliveryLocationSelectionFragmentChange = (DeliveryLocationSelectionFragmentChange) getActivity();
		} catch (ClassCastException ex) {
			Log.e("Interface", ex.toString());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.DELIVERY_LOCATION_SUBURB);
		deliveryLocationSelectionFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.select_your_suburb), true);
	}

	private class SuburbAdapterAsyncTask extends AsyncTask<List<Suburb>, Void, SuburbSelectionAdapter> {

		@Override
		protected SuburbSelectionAdapter doInBackground(List<Suburb>[] lists) {
			return new SuburbSelectionAdapter(lists[0], SuburbSelectionFragment.this);
		}

		@Override
		protected void onPostExecute(SuburbSelectionAdapter suburbSelectionAdapter) {
			suburbAdapter = suburbSelectionAdapter;
			LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
			mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			suburbList.setLayoutManager(mLayoutManager);
			suburbList.setAdapter(suburbAdapter);

			configureSectionScrollbar();
			toggleLoading(false);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getSuburbsAsync !=null && !getSuburbsAsync.isCanceled()){
			getSuburbsAsync.cancel();
		}

		if (setDeliveryLocationSuburb !=null && !setDeliveryLocationSuburb.isCanceled()){
			setDeliveryLocationSuburb.cancel();
		}
	}
}
