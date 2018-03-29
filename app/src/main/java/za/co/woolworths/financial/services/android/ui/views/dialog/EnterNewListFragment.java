package za.co.woolworths.financial.services.android.ui.views.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.CreateListResponse;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToList;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

public class EnterNewListFragment extends Fragment implements View.OnClickListener {
	private WButton mBtnCancel;
	private ImageView mImBack, imCloseIcon;
	private String hideBackButton;
	private WLoanEditTextView mEtNewList;
	private ProgressBar pbCreateList;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			hideBackButton = bundle.getString("OPEN_FROM_POPUP");
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.create_new_list_layout, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Activity activity = getActivity();
		if (activity != null) {
			initUI(view);
			displayKeyboard(view, activity);
		}
	}

	private void initUI(View view) {
		mBtnCancel = view.findViewById(R.id.btnCancel);
		mImBack = view.findViewById(R.id.imBack);
		imCloseIcon = view.findViewById(R.id.imCloseIcon);
		pbCreateList = view.findViewById(R.id.pbCreateList);
		mImBack.setVisibility(TextUtils.isEmpty(hideBackButton) ? View.VISIBLE : View.GONE);
		imCloseIcon.setVisibility(TextUtils.isEmpty(hideBackButton) ? View.GONE : View.VISIBLE);
		mEtNewList = view.findViewById(R.id.etNewList);
		setUpEditText(mEtNewList);
		mBtnCancel.setOnClickListener(this);
		mImBack.setOnClickListener(this);
		imCloseIcon.setOnClickListener(this);
		enableCreateList(false);
	}

	private void displayKeyboard(View view, Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			new KeyboardUtil(activity, view.findViewById(R.id.rlRootList), 0);
		}
	}

	private void setUpEditText(WLoanEditTextView etNewList) {
		etNewList.setOnKeyPreImeListener(onKeyPreImeListener);
		etNewList.setOnEditorActionListener(onEditorActionListener);
		etNewList.requestFocus();
		showSoftKeyboard(etNewList);
		addTextChangedListener(etNewList);
	}

	private WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener = new WLoanEditTextView.OnKeyPreImeListener() {
		@Override
		public void onBackPressed() {
			EnterNewListFragment.this.onBackPressed();
		}
	};

	public void showSoftKeyboard(WLoanEditTextView editTextView) {
		Activity activity = getActivity();
		if (activity != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	private void onBackPressed() {
		Activity activity = getActivity();
		cancelRequest(activity);
	}

	private void addTextChangedListener(final WLoanEditTextView etNewList) {
		etNewList.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.length() > 0) {
					etNewList.setText(getString(R.string.ok));
				} else {
					etNewList.setText(getString(R.string.cancel));
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
	}

	private void enableCreateList(boolean enable) {
		mBtnCancel.setAlpha(enable ? (float) 1.0 : (float) 0.4);
		mBtnCancel.setEnabled(enable);
	}

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

	@Override
	public void onClick(View view) {
		MultiClickPreventer.preventMultiClick(view);
		Activity activity = getActivity();
		switch (view.getId()) {
			case R.id.btnCancel:
				String strCancel = mBtnCancel.getText().toString();
				if (strCancel.equalsIgnoreCase("ok")) {
					String listName = mEtNewList.getText().toString();
					CreateList createList = new CreateList(listName);
					//postCreateList(createList).execute();
				} else {
					cancelRequest(activity);
				}
				break;
			case R.id.imCloseIcon:
				if (activity != null) {
					CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
					customPopUpWindow.finish();
					customPopUpWindow.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
				}
				break;
			case R.id.imBack:
				cancelRequest(activity);
				break;
			default:
				break;
		}
	}

	private void cancelRequest(Activity activity) {
		if (activity != null) {
			CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
			customPopUpWindow.onBackPressed();
		}
	}

	public PostAddList postCreateList(CreateList listName) {
		onLoad(true);
		return new PostAddList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				Activity activity = getActivity();
				if (activity != null) {
					CreateListResponse createListResponse = (CreateListResponse) object;
					switch (createListResponse.httpCode) {
						case 200:

							onLoad(false);
							break;

						default:
							Response response = createListResponse.response;
							if (response != null) {
								Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
							}
							onLoad(false);
							break;
					}
				}
			}

			@Override
			public void onFailure(String e) {


			}
		}, listName);
	}

	private void onLoad(boolean visible) {
		pbCreateList.setVisibility(visible ? View.VISIBLE : View.GONE);
		mBtnCancel.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	public PostAddToList addToList(final List<AddToListRequest> addToListRequest, String listId) {
		final int sizeOfList = addToListRequest.size();
		return new PostAddToList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				AddToListResponse addToListResponse = (AddToListResponse) object;
				Activity activity = getActivity();
				if (activity != null) {
					switch (addToListResponse.httpCode) {
						case 200:
							Toast.makeText(activity, "Added to ShoppingList", Toast.LENGTH_SHORT).show();
							activity.finish();
							activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
							onLoad(false);
						default:
							Response response = addToListResponse.response;
							if (response != null) {
								Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
							}
							onLoad(false);
							break;
					}
				}
			}

			@Override
			public void onFailure(String e) {
				onLoad(false);
			}
		}, addToListRequest, listId);
	}
}
