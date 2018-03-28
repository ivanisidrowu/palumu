package tw.invictus.palumu

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import tw.invictus.palumu.R

/**
 * Copyright 2018 Wu Yu Hao (Ivan Wu)
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
 * Created by ivan on 04/01/2018.
 */
class ScalablePageFrame(context: Context?) : ConstraintLayout(context) {
    var headView: View? = null
    var bodyView: View? = null
    var isClosed = false
    var bottomPadding = 0
    var headRightMargin = 0
    var headBottomMargin = 0
    var listener: ScalablePageFrameListener? = null

    private val tag = this.javaClass.simpleName
    private val dxThreshold = 5
    private val dyThreshold = 15
    private val headScaleFactor = 2f
    private val minScale = 1 / headScaleFactor
    private val minSlidingClickDistance = 10
    private val xViewReleaseThreshold = 0.25f
    private val yViewReleaseThreshold = 0.5f
    private val headDimenRatio = "h,16:9"
    private val invalidPointer = -1
    private val dragHelper: ViewDragHelper

    private var verticalDragRange: Int = 0
    private var horizontalDragRange: Int = 0
    private var mTop: Int = 0
    private var mLeft: Int = 0
    private var verticalDragOffset: Float = 0f
    private var activePointerId = invalidPointer
    private var lastTouchActionDownXPosition = 0f
    private var rootViewGroup: ViewGroup? = null
    private var originHeadWidth = 0
    private var originHeadHeight = 0
    private var draggable = true

    init {
        dragHelper = ViewDragHelper.create(this, 1f, DragHelperCallback())
    }

