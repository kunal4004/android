package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WOnboardingOnFragmentInteractionListener;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class WOnboardingThreeFragment extends Fragment {

    private WOnboardingOnFragmentInteractionListener mListener;
    private WTextView descriptionTitle;
    private WTextView descriptionText;

    public WOnboardingThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wonboarding_three, container, false);
        this.descriptionTitle = (WTextView)view.findViewById(R.id.fragment_wonboarding_three_description_title);
        this.descriptionText = (WTextView)view.findViewById(R.id.fragment_wonboarding_three_description_text);
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
