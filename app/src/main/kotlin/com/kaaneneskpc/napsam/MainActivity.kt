package com.kaaneneskpc.napsam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import com.kaaneneskpc.napsam.core.designsystem.theme.NapsamTheme
import com.kaaneneskpc.napsam.feature.home.HomeRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Geçici kabuk: ana ekran doğrudan açılır. Üst bar, Kaydedilenler/Gizlilik navigasyonu ve tema
        // düğmesi Faz 9–10'da eklenir; o zamana kadar Gizlilik bağlantısı bir yere götürmez.
        setContent {
            NapsamTheme {
                HomeRoute(onOpenPrivacy = {}, contentPadding = WindowInsets.safeDrawing.asPaddingValues())
            }
        }
    }
}