    fun init(head: View, body: View, root: ViewGroup) {
        head.id = R.id.video_page_frame_head
        body.id = R.id.video_page_frame_body

        addView(head)
        addView(body)

        val layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.constrainWidth(R.id.video_page_frame_head, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(R.id.video_page_frame_head, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.setDimensionRatio(R.id.video_page_frame_head, headDimenRatio)
        constraintSet.connect(R.id.video_page_frame_head, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(R.id.video_page_frame_head, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
        constraintSet.connect(R.id.video_page_frame_head, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)

        constraintSet.constrainWidth(R.id.video_page_frame_body, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(R.id.video_page_frame_body, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.connect(R.id.video_page_frame_body, ConstraintSet.TOP, R.id.video_page_frame_head, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.video_page_frame_body, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.video_page_frame_body, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
        constraintSet.connect(R.id.video_page_frame_body, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
        constraintSet.applyTo(this)

        root.addView(this, layoutParams)
        rootViewGroup = root

        setPadding(0, 0, 0, bottomPadding)

        headView = findViewById(R.id.video_page_frame_head)
        bodyView = findViewById(R.id.video_page_frame_body)
    }

    fun enterFullScreen() {
        val head = headView
        val body = bodyView
        if (!isMinimized() && head != null && body != null) {
            draggable = false

            val layoutParams = head.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            body.visibility = View.INVISIBLE
            setPadding(0, 0 , 0, 0)
            invalidate()
        }
    }

    fun leaveFullScreen() {
        val head = headView
        val body = bodyView
        if (head != null && body != null) {
            draggable = true

            val layoutParams = head.layoutParams
            layoutParams.width = originHeadWidth
            layoutParams.height = originHeadHeight

            body.visibility = View.VISIBLE
            setPadding(0, 0, 0, bottomPadding)
            invalidate()
        }
    }

    fun maximize() {
        visibility = View.VISIBLE
        smoothSlideTo(0f)
        listener?.onMaximized()
    }

    fun minimize() {
        visibility = View.VISIBLE
        smoothSlideTo(1f)
        listener?.onMinimized()
    }

    fun close() {
        isClosed = true
        headView?.alpha = 1f
        headView?.scaleX = 1f
        headView?.scaleY = 1f
        headView?.pivotX = 0f
        headView?.pivotY = 0f

        removeView(headView)
        removeView(bodyView)
        rootViewGroup?.removeView(this)
        listener?.onClose()
        listener = null
    }

    fun isMinimized() = isHeadAtBottom() && isHeadAtRight()

    override fun onFinishInflate() {
        super.onFinishInflate()
        originHeadWidth = headView?.width ?: 0
        originHeadHeight = headView?.height ?: 0
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (MotionEventCompat.getActionMasked(ev) and MotionEventCompat.ACTION_MASK) {
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
        val interceptTap = dragHelper.isViewUnder(headView, ev.x.toInt(), ev.y.toInt())
        return dragHelper.shouldInterceptTouchEvent(ev) || interceptTap
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val actionMasked = MotionEventCompat.getActionMasked(ev)
        if (actionMasked and MotionEventCompat.ACTION_MASK == MotionEvent.ACTION_DOWN) {
            activePointerId = MotionEventCompat.getPointerId(ev, actionMasked)
        }
        if (activePointerId == invalidPointer) {
            return false
        }
        dragHelper.processTouchEvent(ev)
        if (isClosed) {
            return false
        }
        val isDragViewHit = isViewHit(headView!!, ev.x.toInt(), ev.y.toInt())
        val isSecondViewHit = isViewHit(bodyView!!, ev.x.toInt(), ev.y.toInt())
        analyzeTouchToMaximizeIfNeeded(ev, isDragViewHit)
        if (!isMinimized()) {
            headView?.dispatchTouchEvent(ev)
        } else {
            headView?.dispatchTouchEvent(cloneMotionEventWithAction(ev, MotionEvent.ACTION_CANCEL))
        }
        return isDragViewHit || isSecondViewHit
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val maxWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(View.resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                View.resolveSizeAndState(maxHeight, heightMeasureSpec, 0))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val headHeight = headView?.height ?: 0
        val scaleX = headView?.scaleX ?: 0f
        verticalDragRange = height - headHeight - bottomPadding
        horizontalDragRange = (width - width * scaleX).toInt()

        headView?.layout(mLeft, mTop, mLeft + headView!!.measuredWidth, mTop + headView!!.measuredHeight)
        bodyView?.layout(0, mTop + headView!!.measuredHeight, r, mTop + b)
    }

    private fun smoothSlideTo(slideOffset: Float): Boolean {
        val topBound = paddingTop
        val y = (topBound + slideOffset * verticalDragRange).toInt()

        if (headView != null && dragHelper.smoothSlideViewTo(headView!!, headView!!.left, y)) {
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    private fun analyzeTouchToMaximizeIfNeeded(ev: MotionEvent, isDragViewHit: Boolean) {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> lastTouchActionDownXPosition = ev.x
            MotionEvent.ACTION_UP -> {
                val clickOffset = ev.x - lastTouchActionDownXPosition
                if (shouldMaximizeOnClick(ev, clickOffset, isDragViewHit)) {
                    if (isMinimized()) {
                        maximize()
                    }
                }
            }
        }
    }

    private fun cloneMotionEventWithAction(event: MotionEvent, action: Int): MotionEvent {
        return MotionEvent.obtain(event.downTime, event.eventTime, action, event.x,
                event.y, event.metaState)
    }

    private fun shouldMaximizeOnClick(ev: MotionEvent, deltaX: Float, isDragViewHit: Boolean): Boolean {
        return (Math.abs(deltaX) < minSlidingClickDistance
                && ev.action != MotionEvent.ACTION_MOVE
                && isDragViewHit)
    }

    private fun isViewHit(view: View, x: Int, y: Int): Boolean {
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val parentLocation = IntArray(2)
        this.getLocationOnScreen(parentLocation)
        val screenX = parentLocation[0] + x
        val screenY = parentLocation[1] + y
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.width &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.height
    }

    private fun isHeadAtBottom(): Boolean {
        val scaleX = headView?.scaleX ?: minScale
        return scaleX == minScale
    }

    private fun isHeadAtRight(): Boolean {
        return mLeft == 0
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === headView
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            if (isHeadAtBottom() && isHeadAtRight()) {
                if (Math.abs(dy) > dyThreshold) {
                    dragVertically(top)
                } else if (Math.abs(dx) > dxThreshold) {
                    dragHorizontally(left)
                }
            } else if (isHeadAtBottom()) {
                dragHorizontally(left)
            } else {
                dragVertically(top)
            }

            requestLayout()
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (!draggable) return

            if (isHeadAtBottom() && !isHeadAtRight()) {
                if ((mLeft < 0 && mLeft > -measuredWidth * xViewReleaseThreshold)
                        || (mLeft > 0 && mLeft < measuredWidth * xViewReleaseThreshold)) {
                    dragHelper.settleCapturedViewAt(0, releasedChild.top)
                } else {
                    close()
                }
            } else {
                var top = paddingTop
                if (yvel > 0f || (yvel == 0f && verticalDragOffset > yViewReleaseThreshold)) {
                    top += verticalDragRange
                }
                dragHelper.settleCapturedViewAt(releasedChild.left, top)
            }

            invalidate()
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return verticalDragRange
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return horizontalDragRange
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if ((isMinimized() && Math.abs(dy) >= dyThreshold)
                    || (!isMinimized() && !isHeadAtBottom())) {
                val topBound = paddingTop
                val headHeight = headView?.height ?: 0
                val headPaddingBottom = headView?.paddingBottom ?: 0
                val bottomBound = height - headHeight - headPaddingBottom - bottomPadding

                return Math.min(Math.max(top, topBound), bottomBound)
            }
            val headHeight = headView?.measuredHeight ?: 0
            return (measuredHeight - (headHeight * scaleX) - bottomPadding).toInt()
        }

        override fun clampViewPositionHorizontal(child: View, targetLeft: Int, dx: Int): Int {
            var newLeft = headView?.left ?: 0

            if ((isMinimized() && Math.abs(dx) > dxThreshold)
                    || (isHeadAtBottom() && !isHeadAtRight())) {
                newLeft = targetLeft
            }

            return newLeft
        }

        private fun dragVertically(top: Int) {
            val head = headView
            val body = bodyView
            if (head != null && body != null && draggable) {
                mTop = top

                verticalDragOffset = top.toFloat() / verticalDragRange

                head.pivotX = head.width.toFloat() - headRightMargin
                head.pivotY = head.height.toFloat() - headBottomMargin
                head.scaleX = 1 - verticalDragOffset / headScaleFactor
                head.scaleY = head.scaleX

                body.alpha = 1 - verticalDragOffset
            }
        }

        private fun dragHorizontally(left: Int) {
            val head = headView
            if (head != null && draggable) {
                mLeft = left
                head.x = left.toFloat()

                if (mLeft < 0) {
                    val draggableRange = measuredWidth * (1 - minScale)
                    head.alpha = 1 - (Math.abs(mLeft) / draggableRange)
                } else {
                    head.alpha = 1 - (mLeft / (measuredWidth * minScale))
                }

                if (head.alpha <= 0) {
                    close()
                }
            }
        }
    }
}