package za.co.woolworths.financial.services.android.ui.fragments.product.shop.list;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.NewListFragmentBinding;

import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddList;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NewListFragment extends BaseFragment<NewListFragmentBinding, NewListViewModel> implements NewListNavigator, View.OnClickListener {

	private NewListViewModel newListFragment;
	private PostAddList mPostAddList;
	private KeyboardUtil mKeyboardUtil;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		newListFragment = ViewModelProviders.of(this).get(NewListViewModel.class);
		newListFragment.setNavigator(this);
	}

	@Override
	public NewListViewModel getViewModel() {
		return newListFragment;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.new_list_fragment;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Activity activity = getActivity();
		displayKeyboard(view, activity);
		showToolbar(R.string.new_list);
		setUpEditText();
		//showKeyboard(view.findViewById(R.id.rlRootList));
		getViewDataBinding().btnCreateList.setOnClickListener(this);
		enableCreateList(false);
	}

	private void displayKeyboard(View view, Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			mKeyboardUtil = new KeyboardUtil(activity, view.findViewById(R.id.rlRootList), getBottomNavigator().getBottomNavigationById().getHeight());
		}
	}

	private void setUpEditText() {
		WLoanEditTextView etNewList = getViewDataBinding().etNewList;
		etNewList.setOnKeyPreImeListener(onKeyPreImeListener);
		etNewList.setOnEditorActionListener(onEditorActionListener);
		showSoftKeyboard(etNewList);
		addTextChangedListener(etNewList);
	}

	private void addTextChangedListener(final WLoanEditTextView etNewList) {
		etNewList.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.toString().trim().length() > 0) {
					enableCreateList(true);
				} else {
					enableCreateList(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
	}

	private void enableCreateList(boolean createListEnabled) {
		WButton btnCreateList = getViewDataBinding().btnCreateList;
		if(createListEnabled ||btnCreateList.isEnabled())
			Utils.fadeView(getViewDataBinding().flBtnContainer, createListEnabled);
		btnCreateList.setEnabled(createListEnabled);
	}

	private WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener = new WLoanEditTextView.OnKeyPreImeListener() {
		@Override
		public void onBackPressed() {
			NewListFragment.this.onBackPressed();
		}
	};

	private WLoanEditTextView.OnEditorActionListener onEditorActionListener = new WLoanEditTextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// Identifier of the action. This will be either the identifier you supplied,
			// or EditorInfo.IME_NULL if being called due to the enter key being pressed.
			if (actionId == EditorInfo.IME_ACTION_SEARCH
					|| actionId == EditorInfo.IME_ACTION_DONE
					|| event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				onBackPressed();
				return true;
			}
			// Return true if you have consumed the action, else false.
			return false;
		}
	};


	private void onBackPressed() {
		Activity activity = getActivity();
		if (activity != null) {
			BottomNavigationActivity bottomNavigationActivity = (BottomNavigationActivity) activity;
			bottomNavigationActivity.onBackPressed();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			showToolbar(R.string.new_list);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnCreateList:
				postAddList();
				break;

			default:
				break;
		}
	}

	@Override
	public void onFailure(String e) {
		loadView(false);
	}

	public void loadView(boolean viewIsEnabled) {
		getViewDataBinding().pbCreateList.setVisibility(viewIsEnabled ? VISIBLE : GONE);
		getViewDataBinding().btnCreateList.setVisibility(viewIsEnabled ? GONE : VISIBLE);
	}

	@Override
	public void onSuccess() {
		popFragmentNoAnim();

	}

	private void postAddList() {
		WLoanEditTextView etNewList = getViewDataBinding().etNewList;
		loadView(true);
		mPostAddList = getViewModel().postCreateList(new CreateList(etNewList.getText().toString().trim(),null));
		mPostAddList.execute();
	}

	@Override
	public void onResume() {
		super.onResume();
		showToolbar(R.string.new_list);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		showSoftwareKeyboard(false);
		cancelRequest(mPostAddList);
	}
	protected void showSoftwareKeyboard(boolean showKeyboard){
		final Activity activity = getActivity();
		final InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getViewDataBinding().etNewList.getWindowToken(), showKeyboard ? InputMethodManager.SHOW_FORCED : InputMethodManager.HIDE_NOT_ALWAYS);
	}
}