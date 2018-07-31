package tw.invictus.palumu

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout

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
 * Created by ivan on 05/03/2018.
 */
class FloatingViewHelper {
    private val tag = this.javaClass.simpleName ?: "FloatingViewHelper"

    var recyclerView: RecyclerView? = null
    var floatingView: View? = null
    var listener: FloatingViewListener? = null
    var currentScrollState = RecyclerView.SCROLL_STATE_IDLE
    var isFullScreen = false
    var topBorder = 0
    var bottomBorder = 0

    private var recyclerScrollListener: RecyclerView.OnScrollListener? = null
    private var scrollChangedListener: ViewTreeObserver.OnScrollChangedListener? = null
    private var originX = 0f
    private var originY = 0f
    private var originWidth: Int = 0
    private var originHeight: Int = 0
    private var screenHeight = 0

    fun start() {
        getScreenHeight()
        moveFloatingView(floatingView, listener?.getTargetView())
        addRecyclerScrollListener()
    }

    fun stop() {
        detachFloatingView()
        removeRecyclerScrollListener()
        removeScrollChangeListenerToCurrentTarget()
        listener = null
        isFullScreen = false
        currentScrollState = RecyclerView.SCROLL_STATE_IDLE
    }

    fun attachFloatingView() {
        val targetView = listener?.getTargetView()
        val theFloatingView = floatingView
        val theRecyclerView = recyclerView

        if (targetView != null && theFloatingView != null && theRecyclerView != null) {
            val rootView = theRecyclerView.parent as ViewGroup
            val layoutParams: ViewGroup.LayoutParams
            if (theFloatingView.parent != null) {

                if (targetView.measuredHeight != theFloatingView.measuredHeight) {
                    layoutParams = theFloatingView.layoutParams
                    layoutParams.height = targetView.height
                    theFloatingView.requestLayout()
                }

            } else {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, targetView.measuredHeight)
                rootView.addView(theFloatingView, layoutParams)
            }

        } else {
            Log.e(tag, "attachFollowerView: view is null!")
        }

        addScrollChangeListenerToCurrentTarget()
    }

    fun detachFloatingView() {
        val rootView = recyclerView?.parent as ViewGroup?
        floatingView?.let {
            it.x = 0f
            it.y = 0f
            rootView?.removeView(it)
        }
    }

    fun moveFloatingView(fromView: View?, toView: View?) {
        if (fromView != null && toView != null) {
            val locTo = IntArray(2)
            toView.getLocationOnScreen(locTo)

            val rect = Rect()
            toView.getLocalVisibleRect(rect)
            val newY = locTo[1] - topBorder
            val isTopOverCovered = listener?.isViewTopOverCovered(toView) ?: false
            val isBottomOverCovered = listener?.isViewBottomOverCovered(toView) ?: false
            val isHorizontallyOverCovered = listener?.isViewHorizontalOverCovered(toView) ?: false
            Log.d(tag, "$isTopOverCovered, $isBottomOverCovered, $isHorizontallyOverCovered")
            // set view with toView visible bounds
            if (!isTopOverCovered && !isBottomOverCovered && !isHorizontallyOverCovered) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    fromView.clipBounds = rect
                }
                fromView.x = toView.x
                fromView.y = newY.toFloat()
                listener?.onViewDragged(true)
            } else {
                listener?.onViewDragged(false)
            }

            if (!isFullScreen) {
                originX = locTo[0].toFloat()
                originY = newY.toFloat()

                originWidth = toView.measuredWidth
                originHeight = toView.measuredHeight
            }
        } else {
            Log.d(tag, "[moveFloatingView]: fromView or toView is null!")
        }
    }

    fun enterFullScreen() {
        Log.d(tag, "[enterFullScreenMode]: ")
        val theFloatingView = floatingView
        if (theFloatingView != null) {
            isFullScreen = true
            val layoutParams = theFloatingView.layoutParams
            if (layoutParams != null) {
                layoutParams.height = MATCH_PARENT
                layoutParams.width = MATCH_PARENT
            }
            theFloatingView.x = 0f
            theFloatingView.y = 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                theFloatingView.clipBounds = null
            }
        }
    }

    fun leaveFullScreen() {
        val theFloatingView = floatingView
        if (theFloatingView != null) {
            Log.d(tag, "[leaveFullScreenMode]: ")
            isFullScreen = false
            val layoutParams = theFloatingView.layoutParams
            if (layoutParams != null) {
                layoutParams.width = originWidth
                layoutParams.height = originHeight
            }
            theFloatingView.x = originX
            theFloatingView.y = originY
        }
    }

    fun addScrollChangeListenerToCurrentTarget() {
        val targetView = listener?.getTargetView()
        if (targetView != null) {
            scrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
                if (!isFullScreen && currentScrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    val target = listener?.getTargetView()
                    moveFloatingView(floatingView, target)
                }
            }
            targetView.viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)
        }
    }

    fun removeScrollChangeListenerToCurrentTarget() {
        val targetView = listener?.getTargetView()
        targetView?.viewTreeObserver?.removeOnScrollChangedListener(scrollChangedListener)
    }

    private fun addRecyclerScrollListener() {
        if (recyclerView != null && recyclerScrollListener == null) {
            recyclerScrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!isFullScreen) {
                        currentScrollState = newState
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            listener?.getTargetView()?.let { moveFloatingView(floatingView, it) }
                        }
                    }
                }
            }
            recyclerView?.addOnScrollListener(recyclerScrollListener)
        }
    }

    private fun removeRecyclerScrollListener() {
        recyclerView?.removeOnScrollListener(recyclerScrollListener)
        recyclerScrollListener = null
    }

    private fun getScreenHeight() {
        val theRecyclerView = recyclerView
        if (theRecyclerView != null) {
            val windowManager = theRecyclerView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            screenHeight = point.y
        }
    }
}
