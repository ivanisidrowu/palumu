package tw.invictus.sample.data

/**
 * Created by ivan on 04/01/2018.
 */
interface DataProvider {
    fun provideVideos(): List<Video>
}