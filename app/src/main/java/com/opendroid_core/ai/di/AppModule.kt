// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.di

import android.content.Context
import com.opendroid_core.ai.core.security.AndroidProviderCredentialStore
import com.opendroid_core.ai.core.security.AndroidSensitiveMemoryStore
import com.opendroid_core.ai.core.security.AndroidUserProfileStore
import com.opendroid_core.ai.core.security.ProviderCredentialStore
import com.opendroid_core.ai.core.security.SensitiveMemoryStore
import com.opendroid_core.ai.core.security.UserProfileStore
import com.opendroid_core.ai.core.settings.AndroidAppSettingsStore
import com.opendroid_core.ai.core.settings.AppSettingsStore
import com.opendroid_core.ai.accessibility.AndroidCallFlowVerifier
import com.opendroid_core.ai.accessibility.CallFlowVerifier
import com.opendroid_core.ai.actions.AndroidMediaPlaybackVerifier
import com.opendroid_core.ai.actions.ActionDispatcher
import com.opendroid_core.ai.actions.MediaPlaybackVerifier
import com.opendroid_core.ai.core.agent.ActionSequenceExecutor
import com.opendroid_core.ai.core.agent.AgentLoop
import com.opendroid_core.ai.core.agent.ChatAgent
import com.opendroid_core.ai.core.llm.OnDeviceEngineReleaser
import com.opendroid_core.ai.core.llm.providers.LiteRTLMProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideProviderCredentialStore(@ApplicationContext context: Context): ProviderCredentialStore {
        return AndroidProviderCredentialStore(context)
    }

    @Provides
    @Singleton
    fun provideUserProfileStore(@ApplicationContext context: Context): UserProfileStore {
        return AndroidUserProfileStore(context)
    }

    @Provides
    @Singleton
    fun provideSensitiveMemoryStore(@ApplicationContext context: Context): SensitiveMemoryStore {
        return AndroidSensitiveMemoryStore(context)
    }

    @Provides
    @Singleton
    fun provideAppSettingsStore(@ApplicationContext context: Context): AppSettingsStore {
        return AndroidAppSettingsStore(context)
    }

    @Provides
    @Singleton
    fun provideSocialCredentialStore(@ApplicationContext context: Context): com.opendroid_core.ai.core.security.SocialCredentialStore {
        return com.opendroid_core.ai.core.security.AndroidSocialCredentialStore(context)
    }

    @Provides
    @Singleton
    fun provideCallFlowVerifier(): CallFlowVerifier = AndroidCallFlowVerifier()

    @Provides
    @Singleton
    fun provideMediaPlaybackVerifier(): MediaPlaybackVerifier = AndroidMediaPlaybackVerifier()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // Lazy: LiteRTLMProvider itself depends on ModelRepository, which takes this releaser.
    @Provides
    @Singleton
    fun provideOnDeviceEngineReleaser(
        liteRTProvider: dagger.Lazy<LiteRTLMProvider>
    ): OnDeviceEngineReleaser = OnDeviceEngineReleaser { modelDir ->
        liteRTProvider.get().releaseIfLoadedFrom(modelDir)
    }

    @Provides
    @Singleton
    fun provideActionSequenceExecutor(
        actionDispatcher: dagger.Lazy<ActionDispatcher>
    ): ActionSequenceExecutor = ActionSequenceExecutor(
        executeAction = { action, params, context ->
            actionDispatcher.get().execute(action, params, context)
        },
        hasAction = { action -> actionDispatcher.get().hasAction(action) }
    )

    @Provides
    fun provideChatAgent(agentLoop: AgentLoop): ChatAgent = agentLoop
}
