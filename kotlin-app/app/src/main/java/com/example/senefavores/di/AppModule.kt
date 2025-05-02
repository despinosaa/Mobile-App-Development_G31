package com.example.senefavores.di

import android.content.Context
import com.example.senefavores.data.remote.SupabaseManagement
import com.example.senefavores.data.repository.FavorRepository
import com.example.senefavores.data.repository.UserRepository
import com.example.senefavores.util.LocationCache
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseManagement().supabase
    }

    @Provides
    @Singleton
    fun provideLocationCache(): LocationCache {
        return LocationCache()
    }

    @Provides
    @Singleton
    fun provideLocationHelper(
        @ApplicationContext context: Context,
        locationCache: LocationCache
    ): LocationHelper {
        return LocationHelper(context, locationCache)
    }

    @Provides
    @Singleton
    fun provideFavorRepository(supabaseClient: SupabaseClient): FavorRepository {
        return FavorRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideUserRepository(supabaseClient: SupabaseClient): UserRepository {
        return UserRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideTelemetryLogger(
        supabaseClient: SupabaseClient,
        @ApplicationContext context: Context
    ): TelemetryLogger {
        return TelemetryLogger(supabaseClient, context)
    }

    @Provides
    @Singleton
    fun provideNetworkChecker(@ApplicationContext context: Context): NetworkChecker {
        return NetworkChecker(context)
    }
}