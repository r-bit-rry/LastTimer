package com.lasttimer.app.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.lasttimer.app.R
import com.lasttimer.app.service.TimerService

class TimerWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimerWidget()
}

class TimerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            TimerWidgetContent(context)
        }
    }
}

@Composable
fun TimerWidgetContent(context: Context) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LastTimer",
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = GlanceModifier.height(16.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer button
                WidgetButton(
                    context = context,
                    text = "Timer",
                    imageProvider = ImageProvider(R.drawable.ic_launcher_foreground),
                    onClick = {
                        // Launch the main activity
                        val intent = Intent(context, com.lasttimer.app.ui.MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
                // Stopwatch button
                WidgetButton(
                    context = context,
                    text = "Stopwatch",
                    imageProvider = ImageProvider(R.drawable.ic_launcher_foreground),
                    onClick = {
                        // Launch the main activity with stopwatch tab
                        val intent = Intent(context, com.lasttimer.app.ui.MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("OPEN_TAB", 1) // Index of stopwatch tab
                        }
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
                // Countdown button
                WidgetButton(
                    context = context,
                    text = "Countdown",
                    imageProvider = ImageProvider(R.drawable.ic_launcher_foreground),
                    onClick = {
                        // Launch the main activity with countdown tab
                        val intent = Intent(context, com.lasttimer.app.ui.MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("OPEN_TAB", 2) // Index of countdown tab
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun WidgetButton(
    context: Context,
    text: String,
    imageProvider: ImageProvider,
    onClick: () -> Unit
) {
    Column(
        modifier = GlanceModifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = imageProvider,
            contentDescription = text,
            modifier = GlanceModifier.size(40.dp)
        )
        
        Spacer(modifier = GlanceModifier.height(4.dp))
        
        Text(
            text = text,
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 12.sp
            )
        )
    }
}
