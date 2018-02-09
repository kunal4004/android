package za.co.woolworths.financial.services.android.ui.fragments.shop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.rest.shop.SetDeliveryLocationSuburb;
import za.co.woolworths.financial.services.android.ui.adapters.DeliveryLocationAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;


public class DeliveryLocationSelectionFragment extends Fragment implements DeliveryLocationAdapter.OnItemClick, View.OnClickListener {

    public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;
    private View selectionContentLayout;
    private ProgressBar loadingProgressBar;
    private RecyclerView deliveryLocationHistoryList;
    private WTextView tvCurrentLocationTitle, tvCurrentLocationDescription;

    private DeliveryLocationAdapter deliveryLocationAdapter;

    private SetDeliveryLocationSuburb setDeliveryLocationSuburb;

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
        selectionContentLayout = view.findViewById(R.id.selectionContentLayout);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        deliveryLocationHistoryList = view.findViewById(R.id.deliveryLocationHistoryList);
        tvCurrentLocationTitle = view.findViewById(R.id.tvCurrentLocationTitle);
        tvCurrentLocationDescription = view.findViewById(R.id.tvCurrentLocationDescription);

        view.findViewById(R.id.currentLocationLayout).setOnClickListener(this);

        configureCurrentLocation();
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

    private void configureCurrentLocation() {
        // TODO: make API request & show loading before setting the current location, if needed

        DeliveryLocationHistory currentLocation = getCurrentDeliveryLocation();
        tvCurrentLocationTitle.setText(currentLocation.suburb.name);
        tvCurrentLocationDescription.setText(currentLocation.province.name);
    }

    private void configureLocationHistory() {
        // TODO: make API request & show loading before setting the list

        deliveryLocationAdapter = new DeliveryLocationAdapter(getDeliveryLocationHistory(), this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        deliveryLocationHistoryList.setLayoutManager(mLayoutManager);
        deliveryLocationHistoryList.setAdapter(deliveryLocationAdapter);
    }

    private DeliveryLocationHistory getCurrentDeliveryLocation() {
        Province province = new Province();
        province.name = "Current province here";
        Suburb suburb = new Suburb();
        suburb.name = "Current suburb here";
        DeliveryLocationHistory location = new DeliveryLocationHistory(province, suburb);
        return location;
    }

    private List<DeliveryLocationHistory> getDeliveryLocationHistory() {
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

    private void onCurrentLocationClicked() {
        // Open province list
        openFragment(new ProvinceSelectionFragment());
    }

    @Override
    public void onItemClick(DeliveryLocationHistory location) {
        Log.i("DeliveryLocation", "Location selected: " + location.suburb.name);
        // TODO: confirm loading when doing this request
        toggleLoading(true);

        setDeliveryLocationSuburb = new SetDeliveryLocationSuburb(getActivity(), location.suburb.id, new OnEventListener() {
            @Override
            public void onSuccess(Object object) {
                Log.i("SuburbSelectionFragment", "setSuburb Succeeded");
                handleSetSuburbResponse((SetDeliveryLocationSuburbResponse) object);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("SuburbSelectionFragment", "setSuburb Error: " + errorMessage);
                // hide loading
                toggleLoading(false);

                // TODO: do something

            }
        });
        setDeliveryLocationSuburb.execute();
    }

    private void handleSetSuburbResponse(SetDeliveryLocationSuburbResponse response) {
        try {
            switch (response.httpCode) {
                case 200:
                    // TODO: go back to cart if no items removed from cart, else go to list of removed items
                    openFragment(new CartFragment());
                    break;
                case 440:
                    // TODO: do something about this
                    break;
                default:
                    // TODO: do something about this
                    break;
            }
        } catch (Exception ignored) {
        }
    }

    private void toggleLoading(boolean show) {
        selectionContentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        if(show) {
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
