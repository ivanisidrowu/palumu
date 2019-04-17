package tw.invictus.sample.data

/**
 * Created by ivan on 04/01/2018.
 */
class DataProviderImpl: DataProvider {

    private val mockVideo = Video("Sample Video", "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4", "")

    override fun provideVideos(): List<Video> {
        return MutableList(10) { mockVideo }
    }
}