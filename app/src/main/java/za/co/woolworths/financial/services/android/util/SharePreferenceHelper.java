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



    public static SharePreferenceHelper getInstance()
    {
        if (sharedPreference == null)
        {
            sharedPreference = new SharePreferenceHelper();
        }
        return sharedPreference;
    }

    public SharePreferenceHelper() {
        super();
    }

    public void save(Context context, String text , String Key) {
        SharedPreferences settings;
        Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putString(Key, text); //3

        editor.commit(); //4
    }

    public String getValue(Context context , String Key) {
        SharedPreferences settings;
        String text = "";
        //  settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(Key, "");
        return text;
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings;
        Editor editor;

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.clear();
        editor.commit();
    }

    public void removeValue(Context context , String value) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.remove(value);
        editor.commit();
    }
}
