package za.co.woolworths.financial.services.android.ui.fragments.product.shop;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.DeliveryLocationHistory;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.SetDeliveryLocationSuburbResponse;
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.rest.shop.GetSuburbs;
import za.co.woolworths.financial.services.android.models.rest.shop.SetDeliveryLocationSuburb;
import za.co.woolworths.financial.services.android.models.service.event.CartState;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.adapters.SuburbSelectionAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.USE_MY_LOCATION;

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
	private GetSuburbs getSuburbsAsync;
	private SetDeliveryLocationSuburb setDeliveryLocationSuburb;

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
		getSuburbsAsync.execute();
	}

	private GetSuburbs getSuburbs(final String locationId) {
		return new GetSuburbs(locationId, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				Log.i("SuburbSelectionFragment", "getRegions Succeeded");
				handleSuburbsResponse((SuburbsResponse) object);
			}

			@Override
			public void onFailure(final String errorMessage) {
				Log.e("SuburbSelectionFragment", "getRegions Error: " + errorMessage);

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (new ConnectionDetector().isOnline(getActivity())) {
									loadSuburbItems();
								}
							}

						});
						mErrorHandlerView.networkFailureHandler(errorMessage);
					}
				});
			}
		});
	}

	public void handleSuburbsResponse(SuburbsResponse response) {
		try {
			switch (response.httpCode) {
				case 200:
					configureSuburbList(response.suburbs);
					break;
				case 440:
					SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), response.response.stsParams);
					SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());

					// hide loading
					toggleLoading(false);
					btnRetry.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (new ConnectionDetector().isOnline(getActivity())) {
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
								if (new ConnectionDetector().isOnline(getActivity())) {
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
		Log.i("SuburbSelection", "Suburb selected: " + suburb.name + " for province: " + selectedProvince.name);
		setSuburbRequest(selectedProvince, suburb);
	}

	private void setSuburbRequest(final Province province, final Suburb suburb) {
		// TODO: confirm loading when doing this request
		mErrorHandlerView.hideErrorHandlerLayout();
		toggleLoading(true);

		setDeliveryLocationSuburb = new SetDeliveryLocationSuburb(suburb.id, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				Log.i("SuburbSelectionFragment", "setSuburb Succeeded");
				handleSetSuburbResponse((SetDeliveryLocationSuburbResponse) object, province, suburb);
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
									setSuburbRequest(province, suburb);
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

	private void handleSetSuburbResponse(SetDeliveryLocationSuburbResponse response, final Province province, final Suburb suburb) {
		try {
			switch (response.httpCode) {
				case 200:
					Utils.sendBus(new CartState(suburb.name + ", " + province.name));
					saveRecentDeliveryLocation(new DeliveryLocationHistory(province, suburb));
					// TODO: go back to cart if no items removed from cart, else go to list of removed items
					closeActivity();
					break;
				case 440:
					SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), response.response.stsParams);
					SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());

					// hide loading
					toggleLoading(false);
					btnRetry.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (new ConnectionDetector().isOnline(getActivity())) {
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
								if (new ConnectionDetector().isOnline(getActivity())) {
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

	private void saveRecentDeliveryLocation(DeliveryLocationHistory historyItem) {
		List<DeliveryLocationHistory> history = getRecentDeliveryLocations();
		SessionDao sessionDao = new SessionDao(getContext());
		sessionDao.key = SessionDao.KEY.DELIVERY_LOCATION_HISTORY;
		Gson gson = new Gson();
		boolean isExist = false;
		if (history == null) {
			history = new ArrayList<>();
			history.add(0, historyItem);
			String json = gson.toJson(history);
			sessionDao.value = json;
			try {
				sessionDao.save();
			} catch (Exception e) {
				Log.e("TAG", e.getMessage());
			}
		} else {
			for (DeliveryLocationHistory item : history) {
				if (item.suburb.id.equals(historyItem.suburb.id)) {
					isExist = true;
				}
			}
			if (!isExist) {
				history.add(0, historyItem);
				if (history.size() > 5)
					history.remove(5);

				sessionDao.value = gson.toJson(history);
				try {
					sessionDao.save();
				} catch (Exception e) {
					Log.e("TAG", e.getMessage());
				}
			}
		}

		//Trigger api validation token in DetailFragment
		Utils.sendBus(new ProductState(USE_MY_LOCATION));
	}

	private List<DeliveryLocationHistory> getRecentDeliveryLocations() {
		List<DeliveryLocationHistory> history = null;
		try {
			SessionDao sessionDao = new SessionDao(getContext(), SessionDao.KEY.DELIVERY_LOCATION_HISTORY).get();
			if (sessionDao.value == null) {
				history = new ArrayList<>();
			} else {
				Gson gson = new Gson();
				Type type = new TypeToken<List<DeliveryLocationHistory>>() {
				}.getType();
				history = gson.fromJson(sessionDao.value, type);
			}
		} catch (Exception e) {
			Log.e("TAG", e.getMessage());
		}
		return history;
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
}
