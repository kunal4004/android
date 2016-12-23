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

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.BankAccountResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountType;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.ui.adapters.CLIBankAccountTypeAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.FourDigitCardFormatWatcher;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.binder.view.CLIBankAccountTypeBinder;


public class CLIThirdStepFragment extends Fragment implements View.OnClickListener, CLIBankAccountTypeBinder.OnCheckboxClickListener {

    private View view;
    private WTextView mTextCreditLimit;
    private RecyclerView mRecycleList;
    private WButton mBtnContinue;
    private ImageView mImgInfo;
    private List<BankAccountType> mBankAccountType;
    private LinearLayoutManager mLayoutManager;
    private CLIThirdStepFragment mContext;
    private CLIBankAccountTypeAdapter mCLIBankAccountAdapter;
    private ProgressDialog mGetBankDetailProgressDialog;
    private UpdateBankDetail mUpdateBankDetail;
    private WoolworthsApplication mWoolworthsApplication;
    private ConnectionDetector mConnectionDetector;
    private WEditTextView mEditAccountNumber;

    public CLIThirdStepFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.cli_fragment_step_three, container, false);
         mContext = this;
         mWoolworthsApplication = (WoolworthsApplication)getActivity().getApplication();
         mConnectionDetector = new ConnectionDetector();

        initUI();
        setListener();
        setContent();
        getBankAccountTypes();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void initUI() {
        mRecycleList = (RecyclerView) view.findViewById(R.id.recycleList);
        mTextCreditLimit = (WTextView)view.findViewById(R.id.textACreditLimit);
        mBtnContinue=(WButton)view.findViewById(R.id.btnContinue);
        mImgInfo = (ImageView)view.findViewById(R.id.imgInfo);
        mEditAccountNumber=(WEditTextView) view.findViewById(R.id.wEditAccountNumber);
    }

    private void setListener(){
        mBtnContinue.setOnClickListener(this);
        mEditAccountNumber.addTextChangedListener(new FourDigitCardFormatWatcher());
    }

    private void setContent(){
        mTextCreditLimit.setText(getString(R.string.cli_account_type));
        mBtnContinue.setText(getString(R.string.cli_complete_process));
        mImgInfo.setVisibility(View.GONE);

    }

    public void getBankAccountTypes(){
        ConnectionDetector connectionDetector = new ConnectionDetector();
        if(connectionDetector.isOnline()) {
            new HttpAsyncTask<String, String, BankAccountTypes>() {

                @Override
                protected void onPreExecute() {
                    mGetBankDetailProgressDialog = new ProgressDialog(getActivity());
                    mGetBankDetailProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.loading), 1, getActivity()));
                    mGetBankDetailProgressDialog.setCancelable(false);
                    mGetBankDetailProgressDialog.show();
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
                    if (bankAccountTypes.bankAccountTypes!=null) {
                        mBankAccountType = bankAccountTypes.bankAccountTypes;
                        mCLIBankAccountAdapter = new CLIBankAccountTypeAdapter(mBankAccountType,mContext);
                        mLayoutManager = new LinearLayoutManager(getActivity());
                        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        mRecycleList.setLayoutManager(mLayoutManager);
                        mRecycleList.setNestedScrollingEnabled(false);
                        mRecycleList.setAdapter(mCLIBankAccountAdapter);
                        mCLIBankAccountAdapter.setCLIContent();
                    }else {
                    }
                    stopProgressDialog();
                }
            }.execute();
        }else{
            WErrorDialog.getErrConnectToServer(getActivity());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnContinue:
                if(mUpdateBankDetail!=null){
                    if(mUpdateBankDetail.getAccountType()!=null){
                        String accountNumber = mEditAccountNumber.getText().toString();
                        if(!TextUtils.isEmpty(accountNumber)){
                            mUpdateBankDetail.setAccountNumber(accountNumber);
                            // set api call

                        }else{
                            WErrorDialog.setErrorMessage(getActivity(),getString(R.string.cli_enter_acc_number_error));
                        }
                    }else {
                        WErrorDialog.setErrorMessage(getActivity(),getString(R.string.cli_select_acc_type));
                    }
                }else{
                    if(mConnectionDetector.isOnline()){
                        WErrorDialog.setErrorMessage(getActivity(),getString(R.string.cli_select_acc_type));
                    }else{
                        WErrorDialog.getErrConnectToServer(getActivity());
                    }
                }
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
        if (mBankAccountType!=null){
            mUpdateBankDetail=mWoolworthsApplication.updateBankDetail;
            if(mUpdateBankDetail!=null) {
                mUpdateBankDetail.setAccountType(mBankAccountType.get(position).accountType);
            }
        }
    }

    public void stopProgressDialog(){
        if(mGetBankDetailProgressDialog != null && mGetBankDetailProgressDialog.isShowing()){
            mGetBankDetailProgressDialog.dismiss();
        }
    }
}
