package za.co.woolworths.financial.services.android.ui.activities.rating_and_review.datasource

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.AdditionalFields
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Normal
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Photos
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.RatingReviewResponse
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Refinements
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Reviews
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SecondaryRatings
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SkinProfile
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.SortOptions
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.Thumbnails
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.datasource.ReviewsDataSource
import za.co.woolworths.financial.services.android.util.TestCoroutineRule

@ExperimentalCoroutinesApi
class ReviewsDataSourceTest {

    private lateinit var mockReview: Reviews

    private lateinit var sortOptions: SortOptions

    private lateinit var refinements: Refinements

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = TestCoroutineRule()

    @Mock
    lateinit var ratingAndReviewApiHelper: RatingAndReviewApiHelper

    lateinit var reviewsPagingSource: ReviewsDataSource

    @Mock
    lateinit var ratingAndResponseLiveData: MutableLiveData<RatingReviewResponse>


    @Before
    fun setup() {
        // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
//        MockitoAnnotations.initMocks(this)
//        setUpMockRatingAndReviewResponse()
//        reviewsPagingSource = ReviewsDataSource(
//                ratingAndReviewApiHelper,
//                mockReview.productId,
//                sortOptions.sortOption,
//                refinements.navigationState,
//                ratingAndResponseLiveData)
    }

    private fun setUpMockRatingAndReviewResponse() {
        mockReview = Reviews(
                isVerifiedBuyer = true,
                isStaffMember = false,
                id = 1,
                productId = "1",
                syndicatedSource = "mock string",
                rating = 4.5f,
                isRecommended = false,
                submissionTime = "1/12/21",
                reviewText = "this is mock review",
                title = "review title",
                userNickname = "nick name",
                totalPositiveFeedbackCount = 1,
                additionalFields = listOf<AdditionalFields>(),
                secondaryRatings = listOf<SecondaryRatings>(),
                contextDataValue = listOf<SkinProfile>(),
                tagDimensions = listOf<SkinProfile>(),
                photos = Photos(listOf<Thumbnails>(), listOf<Normal>())
        )

        sortOptions = SortOptions(
                "newest",
                "newest",
                true
        )

        refinements = Refinements(
                "1 star only",
                "1 star",
                true
        )
    }

    // TODO UNIT TEST: The following code is not aligned with recent implementation, and needs to be updated.
    @Ignore
    @Test
    fun reviewsPagingSourceLoadFailureNullPointerExaception() = runBlockingTest {
        given(ratingAndReviewApiHelper
                .getMoreReviews(mockReview.productId,
                        1,
                        sortOptions.sortOption,
                        "refinements")).willReturn(null)
        val expectedResult = PagingSource.LoadResult.Error<Int,
                RatingReviewResponse>(NullPointerException())
        assertEquals(
                expectedResult.toString(), reviewsPagingSource.load(
                PagingSource.LoadParams.Refresh(
                        key = 0,
                        loadSize = 1,
                        placeholdersEnabled = false
                )
        ).toString()
        )
    }

 //   @Ignore
//    fun `reviews paging source refresh - success`() = runBlockingTest {
//        given(ratingAndReviewApiHelper.getMoreReviews(
//                "prod id",
//                1,
//                "newest",
//                "oldest"
//        )).willReturn(ratingAndReviewData)
//        val expectedResult = PagingSource.LoadResult.Page(
//                data = ratingAndReviewData.data[0].reviews,
//                prevKey = null,
//                nextKey = 1
//        )
//        assertEquals(
//                expectedResult, reviewsPagingSource.load(
//                PagingSource.LoadParams.Refresh(
//                        key = 0,
//                        loadSize = 1,
//                        placeholdersEnabled = false
//                )
//        )
//        )
//    }
}