package za.co.woolworths.financial.services.android.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.coroutines.CoroutineContext

/**
 * Created by Kunal Uttarwar on 25/2/21.
 */
@ExperimentalCoroutinesApi
class TestCoroutineRule : TestRule, CoroutineScope {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher)

    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Unconfined

    fun postDelay(): Deferred<Unit> {
        return async {
            delay(2000)
        }
    }

    override fun apply(base: Statement, description: Description?) = object : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            Dispatchers.setMain(testCoroutineDispatcher)
            base.evaluate()
            Dispatchers.resetMain()
            testCoroutineScope.cleanupTestCoroutines()
        }
    }
}