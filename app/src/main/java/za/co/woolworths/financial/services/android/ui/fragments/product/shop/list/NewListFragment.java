package za.co.woolworths.financial.services.android.ui.fragments.product.shop.list;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.NewListFragmentBinding;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddList;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.ShoppingListFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListItemsFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NewListFragment extends BaseFragment<NewListFragmentBinding, NewListViewModel> implements NewListNavigator, View.OnClickListener {

	private NewListViewModel newListFragment;
	private PostAddList mPostAddList;
	private WLoanEditTextView etNewList;
	private int CREATE_LIST_SUCCESS_RESULT = 53921;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Activity activity = getActivity();
		if (activity == null) return;
		/*****
		 * @Params:
		 * WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
		 * | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
		 * helps to push all content up
		 */
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
		getBottomNavigator().hideBottomNavigationMenu();
		displayVirtualKeyboard(true);
		showToolbar(R.string.new_list);
		setUpEditText();
		getViewDataBinding().btnCreateList.setOnClickListener(this);
		enableCreateList(false);
	}

	private void setUpEditText() {
		etNewList = getViewDataBinding().etNewList;
		etNewList.setOnKeyPreImeListener(onKeyPreImeListener);
		etNewList.setOnEditorActionListener(onEditorActionListener);
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
				messageLabelErrorDisplay(false);
			}
		});
	}

	private void enableCreateList(boolean createListEnabled) {
		WButton btnCreateList = getViewDataBinding().btnCreateList;
		if (createListEnabled || btnCreateList.isEnabled())
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

				if (getViewDataBinding().btnCreateList.isEnabled()) {
					postAddList();
				} else {
					onBackPressed();
				}

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
	public void onShoppingListSuccessResponse(ShoppingListsResponse shoppingListsResponse) {
		Activity activity = getActivity();
		if (activity == null) return;
		popFragmentNoAnim();
		Bundle bundle = new Bundle();
		bundle.putString("listId", shoppingListsResponse.lists.get(0).listId);
		bundle.putString("listName", shoppingListsResponse.lists.get(0).listName);
		ShoppingListItemsFragment shoppingListFragment = new ShoppingListItemsFragment();
		shoppingListFragment.setArguments(bundle);
		pushFragmentNoAnim(shoppingListFragment);
	}

	@Override
	public void onShoppingListFailureResponse(Response response) {
		Activity activity = getActivity();
		if (activity == null) return;
		if (response.code == null) return;
		if (response.desc == null) return;
		if (response.code.equalsIgnoreCase("0654")) {
			messageLabelErrorDisplay(true, response.desc);
		} else {
			displayVirtualKeyboard(false);
			Utils.displayDialog(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc, 1001);
		}
		loadView(false);
	}

	private void postAddList() {
		loadView(true);
		mPostAddList = getViewModel().postCreateList(new CreateList(etNewList.getText().toString().trim(), null));
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
		getBottomNavigator().showBottomNavigationMenu();
		displayVirtualKeyboard(false);
		cancelRequest(mPostAddList);
	}

	private void messageLabelErrorDisplay(boolean isVisible, String message) {
		getViewDataBinding().tvOnErrorLabel.setText(message);
		messageLabelErrorDisplay(isVisible);
	}

	private void messageLabelErrorDisplay(boolean isVisible) {
		getViewDataBinding().tvOnErrorLabel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (etNewList != null) {
			displayVirtualKeyboard(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Do something that differs the Activity's menu here
		super.onCreateOptionsMenu(menu, inflater);
	}

	/**
	 * @param showKeyboard show or hide soft keyboard
	 */
	private void displayVirtualKeyboard(final boolean showKeyboard) {
		final Activity activity = getActivity();
		if (activity == null) return;
		if (showKeyboard)
			showKeyboard(activity);
		else
			hideKeyboard(activity);

	}

	public void hideKeyboard(Activity activity) {
		View view = activity.findViewById(android.R.id.content);
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public void showKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
}