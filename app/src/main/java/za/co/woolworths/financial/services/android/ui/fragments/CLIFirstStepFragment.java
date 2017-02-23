package za.co.woolworths.financial.services.android.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import za.co.woolworths.financial.services.android.models.dto.DeaBanksResponse;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
import za.co.woolworths.financial.services.android.ui.activities.CLISupplyInfoActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CLIDeaBankMapAdapter;
import za.co.woolworths.financial.services.android.ui.views.ProgressDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.binder.view.CLICbxContentBinder;


public class CLIFirstStepFragment extends Fragment implements View.OnClickListener, CLICbxContentBinder.OnCheckboxClickListener {

    private StepNavigatorCallback stepNavigatorCallback;
    private int mSelectedPosition = -1;
    private CLIStepIndicatorActivity mStepIndicatorActivity;
    private CLIStepIndicatorActivity.OnFragmentRefresh onFragmentRefresh;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private FragmentManager fm;
    private ProgressDialogFragment mGetAccountsProgressDialog;

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
    private ConnectionDetector mConnectionDetector;

    public CLIFirstStepFragment() {
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cli_fragment_step_one, container, false);
        mContext = this;
        mWoolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        mConnectionDetector = new ConnectionDetector();
        mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
        setRetainInstance(true);
        initUI();
        setListener();
        setText();
        setDeaBanks();
        return view;
    }

    private void initUI() {
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
        mStepIndicatorActivity = (CLIStepIndicatorActivity) context;
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

        } catch (NullPointerException ignored) {
        }
        if (mConnectionDetector.isOnline(getActivity())) {

            new HttpAsyncTask<String, String, DeaBanks>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
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
                    stopProgressDialog();
                    DeaBanks deaBanks = new DeaBanks();
                    deaBanks.response = new DeaBanksResponse();
                    return deaBanks;
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
                    stopProgressDialog();
                }
            }.execute();
        } else {
            Utils.displayValidationMessage(getActivity(),
                    TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                    getString(R.string.connect_to_server));
        }
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
                        Utils.displayValidationMessage(getActivity(),
                                TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                                getString(R.string.cli_select_bank_error));
                    }
                } else {
                    if (mConnectionDetector.isOnline(getActivity())) {
                        Utils.displayValidationMessage(getActivity(),
                                TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                                getString(R.string.cli_select_bank_error));
                    } else {
                        Utils.displayValidationMessage(getActivity(),
                                TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                                getString(R.string.connect_to_server));
                    }
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
    public void onResume() {
        super.onResume();
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

    @SuppressLint("ValidFragment")
    public CLIFirstStepFragment(StepNavigatorCallback stepNavigatorCallback) {
        this.stepNavigatorCallback = stepNavigatorCallback;
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
}
