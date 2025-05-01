package com.lasttimer.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CustomTimePicker(
    modifier: Modifier = Modifier,
    initialHours: Int = 0,
    initialMinutes: Int = 0,
    initialSeconds: Int = 0,
    onTimeChange: (hours: Int, minutes: Int, seconds: Int) -> Unit
) {
    var selectedHours by remember { mutableStateOf(initialHours) }
    var selectedMinutes by remember { mutableStateOf(initialMinutes) }
    var selectedSeconds by remember { mutableStateOf(initialSeconds) }

    // Animation for entrance
    var isVisible by remember { mutableStateOf(false) }
    val alpha = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    // Show the picker with a slight delay for a nice entrance effect
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    // Notify parent about the initial state
    LaunchedEffect(Unit) {
        onTimeChange(selectedHours, selectedMinutes, selectedSeconds)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha.value),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours Picker
        InfiniteNumberPicker(
            range = 0..99, // Allow up to 99 hours
            initialValue = selectedHours,
            onValueChange = {
                selectedHours = it
                onTimeChange(selectedHours, selectedMinutes, selectedSeconds)
            },
            label = "H"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(":", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.width(16.dp))

        // Minutes Picker
        InfiniteNumberPicker(
            range = 0..59,
            initialValue = selectedMinutes,
            onValueChange = {
                selectedMinutes = it
                onTimeChange(selectedHours, selectedMinutes, selectedSeconds)
            },
            label = "M"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(":", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.width(16.dp))

        // Seconds Picker
        InfiniteNumberPicker(
            range = 0..59,
            initialValue = selectedSeconds,
            onValueChange = {
                selectedSeconds = it
                onTimeChange(selectedHours, selectedMinutes, selectedSeconds)
            },
            label = "S"
        )
    }
}

@Composable
private fun InfiniteNumberPicker(
    modifier: Modifier = Modifier,
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    label: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val itemHeight = 48.dp // Height of each number item
    val totalVirtualItems = Int.MAX_VALUE // Simulate infinite list
    val middleIndex = totalVirtualItems / 2
    val initialIndex = middleIndex - (middleIndex % range.count()) + range.indexOf(initialValue.coerceIn(range))

    // Center the initial value
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex - 1) // Scroll slightly above to center
    }

    // Snap to closest item when scrolling stops
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex > 0) { // Avoid snapping during initial setup
            val centerItemIndex = listState.firstVisibleItemIndex + 1 // Assuming 3 visible items centered
            val targetIndex = centerItemIndex.coerceIn(0, totalVirtualItems -1)
            val targetValueIndex = targetIndex % range.count()
            val selectedValue = range.first + targetValueIndex

            // Animate scroll to center the selected item
            coroutineScope.launch {
                listState.animateScrollToItem(targetIndex - 1) // Center the item
            }
            onValueChange(selectedValue)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = modifier.height(itemHeight * 3).width(64.dp)) { // Display 3 items vertically
            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(totalVirtualItems) { index ->
                    val value = range.first + (index % range.count())
                    val isCenter = index == listState.firstVisibleItemIndex + 1 // Highlight center item
                    
                    // Animate scale and alpha based on whether item is centered
                    val scale = animateFloatAsState(
                        targetValue = if (isCenter) 1.1f else 0.9f,
                        animationSpec = tween(
                            durationMillis = 150,
                            easing = FastOutSlowInEasing
                        ),
                        label = "scale"
                    )
                    
                    val alpha = animateFloatAsState(
                        targetValue = if (isCenter) 1f else 0.6f,
                        animationSpec = tween(
                            durationMillis = 150,
                            easing = FastOutSlowInEasing
                        ),
                        label = "alpha"
                    )

                    Text(
                        text = String.format("%02d", value),
                        style = if (isCenter) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.bodyLarge,
                        color = if (isCenter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .height(itemHeight)
                            .wrapContentHeight(Alignment.CenterVertically) // Center text vertically
                            .scale(scale.value)
                            .alpha(alpha.value)
                    )
                }
            }
            // Center line indicator - fix visibility issue by moving it behind text & making it more subtle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp) // Reduced height for better subtlety
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), // Reduced opacity
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.5.dp)
                    )
                    .align(Alignment.Center)
                    .zIndex(-1f) // Ensure it renders behind the text
            )
        }
        if (label != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// Helper function to get index in the range
private fun IntRange.indexOf(value: Int): Int {
    return value - first
}
