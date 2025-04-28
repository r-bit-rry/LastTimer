package com.lasttimer.app.ui.templates

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.FakeTimerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.UUID

@ExperimentalCoroutinesApi
class TemplatesViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTimerRepository
    private lateinit var viewModel: TemplatesViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTimerRepository()
        viewModel = TemplatesViewModel(repository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should load templates`() = runTest {
        // Given
        repository.addSampleData() // Add sample data including templates
        
        // Advance coroutines to allow the init block to complete
        advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is TemplatesViewModel.TemplatesUiState.Success)
        
        // Verify templates were loaded
        val templates = (uiState as TemplatesViewModel.TemplatesUiState.Success).templates.first()
        assertEquals(1, templates.size) // Should have one template
        assertTrue(templates.first().isTemplate)
    }
    
    @Test
    fun `createEmptyTemplate should create a basic template`() = runTest {
        // Given
        repository.clearData()
        viewModel.updateNewTemplateName("New Template")
        viewModel.updateNewTemplateCategory("Test Category")
        
        // When
        viewModel.createEmptyTemplate()
        advanceUntilIdle()
        
        // Then
        val templates = repository.getAllTemplates().first()
        assertEquals(1, templates.size)
        
        val template = templates.first()
        assertEquals("New Template", template.name)
        assertEquals("Test Category", template.category)
        assertEquals(TimerType.COUNTDOWN, template.type)
        assertEquals(60000, template.durationMillis) // 1 minute default
        assertTrue(template.isTemplate)
        assertEquals(TimerStatus.IDLE, template.status)
    }
    
    @Test
    fun `createTemplate should create template from existing timer`() = runTest {
        // Given
        repository.clearData()
        
        // Create a timer
        val timerId = UUID.randomUUID().toString()
        val timer = Timer(
            id = timerId,
            name = "Original Timer",
            type = TimerType.COUNTDOWN,
            durationMillis = 120000, // 2 minutes
            elapsedTimeMillis = 30000, // Partially completed
            status = TimerStatus.PAUSED,
            createdAt = Date(),
            lastUsedAt = Date()
        )
        repository.saveTimer(timer)
        
        // Set up for creating a template
        viewModel.showCreateTemplateDialog(timer)
        advanceUntilIdle()
        
        // Verify initial values
        assertEquals("Original Timer (Template)", viewModel.newTemplateName.value)
        
        // When
        viewModel.createTemplate()
        advanceUntilIdle()
        
        // Then
        val templates = repository.getAllTemplates().first()
        assertEquals(1, templates.size)
        
        val template = templates.first()
        assertEquals("Original Timer (Template)", template.name)
        assertEquals(TimerType.COUNTDOWN, template.type)
        assertEquals(120000, template.durationMillis)
        assertEquals(0, template.elapsedTimeMillis) // Should be reset to 0
        assertTrue(template.isTemplate)
        assertEquals(TimerStatus.IDLE, template.status) // Should be IDLE
    }
    
    @Test
    fun `createTimerFromTemplate should create timer based on template`() = runTest {
        // Given
        repository.clearData()
        
        // Create a template
        val templateId = UUID.randomUUID().toString()
        val template = Timer(
            id = templateId,
            name = "Template",
            type = TimerType.COUNTDOWN,
            durationMillis = 300000, // 5 minutes
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = Date(),
            isTemplate = true
        )
        repository.saveTimer(template)
        
        // Select the template
        viewModel.selectTemplate(template)
        
        // When
        viewModel.createTimerFromTemplate()
        advanceUntilIdle()
        
        // Then
        val timers = repository.getTimersByType(TimerType.COUNTDOWN).first()
        assertEquals(1, timers.size)
        
        val newTimer = timers.first()
        assertEquals("Template", newTimer.name)
        assertEquals(TimerType.COUNTDOWN, newTimer.type)
        assertEquals(300000, newTimer.durationMillis)
        assertEquals(0, newTimer.elapsedTimeMillis)
        assertFalse(newTimer.isTemplate)
        assertEquals(TimerStatus.IDLE, newTimer.status)
    }
    
    @Test
    fun `deleteTemplate should remove template from repository`() = runTest {
        // Given
        repository.clearData()
        
        // Create a template
        val templateId = UUID.randomUUID().toString()
        val template = Timer(
            id = templateId,
            name = "Template to Delete",
            type = TimerType.COUNTDOWN,
            durationMillis = 60000,
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = Date(),
            isTemplate = true
        )
        repository.saveTimer(template)
        
        // Verify template exists
        val beforeDelete = repository.getAllTemplates().first()
        assertEquals(1, beforeDelete.size)
        
        // When
        viewModel.deleteTemplate(templateId)
        advanceUntilIdle()
        
        // Then
        val afterDelete = repository.getAllTemplates().first()
        assertEquals(0, afterDelete.size)
    }
}
