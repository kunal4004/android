package za.co.woolworths.financial.services.android.util;


import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

public abstract class AbstractFragmentListener extends Fragment {

    protected IActivityEnabledListener aeListener;

    protected interface IActivityEnabledListener {
        void onActivityEnabled(AppCompatActivity activity);
    }

    protected void getAvailableActivity(IActivityEnabledListener listener) {
        if (getActivity() == null) {
            aeListener = listener;

        } else {
            listener.onActivityEnabled((AppCompatActivity) getActivity());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (aeListener != null) {
            aeListener.onActivityEnabled((AppCompatActivity) activity);
            aeListener = null;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (aeListener != null) {
            aeListener.onActivityEnabled((AppCompatActivity) context);
            aeListener = null;
        }
    }
}