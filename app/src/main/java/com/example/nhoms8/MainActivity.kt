package com.example.nhoms8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nhoms8.core.navigation.SpendlyNavHost
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.viewmodel.SpendTogetherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendlyTheme {
                SpendTogetherApp()
            }
        }
    }
}

/**
 * Ứng dụng Spendly (SpendTogether) kết nối Navigation Compose 5-tab kính mờ Haze 2.1.0,
 * quản lý đa quỹ Cloud Firestore, chia tiền 3 chế độ và trợ lý FinSprout AI.
 */
@Composable
fun SpendTogetherApp(
    viewModel: SpendTogetherViewModel = viewModel()
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        SpendlyNavHost(spendTogetherViewModel = viewModel)
    }
}

/**
 * Preview toàn bộ luồng hoạt động ứng dụng Spendly trong Android Studio
 */
@Preview(
    name = "Spendly · Luồng ứng dụng chính",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SpendTogetherAppPreview() {
    SpendlyTheme {
        SpendTogetherApp()
    }
}
