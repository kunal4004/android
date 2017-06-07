package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Bank;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CLIDeaBankMapAdapter;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.ui.views.ProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkChangeListener;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.CLICbxContentBinder;


public class CLIFirstStepFragment extends Fragment implements View.OnClickListener, CLICbxContentBinder.OnCheckboxClickListener, NetworkChangeListener {

	private StepNavigatorCallback stepNavigatorCallback;
	private int mSelectedPosition = -1;
	public FragmentManager fm;
	private ErrorHandlerView mErrorHandlerView;
	private ProgressDialogFragment mGetAccountsProgressDialog;

	private BroadcastReceiver connectionBroadcast;
	private NetworkChangeListener networkChangeListener;
	private String TAG = "CLIFirstStepFragment";

	public interface StepNavigatorCallback {
		void openNextFragment(int index);
	}

	private RecyclerView mRecycleList;
	private CLIDeaBankMapAdapter mCLIDeaBankMapAdapter;
	private WTextView mTextCreditLimit;
	private RelativeLayout relButtonCLIDeaBank;
	private LinearLayoutManager mLayoutManager;
	private WButton mBtnContinue;
	private ImageView mImgInfo;
	private List<Bank> mBanks;
	private CLIFirstStepFragment mContext;
	private WoolworthsApplication mWoolworthsApplication;
	private UpdateBankDetail mUpdateBankDetail;

	private boolean backgroundTaskLoaded = true;

	public CLIFirstStepFragment() {
	}

	View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = this;
		mWoolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
		setRetainInstance(true);
		return inflater.inflate(R.layout.cli_fragment_step_one, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		try {
			networkChangeListener = (NetworkChangeListener) this;
		} catch (ClassCastException ignored) {
		}
		connectionBroadcast = Utils.connectionBroadCast(getActivity(), networkChangeListener);
		initUI(view);
		setListener();
		setText();
		mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//setDeaBanks();
	}

	private void initUI(View view) {
		mRecycleList = (RecyclerView) view.findViewById(R.id.recycleList);
		mTextCreditLimit = (WTextView) view.findViewById(R.id.textACreditLimit);
		relButtonCLIDeaBank = (RelativeLayout) view.findViewById(R.id.relButtonCLIDeaBank);
		mBtnContinue = (WButton) view.findViewById(R.id.btnContinue);
		mImgInfo = (ImageView) view.findViewById(R.id.imgInfo);
	}

	private void setListener() {
		relButtonCLIDeaBank.setOnClickListener(this);
		mBtnContinue.setOnClickListener(this);
		mImgInfo.setOnClickListener(this);
	}

	private void setText() {
		mImgInfo.setVisibility(View.GONE);
		mTextCreditLimit.setText(getActivity().getResources().getString(R.string.cli_select_your_bank));
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		stepNavigatorCallback = (StepNavigatorCallback) getActivity();
	}

	public void setDeaBanks() {
		fm = getActivity().getSupportFragmentManager();
		mGetAccountsProgressDialog = ProgressDialogFragment.newInstance();
		try {
			if (!mGetAccountsProgressDialog.isAdded()) {
				mGetAccountsProgressDialog.show(fm, "v");
			} else {
				mGetAccountsProgressDialog.dismiss();
				mGetAccountsProgressDialog = ProgressDialogFragment.newInstance();
				mGetAccountsProgressDialog.show(fm, "v");
			}

			dEABankAsyncAPI().execute();
		} catch (NullPointerException ignored) {
		}
	}

	private HttpAsyncTask<String, String, DeaBanks> dEABankAsyncAPI() {
		return new HttpAsyncTask<String, String, DeaBanks>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				backgroundTaskLoaded = true;
			}

			@Override
			protected DeaBanks httpDoInBackground(String... params) {
				return ((WoolworthsApplication) getActivity().getApplication()).getApi().getDeaBanks();
			}

			@Override
			protected Class<DeaBanks> httpDoInBackgroundReturnType() {
				return DeaBanks.class;
			}

			@Override
			protected DeaBanks httpError(String errorMessage, HttpErrorCode httpErrorCode) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						stopProgressDialog();
						backgroundTaskLoaded = true;
					}
				});
				return new DeaBanks();
			}

			@Override
			protected void onPostExecute(DeaBanks deaBanks) {
				super.onPostExecute(deaBanks);

				if (deaBanks.banks != null) {
					if (deaBanks.httpCode == 200) {
						mBanks = deaBanks.banks;
						otherChecked(deaBanks.banks);
						mCLIDeaBankMapAdapter = new CLIDeaBankMapAdapter(deaBanks.banks, mContext);
						mLayoutManager = new LinearLayoutManager(getActivity());
						mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
						mRecycleList.setLayoutManager(mLayoutManager);
						mRecycleList.setNestedScrollingEnabled(false);
						mRecycleList.setAdapter(mCLIDeaBankMapAdapter);
						mCLIDeaBankMapAdapter.setCLIContent();
						relButtonCLIDeaBank.setVisibility(View.VISIBLE);
					} else {
						relButtonCLIDeaBank.setVisibility(View.GONE);
					}
				} else {

					if (!TextUtils.isEmpty(deaBanks.response.desc)) {
						Utils.displayValidationMessage(getActivity(),
								TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
								deaBanks.response.desc);
					}
				}
				backgroundTaskLoaded = false;
				stopProgressDialog();
			}
		};
	}

	public void otherChecked(List<Bank> bank) {
		if (bank == null) {
			bank = new ArrayList<>();
		}
		bank.add(new Bank(getActivity().getResources().getString(R.string.cli_others)));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnContinue:
				if (mUpdateBankDetail != null) {
					if (mUpdateBankDetail.getBankName() != null) {
						if (mSelectedPosition == lastPosition()) { //others position clicked
							mWoolworthsApplication.setDEABank(false);
							mWoolworthsApplication.setOther(true);
							stepNavigatorCallback.openNextFragment(2);
						} else {
							mWoolworthsApplication.setDEABank(true);
							mWoolworthsApplication.setOther(false);
							stepNavigatorCallback.openNextFragment(1);
						}
					}
				} else {
					Utils.displayValidationMessage(getActivity(),
							TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
							getString(R.string.cli_select_bank_error));
				}
				break;

			case R.id.imgInfo:
				break;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onCheckboxViewClick(View v, int position) {
		mSelectedPosition = position;
		if (mBanks != null) {
			mUpdateBankDetail = mWoolworthsApplication.updateBankDetail;
			if (mUpdateBankDetail != null) {
				mUpdateBankDetail.setBankName(mBanks.get(position).bankName);
			}
		}
	}

	public int lastPosition() {
		if (mBanks != null)
			return mBanks.size() - 1;
		else
			return 0;
	}

	public void stopProgressDialog() {
		if (mGetAccountsProgressDialog != null && mGetAccountsProgressDialog.isVisible()) {
			mGetAccountsProgressDialog.dismiss();
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
		//connection changed
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (new ConnectionDetector().isOnline()) {
					if (backgroundTaskLoaded) {
						setDeaBanks();
					}
				} else {
					mErrorHandlerView.showToast();
				}
			}
		});
	}
}
