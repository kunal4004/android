/*
 *  Copyright (c) 2018 Absa Bank Limited, All Rights Reserved.
 *
 *  This code is confidential to Absa Bank Limited and shall not be disclosed
 *  outside the Bank without the prior written permission of the Absa Legal
 *
 *  In the event that such disclosure is permitted the code shall not be copied
 *  or distributed other than on a need-to-know basis and any recipients may be
 *  required to sign a confidentiality undertaking in favor of Absa Bank
 *  Limited
 *
 */
package za.co.absa.openbankingapi;

import android.util.Log;

final class Logger {

    static boolean isDebug = true;

    static void setIsDebug(boolean value) {
        isDebug = value;
    }

    static void Error(String tag, String value) {
        if (isDebug && value != null) {
            Log.e(tag, value);
        }
    }
}
