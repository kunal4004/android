package za.co.woolworths.financial.services.android.util;

import android.support.v4.app.Fragment;

import com.awfs.coordination.R;

public class BaseFragment extends Fragment {

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        overridePendingTransitionExit();
//    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        overridePendingTransitionEnter();
//    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        getActivity().overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

}