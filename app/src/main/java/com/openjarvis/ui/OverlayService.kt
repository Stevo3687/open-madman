package com.openjarvis.ui

import com.openjarvis.AssistantUpgrade
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.NotificationCompat
import com.openjarvis.R
import com.openjarvis.agent.AgentCore
import com.openjarvis.agent.AgentState
import com.openjarvis.bridge.SocketServer
import com.openjarvis.graphify.GraphifyRepository
import com.openjarvis.graphify.nodes.TaskNode
import com.openjarvis.ui.overlay.FloatingOverlayWidget
import com.openjarvis.ui.theme.OpenJarvisTheme
import com.openjarvis.voice.VoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayService : LifecycleService() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var overlayLayoutParams: WindowManager.LayoutParams? = null
    
    private lateinit var agentCore: AgentCore
    private lateinit var voiceManager: VoiceManager
    private lateinit var graphifyRepo: GraphifyRepository
    private lateinit var socketServer: SocketServer
    private lateinit var assistantUpgrade: AssistantUpgrade
    private val recentTasks = MutableStateFlow<List<TaskNode>>(emptyList())
    
    private var isInitialized = false

    override fun onCreate() {
        super.onCreate()
        
        agentCore = AgentCore(this)
        voiceManager = VoiceManager(this)
        assistantUpgrade = AssistantUpgrade(this)
        agentCore.setCommandFeedbackListener(voiceManager::speak)
        graphifyRepo = GraphifyRepository(this)
        socketServer = SocketServer(this, agentCore, graphifyRepo)
        
        startForeground(NOTIFICATION_ID, createNotification())

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        showOverlay()
        lifecycleScope.launch { initializeServices() }
        
        socketServer.start()
        
        lifecycleScope.launch(Dispatchers.IO) {
            recentTasks.value = graphifyRepo.getRecentTasks(10)
        }
    }

    private fun showOverlay() {
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = manager

        val density = resources.displayMetrics.density
        val view = ComposeView(this)
        view.setTag(androidx.lifecycle.runtime.R.id.view_tree_lifecycle_owner, this)
        view.setContent {
            OpenJarvisTheme {
                val agentState by agentCore.state.collectAsState()
                val taskList by recentTasks.collectAsState()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    FloatingOverlayWidget(
                        agentState = agentState,
                        voiceManager = voiceManager,
                        recentTasks = taskList,
                        onCommand = ::executeCommand,
                        onCollapse = {},
                        onExpandedChanged = ::setOverlayExpanded,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            (140 * density).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (32 * density).toInt()
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        overlayView = view
        overlayLayoutParams = layoutParams
        manager.addView(view, layoutParams)
    }

    private fun setOverlayExpanded(expanded: Boolean) {
        val view = overlayView ?: return
        val params = overlayLayoutParams ?: return
        val density = resources.displayMetrics.density
        params.width = if (expanded) {
            (resources.displayMetrics.widthPixels - 32 * density).toInt()
        } else {
            (140 * density).toInt()
        }
        windowManager?.updateViewLayout(view, params)
    }
    
    private suspend fun initializeServices() {
        if (isInitialized) return
        isInitialized = true
        
        // Heavy initializations deferred here
        withContext(Dispatchers.IO) {
            try {
                graphifyRepo.getRecentTasks(10)
            } catch (e: Exception) {
                // Graphify initialization
            }
        }
        
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.hasExtra(EXTRA_VOICE_ENABLED) == true) {
            voiceManager.setVoiceEnabled(intent.getBooleanExtra(EXTRA_VOICE_ENABLED, false))
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        overlayView?.let { view ->
            if (view.parent != null) {
                windowManager?.removeView(view)
            }
            view.disposeComposition()
        }
        overlayView = null
        socketServer.stop()
        voiceManager.release()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${assistantUpgrade.activePersona.displayName} Active")
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun getAgentCore() = agentCore
    fun getVoiceManager() = voiceManager
    fun getRecentTasks() = recentTasks.value

    fun executeCommand(command: String) {
        agentCore.executeTask(command)
        
        // Refresh tasks after execution
        lifecycleScope.launch(Dispatchers.IO) {
            recentTasks.value = graphifyRepo.getRecentTasks(10)
        }
    }

    companion object {
        private const val CHANNEL_ID = "jarvis_overlay_channel"
        private const val NOTIFICATION_ID = 1

        private const val EXTRA_VOICE_ENABLED = "voice_enabled"

        fun start(context: Context, voiceEnabled: Boolean = false) {
            val intent = Intent(context, OverlayService::class.java)
                .putExtra(EXTRA_VOICE_ENABLED, voiceEnabled)
            context.startForegroundService(intent)
        }
    }
}