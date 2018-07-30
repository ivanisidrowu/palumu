package tw.invictus.sample.data

/**
 * Created by ivan on 04/01/2018.
 */
class DataProviderImpl: DataProvider {

    private val mockVideo = Video("Sample Video", "https://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_2mb.mp4", "")

    override fun provideVideos(): List<Video> {
        return MutableList(10) { mockVideo }
    }
}