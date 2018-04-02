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

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateList;
import za.co.woolworths.financial.services.android.models.dto.OtherSkus;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse;
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddList;
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToList;
import za.co.woolworths.financial.services.android.models.service.event.ProductState;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WLoanEditTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;

public class CreateListFragment extends Fragment implements View.OnClickListener {

	private ImageView mImBack, imCloseIcon;
	private String hideBackButton;
	private WLoanEditTextView mEtNewList;
	private ProgressBar pbCreateList;
	private WButton mBtnCancel;
	private PostAddToList mAddToList;
	private KeyboardUtil mKeyboardUtils;
	private String addToListItems;
	private List<AddToListRequest> addToListRequests;
	private ErrorHandlerView mErrorHandlerView;
	private CreateList mCreateList;
	private PostAddList mPostCreateList;
	private PostAddToList mPostAddToList;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			hideBackButton = bundle.getString("OPEN_FROM_POPUP");
			addToListItems = bundle.getString("ADD_TO_LIST_ITEMS");
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
			addToListRequests = TextUtils.isEmpty(addToListItems)
					? new ArrayList<AddToListRequest>()
					: Utils.toList(addToListItems, AddToListRequest.class);
			initUI(view);
			displayKeyboard(view, activity);
			mErrorHandlerView = new ErrorHandlerView(activity);
			KeyboardUtil.showKeyboard(activity);
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
	}

	private void displayKeyboard(View view, Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			mKeyboardUtils = new KeyboardUtil(activity, view.findViewById(R.id.rlRootList), 0);
		}
	}

	private void setUpEditText(final WLoanEditTextView etNewList) {
		etNewList.requestFocus();
		etNewList.setOnKeyPreImeListener(onKeyPreImeListener);
		etNewList.setOnEditorActionListener(onEditorActionListener);
		showSoftKeyboard(etNewList);
		textChangeListener(etNewList);
	}

	private void textChangeListener(final WLoanEditTextView etNewList) {
		etNewList.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				mBtnCancel.setText(etNewList.getText().length() > 0 ? getString(R.string.ok) : getString(R.string.cancel));
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
	}

	private WLoanEditTextView.OnKeyPreImeListener onKeyPreImeListener = new WLoanEditTextView.OnKeyPreImeListener() {
		@Override
		public void onBackPressed() {
			CreateListFragment.this.onBackPressed();
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
		if (activity != null) {
			cancelRequest(activity);
		}
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
					mCreateList = new CreateList(listName);
					executeCreateList();
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

	private void executeCreateList() {
		mPostCreateList = postCreateList(mCreateList);
		mPostCreateList.execute();
	}

	private void cancelRequest(Activity activity) {
		if (activity != null) {
			CustomPopUpWindow customPopUpWindow = (CustomPopUpWindow) activity;
			customPopUpWindow.onBackPressed();
		}
	}

	public PostAddList postCreateList(final CreateList listName) {
		onLoad(true);
		return new PostAddList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				Activity activity = getActivity();
				if (activity != null) {
					ShoppingListsResponse createListResponse = (ShoppingListsResponse) object;
					switch (createListResponse.httpCode) {
						case 200:
							List<ShoppingList> itemsInList = createListResponse.lists;
							if (itemsInList != null) {
								//TODO::REMOVE COMMENT AND HARDCODED LIST_ID
								//ShoppingList shoppingList = itemsInList.get(0);
								//String listId = shoppingList.listId;
								String listId = "287241380";

								AddToListRequest addToList = new AddToListRequest();
								WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
								if (woolworthsApplication != null) {
									WGlobalState globalState = woolworthsApplication.getWGlobalState();
									OtherSkus sku = globalState.getSelectedSKUId();
									if (sku != null) {
										addToList.setSkuID(sku.sku);
										addToList.setCatalogRefId(sku.sku);
										addToList.setQuantity("1");
										addToList.setGiftListId(sku.sku);
										addToListRequests.add(0, addToList);
										executeAddToList(listId, addToListRequests);
									}
								}
							}
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
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onLoad(false);
						}
					});
				}
			}
		}, listName);
	}

	private void executeAddToList(String listId, List<AddToListRequest> addToListRequests) {
		mAddToList = addToList(addToListRequests, listId);
		mAddToList.execute();
	}

	private void onLoad(boolean visible) {
		pbCreateList.setVisibility(visible ? View.VISIBLE : View.GONE);
		mBtnCancel.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	public PostAddToList addToList(final List<AddToListRequest> addToListRequest, String listId) {
		final int sizeOfList = addToListRequest.size();
		onLoad(true);
		return new PostAddToList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListItemsResponse addToListResponse = (ShoppingListItemsResponse) object;
				Activity activity = getActivity();
				if (activity != null) {
					switch (addToListResponse.httpCode) {
						case 200:
							((CustomPopUpWindow) activity).startExitAnimation();
							mKeyboardUtils.hideKeyboard(activity);
							KeyboardUtil.hideSoftKeyboard(activity);
							Utils.sendBus(new ProductState(CLOSE_PDP_FROM_ADD_TO_LIST));
							onLoad(false);
							break;
						default:
							Response response = addToListResponse.response;
							if (response.desc != null) {
								Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc, true);
							}
							onLoad(false);
							break;
					}
				}
			}

			@Override
			public void onFailure(String e) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mErrorHandlerView.showToast();
							onLoad(false);
						}
					});
				}
			}
		}, addToListRequest, listId);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mEtNewList != null)
			mEtNewList.performClick();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		cancelRequest(mPostCreateList);
		cancelRequest(mAddToList);
		cancelRequest(mPostAddToList);

	}

	private void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}
}