package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class MultipleImageActivityInstrumentedTest {

    @Rule
    @JvmField
    public val multipleImageActivityTestRule: ActivityTestRule<MultipleImageActivity> = ActivityTestRule<MultipleImageActivity>(MultipleImageActivity::class.java, true, false)

    @Test
    fun launchMultipleImageActivity() {

        var image = "https://images.woolworthsstatic.co.za/BELLA-DI-CERIGNOLA-OLIVE-6009189526398.jpg?V=cSH6&o=gVPS4voEwh6D3MvRbsW2tpo591Qj&"
        var intent = Intent()
        intent.putExtra("auxiliaryImages", image)
        multipleImageActivityTestRule.launchActivity(intent)
    }


}