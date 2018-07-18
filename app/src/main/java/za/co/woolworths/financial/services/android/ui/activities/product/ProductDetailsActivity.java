package za.co.woolworths.financial.services.android.ui.activities.product;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragmentNew;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment;
import za.co.woolworths.financial.services.android.util.Utils;

public class ProductDetailsActivity extends AppCompatActivity {

	ProductDetailsFragmentNew productDetailsFragmentNew;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this);
		setContentView(R.layout.product_details_activity);
		Bundle bundle = getIntent().getExtras();
		productDetailsFragmentNew = new ProductDetailsFragmentNew();
		productDetailsFragmentNew.setArguments(bundle);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, productDetailsFragmentNew).commit();
	}
}
