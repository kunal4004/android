package za.co.woolworths.financial.services.android.ui.views.dialog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.KeyboardUtil;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

import static android.app.Activity.RESULT_OK;
import static za.co.woolworths.financial.services.android.models.service.event.ProductState.CLOSE_PDP_FROM_ADD_TO_LIST;

public class CreateListFragment extends Fragment implements View.OnClickListener, NetworkChangeListener {

	private String hideBackButton;
	private WLoanEditTextView mEtNewList;
	private ProgressBar pbCreateList;
	private WButton mBtnCancel;
	private KeyboardUtil mKeyboardUtils;
	private String addToListItems;
	private List<AddToListRequest> addToListRequests;
	private CreateList mCreateList;
	private PostAddList mPostCreateList;
	private WTextView mTvOnErrorLabel;
	private boolean addToListHasFail = false;
	private int apiCount = 0;
	private ErrorHandlerView mErrorHandlerView;
	private PostAddToList mPostAddToList;
	private BroadcastReceiver mConnectionBroadcast;
	private boolean createListFailed;
	private AddToListRequest mAddToListRequest;
	private List<AddToListRequest> mListRequests;
	private Map<String, List<AddToListRequest>> mMapAddedToList;

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
		return inflater.inflate(R.layout.create_new_list_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Activity activity = getActivity();
		if (activity != null) {
			mConnectionBroadcast = Utils.connectionBroadCast(activity, this);
			addToListRequests = new ArrayList<>();
			initUI(view);
			keyboardState(view, activity);
		}
	}

	private void keyboardState(View view, Activity activity) {
		displayKeyboard(view, activity);
	}

	private void initUI(View view) {
		mBtnCancel = view.findViewById(R.id.btnCancel);
		ImageView mImBack = view.findViewById(R.id.imBack);
		ImageView imCloseIcon = view.findViewById(R.id.imCloseIcon);
		mTvOnErrorLabel = view.findViewById(R.id.tvOnErrorLabel);
		pbCreateList = view.findViewById(R.id.pbCreateList);
		mErrorHandlerView = new ErrorHandlerView(getActivity());
		mImBack.setVisibility(TextUtils.isEmpty(hideBackButton) ? View.VISIBLE : View.GONE);
		imCloseIcon.setVisibility(TextUtils.isEmpty(hideBackButton) ? View.GONE : View.VISIBLE);
		mEtNewList = view.findViewById(R.id.etNewList);
		setUpEditText(mEtNewList);
		mBtnCancel.setOnClickListener(this);
		mImBack.setOnClickListener(this);
		imCloseIcon.setOnClickListener(this);
	}

