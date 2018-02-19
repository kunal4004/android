package za.co.woolworths.financial.services.android.util;


import java.util.ArrayList;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.OtherSkus;

public interface ColorInterface {
	void onUpdate(ArrayList<OtherSkus> otherSkuList, String viewType);

	void onUpdate(List<Integer> quantityList);
}
