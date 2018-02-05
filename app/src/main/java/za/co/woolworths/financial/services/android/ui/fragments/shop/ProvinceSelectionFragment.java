package za.co.woolworths.financial.services.android.ui.fragments.shop;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.Province;
import za.co.woolworths.financial.services.android.ui.adapters.ProvinceSelectionAdapter;
import za.co.woolworths.financial.services.android.util.binder.DeliveryLocationSelectionFragmentChange;

public class ProvinceSelectionFragment extends Fragment implements ProvinceSelectionAdapter.OnItemClick {

    public DeliveryLocationSelectionFragmentChange deliveryLocationSelectionFragmentChange;

    private RecyclerView provinceList;
    private ProvinceSelectionAdapter provinceAdapter;

    public ProvinceSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_province_selection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        provinceList = view.findViewById(R.id.provinceList);

        configureProvinceList();
    }

    private void configureProvinceList() {
        // TODO: make API request & show loading before setting the list

        provinceAdapter = new ProvinceSelectionAdapter(getProvinceItems(), this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        provinceList.setLayoutManager(mLayoutManager);
        provinceList.setAdapter(provinceAdapter);
    }

    private ArrayList<Province> getProvinceItems() {
        ArrayList<Province> provinceItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Province province = new Province();
            province.title = "Province #" + (i + 1);
            provinceItems.add(province);
        }
        return provinceItems;
    }

    @Override
    public void onItemClick(Province province) {
        Log.i("ProvinceSelection", "Province selected: " + province.title);
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
        deliveryLocationSelectionFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.select_your_province), true);
    }
}
