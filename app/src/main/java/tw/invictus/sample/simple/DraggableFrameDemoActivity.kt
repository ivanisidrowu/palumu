package tw.invictus.sample.simple

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import tw.invictus.palumu.DraggablePageFrame
import tw.invictus.sample.R
import tw.invictus.sample.data.DataProviderImpl
import tw.invictus.sample.data.Video

class DraggableFrameDemoActivity : AppCompatActivity() {

    private lateinit var imageView: AppCompatImageView
    private lateinit var adapter: SimpleVideoListAdapter

    private var pageFrame: DraggablePageFrame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_video_list)

        findViewById<RecyclerView>(R.id.video_list).apply {
            layoutManager = LinearLayoutManager(this@DraggableFrameDemoActivity)
            adapter = SimpleVideoListAdapter().apply {
                data = DataProviderImpl().provideVideos()
                itemClickListener = object : SimpleVideoListAdapter.OnItemClickListener {
                    override fun onClick(video: Video) {
                        handleItemClick()
                    }
                }
            }
            this.adapter = adapter
        }

        imageView = AppCompatImageView(this)
        val options = RequestOptions().centerCrop()
        Glide.with(this).load(R.drawable.sample).apply(options).into(imageView)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.release()
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