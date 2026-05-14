package mx.hmng.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import mx.hmng.app.data.socket.SocketManager
import mx.hmng.app.data.sync.SyncWorker
import javax.inject.Inject

@HiltAndroidApp
class HmngApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var socketManager: SocketManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        SyncWorker.schedule(WorkManager.getInstance(this))
        socketManager.connect()
    }

    override fun onTerminate() {
        super.onTerminate()
        socketManager.disconnect()
    }
}
