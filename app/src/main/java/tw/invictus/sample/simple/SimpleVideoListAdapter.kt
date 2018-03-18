package tw.invictus.sample.simple

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import tw.invictus.sample.R
import tw.invictus.sample.data.Video

/**
 * Created by ivan on 06/03/2018.
 */
class SimpleVideoListAdapter: RecyclerView.Adapter<SimpleVideoListAdapter.VideoViewHolder>() {

    var data: List<Video> = listOf()
    var itemClickListener: OnItemClickListener? = null

    fun release() {
        itemClickListener = null
        data = listOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VideoViewHolder {
        val rootView = LayoutInflater.from(parent?.context).inflate(R.layout.simple_list_item_video, parent, false)
        return VideoViewHolder(rootView)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VideoViewHolder?, position: Int) {
        val video = data[position]
        if (TextUtils.isEmpty(video.coverUrl) && holder != null) {
            val options = RequestOptions().centerCrop()
            Glide.with(holder.itemView.context)
                    .load(R.drawable.sample)
                    .apply(options)
                    .into(holder.image)
        }
        holder?.title?.text = video.title
        holder?.itemView?.setOnClickListener({itemClickListener?.onClick(video)})
    }

    override fun onViewRecycled(holder: VideoViewHolder?) {
        holder?.unbindViewHolder()
    }

    inner class VideoViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView? = itemView?.findViewById(R.id.image)
        val title: TextView? = itemView?.findViewById(R.id.title)

        fun unbindViewHolder() {
            title?.text = ""
        }
    }

    interface OnItemClickListener {
        fun onClick(video: Video)
    }
}