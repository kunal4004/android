package za.co.woolworths.financial.services.android.ui.fragments;

/**
 * Created by dimitrij on 2016/12/20.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.CLIStepIndicatorActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class CLISecondStepFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private CLIFirstStepFragment.StepNavigatorCallback stepNavigatorCallback;
    private WTextView mTextApplySolvency;
    private RadioGroup mRadApplySolvency;
    private RadioButton mRadioYesSolvency;
    private RadioButton mRadioNoSolvency;
    private Button mBtnContinue;

    WoolworthsApplication mWoolworthsApplication;
    private CLIStepIndicatorActivity mMain;

    public CLISecondStepFragment() {}
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.cli_fragment_step_two, container, false);
         mWoolworthsApplication = (WoolworthsApplication)getActivity().getApplication();
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
        mTextApplySolvency= (WTextView) view.findViewById(R.id.textApplySolvency);
        mRadApplySolvency =(RadioGroup)view.findViewById(R.id.radApplySolvency);
        mRadioYesSolvency = (RadioButton)view.findViewById(R.id.radioYesSolvency);
        mRadioNoSolvency = (RadioButton)view.findViewById(R.id.radioNoSolvency);
        mBtnContinue =(Button)view.findViewById(R.id.btnContinue);

    }

    private  void setText(){
        mTextApplySolvency.setText(getActivity().getResources().getString(R.string.cli_proof_income));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setTypeface(isChecked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
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

    public void setRadioButtonBold(){
        try {
            RadioButton checked = (RadioButton) view.findViewById(mRadApplySolvency.getCheckedRadioButtonId());
            checked.setTypeface(Typeface.DEFAULT_BOLD);
        }catch (NullPointerException ex){}
        try {
            RadioButton checked = (RadioButton) view.findViewById(mRadApplySolvency.getCheckedRadioButtonId());
            checked.setTypeface(Typeface.DEFAULT_BOLD);
        }catch (NullPointerException ex){}
    }

    public String selectedRadioGroup(RadioGroup radioGroup) {
        int radioID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioID);
        String selectedConfidentialCredit = (String) radioButton.getText();
        return selectedConfidentialCredit;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnContinue:
                String selectedRadSolvency = selectedRadioGroup(mRadApplySolvency);
                if(selectedRadSolvency.equalsIgnoreCase("YES")){
                    mWoolworthsApplication.setDEABank(true);
                    mMain.refresh();
                    stepNavigatorCallback.openNextFragment(2);
                }else{
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
        stepNavigatorCallback = (CLIFirstStepFragment.StepNavigatorCallback)getActivity();
        mMain = (CLIStepIndicatorActivity) getActivity();
    }


}
