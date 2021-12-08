package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.view

import androidx.fragment.app.Fragment
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.utils.BundleMock

@RunWith(MockitoJUnitRunner::class)
class ReportReviewFragmentUnitTest : Fragment() {

    private lateinit var reportReviewFragment: ReportReviewFragment

    @Mock
    private lateinit var reportList: ArrayList<String>

    @Before
    fun init() {
        reportReviewFragment = mock(ReportReviewFragment::class.java, CALLS_REAL_METHODS)
    }

    @Test
    fun test_initContainsKeyForReviewList() {
        val bundle = BundleMock.mock()
        bundle.putStringArrayList(KotlinUtils.REVIEW_REPORT, reportList)
        val containsKey = bundle.containsKey(KotlinUtils.REVIEW_REPORT)
        assertTrue(containsKey)
        reportReviewFragment.init()
        `when`(bundle.getStringArrayList(KotlinUtils.REVIEW_REPORT)).thenReturn(reportList)
        assertNotNull(reportList)

    }

    @Test
    fun test_initNotContainsKeyForReviewList() {
        val bundle = BundleMock.mock()
        val containsKey = bundle.containsKey(KotlinUtils.REVIEW_REPORT)
        reportReviewFragment.init()
        assertFalse(containsKey)
    }
}
