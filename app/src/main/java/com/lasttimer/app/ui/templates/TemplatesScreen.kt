package com.lasttimer.app.ui.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerType
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTimer: (String) -> Unit,
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isCreateTemplateDialogVisible by viewModel.isCreateTemplateDialogVisible.collectAsState()
    val isCreateFromTemplateDialogVisible by viewModel.isCreateFromTemplateDialogVisible.collectAsState()
    val newTemplateName by viewModel.newTemplateName.collectAsState()
    val newTemplateCategory by viewModel.newTemplateCategory.collectAsState()
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()
    
    var templates by remember { mutableStateOf<List<Timer>>(emptyList()) }
    
    // Collect templates when UI state is Success
    when (uiState) {
        is TemplatesViewModel.TemplatesUiState.Success -> {
            LaunchedEffect(Unit) {
                templates = (uiState as TemplatesViewModel.TemplatesUiState.Success)
                    .templates.first()
            }
        }
        else -> {}
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer Templates") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val scale = remember { Animatable(0f) }
            
            // Animate the FAB when it appears
            LaunchedEffect(Unit) {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            
            FloatingActionButton(
                onClick = { viewModel.showCreateTemplateDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Template")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TemplatesViewModel.TemplatesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TemplatesViewModel.TemplatesUiState.Error -> {
                    Text(
                        text = "Error: ${(uiState as TemplatesViewModel.TemplatesUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                is TemplatesViewModel.TemplatesUiState.Success -> {
                    if (templates.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No templates yet",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.showCreateTemplateDialog() }) {
                                Text("Create Template")
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(templates, key = { it.id }) { template ->
                                // Create a transition state for staggered animation
                                val visibleState = remember {
                                    MutableTransitionState(false).apply { targetState = true }
                                }
                                
                                AnimatedVisibility(
                                    visibleState = visibleState,
                                    enter = fadeIn(animationSpec = tween(300)) + 
                                            scaleIn(
                                                initialScale = 0.8f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            ),
                                    exit = fadeOut(animationSpec = tween(300)) + scaleOut()
                                ) {
                                    TemplateItem(
                                        template = template,
                                        onUseTemplate = { viewModel.selectTemplate(template) },
                                        onDeleteTemplate = { viewModel.deleteTemplate(template.id) }
                                    )
                                }
                                
                                // Add a small spacer between items
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Create Template Dialog
    if (isCreateTemplateDialogVisible) {
        // Dialog entrance animation
        val dialogAnimation = remember { androidx.compose.animation.core.Animatable(0.9f) }
        
        LaunchedEffect(Unit) {
            dialogAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
        }
        
        AlertDialog(
            onDismissRequest = { viewModel.hideCreateTemplateDialog() },
            title = { Text("Create Timer Template") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTemplateName,
                        onValueChange = { viewModel.updateNewTemplateName(it) },
                        label = { Text("Template Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newTemplateCategory,
                        onValueChange = { viewModel.updateNewTemplateCategory(it) },
                        label = { Text("Category (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.createEmptyTemplate() }) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.hideCreateTemplateDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Create Timer from Template Dialog
    if (isCreateFromTemplateDialogVisible && selectedTemplate != null) {
        // Dialog entrance animation
        val dialogAnimation = remember { androidx.compose.animation.core.Animatable(0.9f) }
        
        LaunchedEffect(Unit) {
            dialogAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
        }
        
        AlertDialog(
            onDismissRequest = { viewModel.hideCreateFromTemplateDialog() },
            title = { Text("Create Timer from Template") },
            text = {
                Column {
                    Text("Do you want to create a new timer from the template '${selectedTemplate?.name}'?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createTimerFromTemplate()
                        // Navigate to the timer screen if needed
                        selectedTemplate?.id?.let { onNavigateToTimer(it) }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.hideCreateFromTemplateDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateItem(
    template: Timer,
    onUseTemplate: () -> Unit,
    onDeleteTemplate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onUseTemplate
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    template.category?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = when (template.type) {
                            TimerType.COUNTDOWN -> {
                                val hours = template.durationMillis?.div(3600000) ?: 0
                                val minutes = (template.durationMillis?.div(60000) ?: 0) % 60
                                val seconds = (template.durationMillis?.div(1000) ?: 0) % 60
                                
                                if (hours > 0) {
                                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                                } else {
                                    String.format("%02d:%02d", minutes, seconds)
                                }
                            }
                            TimerType.STOPWATCH -> "Stopwatch"
                            TimerType.DATE_COUNTDOWN -> {
                                template.targetDate?.let {
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                                } ?: "Date Countdown"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Created: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(template.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onUseTemplate) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Use Template",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(onClick = onDeleteTemplate) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Template",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
