package za.co.woolworths.financial.services.android.ui.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
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
import android.support.v4.content.FileProvider;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import retrofit.mime.MultipartTypedOutput;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.Document;
import za.co.woolworths.financial.services.android.models.dto.POIDocumentUploadResponse;
import za.co.woolworths.financial.services.android.models.rest.CLIGetBankAccountTypes;
import za.co.woolworths.financial.services.android.models.rest.CLIGetDeaBank;
import za.co.woolworths.financial.services.android.ui.activities.CLIPhase2Activity;
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.activities.SelectFromDriveActivity;
import za.co.woolworths.financial.services.android.ui.adapters.AddedDocumentsListAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.DocumentAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.DocumentsAccountTypeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.POIDocumentSubmitTypeAdapter;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.CountingTypedFile;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.PathUtil;
import za.co.woolworths.financial.services.android.util.PermissionUtils;
import za.co.woolworths.financial.services.android.util.ProgressListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.controller.CLIFragment;
import za.co.woolworths.financial.services.android.util.controller.IncreaseLimitController;

import static android.app.Activity.RESULT_OK;


public class DocumentFragment extends CLIFragment implements DocumentAdapter.OnItemClick, NetworkChangeListener, DocumentsAccountTypeAdapter.OnAccountTypeClick, View.OnClickListener, POIDocumentSubmitTypeAdapter.OnSubmitType, TextWatcher, AddedDocumentsListAdapter.ItemRemoved, View.OnLayoutChangeListener {

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
	private static final int OPEN_WINDOW_FOR_DRIVE_SELECTION = 99;
	private static final int OPEN_GALLERY_TO_PICk_FILE = 11;
	private static final int OPEN_CAMERA_TO_PICk_IMAGE = 33;
	public static final int PERMS_REQUEST_CODE_GALLERY = 222;
	public static final int PERMS_REQUEST_CODE_CAMERA = 333;
	public Uri mCameraUri;

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
		rclAddedDocumentsList = (RecyclerView) view.findViewById(R.id.rclDocumentsList);
		addDocumentButton = (RelativeLayout) view.findViewById(R.id.addDocuments);
		uploadDocumentsLayout = (LinearLayout) view.findViewById(R.id.uploadDocumentsLayout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
		yesPOIFromBank.setOnClickListener(this);
		noPOIFromBank.setOnClickListener(this);
		llAccountNumberLayout.setOnClickListener(this);
		etAccountNumber.addTextChangedListener(this);
		addDocumentButton.setOnClickListener(this);
		poiDocumentInfo.setOnClickListener(this);
		rclAddedDocumentsList.addOnLayoutChangeListener(this);
		btnSubmit.setOnClickListener(this);
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
		switch (position) {
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
				//openGalleryToPickDocuments();
				startActivityForResult(new Intent(getActivity(), SelectFromDriveActivity.class), OPEN_WINDOW_FOR_DRIVE_SELECTION);
				break;
			case R.id.submitCLI:
				if (documentList.size() > 0) {
					uploadDocuments(documentList);
				}
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
		startActivityForResult(uploadIntent, OPEN_GALLERY_TO_PICk_FILE);
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
		}

		return document;
	}

	public void manageSubmitButtonOnDocumentAdd() {
		if (addedDocumentsListAdapter.getItemCount() == 0)
			hideView(btnSubmit);
		else if (addedDocumentsListAdapter.getItemCount() > 0 && btnSubmit.getVisibility() == View.GONE)
			showSubmitButton();
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

	public HttpAsyncTask<String, String, POIDocumentUploadResponse> initUpload(final Document document) {
		return new HttpAsyncTask<String, String, POIDocumentUploadResponse>() {

			private ProgressListener listener;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected POIDocumentUploadResponse httpDoInBackground(String... params) {
				String path = null;
				try {
					path = PathUtil.getPath(getActivity(), document.getUri());
					getRealPathFromURI(getActivity(), document.getUri());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				final File file = new File(path);
				listener = new ProgressListener() {
					@Override
					public void transferred(long num) {
						publishProgress(String.valueOf((int) ((num / (float) file.length()) * 100)));
					}
				};
				MultipartTypedOutput multipartTypedOutput = new MultipartTypedOutput();
				multipartTypedOutput.addPart("files", new CountingTypedFile("*/*", new File(path), listener));
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().uploadPOIDocuments(multipartTypedOutput);
			}

			@Override
			protected Class<POIDocumentUploadResponse> httpDoInBackgroundReturnType() {
				return POIDocumentUploadResponse.class;
			}

			@Override
			protected POIDocumentUploadResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				return new POIDocumentUploadResponse();
			}

			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				Log.i("Progress", values[0]);
				document.setProgress(Integer.parseInt(values[0]));
				addedDocumentsListAdapter.notifyDataSetChanged();
			}

			@Override
			protected void onPostExecute(POIDocumentUploadResponse uploadResponse) {
				super.onPostExecute(uploadResponse);
				int httpCode = uploadResponse.httpCode;
				switch (httpCode) {
					case 200:

						break;

					case 440:

						break;

					default:

						break;
				}
			}
		};
	}

	/*public MultipartTypedOutput buildRequestBody(List<Document> list)
	{
		MultipartTypedOutput multipartTypedOutput = new MultipartTypedOutput();
		for (int i=0;i<list.size();i++)
		{
			try {
				String path= PathUtil.getPath(getActivity(),documentList.get(i).getUri());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return multipartTypedOutput;
	}*/

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
		for (int i = 0; i < dataList.size(); i++)
			initUpload(dataList.get(i)).execute();
	}

	public void openCamera() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mCameraUri = FileProvider.getUriForFile(
				getActivity(),
				getActivity().getApplicationContext()
						.getPackageName() + ".provider", new File(Environment.getExternalStorageDirectory(), "pic_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
		startActivityForResult(intent, OPEN_CAMERA_TO_PICk_IMAGE);

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

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = {MediaStore.Images.Media.DATA};
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

}
