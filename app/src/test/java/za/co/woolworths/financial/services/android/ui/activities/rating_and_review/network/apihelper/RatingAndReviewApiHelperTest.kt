package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper

import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.modules.junit4.PowerMockRunner
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.utils.ApiHelperUtil

@RunWith(MockitoJUnitRunner::class)
class RatingAndReviewApiHelperTest {

    private lateinit var ratingAndReviewApiHelper: RatingAndReviewApiHelper

    @Before
    fun init() {
        ApiHelperUtil.setup()
        ratingAndReviewApiHelper = Mockito.mock(RatingAndReviewApiHelper::class.java,
                Mockito.CALLS_REAL_METHODS)
    }

    @Test
    fun check_if_config_method_get_called() = runBlockingTest {
        ratingAndReviewApiHelper.getMoreReviews(anyString(), anyInt(), anyString(), anyString())
        Mockito.verify(RetrofitConfig.mApiInterface).getMoreReviews(
                anyString(),
                anyString(),
                anyString(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString())
    }

}