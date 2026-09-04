package com.example.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R

@Composable
fun VideoSplashScreen(onVideoFinished: () -> Unit) {
    val context = LocalContext.current
    val videoUri = remember {
        Uri.parse("android.resource://${context.packageName}/${R.raw.pressao_ritmo_logo_entrada}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Background color matching the app's theme or video background
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(videoUri)
                    // Set a transparent background for the VideoView itself if needed
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setOnCompletionListener {
                        onVideoFinished()
                    }
                    setOnErrorListener { _, _, _ ->
                        onVideoFinished() // Skip on error
                        true
                    }
                    start()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
