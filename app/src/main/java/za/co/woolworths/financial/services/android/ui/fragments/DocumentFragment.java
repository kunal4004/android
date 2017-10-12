package za.co.woolworths.financial.services.android.ui.fragments;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.rest.CLIGetBankAccountTypes;
import za.co.woolworths.financial.services.android.models.rest.CLIGetDeaBank;
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

public class DocumentFragment extends CLIFragment implements DocumentAdapter.OnItemClick, NetworkChangeListener,DocumentsAccountTypeAdapter.OnAccountTypeClick ,View.OnClickListener,POIDocumentSubmitTypeAdapter.OnSubmitType,TextWatcher{

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
	private String otherBank="Other";
	private DocumentsAccountTypeAdapter accountTypeAdapter;
	private POIDocumentSubmitTypeAdapter documentSubmitTypeAdapter;
	private WEditTextView etAccountNumber;
	private LinearLayout llAccountNumberLayout;

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
		init(view);
		onLoad();
		cliDeaBankRequest();
		cliBankAccountTypeRequest();
		loadPOIDocumentsSubmitTypeView();
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

	private void cliBankAccountTypeRequest()
	{
		CLIGetBankAccountTypes cliGetBankAccountTypes=new CLIGetBankAccountTypes(getActivity(), new OnEventListener() {
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
		nestedScrollView=(NestedScrollView)view.findViewById(R.id.nested_scrollview);
		bankTypeConfirmationLayout=(LinearLayout)view.findViewById(R.id.bankTypeConfirmationLayout);
		accountTypeLayout=(LinearLayout)view.findViewById(R.id.accountTypeLayout);
		accountNumberLayout=(LinearLayout)view.findViewById(R.id.accountNumberLayout);
		poiDocumentSubmitTypeLayout=(LinearLayout) view.findViewById(R.id.poiDocumentSubmitTypeLayout);
		yesPOIFromBank = (WTextView) view.findViewById(R.id.yesPOIFromBank);
		noPOIFromBank = (WTextView) view.findViewById(R.id.noPOIFromBank);
		btnSubmit=(WTextView)view.findViewById(R.id.submitCLI);
		etAccountNumber=(WEditTextView)view.findViewById(R.id.etAccountNumber);
		llAccountNumberLayout=(LinearLayout)view.findViewById(R.id.llAccountNumberLayout);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
		yesPOIFromBank.setOnClickListener(this);
		noPOIFromBank.setOnClickListener(this);
		llAccountNumberLayout.setOnClickListener(this);
		etAccountNumber.addTextChangedListener(this);
	}

	private void selectBankLayoutManager(List<Bank> deaBankList) {
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		DocumentAdapter documentAdapter = new DocumentAdapter(deaBankList, this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclSelectYourBank.setLayoutManager(mLayoutManager);
		rclSelectYourBank.setAdapter(documentAdapter);
	}

	private void loadBankAccountTypesView(List<BankAccountType> accountTypes)
	{
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		accountTypeAdapter =new DocumentsAccountTypeAdapter(accountTypes,this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclAccountType.setLayoutManager(mLayoutManager);
		rclAccountType.setAdapter(accountTypeAdapter);

	}

	private void loadPOIDocumentsSubmitTypeView(){
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		documentSubmitTypeAdapter=new POIDocumentSubmitTypeAdapter(this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclPOIDocuments.setLayoutManager(mLayoutManager);
		rclPOIDocuments.setAdapter(documentSubmitTypeAdapter);
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
		showSubmitButton();
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
		progressBar.setIndeterminate(true);
		progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
	}

	private void hideView(View v) {
		if(v.getVisibility()==View.VISIBLE)
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
				if(documentSubmitTypeAdapter!=null)
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
				if(accountTypeAdapter!=null)
					accountTypeAdapter.clearSelection();
				scrollUpDocumentSubmitTypeLayout();
				break;
			case R.id.llAccountNumberLayout:
				IncreaseLimitController.focusEditView(etAccountNumber,getActivity());
				break;

		}
	}

	public void scrollUpAccountTypeSelectionLayout(){
		accountTypeLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", accountTypeLayout.getTop()).setDuration(300).start();
			}
		});
	}

	public void scrollUpConfirmationFroPOIFromBankLayout(){
		bankTypeConfirmationLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", bankTypeConfirmationLayout.getTop()).setDuration(300).start();
			}
		});
	}
	public void scrollUpDocumentSubmitTypeLayout(){
		poiDocumentSubmitTypeLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", poiDocumentSubmitTypeLayout.getTop()).setDuration(300).start();
			}
		});
	}

	public void scrollUpAccountNumberLayout(){
		resetAccountNumberView();
		accountNumberLayout.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator.ofInt(nestedScrollView, "scrollY", accountNumberLayout.getTop()).setDuration(300).start();
			}
		});
	}

	public void invalidateBankTypeSelection()
	{
		yesPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
		yesPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
		noPOIFromBank.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
		noPOIFromBank.setTextColor(ContextCompat.getColor(getActivity(), R.color.cli_yes_no_button_color));
		hideView(accountTypeLayout);
		hideView(accountNumberLayout);
		hideView(btnSubmit);
		resetAccountNumberView();
		if(accountTypeAdapter !=null)
			accountTypeAdapter.clearSelection();
		if(documentSubmitTypeAdapter !=null)
			documentSubmitTypeAdapter.clearSelection();
	}

	public void showSubmitButton()
	{
		btnSubmit.setVisibility(View.VISIBLE);
		nestedScrollView.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
			}
		});
	}

	public void resetAccountNumberView()
	{
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
		if (IncreaseLimitController.editTextLength(etAccountNumber.getText().toString()) && btnSubmit.getVisibility()==View.GONE)
			showSubmitButton();
		else if (!IncreaseLimitController.editTextLength(etAccountNumber.getText().toString()))
			hideView(btnSubmit);
	}
}
