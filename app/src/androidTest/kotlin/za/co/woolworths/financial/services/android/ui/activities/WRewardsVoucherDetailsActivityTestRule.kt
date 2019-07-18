package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WRewardsVoucherDetailsActivityTestRule {

    @Rule
    @JvmField
    public val wRewardsVoucherDetailsActivityTestRule: ActivityTestRule<WRewardsVoucherDetailsActivity> = ActivityTestRule<WRewardsVoucherDetailsActivity>(WRewardsVoucherDetailsActivity::class.java, true, false)

    @Test
    fun launchWRewardsVoucherDetailsActivity() {

        var voucherJsonData: String = "{\n" +
                "  \"monthTier\": \"valued\",\n" +
                "  \"vouchers\": [\n" +
                "    {\n" +
                "      \"amount\": 5000,\n" +
                "      \"description\": \"LW Welcome: R50 off R250 on Household \\u0026 Toiletries\",\n" +
                "      \"minimumSpend\": 25000,\n" +
                "      \"termsAndConditions\": \"Only one voucher may be used per transaction and may not be used in conjuction with any other offer\\nThis voucher must be used in conjunction with your Woolworths or MySchool MyVillage MyPlanet card.\\nThis voucher may not be used for utility payments, to purchase gift cards or be redeemed for cash\\nMisuse of this voucher in any way constitutes fraud.\\nThis voucher may not be redeemed via www.woolworths.co.za or Woolworths Foodstops at Engen and cannot be replaced if lost\\nThe image on the voucher does not necessarily relate to the offer of the voucher\\n\",\n" +
                "      \"type\": \"RAND\",\n" +
                "      \"validFromDate\": \"2014-12-15T00:00:00+0000\",\n" +
                "      \"validToDate\": \"2018-07-12T11:03:05+0000\",\n" +
                "      \"voucherNumber\": \"10000198812094016093\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"

        var selectedVoucherPosition = 0

        var intent = Intent()
        intent.putExtra("VOUCHERS", voucherJsonData);
        intent.putExtra("POSITION", selectedVoucherPosition);
        wRewardsVoucherDetailsActivityTestRule.launchActivity(intent)
    }
}