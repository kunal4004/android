package za.co.woolworths.financial.services.android.ui.fragments.contact_us.main_list;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.awfs.coordination.BR;
import com.awfs.coordination.databinding.FragmentContactUsBinding;


import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.ContactUsCustomerServiceFragment;
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.ContactUsFinancialServiceFragment;

public class ContactUsFragment extends BaseFragment<FragmentContactUsBinding, ContactUsViewModel> implements View.OnClickListener, ContactUsNavigator {

	public RelativeLayout fsLayout;
	public RelativeLayout csLayout;

	private ContactUsViewModel contactUsViewModel;


	public ContactUsFragment() {
		// Required empty public constructor
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contactUsViewModel = ViewModelProviders.of(this).get(ContactUsViewModel.class);
		contactUsViewModel.setNavigator(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setTitle(getString(R.string.contact_us));
		showBackNavigationIcon(true);
		setToolbarBackgroundDrawable(R.drawable.appbar_background);

		fsLayout = getViewDataBinding().financialService;
		csLayout = getViewDataBinding().customerService;

		fsLayout.setOnClickListener(this);
		csLayout.setOnClickListener(this);
	}

	@Override
	public ContactUsViewModel getViewModel() {
		return contactUsViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.fragment_contact_us;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.financialService:
				openFragment(new ContactUsFinancialServiceFragment());
				break;
			case R.id.customerService:
				openFragment(new ContactUsCustomerServiceFragment());
				break;
		}
	}

	public void openFragment(Fragment fragment) {
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
				.replace(R.id.frag_container, fragment).addToBackStack(null).commit();
	}


}
