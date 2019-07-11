package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.chat_activity.*
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.ui.adapters.WChatAdapter


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
}