package cc.sovellus.vrcaa.ui.screen.home

import android.content.Context
import android.content.Intent
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cc.sovellus.vrcaa.api.ApiContext
import cc.sovellus.vrcaa.api.avatars.providers.JustHPartyProvider
import cc.sovellus.vrcaa.api.models.Avatars
import cc.sovellus.vrcaa.api.models.Friends
import cc.sovellus.vrcaa.api.models.LimitedWorlds
import cc.sovellus.vrcaa.helper.isMyServiceRunning
import cc.sovellus.vrcaa.service.PipelineService
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val context: Context
) : StateScreenModel<HomeScreenModel.HomeState>(HomeState.Init) {
    private val api: ApiContext = ApiContext(context)

    sealed class HomeState {
        data object Init : HomeState()
        data object Loading : HomeState()
        data class Result(
            val friends: MutableList<Friends.FriendsItem>,
            val lastVisited: MutableList<LimitedWorlds.LimitedWorldItem>,
            val featuredAvatars: MutableList<Avatars.AvatarsItem>,
            val offlineFriends: MutableList<Friends.FriendsItem>,
            val featuredWorlds: MutableList<LimitedWorlds.LimitedWorldItem>
        ) : HomeState()
    }

    private var friends = mutableListOf<Friends.FriendsItem>()
    private var lastVisited = mutableListOf<LimitedWorlds.LimitedWorldItem>()
    private var featuredAvatars = mutableListOf<Avatars.AvatarsItem>()
    private var offlineFriends = mutableListOf<Friends.FriendsItem>()
    private var featuredWorlds = mutableListOf<LimitedWorlds.LimitedWorldItem>()

    init {
        mutableState.value = HomeState.Loading
        getContent()
    }

    private fun getContent() {
        screenModelScope.launch {

            api.getFriends()?.let { friends = it }
            api.getRecentWorlds()?.let { lastVisited = it }
            api.getAvatars()?.let { featuredAvatars = it }
            api.getFriends(true)?.let { offlineFriends = it }
            api.getWorlds()?.let { featuredWorlds = it }

            // Load the service here, I believe it's better than loading at `MainActivity`
            // Because if session expires, it would not restart the service.
            if (!context.isMyServiceRunning(PipelineService::class.java)) {
                val intent = Intent(context, PipelineService::class.java)
                context.startForegroundService(intent)
            }

            mutableState.value = HomeState.Result(
                friends = friends,
                lastVisited = lastVisited,
                featuredAvatars = featuredAvatars,
                offlineFriends = offlineFriends,
                featuredWorlds = featuredWorlds
            )
        }
    }
}