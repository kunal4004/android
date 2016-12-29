package za.co.woolworths.financial.services.android.ui.fragments;

/**
 * Created by dimitrij on 2016/12/20.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.BankAccountResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.IncomeProof;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
import za.co.woolworths.financial.services.android.ui.adapters.CLIBankAccountTypeAdapter;
import za.co.woolworths.financial.services.android.ui.adapters.CLIIncomeProofAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.AccountNumberFormatWatcher;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.DividerItemDecoration;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.SlidingUpViewLayout;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.binder.view.CLIBankAccountTypeBinder;

public class CLIThirdStepFragment extends Fragment implements View.OnClickListener,
        CLIBankAccountTypeBinder.OnCheckboxClickListener, CLIStepIndicatorActivity.OnFragmentRefresh {

    private boolean isDeaBank = false;
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
    private ConnectionDetector mConnectionDetector;
    private WEditTextView mEditAccountNumber;
    private List<IncomeProof> mArrIncomeProof;
    private CLIIncomeProofAdapter mClIIncomeProofAdapter;
    private WTextView mTextIncomeProof;
    private WTextView mTextProofIncomeSize;
    private LinearLayout mLinProofLayout;
    private LinearLayout mLinBankLayout;
    private RecyclerView mRecycleProofIncome;
    private CLIStepIndicatorActivity mStepIndicator;
    private WButton mBtnSendMail;
    private SlidingUpViewLayout mSlidingUpViewLayout;
    private ProgressDialog mPostEmailProgressDialog;
    private WTextView mTextEmailAdress;
    private String mEmail = "";
    private ProgressDialog mUpdateBankDetailProgressDialog;

    public CLIThirdStepFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = this;
        mWoolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        mConnectionDetector = new ConnectionDetector();
        view = inflater.inflate(R.layout.cli_fragment_step_three, container, false);
        mEmail = SharePreferenceHelper.getInstance().getValue(getActivity(),"email");
        mStepIndicator = (CLIStepIndicatorActivity) getActivity();
        mStepIndicator.setOnFragmentRefresh(this);
        mSlidingUpViewLayout = new SlidingUpViewLayout(getActivity());

        mLinProofLayout = (LinearLayout) view.findViewById(R.id.linProofLayout);
        mLinBankLayout = (LinearLayout) view.findViewById(R.id.linBankLayout);

        initUI();
        setListener();
        setContent();
        getBankAccountTypes();

        initProofUI();
        setProofListener();
        populateList();
        setProofContent();

        loadView();
        return view;
    }

    public void loadView() {
        isDeaBank = mWoolworthsApplication.isDEABank();
        if (isDeaBank) {
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
        mEditAccountNumber.addTextChangedListener(new AccountNumberFormatWatcher(getActivity(), mEditAccountNumber));
    }

    private void setContent() {
        mTextCreditLimit.setText(getString(R.string.cli_account_type));
        mBtnContinue.setText(getString(R.string.cli_complete_process));
        mImgInfo.setVisibility(View.GONE);

    }

    public void getBankAccountTypes() {
        if (mConnectionDetector.isOnline()) {
            new HttpAsyncTask<String, String, BankAccountTypes>() {

                @Override
                protected void onPreExecute() {
//                    mGetBankDetailProgressDialog = new ProgressDialog(getActivity());
//                    mGetBankDetailProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.loading), 1, getActivity()));
//                    mGetBankDetailProgressDialog.setCancelable(false);
//                    mGetBankDetailProgressDialog.show();
                    super.onPreExecute();
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
                    stopProgressDialog();
                    return bankAccountTypes;
                }

                @Override
                protected void onPostExecute(BankAccountTypes bankAccountTypes) {
                    super.onPostExecute(bankAccountTypes);
                    if (bankAccountTypes.bankAccountTypes != null) {
                        mBankAccountType = bankAccountTypes.bankAccountTypes;
                        mCLIBankAccountAdapter = new CLIBankAccountTypeAdapter(mBankAccountType, mContext);
                        mLayoutManager = new LinearLayoutManager(getActivity());
                        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        mRecycleList.setLayoutManager(mLayoutManager);
                        mRecycleList.setNestedScrollingEnabled(false);
                        mRecycleList.setAdapter(mCLIBankAccountAdapter);
                        mCLIBankAccountAdapter.setCLIContent();
                    } else {
                    }
                    stopProgressDialog();
                }
            }.execute();
        } else {
            WErrorDialog.getErrConnectToServer(getActivity());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinue:
                if (mUpdateBankDetail != null) {
                    if (mUpdateBankDetail.getAccountType() != null) {
                        String accountNumber = mEditAccountNumber.getText().toString();
                        if (!TextUtils.isEmpty(accountNumber)) {
                            String newAccount = accountNumber.replaceAll(" ","");
                            mUpdateBankDetail.setAccountNumber(newAccount);
                            updateBankDetail();
                        } else {
                            WErrorDialog.setErrorMessage(getActivity(), getString(R.string.cli_enter_acc_number_error));
                        }
                    } else {
                        WErrorDialog.setErrorMessage(getActivity(), getString(R.string.cli_select_acc_type));
                    }
                } else {
                    if (mConnectionDetector.isOnline()) {
                        WErrorDialog.setErrorMessage(getActivity(), getString(R.string.cli_select_acc_type));
                    } else {
                        WErrorDialog.getErrConnectToServer(getActivity());
                    }
                }
                break;

            case R.id.btnSendMail:
                sendEmail(mEmail);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

    public void stopProgressDialog() {
//        if(mGetBankDetailProgressDialog != null && mGetBankDetailProgressDialog.isShowing()){
//            mGetBankDetailProgressDialog.dismiss();
//        }
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
        int[] myImageList = new int[]{R.drawable.icon_paperclip, R.drawable.icon_clip, R.drawable.icon_fax};
        int index = 0;
        for (String option : mOptionTitle) {
            arrIncomeProof.add(new IncomeProof(option, mOptionDesc[index], myImageList[index]));
            index++;
        }
        return arrIncomeProof;
    }

    private void initProofUI() {
        mRecycleProofIncome = (RecyclerView) view.findViewById(R.id.recycleProofIncome);
        mTextIncomeProof = (WTextView) view.findViewById(R.id.textProofIncome);
        mTextProofIncomeSize = (WTextView) view.findViewById(R.id.textProofIncomeSize);
        mTextEmailAdress = (WTextView) view.findViewById(R.id.textEmailAdress);
        mBtnSendMail = (WButton) view.findViewById(R.id.btnSendMail);
    }

    private void setProofListener() {
        mBtnSendMail.setOnClickListener(this);
    }

    private void setProofContent() {
        mTextIncomeProof.setText(getActivity().getResources().getString(R.string.cli_income_proof));
        mBtnSendMail.setText(getString(R.string.cli_send_mail));
        mTextProofIncomeSize.setText(getString(R.string.cli_send_document_title).replace("%s", String.valueOf(arrIncomeProof().size())));
        mTextEmailAdress.setText(mEmail);
    }

    public void populateList() {
        mArrIncomeProof = arrIncomeProof();
        mClIIncomeProofAdapter = new CLIIncomeProofAdapter(mArrIncomeProof);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleProofIncome.setLayoutManager(mLayoutManager);
        mRecycleProofIncome.setNestedScrollingEnabled(false);
        mRecycleProofIncome.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecycleProofIncome.setAdapter(mClIIncomeProofAdapter);
        mClIIncomeProofAdapter.setCLIContent();

    }

    @Override
    public void refreshFragment() {
        loadView();
    }

    public void sendEmail(final String userEmail) {
        if (mConnectionDetector.isOnline()) {
            new HttpAsyncTask<String, String, CLIEmailResponse>() {
                @Override
                protected CLIEmailResponse httpDoInBackground(String... params) {
                    return ((WoolworthsApplication) getActivity().getApplication()).getApi().cliEmailResponse(userEmail);
                }

                @Override
                protected CLIEmailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    stopEmailProgressDialog();
                    CLIEmailResponse cliEmailResponse = new CLIEmailResponse();
                    cliEmailResponse.response = new Response();
                    return cliEmailResponse;
                }

                @Override
                protected Class<CLIEmailResponse> httpDoInBackgroundReturnType() {
                    return CLIEmailResponse.class;
                }

                @Override
                protected void onPreExecute() {
                    mPostEmailProgressDialog = new ProgressDialog(getActivity());
                    mPostEmailProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.cli_sending_email), 1, getActivity()));
                    mPostEmailProgressDialog.setCancelable(false);
                    mPostEmailProgressDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(CLIEmailResponse cliEmailResponse) {
                    super.onPostExecute(cliEmailResponse);
                    stopEmailProgressDialog();
                    int httpCode = cliEmailResponse.httpCode;
                    String desc = cliEmailResponse.response.desc;
                    if (httpCode == 200) {
                        mSlidingUpViewLayout.openOverlayView(mEmail, SlidingUpViewLayout.OVERLAY_TYPE.EMAIL);
                    } else {
                        WErrorDialog.setErrorMessage(getActivity(), desc);
                    }
                }
            }.execute();
        } else {
            WErrorDialog.getErrConnectToServer(getActivity());
        }
    }

    public void stopEmailProgressDialog() {
        if (mPostEmailProgressDialog != null && mPostEmailProgressDialog.isShowing()) {
            mPostEmailProgressDialog.dismiss();
        }
    }

    public void updateBankDetail() {

        if (mConnectionDetector.isOnline()) {
            new HttpAsyncTask<String, String, UpdateBankDetailResponse>() {
                @Override
                protected UpdateBankDetailResponse httpDoInBackground(String... params) {
                    return mWoolworthsApplication.getApi().cliUpdateBankDetail(mUpdateBankDetail);
                }

                @Override
                protected UpdateBankDetailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    UpdateBankDetailResponse updateBankDetailResponse = new UpdateBankDetailResponse();
                    stopUpadateBankProgressDialog();
                    return updateBankDetailResponse;
                }

                @Override
                protected Class<UpdateBankDetailResponse> httpDoInBackgroundReturnType() {
                    return UpdateBankDetailResponse.class;
                }

                @Override
                protected void onPreExecute() {
                    mUpdateBankDetailProgressDialog = new ProgressDialog(getActivity());
                    mUpdateBankDetailProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.cli_updating_bank_detail), 1, getActivity()));
                    mUpdateBankDetailProgressDialog.setCancelable(false);
                    mUpdateBankDetailProgressDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(UpdateBankDetailResponse updateBankDetailResponse) {
                    super.onPostExecute(updateBankDetailResponse);

                    if (updateBankDetailResponse!=null) {
                        if (updateBankDetailResponse.httpCode == 200) {
                            stepNavigatorCallback.openNextFragment(3);
                        } else {
                            WErrorDialog.setErrorMessage(getActivity(), updateBankDetailResponse.response.desc);
                        }
                    }
                    stopUpadateBankProgressDialog();
                }
            }.execute();
        }else{
            WErrorDialog.getErrConnectToServer(getActivity());
        }
    }

    public void stopUpadateBankProgressDialog() {
        if (mUpdateBankDetailProgressDialog != null && mUpdateBankDetailProgressDialog.isShowing()) {
            mUpdateBankDetailProgressDialog.dismiss();
        }
    }

}