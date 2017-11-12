package za.co.woolworths.financial.services.android.ui.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.models.dto.BankAccountResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.CliPoiOriginResponse;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.DeaBanksResponse;
import za.co.woolworths.financial.services.android.models.dto.Document;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.POIDocumentUploadResponse;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.models.rest.CLIGetBankAccountTypes;
import za.co.woolworths.financial.services.android.models.rest.CLIGetDeaBank;
import za.co.woolworths.financial.services.android.models.rest.CLIPOIOriginRequest;
import za.co.woolworths.financial.services.android.models.rest.CLISendEmailRequest;
import za.co.woolworths.financial.services.android.models.rest.CLISubmitPOIRequest;
import za.co.woolworths.financial.services.android.models.rest.CLIUpdateBankDetails;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.SelectFromDriveActivity;
import za.co.woolworths.financial.services.android.ui.adapters.AddedDocumentsListAdapter;
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
import za.co.woolworths.financial.services.android.util.PathUtil;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

import static android.app.Activity.RESULT_OK;


public class DocumentFragment extends CLIFragment implements DocumentAdapter.OnItemClick, NetworkChangeListener, DocumentsAccountTypeAdapter.OnAccountTypeClick, View.OnClickListener, POIDocumentSubmitTypeAdapter.OnSubmitType, TextWatcher, AddedDocumentsListAdapter.ItemRemoved, View.OnLayoutChangeListener {

	private final int ANIM_DURATION = 600;
	private RecyclerView rclSelectYourBank;
	private DeaBanks deaBankList;
	private ProgressBar pbDeaBank;
	private BroadcastReceiver connectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private boolean backgroundTaskLoaded;
	private List<BankAccountType> bankAccountTypesList;
	private RecyclerView rclAccountType;
	private LinearLayout bankTypeConfirmationLayout;
	private LinearLayout accountTypeLayout;
	private LinearLayout accountNumberLayout;
	private LinearLayout poiDocumentSubmitTypeLayout;
	private WTextView btnSubmit;
	private NestedScrollView nestedScrollView;
	private WTextView yesPOIFromBank;
	private WTextView noPOIFromBank;
	private RecyclerView rclPOIDocuments;
	private String otherBank = "Other";
	private DocumentsAccountTypeAdapter accountTypeAdapter;
	private POIDocumentSubmitTypeAdapter documentSubmitTypeAdapter;
	private WEditTextView etAccountNumber;
	private LinearLayout llAccountNumberLayout;
	private CLIPhase2Activity mCliPhase2Activity;
	private RecyclerView rclAddedDocumentsList;
	private AddedDocumentsListAdapter addedDocumentsListAdapter;
	private List<Document> documentList;
	private RelativeLayout addDocumentButton, rlSubmitCli;
	private ImageView poiDocumentInfo, uploadDocumentInfo;
	private LinearLayout uploadDocumentsLayout;
	private static final int OPEN_WINDOW_FOR_DRIVE_SELECTION = 99;
	private static final int OPEN_GALLERY_TO_PICk_FILE = 11;
	private static final int OPEN_CAMERA_TO_PICk_IMAGE = 33;
	public static final int PERMS_REQUEST_CODE_GALLERY = 222;
	public static final int PERMS_REQUEST_CODE_CAMERA = 333;
	public Uri mCameraUri;
	public SubmitType submitType;
	private List<Bank> mDeaBankList;
	public String selectedBankType;
	public String selectedAccountType;
	public UpdateBankDetailResponse updateBankDetailResponse;
	private CLIGetBankAccountTypes cliGetBankAccountTypes;
	private CLIGetDeaBank cliGetDeaBank;
	private ProgressBar pbAccountType;
	private CLIUpdateBankDetails cliUpdateBankDetails;
	private CLISendEmailRequest cliSendEmail;
	private CLIPOIOriginRequest cLIPOIOriginRequest;
	private CLISubmitPOIRequest cliSubmitPOIRequest;
	private WTextView tvCLIAccountTypeTitle, tvAccountSavingTitle;

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

