package za.co.woolworths.financial.services.android.ui.fragments.help.tipstricks;


import android.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.TipsTricksFragmentBinding;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.adapters.TipsAndTricksListAdapter;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class TipsAndTricksFragment extends BaseFragment<TipsTricksFragmentBinding, TipsAndTricksViewModel> implements TipsAndTricksNavigator, CompoundButton.OnCheckedChangeListener {
    TipsAndTricksViewModel tipsAndTricksViewModel;
    RecyclerView rcvTipsAndTricks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tipsAndTricksViewModel = ViewModelProviders.of(this).get(TipsAndTricksViewModel.class);
        tipsAndTricksViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showBackNavigationIcon(true);
        setToolbarBackgroundDrawable(R.drawable.appbar_background);
        setTitle(getString(R.string.tips_tricks));
        showToolbar();
        rcvTipsAndTricks = view.findViewById(R.id.tipsAndTricksList);
        getViewDataBinding().featureSwitch.setOnCheckedChangeListener(this);
        getViewDataBinding().featureSwitch.setChecked(Utils.isFeatureWalkThroughTutorialsEnabled());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcvTipsAndTricks.setLayoutManager(mLayoutManager);
        rcvTipsAndTricks.setNestedScrollingEnabled(false);
        rcvTipsAndTricks.setAdapter(new TipsAndTricksListAdapter(getActivity(), this));
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.TIPS_AND_TRICKS_LIST);
    }

    @Override
    public TipsAndTricksViewModel getViewModel() {
        return tipsAndTricksViewModel;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.tips_tricks_fragment;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showBackNavigationIcon(true);
            setToolbarBackgroundDrawable(R.drawable.appbar_background);
            setTitle(getString(R.string.tips_tricks));
            showToolbar();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.enableFeatureWalkThroughTutorials(isChecked);
    }

    @Override
    public void onListItemSelected(int position) {
        Bundle bundle = this.getArguments();
        Intent intent = new Intent(getActivity(), TipsAndTricksViewPagerActivity.class);
        intent.putExtra("position", position);
        if (bundle != null)
            intent.putExtra("accounts", bundle.getString("accounts"));
        getActivity().startActivityForResult(intent, BottomNavigationActivity.TIPS_AND_TRICKS_CTA_REQUEST_CODE);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
}
