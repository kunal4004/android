package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;

public class CLISecondStepFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private CLIFirstStepFragment.StepNavigatorCallback stepNavigatorCallback;
    private WTextView mTextApplySolvency;
    private RadioGroup mRadApplySolvency;
    private RadioButton mRadioYesSolvency;
    private RadioButton mRadioNoSolvency;
    private Button mBtnContinue;
    final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    WoolworthsApplication mWoolworthsApplication;
    private CLIStepIndicatorActivity mMain;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private Typeface mRdioGroupTypeFace;
    private Typeface mRdioGroupTypeFaceBold;

    public CLISecondStepFragment() {
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cli_fragment_step_two, container, false);
        mWoolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
        mRdioGroupTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/WFutura-Medium.ttf");
        mRdioGroupTypeFaceBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/WFutura-SemiBold.ttf");

        initUI();
        setListener();
        setText();
        return view;
    }

    private void setListener() {
        mRadioYesSolvency.setOnCheckedChangeListener(this);
        mRadioNoSolvency.setOnCheckedChangeListener(this);
        mBtnContinue.setOnClickListener(this);
    }

    private void initUI() {
        mTextApplySolvency = (WTextView) view.findViewById(R.id.textApplySolvency);
        mRadApplySolvency = (RadioGroup) view.findViewById(R.id.radApplySolvency);
        mRadioYesSolvency = (RadioButton) view.findViewById(R.id.radioYesSolvency);
        mRadioNoSolvency = (RadioButton) view.findViewById(R.id.radioNoSolvency);
        mBtnContinue = (Button) view.findViewById(R.id.btnContinue);

    }

    private void setText() {
        mTextApplySolvency.setText(getActivity().getResources().getString(R.string.cli_proof_income));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setTypeface(isChecked ? mRdioGroupTypeFaceBold : mRdioGroupTypeFace);
    }

    @Override
    public void onResume() {
        super.onResume();
        setRadioButtonBold();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setRadioButtonBold() {
        try {
            RadioButton checked = (RadioButton) view.findViewById(mRadApplySolvency.getCheckedRadioButtonId());
            checked.setTypeface(mRdioGroupTypeFace);
        } catch (NullPointerException ignored) {
        }

        try {
            RadioButton checked = (RadioButton) view.findViewById(mRadApplySolvency.getCheckedRadioButtonId());
            checked.setTypeface(mRdioGroupTypeFace);
        } catch (NullPointerException ignored) {
        }
    }

    public String selectedRadioGroup(RadioGroup radioGroup) {
        int radioID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioID);
        return (String) radioButton.getText();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinue:
                mBtnContinue.startAnimation(buttonClick);
                if (mRadApplySolvency.getCheckedRadioButtonId() == -1) {
                    Utils.displayValidationMessage(getActivity(),
                            TransientActivity.VALIDATION_MESSAGE_LIST.ERROR,
                            getString(R.string.cli_check_field));
                    return;
                }

                String selectedRadSolvency = selectedRadioGroup(mRadApplySolvency);
                if (selectedRadSolvency.equalsIgnoreCase("YES")) {
                    mWoolworthsApplication.setDEABank(true);
                    mMain.refresh();
                    stepNavigatorCallback.openNextFragment(2);
                } else {
                    mWoolworthsApplication.setDEABank(false);
                    mMain.refresh();
                    stepNavigatorCallback.openNextFragment(2);
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        stepNavigatorCallback = (CLIFirstStepFragment.StepNavigatorCallback) getActivity();
        mMain = (CLIStepIndicatorActivity) getActivity();
    }
}
