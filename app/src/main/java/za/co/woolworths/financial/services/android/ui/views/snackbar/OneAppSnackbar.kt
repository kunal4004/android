package za.co.woolworths.financial.services.android.ui.views.snackbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.google.android.material.snackbar.BaseTransientBottomBar

class OneAppSnackbar(
        parent: ViewGroup,
        content: OneAppSnackbarView
) : BaseTransientBottomBar<OneAppSnackbar>(parent, content, content) {

    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {

        fun make(view: View, description : String): OneAppSnackbar {

            // First we find a suitable parent for our custom view
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )

            // We inflate our custom view
            val customView = LayoutInflater.from(view.context).inflate(
                R.layout.layout_snackbar_toast,
                parent,
                false
            ) as OneAppSnackbarView

            val toastMessageTextView = customView.findViewById<TextView>(R.id.toastMessageTextView)
            toastMessageTextView?.text = description

            // We create and return our Snackbar
            return OneAppSnackbar(
                parent,
                customView
            )
        }

    }

}