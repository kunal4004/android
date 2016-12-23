package za.co.woolworths.financial.services.android.ui.fragments;

/**
 * Created by dimitrij on 2016/12/20.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WOnboardingOnFragmentInteractionListener;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class CLISecondStepFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private WTextView mTextApplySolvency;
    private RadioGroup mRadApplySolvency;
    private RadioButton mRadioYesSolvency;
    private RadioButton mRadioNoSolvency;

    public CLISecondStepFragment() {}
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.cli_fragment_step_two, container, false);
         initUI();
         setListener();
         setText();
        return view;
    }

    private void setListener() {
        mRadioYesSolvency.setOnCheckedChangeListener(this);
        mRadioNoSolvency.setOnCheckedChangeListener(this);
    }

    private void initUI() {
        mTextApplySolvency= (WTextView) view.findViewById(R.id.textApplySolvency);
        mRadApplySolvency =(RadioGroup)view.findViewById(R.id.radApplySolvency);
        mRadioYesSolvency = (RadioButton)view.findViewById(R.id.radioYesSolvency);
        mRadioNoSolvency = (RadioButton)view.findViewById(R.id.radioNoSolvency);
    }

    private  void setText(){
        mTextApplySolvency.setText(getActivity().getResources().getString(R.string.cli_proof_income));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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

}
