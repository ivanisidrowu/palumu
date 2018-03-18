package tw.invictus.sample

import android.app.Application
import com.facebook.stetho.Stetho

/**
 * Created by ivan on 08/01/2018.
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}