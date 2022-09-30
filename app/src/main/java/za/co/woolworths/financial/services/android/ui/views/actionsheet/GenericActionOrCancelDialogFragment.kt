package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.compose.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.compose.MyriadProFontFamily
import za.co.woolworths.financial.services.android.ui.compose.NoRippleInteractionSource
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class GenericActionOrCancelDialogFragment : WBottomSheetDialogFragment() {

    interface IActionOrCancel {
        fun onDialogActionClicked(dialogId: Int)
    }

    companion object {
        private const val ARG_DIALOG_ID = "argDialogId"
        private const val ARG_TITLE = "argTitle"
        private const val ARG_DESC = "argDesc"
        private const val ARG_ACTION_TEXT = "argActionText"
        private const val ARG_CANCEL_TEXT = "argCancelText"

        private var listener: IActionOrCancel? = null

        fun newInstance(
                dialogId: Int,
                title: String,
                desc: String,
                actionButtonText: String,
                cancelButtonText: String,
                onActionListener: IActionOrCancel
        ): GenericActionOrCancelDialogFragment {
            listener = onActionListener
            return GenericActionOrCancelDialogFragment().withArgs {
                putInt(ARG_DIALOG_ID, dialogId)
                putString(ARG_TITLE, title)
                putString(ARG_DESC, desc)
                putString(ARG_ACTION_TEXT, actionButtonText)
                putString(ARG_CANCEL_TEXT, cancelButtonText)
            }
        }
    }

    var dialogId = -1
    lateinit var title: String
    lateinit var description: String
    lateinit var actionText: String
    lateinit var cancelText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogId = it.getInt(ARG_DIALOG_ID)
            title = it.getString(ARG_TITLE) ?: ""
            description = it.getString(ARG_DESC) ?: ""
            actionText = it.getString(ARG_ACTION_TEXT) ?: ""
            cancelText = it.getString(ARG_CANCEL_TEXT) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView {
        FragmentView(title, description, actionText, cancelText)
    }

    @Preview
    @Composable
    private fun FragmentView(
        title: String = "",
        description: String = "",
        actionText: String = "",
        cancelText: String = ""
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(
                        color = colorResource(id = R.color.color_E5E5E5)
                    )
                    .size(
                        width = 56.dp,
                        height = 6.dp
                    )
            )
            Text(
                modifier = Modifier
                    .padding(
                        top = 24.dp,
                        start = 32.dp,
                        end = 32.dp
                    ),
                text = title,
                fontSize = 20.sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 0.29.sp
            )
            Text(
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        start = 32.dp,
                        end = 32.dp
                    ),
                text = description,
                fontSize = 16.sp,
                fontFamily = MyriadProFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 21.sp,
                color = colorResource(id = R.color.color_7f7f7f),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {
                    listener?.onDialogActionClicked(dialogId)
                    dismissAllowingStateLoss()
                },
                shape = RectangleShape,
                modifier = Modifier
                    .padding(
                        top = 31.dp,
                        start = 32.dp,
                        end = 32.dp
                    )
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.button_style_height)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Black,
                    contentColor = Color.White
                ),
                elevation = null,
                interactionSource = NoRippleInteractionSource()
            ) {
                Text(
                    text = actionText,
                    fontSize = 12.sp,
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }
            Button(
                onClick = {
                    dismissAllowingStateLoss()
                },
                shape = RectangleShape,
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        start = 32.dp,
                        end = 32.dp,
                        bottom = 8.dp
                    )
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.button_style_height)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = colorResource(R.color.color_7f7f7f)
                ),
                elevation = null,
                interactionSource = NoRippleInteractionSource()
            ) {
                Text(
                    text = cancelText,
                    fontSize = 12.sp,
                    fontFamily = FuturaFontFamily,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(
                        textDecoration = TextDecoration.Underline,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}