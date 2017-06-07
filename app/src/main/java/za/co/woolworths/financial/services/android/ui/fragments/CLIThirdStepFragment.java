package za.co.woolworths.financial.services.android.ui.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.BankAccountResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.IncomeProof;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CLIBankAccountTypeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.CLIIncomeProofAdapter;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WEmpyViewDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.CLIBankAccountTypeBinder;

public class CLIThirdStepFragment extends Fragment implements View.OnClickListener,
		CLIBankAccountTypeBinder.OnCheckboxClickListener, CLIStepIndicatorActivity
				.OnFragmentRefresh, NetworkChangeListener {

	private CLIFirstStepFragment.StepNavigatorCallback stepNavigatorCallback;

	private View view;
	private WTextView mTextCreditLimit;
	private RecyclerView mRecycleList;
	private WButton mBtnContinue;
	private ImageView mImgInfo;
	private List<BankAccountType> mBankAccountType;
	private LinearLayoutManager mLayoutManager;
	private CLIThirdStepFragment mContext;
	private CLIBankAccountTypeAdapter mCLIBankAccountAdapter;
	private UpdateBankDetail mUpdateBankDetail;
	private WoolworthsApplication mWoolworthsApplication;
	private WEditTextView mEditAccountNumber;
	protected WTextView mTextIncomeProof;
	private LinearLayout mLinProofLayout;
	private LinearLayout mLinBankLayout;
	public WButton mBtnSendMail;
	private String mEmail = "";
	private FragmentManager fm;
	private WEmpyViewDialogFragment mEmailEmpyViewDialogFragment;
	private ProgressBar mEmailProgressBar;
	private ProgressBar mProgressBar;
	private WEmpyViewDialogFragment mEmpyViewDialogFragment;
	private ErrorHandlerView mErrorHandlerView;
	private String retryBackgroundTask;
	private final String SEND_EMAIL = "sendEmail";
	private final String UPDATE_BANK_DETAIL = "updateBankDetailClicked";
	private final String GET_BANK_ACCOUNT_TYPES = "getBankAccountTypes";
	private RelativeLayout mRelConnectionLayout;
	private NetworkChangeListener networkChangeListener;
	private BroadcastReceiver connectionBroadcast;
	private boolean sendEmailButtonClicked = true;
	private boolean updateBankDetailClicked = false;
	private boolean getBankAccountClicked = true;


	public CLIThirdStepFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = this;
		return inflater.inflate(R.layout.cli_fragment_step_three, container, false);

	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.view = view;
		try {
			networkChangeListener = (NetworkChangeListener) this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(getActivity(), networkChangeListener);
		mWoolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
		SharePreferenceHelper mSharePreferenceHelper = SharePreferenceHelper.getInstance(getActivity());
		setRetainInstance(true);
		fm = getActivity().getSupportFragmentManager();
		mEmail = mSharePreferenceHelper.getValue("email");
		CLIStepIndicatorActivity mStepIndicator = (CLIStepIndicatorActivity) getActivity();
		mStepIndicator.setOnFragmentRefresh(this);
		mLinProofLayout = (LinearLayout) view.findViewById(R.id.linProofLayout);
		mLinBankLayout = (LinearLayout) view.findViewById(R.id.linBankLayout);
		mProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
		mEmailProgressBar = (ProgressBar) view.findViewById(R.id.mEmailWoolworthsProgressBar);
		mRelConnectionLayout = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), mRelConnectionLayout);
		mErrorHandlerView.setMargin(mRelConnectionLayout, 0, 0, 0, 0);
		initUI();
		setListener();
		setContent();
		getBankAccountTypes();

		initProofUI();
		setProofListener();
		populateList();
		setProofContent();

		loadView();
	}

	public void loadView() {
		if (mWoolworthsApplication.isDEABank()) {
			mLinBankLayout.setVisibility(View.VISIBLE);
			mLinProofLayout.setVisibility(View.GONE);
		} else {
			mLinBankLayout.setVisibility(View.GONE);
			mLinProofLayout.setVisibility(View.VISIBLE);
		}
	}

	private void initUI() {
		mRecycleList = (RecyclerView) view.findViewById(R.id.recycleList);
		mTextCreditLimit = (WTextView) view.findViewById(R.id.textACreditLimit);
		mBtnContinue = (WButton) view.findViewById(R.id.btnContinue);
		mImgInfo = (ImageView) view.findViewById(R.id.imgInfo);
		mEditAccountNumber = (WEditTextView) view.findViewById(R.id.wEditAccountNumber);
	}

	private void setListener() {
		mBtnContinue.setOnClickListener(this);
	}

	private void setContent() {
		mTextCreditLimit.setText(getString(R.string.cli_account_type));
		mBtnContinue.setText(getString(R.string.cli_complete_process));
		mImgInfo.setVisibility(View.GONE);

	}

	public HttpAsyncTask<String, String, BankAccountTypes> bankAccountTypeAsyncApI() {
		return new HttpAsyncTask<String, String, BankAccountTypes>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				getBankAccountClicked = true;
			}

			@Override
			protected BankAccountTypes httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getBankAccountTypes();
			}

			@Override
			protected Class<BankAccountTypes> httpDoInBackgroundReturnType() {
				return BankAccountTypes.class;
			}

			@Override
			protected BankAccountTypes httpError(String errorMessage, HttpAsyncTask.HttpErrorCode httpErrorCode) {
				BankAccountTypes bankAccountTypes = new BankAccountTypes();
				bankAccountTypes.response = new BankAccountResponse();
				networkFailureHandler();
				getBankAccountClicked = true;
				return bankAccountTypes;
			}

			@Override
			protected void onPostExecute(BankAccountTypes bankAccountTypes) {
				super.onPostExecute(bankAccountTypes);
				try {
					if (bankAccountTypes.bankAccountTypes != null) {
						if (bankAccountTypes.httpCode == 200) {
							mBankAccountType = bankAccountTypes.bankAccountTypes;
							mCLIBankAccountAdapter = new CLIBankAccountTypeAdapter(mBankAccountType, mContext);
							mLayoutManager = new LinearLayoutManager(getActivity());
							mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
							mRecycleList.setLayoutManager(mLayoutManager);
							mRecycleList.setNestedScrollingEnabled(false);
							mRecycleList.setAdapter(mCLIBankAccountAdapter);
							mCLIBankAccountAdapter.setCLIContent();
						} else {
							if (bankAccountTypes.response.desc != null &&
									!TextUtils.isEmpty(bankAccountTypes.response.desc)) {
								Utils.displayValidationMessage(getActivity(),
										TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
										bankAccountTypes.response.desc);
							}

						}
					}
				} catch (Exception ex) {

				}
				getBankAccountClicked = false;
			}
		};
	}

	//Method to hide keyboard
	public static void hideKeyboard(Context context) {
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		// check if no view has focus:
		View v = ((Activity) context).getCurrentFocus();
		if (v == null)
			return;

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnContinue:
				if (mUpdateBankDetail != null) {
					if (mUpdateBankDetail.getAccountType() != null) {
						String accountNumber = mEditAccountNumber.getText().toString();
						if (!TextUtils.isEmpty(accountNumber)) {
							String newAccount = accountNumber.replaceAll(" ", "");
							mUpdateBankDetail.setAccountNumber(newAccount);
							updateBankDetail();
							//If everything is ok then hide the keyboard
							hideKeyboard(getContext());
						} else {
							Utils.displayValidationMessage(getActivity(),
									TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
									getString(R.string.cli_enter_acc_number_error));
						}
					} else {
						Utils.displayValidationMessage(getActivity(),
								TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
								getString(R.string.cli_select_acc_type));
					}
				} else {
					Utils.displayValidationMessage(getActivity(),
							TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
							getString(R.string.cli_select_acc_type));
				}
				break;

			case R.id.btnSendMail:
				sendEmail();
				break;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onCheckboxViewClick(View v, int position) {
		if (mBankAccountType != null) {
			mUpdateBankDetail = mWoolworthsApplication.updateBankDetail;
			if (mUpdateBankDetail != null) {
				mUpdateBankDetail.setAccountType(mBankAccountType.get(position).accountType);
			}
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		stepNavigatorCallback = (CLIFirstStepFragment.StepNavigatorCallback) getActivity();
	}

	public List<IncomeProof> arrIncomeProof() {
		List<IncomeProof> arrIncomeProof = new ArrayList<>();
		String[] mOptionTitle = getResources().getStringArray(R.array.cli_option);
		String[] mOptionDesc = getResources().getStringArray(R.array.cli_option_desc);
		int[] myImageList = new int[]{R.drawable.icon_paperclip, R.drawable.icon_fax};
		int index = 0;
		for (String option : mOptionTitle) {
			arrIncomeProof.add(new IncomeProof(option, mOptionDesc[index], myImageList[index]));
			index++;
		}
		return arrIncomeProof;
	}

	private void initProofUI() {
		mTextIncomeProof = (WTextView) view.findViewById(R.id.textProofIncome);
		mBtnSendMail = (WButton) view.findViewById(R.id.btnSendMail);
	}

	private void setProofListener() {
		mBtnSendMail.setOnClickListener(this);
	}

	private void setProofContent() {
		mTextIncomeProof.setText(getActivity().getResources().getString(R.string.cli_income_proof));
		mBtnSendMail.setText(getString(R.string.cli_send_mail));
	}

	public void populateList() {
		List<IncomeProof> mArrIncomeProof = arrIncomeProof();
		CLIIncomeProofAdapter mClIIncomeProofAdapter = new CLIIncomeProofAdapter(mArrIncomeProof);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mClIIncomeProofAdapter.setCLIContent();
	}

	@Override
	public void refreshFragment() {
		loadView();
	}

	public HttpAsyncTask<String, String, CLIEmailResponse> sendEmailAsyncAPI() {

		return new HttpAsyncTask<String, String, CLIEmailResponse>() {
			@Override
			protected CLIEmailResponse httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().cliEmailResponse();
			}

			@Override
			protected CLIEmailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				networkFailureHandler();
				sendEmailButtonClicked = true;
				return new CLIEmailResponse();
			}

			@Override
			protected Class<CLIEmailResponse> httpDoInBackgroundReturnType() {
				return CLIEmailResponse.class;
			}

			@Override
			protected void onPreExecute() {
				showEmailProgressBar();
				super.onPreExecute();
				sendEmailButtonClicked = true;
			}

			@Override
			protected void onPostExecute(CLIEmailResponse cliEmailResponse) {
				super.onPostExecute(cliEmailResponse);
				stopProgressDialog();
				try {
					int httpCode = cliEmailResponse.httpCode;
					String desc = cliEmailResponse.response.desc;
					if (httpCode == 200) {
						Utils.displayValidationMessage(getActivity(),
								TransientActivity.VALIDATION_MESSAGE_LIST.EMAIL, mEmail);
					} else {
						Utils.displayValidationMessage(getActivity(),
								TransientActivity.VALIDATION_MESSAGE_LIST.ERROR, desc);
					}
					sendEmailButtonClicked = false;
				} catch (NullPointerException ignored) {
				}
				stopEmailProgressDialog();
			}
		};
	}

	private HttpAsyncTask<String, String, UpdateBankDetailResponse> updateBankDetailAPI() {
		showProgressBar();
		return new HttpAsyncTask<String, String, UpdateBankDetailResponse>() {
			@Override
			protected UpdateBankDetailResponse httpDoInBackground(String... params) {
				return mWoolworthsApplication.getApi().cliUpdateBankDetail(mUpdateBankDetail);
			}

			@Override
			protected UpdateBankDetailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				networkFailureHandler();
				updateBankDetailClicked = true;
				return new UpdateBankDetailResponse();
			}

			@Override
			protected Class<UpdateBankDetailResponse> httpDoInBackgroundReturnType() {
				return UpdateBankDetailResponse.class;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				updateBankDetailClicked = true;
			}

			@Override
			protected void onPostExecute(UpdateBankDetailResponse updateBankDetailResponse) {
				super.onPostExecute(updateBankDetailResponse);
				try {
					if (updateBankDetailResponse != null) {
						if (updateBankDetailResponse.httpCode == 200) {
							stepNavigatorCallback.openNextFragment(3);
						} else {

							String desc = updateBankDetailResponse.response.desc;
							if (!TextUtils.isEmpty(desc)) {
								Utils.displayValidationMessage(getActivity(),
										TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
										desc);
							}
						}
					}
				} catch (Exception ignored) {
				}
				updateBankDetailClicked = true;
				stopProgressDialog();
			}
		};
	}

	public void showProgressBar() {
		mEmpyViewDialogFragment = WEmpyViewDialogFragment.newInstance("blank");
		mEmpyViewDialogFragment.setCancelable(false);
		mEmpyViewDialogFragment.show(fm, "blank");
		mProgressBar.bringToFront();
		mProgressBar.setVisibility(View.VISIBLE);
		mBtnContinue.setVisibility(View.GONE);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
	}

	public void stopProgressDialog() {
		if (mEmpyViewDialogFragment != null) {
			if (mEmpyViewDialogFragment.isVisible()) {
				mEmpyViewDialogFragment.dismiss();
			}
		}
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.GONE);
			mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		}
		mBtnContinue.setVisibility(View.VISIBLE);
	}

	public void showEmailProgressBar() {
		mEmailProgressBar.bringToFront();
		mEmailProgressBar.setVisibility(View.VISIBLE);
		mBtnSendMail.setVisibility(View.GONE);
		mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		mEmailEmpyViewDialogFragment = WEmpyViewDialogFragment.newInstance("blank");
		mEmailEmpyViewDialogFragment.setCancelable(false);
		mEmailEmpyViewDialogFragment.show(fm, "blank");
		mEmailProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

	}

	public void stopEmailProgressDialog() {
		if (mEmailEmpyViewDialogFragment != null) {
			if (mEmailEmpyViewDialogFragment.isVisible()) {
				mEmailEmpyViewDialogFragment.dismiss();
			}
		}
		if (mEmailProgressBar != null) {
			mEmailProgressBar.setVisibility(View.GONE);
			mEmailProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		}
		mBtnSendMail.setVisibility(View.VISIBLE);
	}


	private void networkFailureHandler() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch (retryBackgroundTask) {
					case SEND_EMAIL:
						stopEmailProgressDialog();
						break;

					case UPDATE_BANK_DETAIL:
						stopProgressDialog();
						break;

					case GET_BANK_ACCOUNT_TYPES:
						break;
				}
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(connectionBroadcast);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(connectionBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	private void sendEmail() {
		retryBackgroundTask = SEND_EMAIL;
		sendEmailAsyncAPI().execute();
	}

	private void updateBankDetail() {
		retryBackgroundTask = UPDATE_BANK_DETAIL;
		updateBankDetailAPI().execute();
	}

	private void getBankAccountTypes() {
		retryBackgroundTask = GET_BANK_ACCOUNT_TYPES;
		bankAccountTypeAsyncApI().execute();
	}

	@Override
	public void onConnectionChanged() {
		if (new ConnectionDetector().isOnline()) {
			if (new ConnectionDetector().isOnline()) {
				Log.e("BankAccountType", "retryBackgroundTask " + retryBackgroundTask);
				switch (retryBackgroundTask) {
					case SEND_EMAIL:
						Log.e("BankAccountType", "sendEmailButtonClicked " + sendEmailButtonClicked);
						if (sendEmailButtonClicked) {
							sendEmail();
						}
						break;

					case UPDATE_BANK_DETAIL:
						Log.e("BankAccountType", "UPDATE_BANK_DETAIL " + updateBankDetailClicked + bankAccountIsValid());
						if (updateBankDetailClicked && bankAccountIsValid())
							updateBankDetail();
						break;

					case GET_BANK_ACCOUNT_TYPES:
						Log.e("BankAccountType", "GET_BANK_ACCOUNT_TYPES " + getBankAccountClicked);
						if (getBankAccountClicked)
							getBankAccountTypes();
						break;
				}
			}
		} else {
			mErrorHandlerView.showToast();
		}
	}

	private boolean bankAccountIsValid() {
		if (mUpdateBankDetail.getAccountType() != null) {
			String accountNumber = mEditAccountNumber.getText().toString();
			if (!TextUtils.isEmpty(accountNumber)) {
				String newAccount = accountNumber.replaceAll(" ", "");
				mUpdateBankDetail.setAccountNumber(newAccount);
				return true;
				//If everything is ok then hide the keyboard
			}
		}
		return false;
	}
}