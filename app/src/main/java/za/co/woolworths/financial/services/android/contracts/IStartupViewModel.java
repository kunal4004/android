package za.co.woolworths.financial.services.android.contracts;

import android.content.Intent;

public interface IStartupViewModel {

    void queryServiceGetConfig(ConfigResponseListener responseListener);
    void presentNextScreen();

    String getRandomVideoPath();

    void setIntent(Intent intent);
    Intent getIntent();

    void setPushNotificationUpdate(String pushNotificationUpdate);
    String getPushNotificationUpdate();
}
