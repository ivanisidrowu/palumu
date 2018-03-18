package tw.invictus.sample

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

/**
 * Created by ivan on 01/03/2018.
 */
class ExoVideoPlayer : FrameLayout {

    private val tag = this.javaClass.simpleName

    var repeat = true
    var currentPlayBackState = Player.STATE_IDLE
    var currentPlayWhenReady = false

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var bandWidthMeter: DefaultBandwidthMeter
    private lateinit var videoController: VideoControllerView
    private val defaultEventListener = object : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            currentPlayWhenReady = playWhenReady
            currentPlayBackState = playbackState
        }
    }
    private val buttonClickListener = OnClickListener {
        if (videoController.playPauseButton != null) {
            val selected = videoController.playPauseButton!!.isSelected
            videoController.playPauseButton!!.isSelected = !selected
        }

        exoPlayer.playWhenReady = !currentPlayWhenReady
    }

    constructor(context: Context): super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val playerView = TextureView(context)
        videoController = VideoControllerView(context)
        addView(playerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        addView(videoController, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        bandWidthMeter = DefaultBandwidthMeter()
        val trackSelector = DefaultTrackSelector(bandWidthMeter)
        val renderersFactory = DefaultRenderersFactory(context, null)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector)
        exoPlayer.setVideoTextureView(playerView)
        exoPlayer.addListener(defaultEventListener)

        videoController.playPauseButton?.setOnClickListener(buttonClickListener)
        setBackgroundColor(Color.BLACK)
    }

    fun play(url: String) {
        val dataSourceFactory = DefaultHttpDataSourceFactory(tag, bandWidthMeter)
        val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        exoPlayer.playWhenReady = true
        if (repeat) {
            exoPlayer.repeatMode = REPEAT_MODE_ALL
        }
        exoPlayer.prepare(mediaSource)
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun release() {
        exoPlayer.removeListener(defaultEventListener)
        exoPlayer.release()
        videoController.playPauseButton?.setOnClickListener(null)
    }
}