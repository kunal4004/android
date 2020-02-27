package za.co.woolworths.financial.services.android.util.spannable;

import java.util.ArrayList;
import java.util.List;

class WSpannableAttribute{
    final List<Object> span = new ArrayList();
    final int startIndex;
    final int endIndex;

    public WSpannableAttribute(int startIndex, int endIndex){
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
}