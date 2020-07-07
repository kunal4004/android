package za.co.woolworths.financial.services.android.util.spannable;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.wenum.LinkType;

public class WSpannableStringBuilder extends SpannableStringBuilder {

    private Map<String, List<WSpannableAttribute>> attributes = new HashMap<>();

    public WSpannableStringBuilder(CharSequence charSequence){
        super(charSequence, 0, charSequence.length());
    }

    public void makeStringUnderlined(String stringToUnderline){

        make(stringToUnderline, new UnderlineSpan());
    }

    public void makeStringInteractable(final String stringToMakeInteractable, LinkType linkType){
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (linkType == LinkType.EMAIL){
                    Utils.sendEmail(stringToMakeInteractable);
                } else if (linkType == LinkType.PHONE){

                    StringBuilder stringBuilder = new StringBuilder(stringToMakeInteractable.trim());

                    if (stringBuilder.charAt(0) == '0'){
                        stringBuilder.replace(0, 1, "");
                        stringBuilder.insert(0, "+27");
                    }

                    Utils.makeCall(stringBuilder.toString());
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };

        make(stringToMakeInteractable, clickableSpan);
    }

    private void make(String string, Object span){

        int startIndex = this.toString().indexOf(string, 0);

        while (startIndex != -1) {
            int endIndex = startIndex + string.length();

            if (attributes.get(string) == null) {
                List<WSpannableAttribute> wSpannableAttributeList = new ArrayList<>();
                wSpannableAttributeList.add(new WSpannableAttribute(startIndex, endIndex));
                attributes.put(string, wSpannableAttributeList);
            }

            boolean isNewEntryAdded = false;

            for (int i = 0, l = attributes.get(string).size(); i < l; i++) {
                WSpannableAttribute wSpannableAttribute = attributes.get(string).get(i);
                if (wSpannableAttribute.startIndex == startIndex && wSpannableAttribute.endIndex == endIndex) {
                    attributes.get(string).get(i).span.add(span);
                    isNewEntryAdded = true;
                    break;
                }
            }

            if (!isNewEntryAdded) {
                attributes.get(string).get(0).span.add(span);
            }

            startIndex = this.toString().indexOf(string, endIndex);
        }
    }

    public SpannableString build(){

        for(String key : this.attributes.keySet()){

            for(WSpannableAttribute wSpannableAttribute : this.attributes.get(key)){
                SpannableString spannableString = new SpannableString(key);
                for (Object span : wSpannableAttribute.span){
                    spannableString.setSpan(span, 0, key.length(), 0);
                }

                this.replace(wSpannableAttribute.startIndex, wSpannableAttribute.endIndex, spannableString);
            }
        }

        return SpannableString.valueOf(this);
    }
}