	public OfferActive getCLICreateOfferResponse() {
		return ((CLIPhase2Activity) this.getActivity()).mCLICreateOfferResponse;
	}

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.document_fragment, container, false);
		deaBankList = new DeaBanks();
		mCliStepIndicatorListener.onStepSelected(4);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		connectionBroadcast();
		mCliPhase2Activity = (CLIPhase2Activity) getActivity();
		mCliPhase2Activity.actionBarCloseIcon();
		mCliPhase2Activity.hideDeclineOffer();
		init(view);
		onLoad(pbDeaBank);
		cliDeaBankRequest();
		loadPOIDocumentsSubmitTypeView();
		loadAddedDocumentsListView();
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
				backgroundTaskLoaded(false);
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
						DeaBanksResponse response = deaBankList.response;
						if (response != null) {
							Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
						}
						break;
				}
				onLoadComplete(pbDeaBank);
			}

			@Override
			public void onFailure(String e) {
				mErrorHandlerView.networkFailureHandler(e.toString());
				onLoadComplete(pbDeaBank);
				backgroundTaskLoaded(true);
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
						BankAccountResponse response = bankAccountTypes.response;
						if (response != null) {
							Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc);
						}
						break;
				}
				onLoadComplete(pbAccountType);
			}

			@Override
			public void onFailure(String e) {
				onLoadComplete(pbAccountType);
				mErrorHandlerView.networkFailureHandler(e.toString());
			}
		});
		cliGetBankAccountTypes.execute();
	}

	private void init(View view) {
		rlSubmitCli = (RelativeLayout) view.findViewById(R.id.rlSubmitCli);
		rclSelectYourBank = (RecyclerView) view.findViewById(R.id.rclSelectYourBank);
		rclAccountType = (RecyclerView) view.findViewById(R.id.rclSelectAccountType);
		rclPOIDocuments = (RecyclerView) view.findViewById(R.id.rclPOIDocuments);
		pbDeaBank = (ProgressBar) view.findViewById(R.id.pbDeaBank);
		nestedScrollView = (NestedScrollView) view.findViewById(R.id.nested_scrollview);
		bankTypeConfirmationLayout = (LinearLayout) view.findViewById(R.id.bankTypeConfirmationLayout);
		accountTypeLayout = (LinearLayout) view.findViewById(R.id.accountTypeLayout);
		accountNumberLayout = (LinearLayout) view.findViewById(R.id.accountNumberLayout);
		poiDocumentSubmitTypeLayout = (LinearLayout) view.findViewById(R.id.poiDocumentSubmitTypeLayout);
		yesPOIFromBank = (WTextView) view.findViewById(R.id.yesPOIFromBank);
		tvCLIAccountTypeTitle = (WTextView) view.findViewById(R.id.tvCLIAccountTypeTitle);
		tvAccountSavingTitle = (WTextView) view.findViewById(R.id.tvAccountSavingTitle);
		noPOIFromBank = (WTextView) view.findViewById(R.id.noPOIFromBank);
		btnSubmit = (WTextView) view.findViewById(R.id.submitCLI);
		poiDocumentInfo = (ImageView) view.findViewById(R.id.poiDocumentInfo);
		uploadDocumentInfo = (ImageView) view.findViewById(R.id.uploadDocumentInfo);
		etAccountNumber = (WEditTextView) view.findViewById(R.id.etAccountNumber);
		llAccountNumberLayout = (LinearLayout) view.findViewById(R.id.llAccountNumberLayout);
		rclAddedDocumentsList = (RecyclerView) view.findViewById(R.id.rclDocumentsList);
		addDocumentButton = (RelativeLayout) view.findViewById(R.id.addDocuments);
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
		poiDocumentInfo.setOnClickListener(this);
		rclAddedDocumentsList.addOnLayoutChangeListener(this);
		btnSubmit.setOnClickListener(this);
		uploadDocumentInfo.setOnClickListener(this);
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
		rclPOIDocuments.setLayoutManager(mLayoutManager);
		rclPOIDocuments.setAdapter(documentSubmitTypeAdapter);
	}

	private void loadAddedDocumentsListView() {
		documentList = new ArrayList<>();
		documentList.clear();
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		addedDocumentsListAdapter = new AddedDocumentsListAdapter(this, documentList);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclAddedDocumentsList.setLayoutManager(mLayoutManager);
		rclAddedDocumentsList.setAdapter(addedDocumentsListAdapter);
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
			case 0:
				submitType = SubmitType.DOCUMENTS;
				documentList.clear();
				addedDocumentsListAdapter.notifyDataSetChanged();
				hideView(rlSubmitCli);
				scrollUpAddDocumentsLayout();
				break;
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
	public void onItemRemoved(View view, int position) {
		documentList.remove(position);
		addedDocumentsListAdapter.notifyDataSetChanged();

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
		//retryConnect();
	}

	private void retryConnect() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (new ConnectionDetector().isOnline(getActivity())) {
					if (getBackgroundTaskStatus()) {
						cliDeaBankRequest();
					}
				} else {
					mErrorHandlerView.showToast();
				}
			}
		});
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

	private void backgroundTaskLoaded(boolean status) {
		this.backgroundTaskLoaded = status;
	}

	private boolean getBackgroundTaskStatus() {
		return this.backgroundTaskLoaded;
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
			case R.id.addDocuments:
				//openGalleryToPickDocuments();
				startActivityForResult(new Intent(getActivity(), SelectFromDriveActivity.class), OPEN_WINDOW_FOR_DRIVE_SELECTION);
				getActivity().overridePendingTransition(0, 0);
				break;
			case R.id.submitCLI:
				onSubmitClick(submitType);
				break;
			case R.id.uploadDocumentInfo:
			case R.id.poiDocumentInfo:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.UPLOAD_DOCUMENT_MODAL, "");
				break;

			case R.id.btnRetry:
				if (new ConnectionDetector().isOnline(getActivity())) {
					mErrorHandlerView.hideErrorHandler();
					cliDeaBankRequest();
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
	}

	public void scrollUpAddDocumentsLayout() {
		showView(uploadDocumentsLayout);
		dynamicLayoutPadding(poiDocumentSubmitTypeLayout, true);
		dynamicLayoutPadding(uploadDocumentsLayout, false);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", uploadDocumentsLayout.getTop()).setDuration(ANIM_DURATION).start();
			}
		});
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

	public void showSubmitButton() {
		showView(rlSubmitCli);
		nestedScrollView.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
			}
		});
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


	public void openGalleryToPickDocuments() {
		Intent uploadIntent = new Intent();
		uploadIntent.setType("*/*");
		uploadIntent.putExtra(Intent.EXTRA_MIME_TYPES, Utils.CLI_POI_ACCEPT_MIME_TYPES);
		uploadIntent.setAction(Intent.ACTION_GET_CONTENT);
		uploadIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		uploadIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		startActivityForResult(uploadIntent, OPEN_GALLERY_TO_PICk_FILE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case OPEN_GALLERY_TO_PICk_FILE:
				if (resultCode == RESULT_OK && data != null) {
					addPickedPOIDocumentsToList(data);
				}
				break;
			case OPEN_WINDOW_FOR_DRIVE_SELECTION:
				if (resultCode == RESULT_OK) {
					switch (data.getIntExtra("selected", 0)) {
						case SelectFromDriveActivity.GALLERY:
							if (checkRuntimePermission(PERMS_REQUEST_CODE_GALLERY))
								openGalleryToPickDocuments();
							break;
						case SelectFromDriveActivity.CAMERA:
							if (checkRuntimePermission(PERMS_REQUEST_CODE_CAMERA))
								openCamera();
							break;

					}

				}
				break;
			case OPEN_CAMERA_TO_PICk_IMAGE:
				if (resultCode == RESULT_OK && data != null) {
					data.setData(mCameraUri);
					addPickedPOIDocumentsToList(data);
				}
				break;
			default:
				break;
		}
	}

	public Document convertUtiToDocumentObj(Uri uri) {
		Document document = new Document();
		String uriString = uri.toString();
		document.setUri(uri);
		if (uriString.startsWith("content://")) {
			Cursor cursor = null;
			try {
				cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					document.setName(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
					document.setSize((cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))) / 1024);
				}
			} finally {
				cursor.close();
			}
		} else if (uriString.startsWith("file://")) {
			File file = new File(uri.getPath());
			document.setName(file.getName());
			document.setSize(file.length() / 1024);
		} else if (uriString.startsWith("file:/storage")) {
			File file = new File(uri.getPath());
			document.setName(file.getName());
			document.setSize(file.length() / 1024);
		}

		return document;
	}

	public void manageSubmitButtonOnDocumentAdd() {
		if (addedDocumentsListAdapter.getItemCount() == 0)
			hideView(rlSubmitCli);
		else if (addedDocumentsListAdapter.getItemCount() > 0 && rlSubmitCli.getVisibility() == View.GONE)
			showView(rlSubmitCli);
	}

	@Override
	public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
		switch (view.getId()) {
			case R.id.rclDocumentsList:
				manageSubmitButtonOnDocumentAdd();
				break;
			default:
				break;
		}
	}

	public boolean checkRuntimePermission(int REQUEST_CODE) {
		switch (REQUEST_CODE) {
			case PERMS_REQUEST_CODE_GALLERY:
				if (ContextCompat.checkSelfPermission(getActivity(),
						Manifest.permission.READ_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {
					if (shouldShowRequestPermissionRationale(
							Manifest.permission.READ_EXTERNAL_STORAGE)) {
						requestPermissions(
								new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
								REQUEST_CODE);
					} else {
						//we can request the permission.
						requestPermissions(
								new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
								REQUEST_CODE);
					}
					return false;
				} else {
					return true;
				}

			case PERMS_REQUEST_CODE_CAMERA:
				String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
				if (!PermissionUtils.hasPermissions(getActivity(), PERMISSIONS)) {
					if (shouldShowRequestPermissionRationale(
							Manifest.permission.CAMERA)) {
						requestPermissions(
								PERMISSIONS,
								REQUEST_CODE);
					} else {
						//we can request the permission.
						requestPermissions(
								PERMISSIONS,
								REQUEST_CODE);
					}
					return false;
				} else {
					return true;
				}
			default:
				return false;
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case PERMS_REQUEST_CODE_GALLERY:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
					openGalleryToPickDocuments();
				break;
			case PERMS_REQUEST_CODE_CAMERA:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)
					openCamera();
				break;
		}
	}

	public void uploadDocuments(List<Document> dataList) {
		disableSubmitButton();
		int totalFiles = getValidDocumentList(documentList).size();
		int j = 1;
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).getSize() <= WoolworthsApplication.getPoiDocumentSizeLimit()) {
				dataList.get(i).setFileNumber(j);
				++j;
				initUploadDocument(dataList.get(i), totalFiles, "22", "17318731");
			}
		}
	}

	public void openCamera() {
		//In some devices, the Uri is null in onActivityForResult(). So you need to set Uri to placing the captured image.
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// If there any applications that can handle this intent then call the intent.
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

			URI oldUri = getOutputMediaFile();

			mCameraUri = new Uri.Builder().scheme(oldUri.getScheme())
					.encodedAuthority(oldUri.getRawAuthority())
					.encodedPath(oldUri.getRawPath())
					.query(oldUri.getRawQuery())
					.fragment(oldUri.getRawFragment())
					.build();
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
			startActivityForResult(takePictureIntent, OPEN_CAMERA_TO_PICk_IMAGE);
		}
	}

	public void addPickedPOIDocumentsToList(Intent data) {

		ClipData clipData = data.getClipData();
		if (clipData != null) {
			for (int i = 0; i < clipData.getItemCount(); i++) {
				ClipData.Item item = clipData.getItemAt(i);
				Uri uri = item.getUri();
				documentList.add(convertUtiToDocumentObj(uri));
			}
		} else if (data.getData() != null) {
			Uri uri = data.getData();
			documentList.add(convertUtiToDocumentObj(uri));
		}

		if (documentList.size() > 0) {
			if (addedDocumentsListAdapter.getItemCount() == 0) {
				addedDocumentsListAdapter = new AddedDocumentsListAdapter(this, documentList);
				rclAddedDocumentsList.setAdapter(addedDocumentsListAdapter);
			} else {
				addedDocumentsListAdapter.notifyDataSetChanged();
			}
			manageSubmitButtonOnDocumentAdd();
		}

	}

	public URI getOutputMediaFile() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir;
		// If the external directory is writable then then return the External pictures directory.
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
		} else {
			mediaStorageDir = Environment.getDownloadCacheDirectory();
		}
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}
		// Create a media file name
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + "pic_" + String.valueOf(System.currentTimeMillis()) + ".jpg");

		return mediaFile.toURI();
	}

	public void dynamicLayoutPadding(View view, boolean defaultPaddingEnabled) {
		IncreaseLimitController ilc = new IncreaseLimitController(getActivity());
		int paddingPixel = 16;
		float density = getActivity().getResources().getDisplayMetrics().density;
		int paddingDp = (int) (paddingPixel * density);
		int screenHeight = ilc.getScreenHeight(getActivity()) / 2;
		if (defaultPaddingEnabled) {
			view.setPadding(0, paddingDp, 0, 0);
		} else {
			view.setPadding(0, paddingDp, 0, screenHeight);
		}

	}

	public List<Document> getValidDocumentList(List<Document> docs) {

		List<Document> subList = new ArrayList<>();
		for (Document document : docs) {
			if (document.getSize() <= WoolworthsApplication.getPoiDocumentSizeLimit())
				subList.add(document);
		}

		return subList;
	}

	public void onSubmitClick(SubmitType type) {
		switch (type) {
			case ACCOUNT_NUMBER:
				updateBankDetails();
				break;
			case DOCUMENTS:
				if (getValidDocumentList(documentList).size() > 0) {
					uploadDocuments(documentList);
				}
				break;
			case LATER:
				initSendEmailRequest();
				break;
			default:
				break;

		}
	}

	public boolean isAllFilesUploaded(List<Document> allFiles) {
		for (Document document : allFiles) {
			if (!document.isUploaded())
				return false;
		}

		return true;
	}

	public void updateBankDetails() {
		disableSubmitButton();
		UpdateBankDetail bankDetail = new UpdateBankDetail();
		bankDetail.setCliOfferID(111);
		bankDetail.setAccountType(getSelectedAccountType());
		bankDetail.setBankName(getSelectedBankType());
		bankDetail.setAccountNumber(etAccountNumber.getText().toString().trim());
		cliUpdateBankDetails = new CLIUpdateBankDetails(getActivity(), bankDetail, new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				enableSubmitButton();
				updateBankDetailResponse = (UpdateBankDetailResponse) object;
				moveToProcessCompleteFragment();
			}

			@Override
			public void onFailure(String e) {
				enableSubmitButton();
			}
		});
		cliUpdateBankDetails.execute();
	}

	public void disableSubmitButton() {
		Utils.disableEnableChildViews(nestedScrollView, false);
		btnSubmit.setEnabled(false);
		btnSubmit.setAlpha(0.3f);
	}

	public void enableSubmitButton() {
		Utils.disableEnableChildViews(nestedScrollView, true);
		btnSubmit.setEnabled(true);
		btnSubmit.setAlpha(1f);
	}

	public void moveToProcessCompleteFragment() {
		ProcessCompleteFragment processCompleteFragment = new ProcessCompleteFragment();
		processCompleteFragment.setStepIndicatorListener(mCliStepIndicatorListener);
		FragmentUtils fragmentUtils = new FragmentUtils();
		fragmentUtils.nextFragment((AppCompatActivity) getActivity(), getFragmentManager().beginTransaction(), processCompleteFragment, R.id.cli_steps_container);

	}

	public void initSendEmailRequest() {
		disableSubmitButton();
		cliSendEmail = new CLISendEmailRequest(getActivity(), new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				CLIEmailResponse response = (CLIEmailResponse) object;
				if (response.httpCode == 200)
					moveToProcessCompleteFragment();
				else
					enableSubmitButton();
			}

			@Override
			public void onFailure(String e) {
				enableSubmitButton();
			}
		});

		cliSendEmail.execute();
	}

	public void initPOIOriginRequest() {
		//make dynamic values for cliID and productOfferingID
		cLIPOIOriginRequest = new CLIPOIOriginRequest(getActivity(), 111, "20", new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				CliPoiOriginResponse response = (CliPoiOriginResponse) object;
			}

			@Override
			public void onFailure(String e) {

			}
		});

		cLIPOIOriginRequest.execute();
	}

	public void initUploadDocument(final Document document, int totalFiles, String cliId, String saId) {
		String path = null;
		try {
			path = PathUtil.getPath(getActivity(), document.getUri());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			document.setUploaded(false);
			documentSubmitTypeAdapter.notifyDataSetChanged();
			return;
		}

		cliSubmitPOIRequest = new CLISubmitPOIRequest(getActivity(), path, cliId, document.getFileNumber(), totalFiles, saId, new CLISubmitPOIRequest.UploadEventListener() {
			@Override
			public void onSuccess(POIDocumentUploadResponse response) {
				if (response.httpCode == 200) {
					document.setUploaded(true);
				} else {
					document.setUploaded(false);
					document.setProgress(0);
				}
				addedDocumentsListAdapter.notifyDataSetChanged();

				if (isAllFilesUploaded(getValidDocumentList(documentList))) {
					initPOIOriginRequest();
					enableSubmitButton();
					moveToProcessCompleteFragment();
				}

			}

			@Override
			public void onFailure(String e) {
				document.setUploaded(false);
				document.setProgress(0);
				addedDocumentsListAdapter.notifyDataSetChanged();
			}

			@Override
			public void onProgress(int percentage) {
				document.setProgress(percentage);
				addedDocumentsListAdapter.notifyDataSetChanged();
			}
		});

		cliSubmitPOIRequest.execute();
	}

	private void cancelRequest(HttpAsyncTask httpAsyncTask) {
		if (httpAsyncTask != null) {
			if (!httpAsyncTask.isCancelled()) {
				httpAsyncTask.cancel(true);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cancelRequest(cliGetBankAccountTypes);
		cancelRequest(cliGetDeaBank);
		cancelRequest(cliUpdateBankDetails);
		cancelRequest(cliSendEmail);
		cancelRequest(cLIPOIOriginRequest);
	}
}
