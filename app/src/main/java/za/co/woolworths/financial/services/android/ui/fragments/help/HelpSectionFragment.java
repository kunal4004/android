package za.co.woolworths.financial.services.android.ui.fragments.help;


import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.NeedHelpFragmentBinding;

import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.faq.FAQFragment;
import za.co.woolworths.financial.services.android.ui.fragments.help.tipstricks.TipsAndTricksFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class HelpSectionFragment extends BaseFragment<NeedHelpFragmentBinding, NeedHelpViewModel> implements NeedHelpNavigator, View.OnClickListener {

    RelativeLayout btnTipsAndTricks;
    RelativeLayout btnFAQ;
    NeedHelpViewModel needHelpViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        needHelpViewModel = ViewModelProviders.of(this).get(NeedHelpViewModel.class);
        needHelpViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showBackNavigationIcon(true);
        setToolbarBackgroundDrawable(R.drawable.appbar_background);
        setTitle(getString(R.string.need_help));
        showToolbar();
        getViewDataBinding().relFAQ.setOnClickListener(this);
        getViewDataBinding().tipsAndTricks.setOnClickListener(this);
    }

    @Override
    public NeedHelpViewModel getViewModel() {
        return needHelpViewModel;
    }


    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.need_help_fragment;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.relFAQ:
                pushFragment(new FAQFragment());
                break;
            case R.id.tipsAndTricks:
                pushFragment(new TipsAndTricksFragment());
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
            setTitle(getString(R.string.need_help));
            showToolbar();
        }
    }
}
