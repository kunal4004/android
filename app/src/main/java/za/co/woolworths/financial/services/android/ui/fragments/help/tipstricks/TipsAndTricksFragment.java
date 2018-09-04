package za.co.woolworths.financial.services.android.ui.fragments.help.tipstricks;


import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.TipsTricksFragmentBinding;

import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class TipsAndTricksFragment extends BaseFragment<TipsTricksFragmentBinding, TipsAndTricksViewModel> implements TipsAndTricksNavigator, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    TipsAndTricksViewModel tipsAndTricksViewModel;

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
        getViewDataBinding().rewards.setOnClickListener(this);
        getViewDataBinding().stores.setOnClickListener(this);
        getViewDataBinding().barcode.setOnClickListener(this);
        getViewDataBinding().delivery.setOnClickListener(this);
        getViewDataBinding().featureSwitch.setOnCheckedChangeListener(this);
        getViewDataBinding().featureSwitch.setChecked(Utils.isFeatureWalkThroughTutorialsEnabled());
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rewards:
                openTipsAndTricksActivity(0);
                break;
            case R.id.stores:
                openTipsAndTricksActivity(1);
                break;
            case R.id.barcode:
                openTipsAndTricksActivity(2);
                break;
            case R.id.delivery:
                openTipsAndTricksActivity(3);
                break;
            default:
                break;
        }

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

    public void openTipsAndTricksActivity(int position) {
        Intent intent = new Intent(getActivity(), TipsAndTricksViewPagerActivity.class);
        intent.putExtra("position", position);
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Utils.enableFeatureWalkThroughTutorials(isChecked);
    }
}
