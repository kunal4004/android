package za.co.woolworths.financial.services.android.ui.fragments;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
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
import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.Document;
import za.co.woolworths.financial.services.android.models.rest.CLIGetBankAccountTypes;
import za.co.woolworths.financial.services.android.models.rest.CLIGetDeaBank;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.adapters.AddedDocumentsListAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.DocumentAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.DocumentsAccountTypeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.POIDocumentSubmitTypeAdapter;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

import static android.app.Activity.RESULT_OK;


public class DocumentFragment extends CLIFragment implements DocumentAdapter.OnItemClick, NetworkChangeListener, DocumentsAccountTypeAdapter.OnAccountTypeClick, View.OnClickListener, POIDocumentSubmitTypeAdapter.OnSubmitType, TextWatcher,AddedDocumentsListAdapter.ItemRemoved,View.OnLayoutChangeListener {

	private RecyclerView rclSelectYourBank;
	private List<Bank> deaBankList;
	private ProgressBar pbDeaBank;
	private NetworkChangeListener networkChangeListener;
	private BroadcastReceiver connectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private boolean backgroundTaskLoaded;
	private List<BankAccountType> bankAccountTypes;
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
	private RelativeLayout addDocumentButton;
	private ImageView poiDocumentInfo;
	private LinearLayout uploadDocumentsLayout;
	public DocumentFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.document_fragment, container, false);
		deaBankList = new ArrayList<>();
		cliStepIndicatorListener.onStepSelected(4);
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
		onLoad();
		cliDeaBankRequest();
		cliBankAccountTypeRequest();
		loadPOIDocumentsSubmitTypeView();
		loadAddedDocumentsListView();
	}

	private void connectionBroadcast() {
		try {
			networkChangeListener = this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(getActivity(), networkChangeListener);
	}

	private void onLoad() {
		showView(pbDeaBank);
		progressColorFilter(pbDeaBank, R.color.black);
	}

	private void onLoadComplete() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				hideView(pbDeaBank);
			}
		});
	}

	private void cliDeaBankRequest() {
		onLoad();
		CLIGetDeaBank cliGetDeaBank = new CLIGetDeaBank(getActivity(), new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				backgroundTaskLoaded(false);
				onLoadComplete();
				deaBankList = ((DeaBanks) object).banks;
				deaBankList.add(new Bank(otherBank));
				selectBankLayoutManager(deaBankList);
			}

			@Override
			public void onFailure(String e) {
				onLoadComplete();
				backgroundTaskLoaded(true);
			}
		});
		cliGetDeaBank.execute();
	}

	private void cliBankAccountTypeRequest() {
		CLIGetBankAccountTypes cliGetBankAccountTypes = new CLIGetBankAccountTypes(getActivity(), new OnEventListener() {
			@Override
			public void onSuccess(Object object) {
				bankAccountTypes = ((BankAccountTypes) object).bankAccountTypes;
				loadBankAccountTypesView(bankAccountTypes);
			}

			@Override
			public void onFailure(String e) {

			}
		});
		cliGetBankAccountTypes.execute();
	}

	private void init(View view) {
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
		noPOIFromBank = (WTextView) view.findViewById(R.id.noPOIFromBank);
		btnSubmit = (WTextView) view.findViewById(R.id.submitCLI);
		poiDocumentInfo = (ImageView) view.findViewById(R.id.poiDocumentInfo);
		etAccountNumber = (WEditTextView) view.findViewById(R.id.etAccountNumber);
		llAccountNumberLayout = (LinearLayout) view.findViewById(R.id.llAccountNumberLayout);
		rclAddedDocumentsList=(RecyclerView)view.findViewById(R.id.rclDocumentsList);
		addDocumentButton=(RelativeLayout)view.findViewById(R.id.addDocuments);
		uploadDocumentsLayout=(LinearLayout)view.findViewById(R.id.uploadDocumentsLayout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
		yesPOIFromBank.setOnClickListener(this);
		noPOIFromBank.setOnClickListener(this);
		llAccountNumberLayout.setOnClickListener(this);
		etAccountNumber.addTextChangedListener(this);
		addDocumentButton.setOnClickListener(this);
		poiDocumentInfo.setOnClickListener(this);
		rclAddedDocumentsList.addOnLayoutChangeListener(this);
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
		Bank selectedBank = deaBankList.get(position);
		Log.e("selectedBank", selectedBank.bankName);
		if (selectedBank.bankName.equalsIgnoreCase(otherBank)) {
			hideView(bankTypeConfirmationLayout);
			invalidateBankTypeSelection();
			scrollUpDocumentSubmitTypeLayout();
		} else {
			hideView(poiDocumentSubmitTypeLayout);
			invalidateBankTypeSelection();
			scrollUpConfirmationFroPOIFromBankLayout();
		}

	}

	@Override
	public void onAccountTypeClick(View view, int position) {
		scrollUpAccountNumberLayout();
	}

	@Override
	public void onSubmitTypeSelected(View view, int position) {
		switch (position){
			case 0:
				documentList.clear();
				hideView(btnSubmit);
				scrollUpAddDocumentsLayout();
				break;
			case 1:
				scrollUpDocumentSubmitTypeLayout();
				showView(btnSubmit);
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
		retryConnect();
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
		switch (view.getId()) {
			case R.id.yesPOIFromBank:
				noPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
				noPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
				yesPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black));
				yesPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
				hideView(poiDocumentSubmitTypeLayout);
				hideView(btnSubmit);
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
				IncreaseLimitController.focusEditView(etAccountNumber, getActivity());
				break;
			case R.id.addDocuments:
				openGalleryToPickDocuments();
				break;

			case R.id.poiDocumentInfo:
				Utils.displayValidationMessage(getActivity(), CustomPopUpWindow.MODAL_LAYOUT.UPLOAD_DOCUMENT_MODAL, "");
				break;

			default:
				break;

		}
	}

	public void scrollUpAccountTypeSelectionLayout() {
		accountTypeLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", accountTypeLayout.getTop()).setDuration(300).start();
			}
		});
	}

	public void scrollUpConfirmationFroPOIFromBankLayout() {
		bankTypeConfirmationLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", bankTypeConfirmationLayout.getTop()).setDuration(300).start();
			}
		});
	}

	public void scrollUpDocumentSubmitTypeLayout() {
		hideView(uploadDocumentsLayout);
		poiDocumentSubmitTypeLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", poiDocumentSubmitTypeLayout.getTop()).setDuration(300).start();
			}
		});
	}

	public void scrollUpAccountNumberLayout() {
		resetAccountNumberView();
		accountNumberLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", accountNumberLayout.getTop()).setDuration(300).start();
			}
		});
	}
	public void scrollUpAddDocumentsLayout() {
		uploadDocumentsLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", uploadDocumentsLayout.getTop()).setDuration(300).start();
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
		hideView(btnSubmit);
		hideView(uploadDocumentsLayout);
		resetAccountNumberView();
		if (accountTypeAdapter != null)
			accountTypeAdapter.clearSelection();
		if (documentSubmitTypeAdapter != null)
			documentSubmitTypeAdapter.clearSelection();
	}

	public void showSubmitButton() {
		btnSubmit.setVisibility(View.VISIBLE);
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
		hideView(btnSubmit);
	}


	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void afterTextChanged(Editable editable) {
		if (IncreaseLimitController.editTextLength(etAccountNumber.getText().toString()) && btnSubmit.getVisibility() == View.GONE)
			showSubmitButton();
		else if (!IncreaseLimitController.editTextLength(etAccountNumber.getText().toString()))
			hideView(btnSubmit);
	}


	public void openGalleryToPickDocuments() {
		Intent uploadIntent = new Intent();
		uploadIntent.setType("*/*");
		uploadIntent.setAction(Intent.ACTION_GET_CONTENT);
		uploadIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		uploadIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		startActivityForResult(uploadIntent, 11);
		/*String[] zips = {".zip",".rar"};
		String[] pdfs = {".pdf"};
		FilePickerBuilder.getInstance().setMaxCount(5)
				.setActivityTheme(R.style.PickerTheme)
				.addFileSupport("ZIP",zips)
				.addFileSupport("PDF",pdfs)
				.enableDocSupport(false)
				.enableImagePicker(true)
				.withOrientation(Orientation.UNSPECIFIED)
				.pickFile(this);*/
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 11 && resultCode == RESULT_OK && data != null) {
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

			if(documentList.size()>0) {
				if (addedDocumentsListAdapter.getItemCount() == 0) {
					addedDocumentsListAdapter = new AddedDocumentsListAdapter(this, documentList);
					rclAddedDocumentsList.setAdapter(addedDocumentsListAdapter);
				} else {
					addedDocumentsListAdapter.notifyDataSetChanged();
				}
				manageSubmitButtonOnDocumentAdd();
			}

		/*	// Get the Uri of the selected file
			Uri uri = data.getData();
			String uriString = uri.toString();//uri.getPath()
			File myFile = new File(uriString);
			String path = myFile.getAbsolutePath();
			String displayName = null;

			if (uriString.startsWith("content://")) {
				Cursor cursor = null;
				try {
					cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
					if (cursor != null && cursor.moveToFirst()) {
						displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
					}
				} finally {
					cursor.close();
				}
			} else if (uriString.startsWith("file://")) {
				displayName = myFile.getName();
			}*/
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
		}

		return document;
	}

	public void manageSubmitButtonOnDocumentAdd()
	{
		if(addedDocumentsListAdapter.getItemCount()==0)
			hideView(btnSubmit);
		else if(addedDocumentsListAdapter.getItemCount()>0 && btnSubmit.getVisibility()==View.GONE)
			showSubmitButton();
	}

	@Override
	public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
		switch (view.getId())
		{
			case R.id.rclDocumentsList:
				manageSubmitButtonOnDocumentAdd();
				break;
			default:
				break;
		}
	}
}
