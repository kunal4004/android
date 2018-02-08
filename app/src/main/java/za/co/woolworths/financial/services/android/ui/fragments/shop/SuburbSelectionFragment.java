package za.co.woolworths.financial.services.android.ui.fragments.shop;


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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.rest.shop.GetSuburbs;
import za.co.woolworths.financial.services.android.ui.adapters.SuburbSelectionAdapter;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

public class SuburbSelectionFragment extends Fragment implements SuburbSelectionAdapter.SuburbSelectionCallback {

    public Province selectedProvince;

    public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;

    private RelativeLayout suburbContentLayout;
    private ProgressBar loadingProgressBar;
    private RecyclerView suburbList;
    private LinearLayout scrollbarLayout;
    private SuburbSelectionAdapter suburbAdapter;
    private GetSuburbs getSuburbsAsync;

    private SuburbAdapterAsyncTask listConfiguration;

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

    private void loadSuburbItems() {
        toggleLoading(true);
        getSuburbsAsync = getSuburbs(selectedProvince.id);
        getSuburbsAsync.execute();
    }

    private GetSuburbs getSuburbs(String locationId) {
        return new GetSuburbs(locationId, new OnEventListener() {
            @Override
            public void onSuccess(Object object) {
                Log.i("SuburbSelectionFragment", "getRegions Succeeded");
                handleSuburbsResponse(((SuburbsResponse) object));
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("SuburbSelectionFragment", "getRegions Error: " + errorMessage);
                // TODO: show error message
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
                    // TODO: do something about this
                    break;
                default:
                    // TODO: do something about this
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
            // TODO: scroll on touch/hover instead of click
            tvHeaderItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    suburbList.scrollToPosition(header.position);
                }
            });
            scrollbarLayout.addView(tvHeaderItem);
        }
    }

    @Override
    public void onItemClick(Suburb suburb) {
        Log.i("SuburbSelection", "Suburb selected: " + suburb.name + " for province: " + selectedProvince.name);
        // TODO: perform set suburb API request, add to db, then go back to cart
    }

    @Override
    public void setScrollbarVisibility(boolean visible) {
        scrollbarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
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
