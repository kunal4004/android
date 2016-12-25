package za.co.woolworths.financial.services.android.ui.fragments;

/**
 * Created by DimitriJ on 2016/12/20.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.IncomeProof;
import za.co.woolworths.financial.services.android.ui.adapters.CLIIncomeProofAdapter;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.DividerItemDecoration;


public class CLIIncomeProofFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mRecycleList;
    private WTextView mTextIncomeProof;
    private RelativeLayout relButtonCLIDeaBank;
    private LinearLayoutManager mLayoutManager;
    private WButton mBtnContinue;
    private CLIIncomeProofFragment mContext;
    private CLIIncomeProofAdapter mClIIncomeProofAdapter;
    private List<IncomeProof> mArrIncomeProof;
    private WTextView mTextProofIncomeSize;

    public CLIIncomeProofFragment() {}

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.cli_fragment_income_proof, container, false);
        mContext = this;
        initUI();
        setListener();
        populateList();
        setContent();
        return view;
    }

    private void initUI() {
        mRecycleList = (RecyclerView) view.findViewById(R.id.recycleList);
        mTextIncomeProof = (WTextView)view.findViewById(R.id.textProofIncome);
        mTextProofIncomeSize = (WTextView)view.findViewById(R.id.textProofIncomeSize);
        relButtonCLIDeaBank = (RelativeLayout)view.findViewById(R.id.relButtonCLIDeaBank);
        mBtnContinue=(WButton)view.findViewById(R.id.btnContinue);
    }

    private void setListener(){
        relButtonCLIDeaBank.setOnClickListener(this);
        mBtnContinue.setOnClickListener(this);
    }

    private void setContent(){
        mTextIncomeProof.setText(getActivity().getResources().getString(R.string.cli_income_proof));
        mBtnContinue.setText(getString(R.string.cli_send_mail));
        mTextProofIncomeSize.setText(getString(R.string.cli_send_document_title).replace("%s",String.valueOf(arrIncomeProof().size())));
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnContinue:


                break;
        }
    }

    public void populateList(){
        mArrIncomeProof =  arrIncomeProof();
        mClIIncomeProofAdapter = new CLIIncomeProofAdapter(mArrIncomeProof);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleList.setLayoutManager(mLayoutManager);
        mRecycleList.setNestedScrollingEnabled(false);
        mRecycleList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecycleList.setAdapter(mClIIncomeProofAdapter);
        mClIIncomeProofAdapter.setCLIContent();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public List<IncomeProof> arrIncomeProof(){
        List<IncomeProof>arrIncomeProof = new ArrayList<>();
        String[] mOptionTitle = getResources().getStringArray(R.array.cli_option);
        String[] mOptionDesc = getResources().getStringArray(R.array.cli_option_desc);
        int[] myImageList = new int[]{R.drawable.icon_paperclip, R.drawable.icon_clip,R.drawable.icon_fax};
        int index=0;
        for (String option: mOptionTitle){
            arrIncomeProof.add(new IncomeProof(option,mOptionDesc[index],myImageList[index]));
            index++;
        }
        return arrIncomeProof;
    }


}
