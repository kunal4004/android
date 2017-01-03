package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by dimitrij on 2016/12/19.
 */

public class CLI {

    private int mImage;
    private int mColor;
    private String mTitle;
    private String mSubTitle;
    private String mBoldText;
    private String mDescription;

    public CLI(int image, int mColor,String title, String subTitle, String boldText, String description) {
        this.mImage = image;
        this.mColor = mColor;
        this.mTitle = title;
        this.mSubTitle = subTitle;
        this.mBoldText = boldText;
        this.mDescription = description;
    }

    public int getmImage() {
        return mImage;
    }

    public int getmColor() {return mColor;}

    public String getmTitle() {
        return mTitle;
    }

    public String getmSubTitle() {
        return mSubTitle;
    }

    public String getmBoldText() {
        return mBoldText;
    }

    public String getmDescription() {
        return mDescription;
    }
}
