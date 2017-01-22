package za.co.woolworths.financial.services.android.ui.fragments;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.awfs.coordination.R;


public class DialogFragmentHelper extends DialogFragment{
    public static DialogFragmentHelper newInstance(String msg){
        DialogFragmentHelper f = new DialogFragmentHelper();
        Bundle args = new Bundle();
        args.putString("msg",msg);
        f.setArguments(args);
        return f;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.dialog, container, false);
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        String msg = getArguments().getString("msg");

        return v;
    }
}