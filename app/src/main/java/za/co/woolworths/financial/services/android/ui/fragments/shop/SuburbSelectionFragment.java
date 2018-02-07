package za.co.woolworths.financial.services.android.ui.fragments.shop;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import za.co.woolworths.financial.services.android.models.dto.RegionResponse;
import za.co.woolworths.financial.services.android.models.dto.Suburb;
import za.co.woolworths.financial.services.android.models.rest.shop.GetRegions;
import za.co.woolworths.financial.services.android.ui.adapters.SuburbSelectionAdapter;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

public class SuburbSelectionFragment extends Fragment implements SuburbSelectionAdapter.OnItemClick, TextWatcher {

    public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;

    private RecyclerView suburbList;
    private LinearLayout scrollbarLayout;
    private View searchSeparator;
    private SuburbSelectionAdapter suburbAdapter;
    private GetRegions getRegionsAsync;

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
        suburbList = view.findViewById(R.id.suburbList);
        scrollbarLayout = view.findViewById(R.id.scrollbarLayout);
        searchSeparator = view.findViewById(R.id.searchSeparator);

        WEditTextView etvSuburbFilter = view.findViewById(R.id.etvSuburbFilter);
        etvSuburbFilter.addTextChangedListener(this);

        loadSuburbItems();
    }

    private void configureSuburbList(List<Suburb> suburbItems) {
        // TODO: make API request & show loading before setting the list
        suburbAdapter = new SuburbSelectionAdapter(suburbItems, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        suburbList.setLayoutManager(mLayoutManager);
        suburbList.setAdapter(suburbAdapter);

        configureSectionScrollbar();
    }



    private void loadSuburbItems() {
        getRegionsAsync = getRegions("2000030");
        getRegionsAsync.execute();
    }

    private GetRegions getRegions(String locationId) {
        return new GetRegions(locationId, new OnEventListener() {
            @Override
            public void onSuccess(Object object) {
                handleVoucherResponse(((RegionResponse) object));
            }

            @Override
            public void onFailure(String errorMessage) {
//                mErrorHandlerView.networkFailureHandler(errorMessage);
            }
        });
    }

    public void handleVoucherResponse(RegionResponse response) {
        try {
            switch (response.httpCode) {
                case 200:
//                    mWGlobalState.setRewardSignInState(true);
//                    mWGlobalState.setRewardHasExpired(false);
//                    if (response.voucherCollection.vouchers != null) {
//                        getBottomNavigator().addBadge(3, response.voucherCollection.vouchers.size());
//                    } else {
//                        clearVoucherCounter();
//                    }
//                    voucherResponse = response;
//                    isWrewardsCalled = true;
//                    handleWrewardsAndCardDetailsResponse();

                    configureSuburbList(response.suburbs);
                    break;
                case 440:
//                    progressBar.setVisibility(View.GONE);
//                    fragmentView.setVisibility(View.VISIBLE);
//                    clearVoucherCounter();
//                    mWGlobalState.setRewardHasExpired(true);
//                    mWGlobalState.setRewardSignInState(false);
//                    SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), response.response.stsParams);
//                    Utils.setBadgeCounter(getActivity(), 0);
//                    Intent intent = new Intent();
//                    WRewardsFragment mParentFragment = (WRewardsFragment) getParentFragment();
//                    if (mParentFragment != null) {
//                        mParentFragment.onActivityResult(WRewardsFragment.FRAGMENT_CODE_2, Activity.RESULT_OK, intent);
//                    }
//                    getFragmentManager().popBackStack();
//                    SessionExpiredUtilities.INSTANCE.showSessionExpireDialog(getActivity());
                    break;
                default:
//                    progressBar.setVisibility(View.GONE);
//                    fragmentView.setVisibility(View.VISIBLE);
//                    clearVoucherCounter();
//                    mWGlobalState.setRewardSignInState(false);
//                    setupErrorViewPager(viewPager);
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
                    Log.i("SuburbSelection", "Scroll header clicked: " + header.title + " at position: " + header.position);
                    // TODO: fix scrolling issue, might not be working because of recyclerview inside of nestedscrollview
                    suburbList.smoothScrollToPosition(header.position);
                }
            });
            scrollbarLayout.addView(tvHeaderItem);
        }
    }

    private ArrayList<Suburb> getSuburbItems() {
        ArrayList<Suburb> suburbItems = new ArrayList<>();
        Random r = new Random();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < 40; i++) {
            Suburb suburb = new Suburb();
            suburb.name = alphabet.charAt(r.nextInt(alphabet.length())) + " Suburb #" + (i + 1);
            suburbItems.add(suburb);
        }
        Collections.sort(suburbItems, new Comparator<Suburb>() {
            @Override
            public int compare(Suburb left, Suburb right) {
                return left.name.compareTo(right.name);
            }
        });
        return suburbItems;
    }

    @Override
    public void onItemClick(Suburb suburb) {
        Log.i("SuburbSelection", "Suburb selected: " + suburb.name);
    }

    @Override
    public void afterTextChanged(Editable s) {
        // filter list
        suburbAdapter.getFilter().filter(s.toString());
        scrollbarLayout.setVisibility(s.length() == 0 ? View.VISIBLE : View.GONE);
        searchSeparator.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
}
