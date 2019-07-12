package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import com.awfs.coordination.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.chat_activity.*
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.onAction
import java.util.concurrent.TimeUnit


class WChatActivity : AppCompatActivity() {

    private var adapter: WChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()
        if (savedInstanceState == null)
            getBundleArgument()
        initViews()
    }

    private fun actionBar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun getBundleArgument() {
        intent?.extras?.apply {

        }
    }

    fun initViews() {
        reyclerview_message_list.layoutManager = LinearLayoutManager(this)
        adapter = WChatAdapter()
        reyclerview_message_list.adapter = adapter
        button_send.setOnClickListener { sendMessage() }
        edittext_chatbox.afterTextChanged { button_send.isEnabled = it.isNotEmpty() }
        edittext_chatbox.onAction(EditorInfo.IME_ACTION_DONE){sendMessage()}

    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return false
    }

    private fun updateMessageList(message: ChatMessage) {
        runOnUiThread {
            adapter?.let {
                it.addMessage(message)
                reyclerview_message_list.scrollToPosition(it.itemCount - 1)
            }
        }
    }

    private fun sendMessage() {
        if (edittext_chatbox.text.isNotEmpty()) {
            val message = edittext_chatbox.text.toString().trim()
            updateMessageList(ChatMessage(if (message.contains("R:")) ChatMessage.Type.RECEIVED else ChatMessage.Type.SENT, if (message.contains("R:")) message.replace("R:", "") else message))
            edittext_chatbox.text.clear()
        }
    }

    private fun checkAgentAvailable(){
        val disposables = CompositeDisposable()
            disposables.add( Observable.interval(0,5,TimeUnit.SECONDS)
                    .flatMap { OneAppService.pollAgentsAvailable().takeUntil(Observable.timer(5,TimeUnit.SECONDS))}
                    .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe { result->
                        result.agentsAvailable
                    })
    }
}