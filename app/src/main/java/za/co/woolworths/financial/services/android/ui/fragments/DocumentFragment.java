package za.co.woolworths.financial.services.android.ui.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.models.rest.CLIGetBankAccountTypes;
import za.co.woolworths.financial.services.android.models.rest.CLIGetDeaBank;
import za.co.woolworths.financial.services.android.models.rest.CLISendEmailRequest;
import za.co.woolworths.financial.services.android.models.rest.CLIUpdateBankDetails;
import za.co.woolworths.financial.services.android.models.service.event.LoadState;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.adapters.DocumentAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.DocumentsAccountTypeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.POIDocumentSubmitTypeAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.FragmentUtils;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.MultiClickPreventer;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;


public class DocumentFragment extends CLIFragment implements DocumentAdapter.OnItemClick, NetworkChangeListener, DocumentsAccountTypeAdapter.OnAccountTypeClick, View.OnClickListener, POIDocumentSubmitTypeAdapter.OnSubmitType, TextWatcher {

	private final int ANIM_DURATION = 600;
	private RecyclerView rclSelectYourBank;
	private DeaBanks deaBankList;
	private ProgressBar pbDeaBank;
	private BroadcastReceiver connectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private List<BankAccountType> bankAccountTypesList;
	private RecyclerView rclAccountType;
	private LinearLayout bankTypeConfirmationLayout;
	private LinearLayout accountTypeLayout;
	private LinearLayout accountNumberLayout;
	private LinearLayout poiDocumentSubmitTypeLayout;
	private WButton btnSubmit;
	private NestedScrollView nestedScrollView;
	private WTextView yesPOIFromBank;
	private WTextView noPOIFromBank;
	private String otherBank = "Other";
	private DocumentsAccountTypeAdapter accountTypeAdapter;
	private POIDocumentSubmitTypeAdapter documentSubmitTypeAdapter;
	private WEditTextView etAccountNumber;
	public RelativeLayout rlSubmitCli;
	private LinearLayout uploadDocumentsLayout;
	public SubmitType submitType;
	private List<Bank> mDeaBankList;
	public String selectedBankType;
	public String selectedAccountType;
	public UpdateBankDetailResponse updateBankDetailResponse;
	private CLIGetBankAccountTypes cliGetBankAccountTypes;
	private CLIGetDeaBank cliGetDeaBank;
	private ProgressBar pbAccountType, pbSubmit;
	private CLIUpdateBankDetails cliUpdateBankDetails;
	private CLISendEmailRequest cliSendEmail;
	private WTextView tvCLIAccountTypeTitle, tvAccountSavingTitle;
	private LoadState loadState;
	private OfferActive activeOfferObj;
	private View view;

	private enum NetworkFailureRequest {DEA_BANK, ACCOUNT_TYPE}

	public enum SubmitType {
		ACCOUNT_NUMBER(1),
		DOCUMENTS(2),
		LATER(3);

		SubmitType(int type) {
			this.result = type;
		}

		private int result;

		public int getType() {
			return result;
		}
	}

	private NetworkFailureRequest networkFailureRequest;

	public String getSelectedBankType() {
		return selectedBankType;
	}

	public void setSelectedBankType(String selectedBankType) {
		this.selectedBankType = selectedBankType;
	}

	public String getSelectedAccountType() {
		return selectedAccountType;
	}

	public void setSelectedAccountType(String selectedAccountType) {
		this.selectedAccountType = selectedAccountType;
	}

