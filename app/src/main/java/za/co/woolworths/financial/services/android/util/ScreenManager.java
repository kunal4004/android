package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.OnboardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;

/**
 * Created by eesajacobs on 2016/11/30.
 */

public class ScreenManager {

    public static void presentSSOSignin(Activity activity){

        Intent intent = new Intent(activity, SSOActivity.class);
        intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
        intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
        intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue());

        activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
    }

    public static void presentSSORegister(Activity activity){

        Intent intent = new Intent(activity, SSOActivity.class);
        intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
        intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
        intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.REGISTER.rawValue());

        activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
    }

    public static void presentOnboarding(Activity activity){

        Intent intent = new Intent(activity, OnboardingActivity.class);

        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }
}
