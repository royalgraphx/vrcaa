package cc.sovellus.vrcaa.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cc.sovellus.vrcaa.App
import cc.sovellus.vrcaa.R
import cc.sovellus.vrcaa.api.ApiContext
import cc.sovellus.vrcaa.api.PipelineContext
import cc.sovellus.vrcaa.api.models.Friends
import cc.sovellus.vrcaa.api.models.pipeline.FriendLocation
import cc.sovellus.vrcaa.api.models.pipeline.FriendOffline
import cc.sovellus.vrcaa.api.models.pipeline.FriendOnline
import cc.sovellus.vrcaa.manager.FeedManager
import cc.sovellus.vrcaa.manager.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PipelineService : Service(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob()

    private lateinit var pipeline: PipelineContext
    private lateinit var api: ApiContext
    private lateinit var notificationManager: NotificationManager

    private val feedManager = FeedManager()

    private var friends: ArrayList<FriendOnline.User> = arrayListOf()
    private lateinit var activeFriends: Friends

    private val listener = object : PipelineContext.SocketListener {
        override fun onMessage(message: Any?) {
            if (message != null) {
                serviceHandler?.obtainMessage()?.also { msg ->
                    msg.obj = message
                    serviceHandler?.sendMessage(msg)
                }
            }
        }

    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private var notificationCounter: Int = 0

    private fun pushNotification(
        title: String,
        content: String
    ) {
        val flags = NotificationCompat.FLAG_ONGOING_EVENT

        val builder = NotificationCompat.Builder(this, App.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(flags)

        val notificationManager = NotificationManagerCompat.from(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationCounter, builder.build())
        }
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            when (msg.obj) {
                is FriendOnline -> {
                    val friend = msg.obj as FriendOnline

                    // We don't want to add duplicates to the list.
                    if (friends.find { it.id == friend.userId } == null)
                        friends.add(friend.user)

                    if (notificationManager.isOnWhitelist(friend.userId) &&
                        notificationManager.isIntentEnabled(friend.userId, NotificationManager.Intents.FRIEND_FLAG_ONLINE)) {
                        pushNotification(
                            title = application.getString(R.string.notification_service_title_online),
                            content = application.getString(R.string.notification_service_description_online).format(friend.user.displayName)
                        )
                    }

                    feedManager.addFeed(FeedManager.Feed(FeedManager.FeedType.FRIEND_FEED_ONLINE).apply {
                        friendName = friend.user.displayName
                        travelDestination = ""
                    })

                    notificationCounter++
                }
                is FriendOffline -> {
                    val friend = msg.obj as FriendOffline

                    // Remove from list if found.
                    val friendObject = friends.find { it.id == friend.userId }
                    if (friendObject != null)
                    {
                        if (notificationManager.isOnWhitelist(friend.userId) &&
                            notificationManager.isIntentEnabled(friend.userId, NotificationManager.Intents.FRIEND_FLAG_OFFLINE)) {
                            pushNotification(
                                title = application.getString(R.string.notification_service_title_offline),
                                content = application.getString(R.string.notification_service_description_offline).format(friendObject.displayName)
                            )
                        }

                        feedManager.addFeed(FeedManager.Feed(FeedManager.FeedType.FRIEND_FEED_OFFLINE).apply {
                            friendName = friendObject.displayName
                        })

                        friends = friends.filter { it.id != friend.userId } as ArrayList<FriendOnline.User>
                    } else {
                        // It seems it was not cached during local session, instead fallback to currentFriends
                        val fallbackFriend = activeFriends.find { it.id == friend.userId }

                        if (notificationManager.isOnWhitelist(friend.userId) &&
                            notificationManager.isIntentEnabled(friend.userId, NotificationManager.Intents.FRIEND_FLAG_OFFLINE)) {
                            pushNotification(
                                title = application.getString(R.string.notification_service_title_offline),
                                content = application.getString(R.string.notification_service_description_offline).format(fallbackFriend?.displayName)
                            )
                        }

                        feedManager.addFeed(FeedManager.Feed(FeedManager.FeedType.FRIEND_FEED_OFFLINE).apply {
                            friendName = fallbackFriend?.displayName.toString()
                        })

                        activeFriends.remove(activeFriends.find { it.id == friend.userId })
                    }

                    notificationCounter++
                }
                is FriendLocation -> {
                    val friend = msg.obj as FriendLocation

                    // if "friend.travelingToLocation" is not empty, it means friend is currently travelling.
                    // We want to show it only once, so only show when the travelling is done.
                    if (friend.travelingToLocation.isEmpty()) {
                        if (notificationManager.isOnWhitelist(friend.userId) &&
                            notificationManager.isIntentEnabled(friend.userId, NotificationManager.Intents.FRIEND_FLAG_LOCATION)) {
                            pushNotification(
                                title = application.getString(R.string.notification_service_title_location),
                                content = application.getString(R.string.notification_service_description_location).format(friend.user.displayName, friend.world.name)
                            )
                        }

                        feedManager.addFeed(FeedManager.Feed(FeedManager.FeedType.FRIEND_FEED_LOCATION).apply {
                            friendName = friend.user.displayName
                            travelDestination = friend.world.name
                        })
                    }

                    notificationCounter++
                }
                else -> { /* Not Implemented */ }
            }
        }
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", 10).apply {
            start()

            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        this.api = ApiContext(this, true)
        this.notificationManager = NotificationManager(this)

        launch {
            withContext(Dispatchers.Main) {
                api.getAuth().let { token ->
                    if (!token.isNullOrEmpty()) {
                        pipeline = PipelineContext(token)
                        pipeline.let { pipeline ->
                            pipeline.connect()
                            listener.let { pipeline.setListener(it) }
                        }
                    }
                }

                api.getFriends().let { friends ->
                    if (friends != null) {
                        activeFriends = friends
                    }
                }
            }
        }

        val builder = NotificationCompat.Builder(this, App.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("VRCAA")
            .setContentText(application.getString(R.string.service_notification))
            .setPriority(NotificationCompat.FLAG_FOREGROUND_SERVICE) // Make the notification sticky.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID_STICKY, builder.build(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            // Older versions do not require to specify the `foregroundServiceType`
            startForeground(NOTIFICATION_ID_STICKY, builder.build())
        }

        return START_STICKY // This makes sure, the service does not get killed.
    }

    override fun onDestroy() {
        super.onDestroy()
        pipeline.disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val NOTIFICATION_ID_STICKY = 42069
    }
}