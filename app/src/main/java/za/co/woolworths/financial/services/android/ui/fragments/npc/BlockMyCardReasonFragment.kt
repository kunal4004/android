package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BlockMyCardFragmentBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.npc.BlockReason
import za.co.woolworths.financial.services.android.ui.adapters.BlockCardReasonAdapter
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class BlockMyCardReasonFragment : MyCardExtension(R.layout.block_my_card_fragment) {

    companion object {
        fun newInstance() = BlockMyCardReasonFragment()
        var blockReason: Int? = null
    }

    private lateinit var binding: BlockMyCardFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            AnimationUtilExtension.animateViewPushDown(btnBlockCard)

            btnBlockCard?.setOnClickListener {
                (activity as? AppCompatActivity)?.let {
                    navigateToPermanentCardBlockFragment(it)
                }
            }

            val blockReasonList = mutableListOf<BlockReason>()
            AppConfigSingleton.storeCardBlockReasons?.forEach {
                val key = it.keys.first()
                val value = it[key]
                blockReasonList.add(BlockReason(key.toInt(), value.toString(), false))
            }

            activity?.let {
                rclBlockCardReason?.apply {
                    layoutManager = LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false)
                    adapter = BlockCardReasonAdapter(blockReasonList) {
                        btnBlockCard?.isEnabled = true
                        blockReason = it.key
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showToolbar()
    }

    fun processBlockCardRequest() {
        replaceFragment(
                fragment = ProcessBlockCardFragment.newInstance(false, blockReason),
                tag = ProcessBlockCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }
}