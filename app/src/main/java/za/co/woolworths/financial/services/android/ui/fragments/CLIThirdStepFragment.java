package za.co.woolworths.financial.services.android.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

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
import za.co.woolworths.financial.services.android.ui.views.WEmpyViewDialogFragment;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.binder.view.CLIBankAccountTypeBinder;

public class CLIThirdStepFragment extends Fragment implements View.OnClickListener,
        CLIBankAccountTypeBinder.OnCheckboxClickListener, CLIStepIndicatorActivity.OnFragmentRefresh {

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
    protected WTextView mTextIncomeProof;
    private LinearLayout mLinProofLayout;
    private LinearLayout mLinBankLayout;
    public WButton mBtnSendMail;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private String mEmail = "";
    private PopupWindow mDarkenScreen;
    private PopupWindow mPopWindow;
    private FragmentManager fm;
    private WEmpyViewDialogFragment mEmailEmpyViewDialogFragment;
    private ProgressBar mEmailProgressBar;
    private ProgressBar mProgressBar;
    private WEmpyViewDialogFragment mEmpyViewDialogFragment;


    public CLIThirdStepFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = this;
        mWoolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        mConnectionDetector = new ConnectionDetector();
        view = inflater.inflate(R.layout.cli_fragment_step_three, container, false);
        SharePreferenceHelper mSharePreferenceHelper = SharePreferenceHelper.getInstance(getActivity());
        setRetainInstance(true);
        fm = getActivity().getSupportFragmentManager();
        mEmail = mSharePreferenceHelper.getValue("email");
        CLIStepIndicatorActivity mStepIndicator = (CLIStepIndicatorActivity) getActivity();
        mStepIndicator.setOnFragmentRefresh(this);
        mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
        mLinProofLayout = (LinearLayout) view.findViewById(R.id.linProofLayout);
        mLinBankLayout = (LinearLayout) view.findViewById(R.id.linBankLayout);
        mProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
        mEmailProgressBar = (ProgressBar) view.findViewById(R.id.mEmailWoolworthsProgressBar);

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
        boolean isDeaBank = mWoolworthsApplication.isDEABank();
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
    }

    private void setContent() {
        mTextCreditLimit.setText(getString(R.string.cli_account_type));
        mBtnContinue.setText(getString(R.string.cli_complete_process));
        mImgInfo.setVisibility(View.GONE);

    }

    public void getBankAccountTypes() {
        if (mConnectionDetector.isOnline(getActivity())) {
            new HttpAsyncTask<String, String, BankAccountTypes>() {

                @Override
                protected void onPreExecute() {
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
                    return bankAccountTypes;
                }

                @Override
                protected void onPostExecute(BankAccountTypes bankAccountTypes) {
                    super.onPostExecute(bankAccountTypes);
                    if (bankAccountTypes.bankAccountTypes != null) {
                        if (bankAccountTypes.httpCode == 200) {
                            mBankAccountType = bankAccountTypes.bankAccountTypes;
                            mCLIBankAccountAdapter = new CLIBankAccountTypeAdapter(mBankAccountType, mContext);
                            mLayoutManager = new LinearLayoutManager(getActivity());
                            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            mRecycleList.setLayoutManager(mLayoutManager);
                            mRecycleList.setNestedScrollingEnabled(false);
                            mRecycleList.setAdapter(mCLIBankAccountAdapter);
                            mCLIBankAccountAdapter.setCLIContent();
                        } else {
                            mPopWindowValidationMessage.displayValidationMessage(bankAccountTypes.response.desc,
                                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                        }
                    }
                }
            }.execute();
        } else {
            mPopWindowValidationMessage.displayValidationMessage(getString(R.string.cli_enter_acc_number_error),
                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
        }
    }

    //Method to hide keyboard
    public static void hideKeyboard(Context context) {
        InputMethodManager inputManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) context).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinue:
                if (mUpdateBankDetail != null) {
                    if (mUpdateBankDetail.getAccountType() != null) {
                        String accountNumber = mEditAccountNumber.getText().toString();
                        if (!TextUtils.isEmpty(accountNumber)) {
                            String newAccount = accountNumber.replaceAll(" ", "");
                            mUpdateBankDetail.setAccountNumber(newAccount);
                            updateBankDetail();
                            //If everything is ok then hide the keyboard
                            hideKeyboard(getContext());
                        } else {
                            mPopWindowValidationMessage.displayValidationMessage(getString(R.string.cli_enter_acc_number_error),
                                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                        }
                    } else {
                        mPopWindowValidationMessage.displayValidationMessage(getString(R.string.cli_select_acc_type),
                                PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                    }
                } else {
                    if (mConnectionDetector.isOnline(getActivity())) {
                        mPopWindowValidationMessage.displayValidationMessage(getString(R.string.cli_select_acc_type),
                                PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                    } else {
                        mPopWindowValidationMessage.displayValidationMessage(getString(R.string.connect_to_server),
                                PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                    }
                }
                break;

            case R.id.btnSendMail:
                sendEmail();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        stepNavigatorCallback = (CLIFirstStepFragment.StepNavigatorCallback) getActivity();

    }

    public List<IncomeProof> arrIncomeProof() {
        List<IncomeProof> arrIncomeProof = new ArrayList<>();
        String[] mOptionTitle = getResources().getStringArray(R.array.cli_option);
        String[] mOptionDesc = getResources().getStringArray(R.array.cli_option_desc);
        int[] myImageList = new int[]{R.drawable.icon_paperclip, R.drawable.icon_fax};
        int index = 0;
        for (String option : mOptionTitle) {
            arrIncomeProof.add(new IncomeProof(option, mOptionDesc[index], myImageList[index]));
            index++;
        }
        return arrIncomeProof;
    }

    private void initProofUI() {
        mTextIncomeProof = (WTextView) view.findViewById(R.id.textProofIncome);
        mBtnSendMail = (WButton) view.findViewById(R.id.btnSendMail);
    }

    private void setProofListener() {
        mBtnSendMail.setOnClickListener(this);
    }

    private void setProofContent() {
        mTextIncomeProof.setText(getActivity().getResources().getString(R.string.cli_income_proof));
        mBtnSendMail.setText(getString(R.string.cli_send_mail));
    }

    public void populateList() {
        List<IncomeProof> mArrIncomeProof = arrIncomeProof();
        CLIIncomeProofAdapter mClIIncomeProofAdapter = new CLIIncomeProofAdapter(mArrIncomeProof);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mClIIncomeProofAdapter.setCLIContent();
    }

    @Override
    public void refreshFragment() {
        loadView();
    }

    public void sendEmail() {
        if (mConnectionDetector.isOnline(getActivity())) {
            new HttpAsyncTask<String, String, CLIEmailResponse>() {
                @Override
                protected CLIEmailResponse httpDoInBackground(String... params) {
                    return ((WoolworthsApplication) getActivity().getApplication()).getApi().cliEmailResponse();
                }

                @Override
                protected CLIEmailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    stopProgressDialog();
                    CLIEmailResponse cliEmailResponse = new CLIEmailResponse();
                    stopEmailProgressDialog();
                    cliEmailResponse.response = new Response();
                    return cliEmailResponse;
                }

                @Override
                protected Class<CLIEmailResponse> httpDoInBackgroundReturnType() {
                    return CLIEmailResponse.class;
                }

                @Override
                protected void onPreExecute() {
                    showEmailProgressBar();
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(CLIEmailResponse cliEmailResponse) {
                    super.onPostExecute(cliEmailResponse);
                    stopProgressDialog();
                    int httpCode = cliEmailResponse.httpCode;
                    String desc = cliEmailResponse.response.desc;
                    if (httpCode == 200) {
                        popEmail();
                    } else {
                        mPopWindowValidationMessage.displayValidationMessage(desc,
                                PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                    }

                    stopEmailProgressDialog();
                }

            }.execute();
        } else {
            mPopWindowValidationMessage.displayValidationMessage(getString(R.string.connect_to_server),
                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
        }
    }

    public void updateBankDetail() {
        if (mConnectionDetector.isOnline(getActivity())) {
            showProgressBar();
            new HttpAsyncTask<String, String, UpdateBankDetailResponse>() {
                @Override
                protected UpdateBankDetailResponse httpDoInBackground(String... params) {
                    return mWoolworthsApplication.getApi().cliUpdateBankDetail(mUpdateBankDetail);
                }

                @Override
                protected UpdateBankDetailResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    UpdateBankDetailResponse updateBankDetailResponse = new UpdateBankDetailResponse();
                    stopProgressDialog();
                    return updateBankDetailResponse;
                }

                @Override
                protected Class<UpdateBankDetailResponse> httpDoInBackgroundReturnType() {
                    return UpdateBankDetailResponse.class;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(UpdateBankDetailResponse updateBankDetailResponse) {
                    super.onPostExecute(updateBankDetailResponse);
                    if (updateBankDetailResponse != null) {
                        if (updateBankDetailResponse.httpCode == 200) {
                            stepNavigatorCallback.openNextFragment(3);
                        } else {
                            mPopWindowValidationMessage.displayValidationMessage(updateBankDetailResponse.response.desc,
                                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                        }
                    }
                    stopProgressDialog();
                }
            }.execute();
        } else {
            mPopWindowValidationMessage.displayValidationMessage(getString(R.string.cli_enter_acc_number_error),
                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
        }
    }

    public PopupWindow popEmail() {
        //darken the current screen
        View view = getActivity().getLayoutInflater().inflate(R.layout.open_nativemaps_layout, null);
        mDarkenScreen = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDarkenScreen.setAnimationStyle(R.style.Darken_Screen);
        mDarkenScreen.showAtLocation(view, Gravity.CENTER, 0, 0);
        mDarkenScreen.setOutsideTouchable(false);
        //Then popup window appears
        final View popupView = getActivity().getLayoutInflater().inflate(R.layout.cli_email_layout, null);
        WButton mOverlayBtn = (WButton) popupView.findViewById(R.id.btnOk);
        WTextView textEmailContent = (WTextView) popupView.findViewById(R.id.textEmailAddress);
        textEmailContent.setText(mEmail);
        mPopWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setAnimationStyle(R.style.Animations_popup);
        mPopWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        mPopWindow.setOutsideTouchable(false);
        //Dismiss popup when touch outside
        mPopWindow.setTouchable(false);

        mOverlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CLIStepIndicatorActivity cliStepIndicatorActivity = (CLIStepIndicatorActivity) getActivity();
                if (cliStepIndicatorActivity instanceof Activity) {
                    cliStepIndicatorActivity.moveToPage(3);
                }
                mPopWindow.dismiss();
                mDarkenScreen.dismiss();
            }
        });
        return mDarkenScreen;
    }


    public void showProgressBar() {
        mEmpyViewDialogFragment = WEmpyViewDialogFragment.newInstance("blank");
        mEmpyViewDialogFragment.setCancelable(false);
        mEmpyViewDialogFragment.show(fm, "blank");
        mProgressBar.bringToFront();
        mProgressBar.setVisibility(View.VISIBLE);
        mBtnContinue.setVisibility(View.GONE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
    }

    public void stopProgressDialog() {
        if (mEmpyViewDialogFragment != null) {
            if (mEmpyViewDialogFragment.isVisible()) {
                mEmpyViewDialogFragment.dismiss();
            }
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        }
        mBtnContinue.setVisibility(View.VISIBLE);
    }

    public void showEmailProgressBar() {
        mEmailProgressBar.bringToFront();
        mEmailProgressBar.setVisibility(View.VISIBLE);
        mBtnSendMail.setVisibility(View.GONE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        mEmailEmpyViewDialogFragment = WEmpyViewDialogFragment.newInstance("blank");
        mEmailEmpyViewDialogFragment.setCancelable(false);
        mEmailEmpyViewDialogFragment.show(fm, "blank");
        mEmailProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

    }

    public void stopEmailProgressDialog() {
        if (mEmailEmpyViewDialogFragment != null) {
            if (mEmailEmpyViewDialogFragment.isVisible()) {
                mEmailEmpyViewDialogFragment.dismiss();
            }
        }
        if (mEmailProgressBar != null) {
            mEmailProgressBar.setVisibility(View.GONE);
            mEmailProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        }
        mBtnSendMail.setVisibility(View.VISIBLE);
    }
}