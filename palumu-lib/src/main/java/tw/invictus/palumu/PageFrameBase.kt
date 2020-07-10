package tw.invictus.palumu

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.ViewGroup

abstract class PageFrameBase(context: Context) : ConstraintLayout(context) {

    var listener: FrameListener? = null
    protected var rootViewGroup: ViewGroup? = null

    open fun attach(root: ViewGroup) {
        root.addView(this, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        rootViewGroup = root
    }

    open fun detach() {
        rootViewGroup?.removeView(this)
    }

    open fun isMinimized() = false

    open fun maximize() {
        listener?.onMaximized()
    }

    open fun minimize() {
        listener?.onMinimized()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(View.resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                View.resolveSizeAndState(maxHeight, heightMeasureSpec, 0))
    }
}