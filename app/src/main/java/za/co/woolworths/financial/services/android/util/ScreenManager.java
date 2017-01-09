package za.co.woolworths.financial.services.android.util;

import android.app.Activity;
import android.content.Intent;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.WOnboardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;

/**
 * Created by eesajacobs on 2016/11/30.
 */

public class ScreenManager {

    public static void presentMain(Activity activity){

        Intent intent = new Intent(activity, WOneAppBaseActivity.class);

        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }

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

        Intent intent = new Intent(activity, WOnboardingActivity.class);

        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }

    public static void presentSSOLinkAccounts(Activity activity) {

        Intent intent = new Intent(activity, SSOActivity.class);
        intent.putExtra(SSOActivity.TAG_PROTOCOL, SSOActivity.Protocol.HTTPS.rawValue());
        intent.putExtra(SSOActivity.TAG_HOST, SSOActivity.Host.STS.rawValue());
        intent.putExtra(SSOActivity.TAG_PATH, SSOActivity.Path.SIGNIN.rawValue());
        intent.putExtra(SSOActivity.TAG_SCOPE, "C2Id");

        activity.startActivityForResult(intent, SSOActivity.SSOActivityResult.LAUNCH.rawValue());
    }
}
