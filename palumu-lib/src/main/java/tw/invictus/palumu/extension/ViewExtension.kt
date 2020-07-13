package tw.invictus.palumu.extension

import android.view.View

fun View.isViewHit(target: View, x: Int, y: Int): Boolean {
    val viewLocation = IntArray(2)
    target.getLocationOnScreen(viewLocation)
    val parentLocation = IntArray(2)
    getLocationOnScreen(parentLocation)
    val screenX = parentLocation[0] + x
    val screenY = parentLocation[1] + y
    return screenX in (viewLocation[0] until (viewLocation[0] + target.width))
            && screenY in (viewLocation[1] until (viewLocation[1] + target.height))
}