package tw.invictus.sample.simple

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import tw.invictus.palumu.DraggablePageFrame
import tw.invictus.sample.R
import tw.invictus.sample.data.DataProviderImpl
import tw.invictus.sample.data.Video

class DraggableFrameDemoActivity : AppCompatActivity() {

    private lateinit var imageView: AppCompatImageView
    private lateinit var simpleVideoListAdapter: SimpleVideoListAdapter

    private var pageFrame: DraggablePageFrame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_video_list)

        findViewById<RecyclerView>(R.id.video_list).apply {
            layoutManager = LinearLayoutManager(this@DraggableFrameDemoActivity)
            simpleVideoListAdapter = SimpleVideoListAdapter().apply {
                data = DataProviderImpl().provideVideos()
                itemClickListener = object : SimpleVideoListAdapter.OnItemClickListener {
                    override fun onClick(video: Video) {
                        handleItemClick()
                    }
                }
            }
            this.adapter = simpleVideoListAdapter
        }

        imageView = AppCompatImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.sample)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleVideoListAdapter.release()
        imageView.setOnClickListener(null)
        pageFrame?.detach()
    }

    private fun initVideoPageFrame() {
        val root = window.decorView as ViewGroup
        pageFrame = DraggablePageFrame(this).apply {
            setContentView(imageView)
            val margin = resources.getDimensionPixelSize(R.dimen.item_margin)
            setContentMargin(margin)
            attach(root)
        }
    }

    private fun handleItemClick() {
        if (pageFrame == null) {
            initVideoPageFrame()
            return
        }
        pageFrame?.maximize()
    }
}