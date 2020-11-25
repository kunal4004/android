package za.co.woolworths.financial.services.android.ui.views.card_swipe

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewSwipeDecorator private constructor() {
    private lateinit var canvas: Canvas
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewHolder: RecyclerView.ViewHolder
    private var dX: Float = 0.0f
    private var dY: Float = 0.0f
    private var actionState: Int = 0
    private var isCurrentlyActive: Boolean = false
    private var swipeLeftBackgroundColor = 0
    private var swipeLeftActionIconId = 0
    private var swipeLeftActionIconTint: Int? = null
    private var swipeRightBackgroundColor = 0
    private var swipeRightActionIconId = 0
    private var swipeRightActionIconTint: Int? = null
    private var iconHorizontalMargin: Int = 0
    private var mSwipeLeftText: String? = null
    private var mSwipeLeftTextSize = 14f
    private var mSwipeLeftTextUnit = TypedValue.COMPLEX_UNIT_SP
    private var mSwipeLeftTextColor = Color.DKGRAY
    private var mSwipeLeftTypeface = Typeface.SANS_SERIF
    private var mSwipeRightText: String? = null
    private var mSwipeRightTextSize = 14f
    private var mSwipeRightTextUnit = TypedValue.COMPLEX_UNIT_SP
    private var mSwipeRightTextColor = Color.DKGRAY
    private var mSwipeRightTypeface = Typeface.SANS_SERIF

    constructor(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) : this() {
        this.canvas = canvas
        this.recyclerView = recyclerView
        this.viewHolder = viewHolder
        this.dX = dX
        this.dY = dY
        this.actionState = actionState
        this.isCurrentlyActive = isCurrentlyActive
        iconHorizontalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, recyclerView.context.resources.displayMetrics).toInt()
    }

    fun setBackgroundColor(backgroundColor: Int) {
        swipeLeftBackgroundColor = backgroundColor
        swipeRightBackgroundColor = backgroundColor
    }

    fun setActionIconId(actionIconId: Int) {
        swipeLeftActionIconId = actionIconId
        swipeRightActionIconId = actionIconId
    }

    fun setActionIconTint(color: Int) {
        setSwipeLeftActionIconTint(color)
        setSwipeRightActionIconTint(color)
    }

    fun setSwipeLeftBackgroundColor(swipeLeftBackgroundColor: Int) {
        this.swipeLeftBackgroundColor = swipeLeftBackgroundColor
    }

    fun setSwipeLeftActionIconId(swipeLeftActionIconId: Int) {
        this.swipeLeftActionIconId = swipeLeftActionIconId
    }

    fun setSwipeLeftActionIconTint(color: Int) {
        swipeLeftActionIconTint = color
    }

    fun setSwipeRightBackgroundColor(swipeRightBackgroundColor: Int) {
        this.swipeRightBackgroundColor = swipeRightBackgroundColor
    }

    fun setSwipeRightActionIconId(swipeRightActionIconId: Int) {
        this.swipeRightActionIconId = swipeRightActionIconId
    }

    fun setSwipeRightActionIconTint(color: Int) {
        swipeRightActionIconTint = color
    }

    fun setSwipeRightLabel(label: String?) {
        mSwipeRightText = label
    }

    fun setSwipeRightTextSize(unit: Int, size: Float) {
        mSwipeRightTextUnit = unit
        mSwipeRightTextSize = size
    }

    fun setSwipeRightTextColor(color: Int) {
        mSwipeRightTextColor = color
    }

    fun setSwipeRightTypeface(typeface: Typeface) {
        mSwipeRightTypeface = typeface
    }

    fun setIconHorizontalMargin(iconHorizontalMargin: Int) {
        setIconHorizontalMargin(TypedValue.COMPLEX_UNIT_DIP, iconHorizontalMargin)
    }

    fun setIconHorizontalMargin(unit: Int, iconHorizontalMargin: Int) {
        this.iconHorizontalMargin = TypedValue.applyDimension(unit, iconHorizontalMargin.toFloat(), recyclerView.context.resources.displayMetrics).toInt()
    }

    fun setSwipeLeftLabel(label: String?) {
        mSwipeLeftText = label
    }

    fun setSwipeLeftTextSize(unit: Int, size: Float) {
        mSwipeLeftTextUnit = unit
        mSwipeLeftTextSize = size
    }

    fun setSwipeLeftTextColor(color: Int) {
        mSwipeLeftTextColor = color
    }

    fun setSwipeLeftTypeface(typeface: Typeface) {
        mSwipeLeftTypeface = typeface
    }

    fun decorate() {
        try {
            if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return
            if (dX > 0) {
                // Swiping Right
                canvas.clipRect(viewHolder.itemView.left, viewHolder.itemView.top, viewHolder.itemView.left + dX.toInt(), viewHolder.itemView.bottom)
                if (swipeRightBackgroundColor != 0) {
                    val background = ColorDrawable(swipeRightBackgroundColor)
                    background.setBounds(viewHolder.itemView.left, viewHolder.itemView.top, viewHolder.itemView.left + dX.toInt(), viewHolder.itemView.bottom)
                    background.draw(canvas)
                }
                var iconSize = 0
                if (swipeRightActionIconId != 0 && dX > iconHorizontalMargin) {
                    val icon = ContextCompat.getDrawable(recyclerView.context, swipeRightActionIconId)
                    if (icon != null) {
                        iconSize = icon.intrinsicHeight
                        val halfIcon = iconSize / 2
                        val top = viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                        icon.setBounds(viewHolder.itemView.left + iconHorizontalMargin, top, viewHolder.itemView.left + iconHorizontalMargin + icon.intrinsicWidth, top + icon.intrinsicHeight)
                        if (swipeRightActionIconTint != null) icon.setColorFilter(swipeRightActionIconTint!!, PorterDuff.Mode.SRC_IN)
                        icon.draw(canvas)
                    }
                }
                if (mSwipeRightText != null && mSwipeRightText?.length ?: 0 > 0 && dX > iconHorizontalMargin + iconSize) {
                    val textPaint = TextPaint()
                    textPaint.isAntiAlias = true
                    textPaint.textSize = TypedValue.applyDimension(mSwipeRightTextUnit, mSwipeRightTextSize, recyclerView.context.resources.displayMetrics)
                    textPaint.color = mSwipeRightTextColor
                    textPaint.typeface = mSwipeRightTypeface
                    val textTop = (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2).toInt()
                    canvas.drawText(mSwipeRightText!!, viewHolder.itemView.left + iconHorizontalMargin + iconSize + (if (iconSize > 0) iconHorizontalMargin / 2 else 0).toFloat(), textTop.toFloat(), textPaint)
                }
            } else if (dX < 0) {
                // Swiping Left
                canvas.clipRect(viewHolder.itemView.right + dX.toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                if (swipeLeftBackgroundColor != 0) {
                    val background = ColorDrawable(swipeLeftBackgroundColor)
                    background.setBounds(viewHolder.itemView.right + dX.toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                    background.draw(canvas)
                }
                var iconSize = 0
                var imgLeft = viewHolder.itemView.right
                if (swipeLeftActionIconId != 0 && dX < -iconHorizontalMargin) {
                    val icon = ContextCompat.getDrawable(recyclerView.context, swipeLeftActionIconId)
                    if (icon != null) {
                        iconSize = icon.intrinsicHeight
                        val halfIcon = iconSize / 2
                        val top = viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIcon)
                        imgLeft = viewHolder.itemView.right - iconHorizontalMargin - halfIcon * 2
                        icon.setBounds(imgLeft, top, viewHolder.itemView.right - iconHorizontalMargin, top + icon.intrinsicHeight)
                        if (swipeLeftActionIconTint != null) icon.setColorFilter(swipeLeftActionIconTint!!, PorterDuff.Mode.SRC_IN)
                        icon.draw(canvas)
                    }
                }
                if (mSwipeLeftText != null && mSwipeLeftText?.isNotEmpty() == true && dX < -iconHorizontalMargin - iconSize) {
                    val textPaint = TextPaint()
                    textPaint.isAntiAlias = true
                    textPaint.letterSpacing = 0.1f
                    textPaint.textSize = TypedValue.applyDimension(mSwipeLeftTextUnit, mSwipeLeftTextSize, recyclerView.context.resources.displayMetrics)
                    textPaint.color = mSwipeLeftTextColor
                    textPaint.typeface = mSwipeLeftTypeface
                    val width = textPaint.measureText(mSwipeLeftText)
                    val textTop = (viewHolder.itemView.top + (viewHolder.itemView.bottom - viewHolder.itemView.top) / 2.0 + textPaint.textSize / 2).toInt()
                    canvas.drawText(mSwipeLeftText!!, imgLeft - width - if (imgLeft == viewHolder.itemView.right) iconHorizontalMargin else iconHorizontalMargin / 2, textTop.toFloat(), textPaint)
                }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.name, e.message)
        }
    }

    class Builder(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        private val mDecorator: RecyclerViewSwipeDecorator = RecyclerViewSwipeDecorator(
                canvas,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
        )

        fun addBackgroundColor(color: Int): Builder {
            mDecorator.setBackgroundColor(color)
            return this
        }

        fun addActionIcon(drawableId: Int): Builder {
            mDecorator.setActionIconId(drawableId)
            return this
        }

        fun setActionIconTint(color: Int): Builder {
            mDecorator.setActionIconTint(color)
            return this
        }

        fun addSwipeRightBackgroundColor(color: Int): Builder {
            mDecorator.setSwipeRightBackgroundColor(color)
            return this
        }

        fun addSwipeRightActionIcon(drawableId: Int): Builder {
            mDecorator.setSwipeRightActionIconId(drawableId)
            return this
        }

        fun setSwipeRightActionIconTint(color: Int): Builder {
            mDecorator.setSwipeRightActionIconTint(color)
            return this
        }

        fun addSwipeRightLabel(label: String?): Builder {
            mDecorator.setSwipeRightLabel(label)
            return this
        }

        fun setSwipeRightLabelColor(color: Int): Builder {
            mDecorator.setSwipeRightTextColor(color)
            return this
        }

        fun setSwipeRightLabelTextSize(unit: Int, size: Float): Builder {
            mDecorator.setSwipeRightTextSize(unit, size)
            return this
        }

        fun setSwipeRightLabelTypeface(typeface: Typeface): Builder {
            mDecorator.setSwipeRightTypeface(typeface)
            return this
        }

        fun addSwipeLeftBackgroundColor(color: Int): Builder {
            mDecorator.setSwipeLeftBackgroundColor(color)
            return this
        }

        fun addSwipeLeftActionIcon(drawableId: Int): Builder {
            mDecorator.setSwipeLeftActionIconId(drawableId)
            return this
        }

        fun setSwipeLeftActionIconTint(color: Int): Builder {
            mDecorator.setSwipeLeftActionIconTint(color)
            return this
        }

        fun addSwipeLeftLabel(label: String?): Builder {
            mDecorator.setSwipeLeftLabel(label)
            return this
        }

        fun setSwipeLeftLabelColor(color: Int): Builder {
            mDecorator.setSwipeLeftTextColor(color)
            return this
        }

        fun setSwipeLeftLabelTextSize(unit: Int, size: Float): Builder {
            mDecorator.setSwipeLeftTextSize(unit, size)
            return this
        }

        fun setSwipeLeftLabelTypeface(typeface: Typeface): Builder {
            mDecorator.setSwipeLeftTypeface(typeface)
            return this
        }

        fun setSwipeLeftTextColor(color: Int): Builder {
            mDecorator.setSwipeLeftTextColor(color)
            return this
        }

        @Deprecated("")
        fun setIconHorizontalMargin(pixels: Int): Builder {
            mDecorator.setIconHorizontalMargin(pixels)
            return this
        }

        fun setIconHorizontalMargin(unit: Int, iconHorizontalMargin: Int): Builder {
            mDecorator.setIconHorizontalMargin(unit, iconHorizontalMargin)
            return this
        }

        fun create(): RecyclerViewSwipeDecorator {
            return mDecorator
        }
    }

}