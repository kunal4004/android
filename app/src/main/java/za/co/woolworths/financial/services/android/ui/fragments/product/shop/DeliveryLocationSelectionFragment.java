package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.rest.shop.SetDeliveryLocationSuburb;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.ui.adapters.DeliveryLocationAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;


public class DeliveryLocationSelectionFragment extends Fragment implements DeliveryLocationAdapter.OnItemClick, View.OnClickListener {

	public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;

	private ErrorHandlerView mErrorHandlerView;
	private View btnRetry;
	private final int SUBURB_SET_RESULT = 123401;

	private View selectionContentLayout, layoutPreviousSelectedLocations;
	private ProgressBar loadingProgressBar;
	private RecyclerView deliveryLocationHistoryList;
	private WTextView tvCurrentLocationTitle, tvCurrentLocationDescription;
	private ImageView imDeliveryLocation;

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

		Bundle bundle = getArguments();
		String suburbName = null, provinceName = null;
		if (bundle != null) {
			suburbName = bundle.getString("suburbName");
			provinceName = bundle.getString("provinceName");
		}

		RelativeLayout relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		btnRetry = view.findViewById(R.id.btnRetry);

		selectionContentLayout = view.findViewById(R.id.selectionContentLayout);
		layoutPreviousSelectedLocations = view.findViewById(R.id.layoutPreviousSelectedLocations);
		loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
		deliveryLocationHistoryList = view.findViewById(R.id.deliveryLocationHistoryList);
		tvCurrentLocationTitle = view.findViewById(R.id.tvCurrentLocationTitle);
		tvCurrentLocationDescription = view.findViewById(R.id.tvCurrentLocationDescription);
		imDeliveryLocation = view.findViewById(R.id.iconTick);

		view.findViewById(R.id.currentLocationLayout).setOnClickListener(this);

		configureLocationHistory();

		List<ShoppingDeliveryLocation> deliveryHistory = Utils.getDeliveryLocationHistory(this.getContext());
		if (deliveryHistory != null && deliveryHistory.size() > 0) {
			if (!TextUtils.isEmpty(suburbName)) {
				tvCurrentLocationTitle.setText(suburbName);
				tvCurrentLocationDescription.setText(provinceName);
				tvCurrentLocationDescription.setVisibility(View.VISIBLE);
				imDeliveryLocation.setBackgroundResource(R.drawable.tick_cli_active);
			}
		}
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
		List<ShoppingDeliveryLocation> history = getDeliveryLocationHistory();
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

	private ShoppingDeliveryLocation getCurrentDeliveryLocation() {
		Province province = new Province();
		province.name = "Current province here";
		Suburb suburb = new Suburb();
		suburb.name = "Current suburb here";
		return new ShoppingDeliveryLocation(province, suburb);
	}

	private List<ShoppingDeliveryLocation> getDeliveryLocationHistory() {
		List<ShoppingDeliveryLocation> history = null;
		try {
			SessionDao sessionDao = SessionDao.getByKey(SessionDao.KEY.DELIVERY_LOCATION_HISTORY);
			if (sessionDao.value == null) {
				history = new ArrayList<>();
			} else {
				Gson gson = new Gson();
				Type type = new TypeToken<List<ShoppingDeliveryLocation>>() {
				}.getType();
				history = gson.fromJson(sessionDao.value, type);
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return history;
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

		SetDeliveryLocationSuburb setDeliveryLocationSuburb = new SetDeliveryLocationSuburb(location.suburb.id, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				Log.i("SuburbSelectionFragment", "setSuburb Succeeded");
				handleSetSuburbResponse((SetDeliveryLocationSuburbResponse) object, location);
			}

			@Override
			public void onFailure(final String errorMessage) {
				Log.e("SuburbSelectionFragment", "setSuburb Error: " + errorMessage);

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (new ConnectionDetector().isOnline(getActivity())) {
									setSuburb(location);
								}
							}

						});
						mErrorHandlerView.networkFailureHandler(errorMessage);
					}
				});

			}
		});
		setDeliveryLocationSuburb.execute();
	}

	private void handleSetSuburbResponse(SetDeliveryLocationSuburbResponse response, final ShoppingDeliveryLocation location) {
		try {
			switch (response.httpCode) {
				case 200:
					// TODO: go back to cart if no items removed from cart, else go to list of removed items
					Activity activity = getActivity();
					if (activity != null) {
						Utils.saveRecentDeliveryLocation(location,getActivity());
						new AppInstanceObject(location).save();
						activity.setResult(SUBURB_SET_RESULT);
						Utils.sendBus(new CartState(location.suburb.name + ", " + location.province.name));
						activity.finish();
						activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
					}
					break;
				case 440:

					SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams);
					SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());

					// hide loading
					toggleLoading(false);
					btnRetry.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (new ConnectionDetector().isOnline(getActivity())) {
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
								if (new ConnectionDetector().isOnline(getActivity())) {
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
		deliveryLocationSelectionFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.delivery_location), false);
	}
}
