package za.co.woolworths.financial.services.android.util;

/**
 * Created by dimitrij on 2016/12/29.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePreferenceHelper {

    private static SharePreferenceHelper sharedPreference;
    public static final String PREFS_NAME = "AOP_PREFS";
    public static final String PREFS_KEY = "AOP_PREFS_String";
    private static Context mContext;

    public static SharePreferenceHelper getInstance(Context context)
    {
        if (sharedPreference == null)
        {
            sharedPreference = new SharePreferenceHelper();
        }

        mContext = context;
        return sharedPreference;
    }

    public SharePreferenceHelper() {
        super();
    }

    public void save(String text , String Key) {
        SharedPreferences settings;
        Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putString(Key, text); //3

        editor.commit(); //4
    }

    public String getValue(String Key) {
        SharedPreferences settings;
        String text = "";
        //  settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(Key, "");
        return text;
    }

    public void clearSharedPreference() {
        SharedPreferences settings;
        Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.clear();
        editor.commit();
    }

    public void removeValue(String value) {
        SharedPreferences settings;
        Editor editor;

        settings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.remove(value);
        editor.commit();
    }

    public class getInstance extends SharePreferenceHelper {
    }
}