	private void displayKeyboard(View view, Activity activity) {
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		mKeyboardUtils = new KeyboardUtil(activity, view.findViewById(R.id.rlRootList), 0);
		mKeyboardUtils.enable();
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
				Activity activity = getActivity();
				if (activity != null) {
					mBtnCancel.setText(etNewList.getText().toString().trim().length() > 0 ? getString(R.string.ok) : getString(R.string.cancel));
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				messageLabelErrorDisplay(false);
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
			assert imm != null;
			imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	private void onBackPressed() {
		Activity activity = getActivity();
		if (activity != null) {
			hideKeyboard(activity);
			cancelRequest(activity);
		}
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
				String cancelText = mBtnCancel.getText().toString();
				if (cancelText.equalsIgnoreCase("ok")) {
					String listName = mEtNewList.getText().toString();
					mCreateList = new CreateList(listName, getItems());
					executeCreateList();
				} else {
					onBackPressed();
				}
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
					String listName = mEtNewList.getText().toString().trim();
					mCreateList = new CreateList(listName, getItems());
					if ((NetworkManager.getInstance().isConnectedToNetwork(activity))) {
						setCreateListFailed(false);
						executeCreateList();
					} else {
						setCreateListFailed(true);
						mErrorHandlerView.showToast();
					}
				} else {
					cancelRequest(activity);
				}
				break;
			case R.id.imCloseIcon:
				if (activity != null) {
					hideKeyboard(activity);
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

	private List<AddToListRequest> getItems() {
		AddToListRequest addToList = new AddToListRequest();
		addToListRequests = new ArrayList<>();
		List<AddToListRequest> addToListRequestList = Utils.toList(addToListItems);
		WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
		if (woolworthsApplication != null) {
			WGlobalState globalState = woolworthsApplication.getWGlobalState();
			OtherSkus sku = globalState.getSelectedSKUId();
			if (sku != null) {
				addToList.setSkuID(sku.sku);
				addToList.setCatalogRefId(sku.sku);
				addToList.setQuantity("1");
				addToList.setListId("0");
				addToList.setGiftListId(sku.sku);
				addToListRequests.add(addToList);
			}
		}
		addToListRequests = addToListRequestList;
		mMapAddedToList = groupListByListId();
		mListRequests = mMapAddedToList.get(getCurrentListId());
		return mListRequests;
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
							addToListRequests = new ArrayList<>();
							addToListRequests = Utils.toList(addToListItems);
							WoolworthsApplication woolworthsApplication = WoolworthsApplication.getInstance();
							WGlobalState wGlobalState = woolworthsApplication.getWGlobalState();
							if (wGlobalState.getSelectedSKUId() != null) {
								for (AddToListRequest addToListRequest : addToListRequests) {
									if (addToListRequest.getListId().equalsIgnoreCase(wGlobalState.getSelectedSKUId().sku)) {
										addToListRequest.setListId("0");
									}
								}
							}
							mMapAddedToList = groupListByListId();
							if (addToListRequests != null
									&& addToListRequests.size() > 0
									&& !getCurrentListId().equalsIgnoreCase("0")) {
								mListRequests = mMapAddedToList.get(getCurrentListId());
								mMapAddedToList = groupListByListId();
								postAddToList(mListRequests, getCurrentListId());
							} else {
								if (woolworthsApplication != null) {
									if (wGlobalState != null) {
										List<ShoppingList> shoppingLists = createListResponse.lists;
										shoppingLists.get(0).shoppingListRowWasSelected = true;
										wGlobalState.setShoppingListRequest(shoppingLists);
									}
								}
								((CustomPopUpWindow) activity).startExitAnimation();
								mKeyboardUtils.hideKeyboard(activity);
								KeyboardUtil.hideSoftKeyboard(activity);
								Utils.sendBus(new ProductState(1, CLOSE_PDP_FROM_ADD_TO_LIST));
								onLoad(false);
							}
							break;

						case 440:
							((CustomPopUpWindow) activity).startExitAnimationForAddToListResult();
							getActivity().setResult(RESULT_OK, new Intent().putExtra("sessionExpired", true));
						case 400:
							//TODO:: HANDLE SESSION TIMEOUT
							break;
						default:
							Response response = createListResponse.response;
							if (response.desc != null) {
								if (response.code.equalsIgnoreCase("0654")) {
									messageLabelErrorDisplay(true, response.desc);
								} else {
									Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
									CreateListFragment.this.getActivity().finish();
								}
							}
							onLoad(false);
							break;
					}
					setAddToListHasFail(false);
				}
			}

			@Override
			public void onFailure(String e) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setAddToListHasFail(true);
							mErrorHandlerView.showToast();
							onLoad(false);
						}
					});
				}
			}
		}, listName);
	}

	private void onLoad(boolean visible) {
		pbCreateList.setVisibility(visible ? View.VISIBLE : View.GONE);
		mBtnCancel.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver();
		if (mEtNewList != null)
			mEtNewList.performClick();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		cancelRequest(mPostCreateList);
		cancelRequest(mPostAddToList);
	}

	private void hideKeyboard(Activity activity) {
		if (mKeyboardUtils != null)
			mKeyboardUtils.hideKeyboard(activity);
	}

	private void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}

	private void messageLabelErrorDisplay(boolean isVisible, String message) {
		mTvOnErrorLabel.setText(message);
		messageLabelErrorDisplay(isVisible);
	}

	private void messageLabelErrorDisplay(boolean isVisible) {
		mTvOnErrorLabel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	private void setAddToListHasFail(boolean value) {
		addToListHasFail = value;
	}

	private void postAddToList(final List<AddToListRequest> addToListRequest, String listId) {
		mPostAddToList = addToList(addToListRequest, listId);
		mPostAddToList.execute();
	}

	public PostAddToList addToList(final List<AddToListRequest> addToListRequest, String listId) {
		mMapAddedToList = groupListByListId();
		final int sizeOfList = mMapAddedToList.size();
		onLoad(true);
		return new PostAddToList(new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				ShoppingListItemsResponse addToListResponse = (ShoppingListItemsResponse) object;
				Activity activity = getActivity();
				if (activity != null) {
					switch (addToListResponse.httpCode) {
						case 200:
							apiCount += 1;
							if (apiCount < sizeOfList) {
								mListRequests = mMapAddedToList.get(getCurrentListId());
								PostAddToList postAddToList = addToList(mListRequests, getCurrentListId());
								postAddToList.execute();
							} else {
								((CustomPopUpWindow) activity).startExitAnimation();
								mKeyboardUtils.hideKeyboard(activity);
								KeyboardUtil.hideSoftKeyboard(activity);
								Utils.sendBus(new ProductState(sizeOfList + 1, CLOSE_PDP_FROM_ADD_TO_LIST));
								onLoad(false);
							}
							break;
						default:
							Response response = addToListResponse.response;
							if (response.desc != null) {
								Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc, true);
							}
							onLoad(false);
							break;
					}
					setAddToListHasFail(false);
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
							setAddToListHasFail(true);
						}
					});
				}
			}
		}, addToListRequest, listId);
	}

	private Map<String, List<AddToListRequest>> groupListByListId() {
		mMapAddedToList = new HashMap<>();
		for (AddToListRequest student : addToListRequests) {
			String key = student.getListId();
			if (mMapAddedToList.containsKey(key)) {
				List<AddToListRequest> list = mMapAddedToList.get(key);
				list.add(student);
			} else {
				List<AddToListRequest> list = new ArrayList<>();
				list.add(student);
				mMapAddedToList.put(key, list);
			}
		}

		return mMapAddedToList;
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	private void unregisterReceiver() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.unregisterReceiver(mConnectionBroadcast);
		}
	}

	private void registerReceiver() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.registerReceiver(mConnectionBroadcast,
					new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		}
	}

	@Override
	public void onConnectionChanged() {
		if (getCreateListFailed()) {
			executeCreateList();
			setCreateListFailed(false);
		}

		if (addToListHasFail) {
			addToListRequests = Utils.toList(addToListItems);
			if (addToListRequests != null && addToListRequests.size() > 0) {
				mAddToListRequest = addToListRequests.get(apiCount);
				mListRequests = new ArrayList<>();
				mListRequests.add(mAddToListRequest);
				postAddToList(mListRequests, mAddToListRequest.getGiftListId());
				setAddToListHasFail(false);
			}
		}
	}

	public void setCreateListFailed(boolean createListFailed) {
		this.createListFailed = createListFailed;
	}

	public boolean getCreateListFailed() {
		return createListFailed;
	}

	private String getCurrentListId() {
		Object[] keys = mMapAddedToList.keySet().toArray();
		return getCurrentListId(keys);
	}

	private String getCurrentListId(Object[] keys) {
		return String.valueOf(keys[apiCount]);
	}
}
