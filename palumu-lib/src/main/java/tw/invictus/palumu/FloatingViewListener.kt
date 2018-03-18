package tw.invictus.palumu

import android.view.View

/**
 * Created by ivan on 16/03/2018.
 */
interface FloatingViewListener {
    fun onViewDragged(isVisible: Boolean)
    fun getTargetView(): View?
}