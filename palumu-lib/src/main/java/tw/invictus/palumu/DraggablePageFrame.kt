package tw.invictus.palumu

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.Dimension
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.view.MotionEvent
import android.view.View
import tw.invictus.palumu.extension.isViewHit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * Copyright 2020 Wu Yu Hao (Ivan Wu)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by ivan on 07/10/2020.
 */
open class DraggablePageFrame(context: Context) : PageFrameBase(context) {
    var contentTopMargin = 0
    var contentBottomMargin = 0
    var contentLeftMargin = 0
    var contentRightMargin = 0

    private val animationDuration = 100L
    private val scaleFactor = 4f
    private val invalidPointer = -1
    private val dragHelper: ViewDragHelper
    private val startConstraintSet = ConstraintSet()
    private val endConstraintSet = ConstraintSet()
    private val minSlidingClickDistance = 5

    private var contentView: View? = null
    private var activePointerId = invalidPointer
    private var lastTouchActionDownXPosition = 0f
    private var lastTouchActionDownYPosition = 0f
    private var contentTransition = AutoTransition().apply {
        duration = animationDuration
    }

    init {
        dragHelper = ViewDragHelper.create(this, DragHelperCallback())
    }

    fun setContentView(view: View) {
        addView(view, LayoutParams.MATCH_CONSTRAINT, LayoutParams.MATCH_CONSTRAINT)
        contentView = view
        view.id = R.id.content
        startConstraintSet.run {
            clear(view.id)
            connect(view.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
            connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(view.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
            applyTo(this@DraggablePageFrame)
        }
        requestLayout()
    }

    fun setContentMargin(@Dimension(unit = Dimension.PX) margin: Int) {
        contentTopMargin = margin
        contentLeftMargin = margin
        contentRightMargin = margin
        contentBottomMargin = margin
    }

    override fun maximize() {
        TransitionManager.endTransitions(this)
        TransitionManager.beginDelayedTransition(this, contentTransition)
        startConstraintSet.applyTo(this)

        super.maximize()
    }

    override fun minimize() {
        TransitionManager.endTransitions(this)
        TransitionManager.beginDelayedTransition(this, contentTransition)
        endConstraintSet.applyTo(this)

        super.minimize()
    }

    override fun isMinimized() = contentView?.measuredWidth != measuredWidth

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                dragHelper.cancel()
                return false
            }
            MotionEvent.ACTION_DOWN -> {
                val index = MotionEventCompat.getActionIndex(ev)
                activePointerId = MotionEventCompat.getPointerId(ev, index)
                if (activePointerId == invalidPointer) {
                    return false
                }
            }
        }
        val interceptTap = dragHelper.isViewUnder(contentView, ev.x.toInt(), ev.y.toInt())
        return interceptTap || dragHelper.shouldInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val content = contentView ?: return super.onTouchEvent(ev)

        if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
            activePointerId = ev.getPointerId(ev.action)
        }
        if (activePointerId == invalidPointer) return false

        val isDragViewHit = isViewHit(content, ev.x.toInt(), ev.y.toInt())

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchActionDownXPosition = ev.x
                lastTouchActionDownYPosition = ev.y
            }
            MotionEvent.ACTION_UP -> {
                val dx = abs(ev.x - lastTouchActionDownXPosition)
                val dy = abs(ev.y - lastTouchActionDownYPosition)
                // Check valid click event. If it's yes, we don't use dragHelper.
                if (dx < minSlidingClickDistance && dy < minSlidingClickDistance && isDragViewHit) {
                    if (isMinimized()) {
                        maximize()
                    } else {
                        minimize()
                    }
                    return content.dispatchTouchEvent(ev)
                }
            }
        }
        dragHelper.processTouchEvent(ev)
        content.dispatchTouchEvent(ev)
        return isDragViewHit
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val view = contentView ?: return

        startConstraintSet.run {
            clear(view.id)
            connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(view.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
            constrainWidth(view.id, measuredWidth)
            constrainHeight(view.id, measuredHeight)
        }

        endConstraintSet.run {
            clear(view.id)
            connect(view.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, contentBottomMargin)
            connect(view.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, contentRightMargin)
            constrainWidth(view.id, (measuredWidth / scaleFactor).toInt())
            constrainHeight(view.id, (measuredHeight / scaleFactor).toInt())
        }
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {

        private var currentTop = 0
        private var currentLeft = 0

        override fun tryCaptureView(child: View, pointerId: Int) = child === contentView

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (!isMinimized()) return
            
            val childWidth = releasedChild.width
            val parentWidth = width
            val leftBound = paddingLeft + contentLeftMargin
            val rightBound = width - releasedChild.width - paddingRight - contentRightMargin
            if (childWidth / 2 + currentLeft < parentWidth / 2) {
                dragHelper.settleCapturedViewAt(leftBound, currentTop)
            } else {
                dragHelper.settleCapturedViewAt(rightBound, currentTop)
            }
            invalidate()
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val leftBound = paddingLeft
            val rightBound = width - child.width - paddingRight
            val newLeft = min(max(left, leftBound), rightBound)
            currentLeft = newLeft
            return newLeft
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val topBound = paddingTop
            val bottomBound = height - child.height - paddingBottom
            val newTop = min(max(top, topBound), bottomBound)
            currentTop = newTop
            return newTop
        }
    }
}