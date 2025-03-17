package com.example.senefavores.di

import com.example.senefavores.data.remote.SupabaseClient
import com.example.senefavores.data.repository.FavorRepository
import com.example.senefavores.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClient() // Now using a class constructor
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
}
