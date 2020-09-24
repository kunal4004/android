package za.co.woolworths.financial.services.android.ui.activities.account.chat

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceViewModel

class ChatServiceActivity : AppCompatActivity() {

    private val chatServiceViewModel: ChatCustomerServiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.chat_for_collections_activity)
        

    }

}