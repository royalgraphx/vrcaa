package cc.sovellus.vrcaa.ui.screen.profile

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cc.sovellus.vrcaa.api.ApiContext
import cc.sovellus.vrcaa.api.models.User
import kotlinx.coroutines.launch

class ProfileScreenModel(
    context: Context
) : StateScreenModel<ProfileScreenModel.ProfileState>(ProfileState.Init) {
    private val api = ApiContext(context)

    sealed class ProfileState {
        data object Init : ProfileState()
        data object Loading : ProfileState()
        data class Result (val profile: User) : ProfileState()
    }

    private var profile = mutableStateOf<User?>(null)

    init {
        mutableState.value = ProfileState.Loading
        getProfile()
    }
    private fun getProfile() {
        screenModelScope.launch {
            profile.value = api.getSelf()
            mutableState.value = ProfileState.Result(profile.value!!)
        }
    }
}