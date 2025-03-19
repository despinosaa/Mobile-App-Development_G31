package com.example.senefavores.di

import com.example.senefavores.data.remote.SupabaseManagement
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
    fun provideSupabaseManager(): SupabaseManagement {
        return SupabaseManagement() // Now using a class constructor
    }

    @Provides
    @Singleton
    fun provideFavorRepository(supabaseClient: SupabaseManagement): FavorRepository {
        return FavorRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideUserRepository(supabaseClient: SupabaseManagement): UserRepository {
        return UserRepository(supabaseClient)
    }
}
