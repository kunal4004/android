package za.co.woolworths.financial.services.android.ui.fragments.product.shop;


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
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.ProvincesResponse;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.adapters.ProvinceSelectionAdapter;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

public class ProvinceSelectionFragment extends Fragment implements ProvinceSelectionAdapter.OnItemClick {

	public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;

	private ErrorHandlerView mErrorHandlerView;
	private View btnRetry;

	private ProgressBar loadingProgressBar;
	private RecyclerView provinceList;
	private ProvinceSelectionAdapter provinceAdapter;
	private Call<ProvincesResponse> getProvincesAsync;

	public ProvinceSelectionFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_province_selection, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RelativeLayout relNoConnectionLayout = view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relNoConnectionLayout);
		mErrorHandlerView.setMargin(relNoConnectionLayout, 0, 0, 0, 0);
		btnRetry = view.findViewById(R.id.btnRetry);

		loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
		provinceList = view.findViewById(R.id.provinceList);

		loadProvinceItems();
	}

	private void toggleLoading(boolean show) {
		provinceList.setVisibility(show ? View.GONE : View.VISIBLE);
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

	private void loadProvinceItems() {
		toggleLoading(true);
		mErrorHandlerView.hideErrorHandler();
		getProvincesAsync = getProvinces();
	}

	private Call<ProvincesResponse> getProvinces() {
		Call<ProvincesResponse> provincesResponseCall = OneAppService.INSTANCE.getProvinces();
		provincesResponseCall.enqueue(new CompletionHandler<>(new RequestListener<ProvincesResponse>() {
			@Override
			public void onSuccess(ProvincesResponse provincesResponse) {
				Log.i("ProvinceSelection", "getRegions Succeeded");
				handleProvincesResponse(provincesResponse);
			}

			@Override
			public void onFailure(final Throwable error) {
				if (error == null) return;
				Log.e("SuburbSelectionFragment", "getRegions Error: " + error.getMessage());
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// hide loading
						toggleLoading(false);
						btnRetry.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
									loadProvinceItems();
								}
							}

						});
						mErrorHandlerView.networkFailureHandler(error.getMessage());
					}
				});
			}
		},ProvincesResponse.class));

		return provincesResponseCall;
	}

	public void handleProvincesResponse(ProvincesResponse response) {
		try {
			switch (response.httpCode) {
				case 200:
					configureProvinceList(response.regions);
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
								loadProvinceItems();
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
									loadProvinceItems();
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

	private void configureProvinceList(List<Province> items) {
		provinceAdapter = new ProvinceSelectionAdapter(items, this);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		provinceList.setLayoutManager(mLayoutManager);
		provinceList.setAdapter(provinceAdapter);

		toggleLoading(false);
	}

	@Override
	public void onItemClick(Province province) {
		Log.i("ProvinceSelection", "Province selected: " + province.name);
		// Open suburb list
		SuburbSelectionFragment suburbSelectionFragment = new SuburbSelectionFragment();
		suburbSelectionFragment.selectedProvince = province;
		openFragment(suburbSelectionFragment);
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
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.DELIVERY_LOCATION_PROVINCE);
		deliveryLocationSelectionFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.select_your_province), true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getProvincesAsync != null && !getProvincesAsync.isCanceled()) {
			getProvincesAsync.cancel();
		}
	}
}
