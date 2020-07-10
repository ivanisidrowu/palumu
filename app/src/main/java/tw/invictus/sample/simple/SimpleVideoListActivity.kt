package tw.invictus.sample.simple

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_simple_video_list.*
import tw.invictus.palumu.FloatingViewHelper
import tw.invictus.palumu.FloatingViewListener
import tw.invictus.palumu.FrameListener
import tw.invictus.palumu.ScalablePageFrame
import tw.invictus.sample.ExoVideoPlayer
import tw.invictus.sample.R
import tw.invictus.sample.data.DataProviderImpl
import tw.invictus.sample.data.Video

/**
 * Created by ivan on 07/03/2018.
 */
class SimpleVideoListActivity : AppCompatActivity(), FrameListener, FloatingViewListener {

    private lateinit var floatingPlayer: ExoVideoPlayer
    private lateinit var adapter: SimpleVideoListAdapter
    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    private var videoPageFrame: ScalablePageFrame? = null
    private var currentAdapterPosition = 0
    private var floatingViewHelper: FloatingViewHelper? = null
    private val uiHandler = Handler()
    private val floatingViewStartRunnable = Runnable { floatingViewHelper?.start() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_video_list)

        video_list.layoutManager = LinearLayoutManager(this)
        adapter = SimpleVideoListAdapter().apply {
            data = DataProviderImpl().provideVideos()
            itemClickListener = object : SimpleVideoListAdapter.OnItemClickListener {
                override fun onClick(video: Video) {
                    handleItemClick(video)
                }
            }
        }
        video_list.adapter = adapter
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            startFloatingHelper()
            if (getTargetView() != null) {
                video_list.viewTreeObserver.removeGlobalOnLayoutListener(globalLayoutListener)
            }
        }
        video_list.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        floatingPlayer = ExoVideoPlayer(this)
        floatingPlayer.play(adapter.data[currentAdapterPosition].url)
    }

    override fun onResume() {
        super.onResume()
        if (videoPageFrame?.isClosed != false) {
            startFloatingHelper()
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoPageFrame?.isClosed != false) {
            floatingViewHelper?.stop()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig != null) {
            if (videoPageFrame == null) {
                if (isFullScreenConfig(newConfig)) {
                    modifyFullScreenSettings(true)
                    floatingViewHelper?.enterFullScreen()
                } else {
                    modifyFullScreenSettings(false)
                    floatingViewHelper?.leaveFullScreen()
                }
            } else {
                if (isFullScreenConfig(newConfig)) {
                    modifyFullScreenSettings(true)
                    videoPageFrame?.enterFullScreen()
                } else {
                    modifyFullScreenSettings(false)
                    videoPageFrame?.leaveFullScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingViewHelper?.stop()
        adapter.release()
        floatingPlayer.release()
        uiHandler.removeCallbacks(floatingViewStartRunnable)
    }

    override fun onClose() {
        videoPageFrame = null
        floatingPlayer.visibility = View.INVISIBLE
        startFloatingHelper()
    }

    override fun onViewDragged(isVisible: Boolean) {
        val playWhenReady = floatingPlayer.currentPlayWhenReady
        val state = floatingPlayer.currentPlayBackState

        if (!playWhenReady && state != Player.STATE_IDLE) {
            floatingPlayer.play(adapter.data[currentAdapterPosition].url)
        }

        floatingPlayer.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    override fun getTargetView(): View? {
        val layoutManager = video_list.layoutManager as LinearLayoutManager

        var position = layoutManager.findFirstCompletelyVisibleItemPosition()
        var itemView: View? = layoutManager.findViewByPosition(position)
        if (itemView == null) {
            position = layoutManager.findFirstVisibleItemPosition()
            itemView = layoutManager.findViewByPosition(position)
        }

        if (position != currentAdapterPosition && position != RecyclerView.NO_POSITION) {
            currentAdapterPosition = position
            floatingPlayer.play(adapter.data[position].url)
        }

        return itemView?.findViewById(R.id.image)
    }

    override fun isViewOverCovered(target: View?) = false

    private fun modifyFullScreenSettings(isFullScreen: Boolean) {
        if (isFullScreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            supportActionBar?.hide()
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            supportActionBar?.show()
        }
    }

    private fun isFullScreenConfig(newConfig: Configuration) = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun initFloatingHelper() {
        if (floatingViewHelper == null) {
            floatingViewHelper = FloatingViewHelper()
        }
        floatingViewHelper?.apply {
            floatingView = floatingPlayer
            recyclerView = video_list
            topBorder = getTopBorder()
            bottomBorder = 0
            listener = this@SimpleVideoListActivity
        }
    }

    private fun initVideoPageFrame() {
        videoPageFrame = ScalablePageFrame(this)

        val root = window.decorView as ViewGroup
        videoPageFrame?.apply {
            headRightMargin = 80
            headBottomMargin = 80
            setHead(floatingPlayer)
            setBody(BodyFragment(), supportFragmentManager)
            attach(root)
            listener = this@SimpleVideoListActivity
        }
    }

    private fun startFloatingHelper() {
        initFloatingHelper()
        floatingViewHelper?.attachFloatingView()
        uiHandler.post(floatingViewStartRunnable)
        floatingPlayer.play(adapter.data[currentAdapterPosition].url)
    }

    private fun getTopBorder(): Int {
        val contentView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.y - contentView.measuredHeight
    }

    private fun handleItemClick(video: Video) {
        val isClosed = videoPageFrame?.isClosed ?: true
        if (isClosed) {
            floatingViewHelper?.stop()
            floatingPlayer.release()

            floatingPlayer = ExoVideoPlayer(this).apply { play(adapter.data[currentAdapterPosition].url) }
            initVideoPageFrame()
        } else {
            videoPageFrame?.maximize()
        }

        val source = adapter.data[currentAdapterPosition]
        if (source != video) {
            floatingPlayer.play(source.url)
        }
    }

}