	public DocumentFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		view = inflater.inflate(R.layout.document_fragment, container, false);
		deaBankList = new DeaBanks();
		mCliStepIndicatorListener.onStepSelected(4);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		connectionBroadcast();
		Bundle b = this.getArguments();
		if (b != null) {
			String offerActive = b.getString("OFFER_ACTIVE_PAYLOAD");
			if (!TextUtils.isEmpty(offerActive)) {
				activeOfferObj = new Gson().fromJson(offerActive, OfferActive.class);
			}
		}
		CLIPhase2Activity mCliPhase2Activity = (CLIPhase2Activity) getActivity();
		loadState = new LoadState();
		mCliPhase2Activity.actionBarCloseIcon();
		mCliPhase2Activity.hideDeclineOffer();
		init(view);
		onLoad(pbDeaBank);
		cliDeaBankRequest();
		loadPOIDocumentsSubmitTypeView();
	}

	private void connectionBroadcast() {
		connectionBroadcast = Utils.connectionBroadCast(getActivity(), this);
	}

	private void onLoad(ProgressBar pBar) {
		showView(pBar);
		progressColorFilter(pBar, R.color.black);
	}

	private void onLoadComplete(final View v) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				hideView(v);
			}
		});
	}

	private void cliDeaBankRequest() {
		onLoad(pbDeaBank);
		cliGetDeaBank = new CLIGetDeaBank(getActivity(), new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				deaBankList = ((DeaBanks) object);
				int httpCode = deaBankList.httpCode;
				switch (httpCode) {
					case 200:
						mDeaBankList = deaBankList.banks;
						if (mDeaBankList != null) {
							Random rand = new Random();
							int n = rand.nextInt(50) + 1;
							mDeaBankList.add(new Bank(n, otherBank, ""));
						}
						selectBankLayoutManager(mDeaBankList);
						break;
					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), deaBankList
								.response.stsParams);
						break;

					default:
						mErrorHandlerView.responseError(view, "");
						break;
				}
				loadSuccess();
				onLoadComplete(pbDeaBank);
			}

			@Override
			public void onFailure(String e) {
				loadFailure();
				setNetworkFailureRequest(NetworkFailureRequest.DEA_BANK);
				mErrorHandlerView.responseError(view, e);
				onLoadComplete(pbDeaBank);
			}
		});
		cliGetDeaBank.execute();
	}

	private void cliBankAccountTypeRequest() {
		onLoad(pbAccountType);
		if (accountTypeAdapter != null && bankAccountTypesList != null) {
			bankAccountTypesList.clear();
			accountTypeAdapter.notifyDataSetChanged();
		}

		cliGetBankAccountTypes = new CLIGetBankAccountTypes(getActivity(), new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				BankAccountTypes bankAccountTypes = (BankAccountTypes) object;
				switch (bankAccountTypes.httpCode) {
					case 200:
						bankAccountTypesList = bankAccountTypes.bankAccountTypes;
						loadBankAccountTypesView(bankAccountTypesList);
						break;
					case 440:
						SessionExpiredUtilities.INSTANCE.setAccountSessionExpired(getActivity(), bankAccountTypes
								.response.stsParams);
						break;
					default:
						mErrorHandlerView.responseError(view, "");
						break;
				}
				onLoadComplete(pbAccountType);
			}

			@Override
			public void onFailure(String e) {
				onLoadComplete(pbAccountType);
				setNetworkFailureRequest(NetworkFailureRequest.ACCOUNT_TYPE);
				mErrorHandlerView.responseError(view, e);
			}
		});
		cliGetBankAccountTypes.execute();
	}

	private void init(View view) {
		rlSubmitCli = (RelativeLayout) view.findViewById(R.id.rlSubmitCli);
		rclSelectYourBank = (RecyclerView) view.findViewById(R.id.rclSelectYourBank);
		((SimpleItemAnimator) rclSelectYourBank.getItemAnimator()).setSupportsChangeAnimations(false);

		rclAccountType = (RecyclerView) view.findViewById(R.id.rclSelectAccountType);
		pbDeaBank = (ProgressBar) view.findViewById(R.id.pbDeaBank);
		nestedScrollView = (NestedScrollView) view.findViewById(R.id.nested_scrollview);
		bankTypeConfirmationLayout = (LinearLayout) view.findViewById(R.id.bankTypeConfirmationLayout);
		accountTypeLayout = (LinearLayout) view.findViewById(R.id.accountTypeLayout);
		accountNumberLayout = (LinearLayout) view.findViewById(R.id.accountNumberLayout);
		poiDocumentSubmitTypeLayout = (LinearLayout) view.findViewById(R.id.poiDocumentSubmitTypeLayout);
		pbSubmit = (ProgressBar) view.findViewById(R.id.pbSubmit);
		yesPOIFromBank = (WTextView) view.findViewById(R.id.yesPOIFromBank);
		tvCLIAccountTypeTitle = (WTextView) view.findViewById(R.id.tvCLIAccountTypeTitle);
		tvAccountSavingTitle = (WTextView) view.findViewById(R.id.tvAccountSavingTitle);
		noPOIFromBank = (WTextView) view.findViewById(R.id.noPOIFromBank);
		btnSubmit = (WButton) view.findViewById(R.id.btnSubmit);
		etAccountNumber = (WEditTextView) view.findViewById(R.id.etAccountNumber);
		LinearLayout llAccountNumberLayout = (LinearLayout) view.findViewById(R.id.llAccountNumberLayout);
		RelativeLayout addDocumentButton = (RelativeLayout) view.findViewById(R.id.addDocuments);
		uploadDocumentsLayout = (LinearLayout) view.findViewById(R.id.uploadDocumentsLayout);
		WButton btnRetry = (WButton) view.findViewById(R.id.btnRetry);
		RelativeLayout relConnect = (RelativeLayout) view.findViewById(R.id.no_connection_layout);
		pbAccountType = (ProgressBar) view.findViewById(R.id.pbAccountType);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), relConnect);
		mErrorHandlerView.setMargin(relConnect, 0, 0, 0, 0);

		yesPOIFromBank.setOnClickListener(this);
		noPOIFromBank.setOnClickListener(this);
		llAccountNumberLayout.setOnClickListener(this);
		etAccountNumber.addTextChangedListener(this);
		addDocumentButton.setOnClickListener(this);
		btnSubmit.setOnClickListener(this);
		btnRetry.setOnClickListener(this);
	}

	private void selectBankLayoutManager(List<Bank> deaBankList) {
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		DocumentAdapter documentAdapter = new DocumentAdapter(deaBankList, this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclSelectYourBank.setLayoutManager(mLayoutManager);
		rclSelectYourBank.setAdapter(documentAdapter);
	}

	private void loadBankAccountTypesView(List<BankAccountType> accountTypes) {
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		accountTypeAdapter = new DocumentsAccountTypeAdapter(accountTypes, this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclAccountType.setLayoutManager(mLayoutManager);
		rclAccountType.setAdapter(accountTypeAdapter);

	}

	private void loadPOIDocumentsSubmitTypeView() {
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		documentSubmitTypeAdapter = new POIDocumentSubmitTypeAdapter(this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
	}

	@Override
	public void onItemClick(View view, int position) {
		Bank selectedBank = mDeaBankList.get(position);
		if (selectedBank.bankName.equalsIgnoreCase(otherBank)) {
			hideView(bankTypeConfirmationLayout);
			invalidateBankTypeSelection();
			scrollUpDocumentSubmitTypeLayout();
		} else {
			String selectedBankName = selectedBank.bankName;
			setSelectedBankType(selectedBank.bankName);
			String defaultAccountTypeTitle = getString(R.string.account_type);
			String defaultAccountSavingTitle = getString(R.string.account_saving_title);
			tvCLIAccountTypeTitle.setText(defaultAccountTypeTitle.replace("###", selectedBankName));
			tvAccountSavingTitle.setText(defaultAccountSavingTitle.replace("###", selectedBankName));
			hideView(poiDocumentSubmitTypeLayout);
			invalidateBankTypeSelection();
			scrollUpConfirmationFroPOIFromBankLayout();
		}
	}

	@Override
	public void onAccountTypeClick(View view, int position) {
		setSelectedAccountType(bankAccountTypesList.get(position).accountType);
		scrollUpAccountNumberLayout();
	}

	@Override
	public void onSubmitTypeSelected(View view, int position) {
		switch (position) {
			case 1:
				submitType = SubmitType.LATER;
				scrollUpDocumentSubmitTypeLayout();
				showView(rlSubmitCli);
				break;
			default:
				break;
		}
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

	@Override
	public void onConnectionChanged() {
		retryConnect();
	}

	private void retryConnect() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (new ConnectionDetector().isOnline(getActivity())) {
						if (!loadState.onLoanCompleted()) {
							if (submitType != null) {
								btnSubmit.performClick();
							}
						}
					} else {
						mErrorHandlerView.showToast();
						enableSubmitButton();
					}
				}
			});
		}
	}

	private void progressColorFilter(ProgressBar progressBar, int color) {
		progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
	}

	private void hideView(View v) {
		if (v.getVisibility() == View.VISIBLE)
			v.setVisibility(View.GONE);
	}

	private void showView(View v) {
		v.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View view) {
		MultiClickPreventer.preventMultiClick(view);
		switch (view.getId()) {
			case R.id.yesPOIFromBank:
				submitType = SubmitType.ACCOUNT_NUMBER;
				noPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
				noPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
				yesPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
				yesPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
				hideView(poiDocumentSubmitTypeLayout);
				hideView(rlSubmitCli);
				if (documentSubmitTypeAdapter != null)
					documentSubmitTypeAdapter.clearSelection();
				scrollUpAccountTypeSelectionLayout();
				break;
			case R.id.noPOIFromBank:
				submitType = SubmitType.LATER;
				yesPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
				yesPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
				noPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
				noPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
				resetAccountNumberView();
				hideView(accountTypeLayout);
				hideView(accountNumberLayout);
				if (accountTypeAdapter != null)
					accountTypeAdapter.clearSelection();
				scrollUpDocumentSubmitTypeLayout();
				break;
			case R.id.llAccountNumberLayout:
				IncreaseLimitController.populateExpenseField(etAccountNumber, getActivity());
				break;
			case R.id.btnSubmit:
				onSubmitClick(submitType);
				break;
			case R.id.uploadDocumentInfo:

				break;

			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity())) {
					mErrorHandlerView.hideErrorHandler();
					switch (getNetworkFailureRequest()) {
						case DEA_BANK:
							cliDeaBankRequest();
							break;
						case ACCOUNT_TYPE:
							cliBankAccountTypeRequest();
							break;
						default:
							break;
					}
				}
				break;
			default:
				break;

		}
	}

	public void scrollUpAccountTypeSelectionLayout() {
		cliBankAccountTypeRequest();
		hideView(uploadDocumentsLayout);
		showView(accountTypeLayout);
		dynamicLayoutPadding(bankTypeConfirmationLayout, true);
		dynamicLayoutPadding(poiDocumentSubmitTypeLayout, true);
		dynamicLayoutPadding(accountTypeLayout, false);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", accountTypeLayout.getTop()).setDuration(ANIM_DURATION).start();
			}
		});
	}

	public void scrollUpConfirmationFroPOIFromBankLayout() {
		showView(bankTypeConfirmationLayout);
		dynamicLayoutPadding(bankTypeConfirmationLayout, false);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", bankTypeConfirmationLayout.getTop()).setDuration(ANIM_DURATION).start();
			}
		});
	}

	public void scrollUpDocumentSubmitTypeLayout() {
		submitType = SubmitType.LATER;
		hideView(uploadDocumentsLayout);
		showView(poiDocumentSubmitTypeLayout);
		dynamicLayoutPadding(bankTypeConfirmationLayout, true);
		dynamicLayoutPadding(poiDocumentSubmitTypeLayout, false);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", poiDocumentSubmitTypeLayout.getTop()).setDuration(ANIM_DURATION).start();
			}
		});
		showView(rlSubmitCli);
		setButtonProceed();
	}

	public void scrollUpAccountNumberLayout() {
		resetAccountNumberView();
		hideView(uploadDocumentsLayout);
		showView(accountNumberLayout);
		dynamicLayoutPadding(accountTypeLayout, true);
		dynamicLayoutPadding(accountNumberLayout, false);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", accountNumberLayout.getTop()).setDuration(ANIM_DURATION).start();
			}
		});
		setButtonSubmit();
	}

	public void invalidateBankTypeSelection() {
		yesPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
		yesPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
		noPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
		noPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
		hideView(accountTypeLayout);
		hideView(accountNumberLayout);
		hideView(rlSubmitCli);
		hideView(uploadDocumentsLayout);
		resetAccountNumberView();
		if (accountTypeAdapter != null)
			accountTypeAdapter.clearSelection();
		if (documentSubmitTypeAdapter != null)
			documentSubmitTypeAdapter.clearSelection();
	}

	public void resetAccountNumberView() {
		etAccountNumber.getText().clear();
		hideView(rlSubmitCli);
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void afterTextChanged(Editable editable) {
		if (Utils.checkCLIAccountNumberValidation(etAccountNumber.getText().toString()) && rlSubmitCli.getVisibility() == View.GONE)
			showView(rlSubmitCli);
		else if (!Utils.checkCLIAccountNumberValidation(etAccountNumber.getText().toString()))
			hideView(rlSubmitCli);
	}

	public void dynamicLayoutPadding(View view, boolean defaultPaddingEnabled) {
		IncreaseLimitController ilc = new IncreaseLimitController(getActivity());
		int paddingPixel = 16;
		float density = getActivity().getResources().getDisplayMetrics().density;
		int paddingDp = (int) (paddingPixel * density);
		int screenHeight = (ilc.getScreenHeight(getActivity()))/3;
		if (defaultPaddingEnabled) {
			view.setPadding(0, paddingDp, 0, 0);
		} else {
			view.setPadding(0, paddingDp, 0, screenHeight);
		}

	}

	public void onSubmitClick(SubmitType type) {
		switch (type) {
			case ACCOUNT_NUMBER:
				updateBankDetails();
				break;
			case LATER:
				initSendEmailRequest();
				break;
			default:
				break;

		}
	}

	public void updateBankDetails() {
		disableSubmitButton();
		UpdateBankDetail bankDetail = new UpdateBankDetail();
		bankDetail.setCliOfferID(activeOfferObj.offer.offerId);
		bankDetail.setAccountType(getSelectedAccountType());
		bankDetail.setBankName(getSelectedBankType());
		bankDetail.setAccountNumber(etAccountNumber.getText().toString().trim());
		cliUpdateBankDetails = new CLIUpdateBankDetails(getActivity(), bankDetail, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				enableSubmitButton();
				updateBankDetailResponse = (UpdateBankDetailResponse) object;
				ProcessCompleteFragment processCompleteFragment = new ProcessCompleteFragment();
				moveToProcessCompleteFragment(processCompleteFragment);
				loadSuccess();
			}

			@Override
			public void onFailure(String e) {
				loadFailure();
				enableSubmitButton();
			}
		});
		cliUpdateBankDetails.execute();
	}

	public void disableSubmitButton() {
		Utils.disableEnableChildViews(nestedScrollView, false);
		pbSubmit.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
		pbSubmit.setVisibility(View.VISIBLE);
		btnSubmit.setVisibility(View.GONE);
	}

	public void enableSubmitButton() {
		Utils.disableEnableChildViews(nestedScrollView, true);
		pbSubmit.setVisibility(View.GONE);
		btnSubmit.setVisibility(View.VISIBLE);
	}

	@SuppressLint("CommitTransaction")
	public void moveToProcessCompleteFragment(CLIFragment fragment) {
		fragment.setStepIndicatorListener(mCliStepIndicatorListener);
		FragmentUtils fragmentUtils = new FragmentUtils();
		fragmentUtils.nextFragment((AppCompatActivity) getActivity(), getFragmentManager().beginTransaction(), fragment, R.id.cli_steps_container);

	}

	public void initSendEmailRequest() {
		disableSubmitButton();
		cliSendEmail = new CLISendEmailRequest(getActivity(), new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				CLIEmailResponse response = (CLIEmailResponse) object;
				if (response.httpCode == 200) {
					CliEmailSentFragment cliEmailSentFragment = new CliEmailSentFragment();
					moveToProcessCompleteFragment(cliEmailSentFragment);
				} else {
					enableSubmitButton();
				}

				loadSuccess();
			}

			@Override
			public void onFailure(String e) {
				loadFailure();
				enableSubmitButton();
			}
		});

		cliSendEmail.execute();
	}

	private void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}

	public NetworkFailureRequest getNetworkFailureRequest() {
		return networkFailureRequest;
	}

	public void setNetworkFailureRequest(NetworkFailureRequest networkFailureRequest) {
		this.networkFailureRequest = networkFailureRequest;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelRequest(cliGetBankAccountTypes);
		cancelRequest(cliGetDeaBank);
		cancelRequest(cliUpdateBankDetails);
		cancelRequest(cliSendEmail);
	}

	private void loadSuccess() {
		loadState.setLoadComplete(true);
	}

	private void loadFailure() {
		loadState.setLoadComplete(false);
	}

	private void setButtonProceed() {
		btnSubmit.setText(getString(R.string.proceed));
		rlSubmitCli.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
	}

	private void setButtonSubmit() {
		btnSubmit.setText(getString(R.string.submit));
		rlSubmitCli.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.recent_search_bg));
	}
}
