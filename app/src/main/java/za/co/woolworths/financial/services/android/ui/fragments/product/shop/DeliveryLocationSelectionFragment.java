package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IResponseListener;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.ui.adapters.DeliveryLocationAdapter;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

public class DeliveryLocationSelectionFragment extends Fragment implements DeliveryLocationAdapter.OnItemClick, View.OnClickListener {

	public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;

	private ErrorHandlerView mErrorHandlerView;
	private View btnRetry;

	private View selectionContentLayout, layoutPreviousSelectedLocations;
	private ProgressBar loadingProgressBar;
	private RecyclerView deliveryLocationHistoryList;

	public DeliveryLocationSelectionFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_delivery_location_selection, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RelativeLayout relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		btnRetry = view.findViewById(R.id.btnRetry);

		selectionContentLayout = view.findViewById(R.id.selectionContentLayout);
		layoutPreviousSelectedLocations = view.findViewById(R.id.layoutPreviousSelectedLocations);
		loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
		deliveryLocationHistoryList = view.findViewById(R.id.deliveryLocationHistoryList);
		view.findViewById(R.id.currentLocationLayout).setOnClickListener(this);

		configureLocationHistory();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.currentLocationLayout:
				onCurrentLocationClicked();
				break;
		}
	}

	private void configureLocationHistory() {
		// TODO: make API request & show loading before setting the list
		List<ShoppingDeliveryLocation> history = Utils.getShoppingDeliveryLocationHistory();
		if (history != null && history.size() > 0) {
			DeliveryLocationAdapter deliveryLocationAdapter = new DeliveryLocationAdapter(history, this);
			LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
			mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			deliveryLocationHistoryList.setLayoutManager(mLayoutManager);
			deliveryLocationHistoryList.setAdapter(deliveryLocationAdapter);
		} else {
			layoutPreviousSelectedLocations.setVisibility(View.GONE);
		}
	}

	private void onCurrentLocationClicked() {
		// Open province list
		openFragment(new ProvinceSelectionFragment());
	}

	@Override
	public void onItemClick(ShoppingDeliveryLocation location) {
		Log.i("DeliveryLocation", "Location selected: " + location.suburb.name);
		setSuburb(location);
	}

	private void setSuburb(final ShoppingDeliveryLocation location) {
		// TODO: confirm loading when doing this request
		toggleLoading(true);
		Call<SetDeliveryLocationSuburbResponse> setDeliveryLocationSuburbResponseCall =  OneAppService.INSTANCE.setSuburb(location.suburb.id);
		setDeliveryLocationSuburbResponseCall.enqueue(new CompletionHandler<>(new IResponseListener<SetDeliveryLocationSuburbResponse>() {
			@Override
			public void onSuccess(SetDeliveryLocationSuburbResponse setDeliveryLocationSuburbResponse) {
				Log.i("SuburbSelectionFragment", "setSuburb Succeeded");
				handleSetSuburbResponse(setDeliveryLocationSuburbResponse, location);
			}

			@Override
			public void onFailure(final Throwable error) {
				if (error == null) return;
				Log.e("SuburbSelectionFragment", "setSuburb Error: " + error.getMessage());

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
									setSuburb(location);
								}
							}

						});
						mErrorHandlerView.networkFailureHandler(error.getMessage());
					}
				});
			}
		},SetDeliveryLocationSuburbResponse.class));

	}

	private void handleSetSuburbResponse(SetDeliveryLocationSuburbResponse response, final ShoppingDeliveryLocation location) {
		try {
			switch (response.httpCode) {
				case 200:
					// TODO: go back to cart if no items removed from cart, else go to list of removed items
					Activity activity = getActivity();
					if (activity != null) {
						Utils.savePreferredDeliveryLocation(location);
						activity.setResult(Activity.RESULT_OK);
						Utils.sendBus(new CartState(location.suburb.name + ", " + location.province.name));
						activity.finish();
						activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
					}
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
								setSuburb(location);
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
									setSuburb(location);
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

	private void toggleLoading(boolean show) {
		selectionContentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
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

	public void openFragment(Fragment fragment) {
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(R.id.content_frame, fragment).addToBackStack(null).commit();
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
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.DELIVERY_LOCATION_HISTORY);
		deliveryLocationSelectionFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.delivery_location), false);
	}
}
