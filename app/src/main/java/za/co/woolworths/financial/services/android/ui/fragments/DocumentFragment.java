package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.OnEventListener;
import za.co.woolworths.financial.services.android.util.Utils;

public class DocumentFragment extends Fragment implements DocumentAdapter.OnItemClick, NetworkChangeListener {

	private RecyclerView rclSelectYourBank;
	private List<Bank> deaBankList;
	private ProgressBar pbDeaBank;
	private NetworkChangeListener networkChangeListener;
	private BroadcastReceiver connectionBroadcast;
	private ErrorHandlerView mErrorHandlerView;
	private boolean backgroundTaskLoaded;
	private List<BankAccountType> bankAccountTypes;

	public DocumentFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.document_fragment, container, false);
		deaBankList = new ArrayList<>();
		Utils.updateCLIStepIndicator(4,DocumentFragment.this);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		connectionBroadcast();
		init(view);
		onLoad();
		cliDeaBankRequest();
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
				deaBankList.add(new Bank("Other"));
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

			}

			@Override
			public void onFailure(String e) {

			}
		});
		cliGetBankAccountTypes.execute();
	}

	private void init(View view) {
		rclSelectYourBank = (RecyclerView) view.findViewById(R.id.rclSelectYourBank);
		pbDeaBank = (ProgressBar) view.findViewById(R.id.pbDeaBank);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
	}

	private void selectBankLayoutManager(List<Bank> deaBankList) {
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
		DocumentAdapter documentAdapter = new DocumentAdapter(deaBankList, this);
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		rclSelectYourBank.setLayoutManager(mLayoutManager);
		rclSelectYourBank.setAdapter(documentAdapter);
	}
	private void loadBankAccountTypesView()
	{

	}

	@Override
	public void onItemClick(View view, int position) {
		Bank selectedBank = deaBankList.get(position);
		Log.e("selectedBank", selectedBank.bankName);
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
}
