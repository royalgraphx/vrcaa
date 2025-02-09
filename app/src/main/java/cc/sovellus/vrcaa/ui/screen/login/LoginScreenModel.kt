package cc.sovellus.vrcaa.ui.screen.login

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cc.sovellus.vrcaa.api.ApiContext
import cc.sovellus.vrcaa.helper.cookies
import cc.sovellus.vrcaa.helper.userCredentials
import kotlinx.coroutines.launch

class LoginScreenModel(
    private val context: Context,
    private val navigator: Navigator
) : ScreenModel {
    private val api = ApiContext(context)

    var username = mutableStateOf("")
    var password = mutableStateOf("")
    var passwordVisible = mutableStateOf(false)

    fun doLogin() {
        screenModelScope.launch {
            val result = api.getToken(username.value, password.value)
            if (result != null) {
                val preferences = context.getSharedPreferences(
                    "vrcaa_prefs", MODE_PRIVATE
                )

                // STORE credentials, so we can request new session later when it expires, for any given reason.
                preferences.userCredentials = Pair(username.value, password.value)
                preferences.cookies = result.second

                navigator.push(TwoAuthScreen(result.first, result.second))
            } else {
                Toast.makeText(
                    context,
                    "Wrong username or password.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}