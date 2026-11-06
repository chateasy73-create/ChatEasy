package uk.ac.tees.mad.chateasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import uk.ac.tees.mad.chateasy.navigation.AppNav
import uk.ac.tees.mad.chateasy.ui.theme.ChatEasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatEasyTheme {
                AppNav()
            }
        }
    }
}