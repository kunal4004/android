package za.co.woolworths.financial.services.android.ui.activities.product;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.awfs.coordination.R;
import com.google.gson.JsonElement;

import org.jetbrains.annotations.Nullable;

import za.co.woolworths.financial.services.android.contracts.IToastInterface;
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment;
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList;
import za.co.woolworths.financial.services.android.ui.views.ToastFactory;
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView;
import za.co.woolworths.financial.services.android.util.Utils;

import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_REQUEST_CODE;
import static za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE;

public class ProductDetailsActivity extends AppCompatActivity implements IToastInterface {

    ProductDetailsFragment productDetailsFragmentNew;
    public static WMaterialShowcaseView walkThroughPromtView = null;
    private FrameLayout flContentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.product_details_activity);
        Bundle bundle = getIntent().getExtras();
        flContentFrame = findViewById(R.id.content_frame);
        productDetailsFragmentNew = ProductDetailsFragment.Companion.newInstance();
        productDetailsFragmentNew.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, productDetailsFragmentNew).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TO_SHOPPING_LIST_REQUEST_CODE
                && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
                ToastFactory.Companion.buildShoppingListToast(this,flContentFrame, true, data, this);
                return;
            }

        if (productDetailsFragmentNew != null)
            productDetailsFragmentNew.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (walkThroughPromtView != null && !walkThroughPromtView.isDismissed()) {
            walkThroughPromtView.hide();
            return;
        }
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (productDetailsFragmentNew != null)
            productDetailsFragmentNew.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onToastButtonClicked(@Nullable JsonElement jsonElement) {
        NavigateToShoppingList.Companion navigateTo = NavigateToShoppingList.Companion;
        if (jsonElement != null)
            navigateTo.navigateToShoppingListOnToastClicked(this, jsonElement);
    }
}
