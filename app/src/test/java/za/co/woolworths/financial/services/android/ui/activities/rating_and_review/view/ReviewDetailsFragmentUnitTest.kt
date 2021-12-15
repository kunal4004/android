package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mock
import org.mockito.Mockito
import za.co.woolworths.financial.services.android.models.dto.rating_n_reviews.RatingReviewResponse
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.utils.BundleMock

class ReviewDetailsFragmentUnitTest {
     private lateinit var reviewDetailsFragment: ReviewDetailsFragment

    @Mock
    private lateinit var reportList: ArrayList<String>

    @Before
     fun init() {
         reviewDetailsFragment = Mockito.mock(ReviewDetailsFragment::class.java,
                 Mockito.CALLS_REAL_METHODS)
     }

     @Test
     fun test_init() {
//         val bundle = BundleMock.mock()
//         val ratingAndResponseData = Utils.jsonStringToObject(KotlinUtils.REVIEW_DATA,
//                 RatingReviewResponse::class.java) as RatingReviewResponse
//         bundle.putStringArrayList(KotlinUtils.REVIEW_REPORT, reportList)
//         val containsKey = bundle.containsKey(KotlinUtils.REVIEW_DATA)
//         assertTrue(containsKey)
//         reviewDetailsFragment.setDataForReportScreenData(reportList)
//         Mockito.`when`(bundle.getStringArrayList(KotlinUtils.REVIEW_REPORT)).thenReturn(reportList)
//         assertNotNull(reportList)
     }
 }
