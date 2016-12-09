package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WOnboardingOnFragmentInteractionListener;

public class WOnboardingOneFragment extends Fragment {

    private WOnboardingOnFragmentInteractionListener mListener;
    private TextView descriptionTitle;
    private TextView descriptionText;

    public WOnboardingOneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wonboarding_one, container, false);
        this.descriptionTitle = (TextView)view.findViewById(R.id.fragment_wonboarding_one_description_title);
        this.descriptionText = (TextView)view.findViewById(R.id.fragment_wonboarding_one_description_text);

        this.descriptionTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/WFutura-SemiBold.ttf"));
        this.descriptionText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro-Regular.otf"));
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WOnboardingOnFragmentInteractionListener) {
            mListener = (WOnboardingOnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
