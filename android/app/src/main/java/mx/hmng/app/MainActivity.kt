package mx.hmng.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import mx.hmng.app.core.theme.HmngTheme
import mx.hmng.app.data.session.SessionManager
import mx.hmng.app.presentation.navigation.HmngNavHost
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HmngTheme {
                HmngNavHost(sessionManager = sessionManager)
            }
        }
    }
}
