package za.co.woolworths.financial.services.android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.autoCleared

// TODO :: Check against fragment documentation
abstract class ViewBindingFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) : Fragment() {

    private var _binding: VB by autoCleared()

    val binding: VB
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }
}

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

