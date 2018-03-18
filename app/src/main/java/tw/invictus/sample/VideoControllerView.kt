package tw.invictus.sample

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton

/**
 * Created by ivan on 01/03/2018.
 */
class VideoControllerView: ConstraintLayout {
    var playPauseButton: ImageButton? = null

    constructor(context: Context) : super(context) {
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.video_controller, this)
        playPauseButton = findViewById(R.id.play_pause)
    }
}