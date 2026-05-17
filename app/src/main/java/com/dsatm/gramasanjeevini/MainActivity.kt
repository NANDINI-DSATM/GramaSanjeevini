package com.dsatm.gramasanjeevini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dsatm.gramasanjeevini.ui.navigation.NavGraph
import com.dsatm.gramasanjeevini.ui.theme.GramaSanjeeviniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GramaSanjeeviniTheme {
                NavGraph()
            }
        }
    }
}