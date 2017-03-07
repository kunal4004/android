package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.DialogFragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.DrawImage;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.animation.BlurDialogFragment;

public class AddToShoppingListFragment extends BlurDialogFragment implements View.OnClickListener {

    private String productId;
    private String productName;
    private String externalImageRef;

    public AddToShoppingListFragment() {
    }

    public static AddToShoppingListFragment newInstance(String productId, String productName, String externalImageRef) {
        AddToShoppingListFragment frag = new AddToShoppingListFragment();
        Bundle args = new Bundle();
        args.putString("productId", productId);
        args.putString("productName", productName);
        args.putString("externalImageRef", externalImageRef);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FragmentDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getDialog().getWindow().setBackgroundDrawableResource(
                R.color.semi_per_black);
        return inflater.inflate(R.layout.add_shopping_list_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            productId = bundle.getString("productId");
            productName = bundle.getString("productName");
            externalImageRef = bundle.getString("externalImageRef");
        }

        ImageView imgShoppingList = (ImageView) view.findViewById(R.id.imgShoppingList);
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        DrawImage drawImage = new DrawImage(getActivity());
        drawImage.displayImage(imgShoppingList, externalImageRef+"?w="+width);

        WButton wAddToShoppingCart = (WButton) view.findViewById(R.id.btnAddShoppingList);
        wAddToShoppingCart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddShoppingList:
                Utils.addToShoppingCart(getActivity(), new ShoppingList(
                        productId,
                        productName, false));

                Utils.displayValidationMessage(getActivity(),
                        TransientActivity.VALIDATION_MESSAGE_LIST.SHOPPING_LIST_INFO,
                        "viewShoppingList");
                break;
        }
    }
}