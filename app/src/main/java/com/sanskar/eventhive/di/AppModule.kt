package com.sanskar.eventhive.di

import android.app.Application
import android.content.Context
import com.sanskar.eventhive.data.repository.CloudFunctionApi
import com.sanskar.eventhive.Notification.FirebaseMessaging.FcmApi
import com.sanskar.eventhive.data.repository.Inteface.AuthRepository
import com.sanskar.eventhive.data.repository.Implementation.AuthRepositoryImpl
import com.sanskar.eventhive.data.repository.Inteface.ClubCategoryRepository
import com.sanskar.eventhive.data.repository.Implementation.ClubCategoryRepositoryImpl
import com.sanskar.eventhive.data.repository.Inteface.ClubRepository
import com.sanskar.eventhive.data.repository.Implementation.ClubRepositoryImpl
import com.sanskar.eventhive.data.repository.Inteface.EventRepository
import com.sanskar.eventhive.data.repository.Implementation.EventRepositoryImpl
import com.sanskar.eventhive.data.repository.Implementation.NotificationRepositoryImpl
import com.sanskar.eventhive.data.repository.Implementation.SubEventRepositoryImpl
import com.sanskar.eventhive.data.repository.Implementation.TicketRepositoryImpl
import com.sanskar.eventhive.data.repository.Inteface.UserRepository
import com.sanskar.eventhive.data.repository.Implementation.UserRepositoryImpl
import com.sanskar.eventhive.data.repository.Inteface.NotificationRepository
import com.sanskar.eventhive.data.repository.Inteface.SubEventRepository
import com.sanskar.eventhive.data.repository.Inteface.TicketRepository
import com.sanskar.eventhive.data.repository.Implementation.BugReportRepositoryImpl
import com.sanskar.eventhive.data.repository.Inteface.BugReportRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    fun provideStorage (): FirebaseStorage = Firebase.storage

    @Provides
    fun provideFirebaseMessaging (): FirebaseMessaging = Firebase.messaging

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = Firebase.database

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository{
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        storage: FirebaseStorage,
    ): UserRepository {
        return UserRepositoryImpl(firestore, firebaseAuth, storage)
    }

    @Provides
    @Singleton
    fun provideClubCategoryRepository(
        firestore: FirebaseFirestore
    ): ClubCategoryRepository {
        return ClubCategoryRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideClubRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ClubRepository {
        return ClubRepositoryImpl(firestore,storage)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
    ): EventRepository {
        return EventRepositoryImpl(firestore, storage)
    }


    @Provides
    @Singleton
    fun provideTicketRepository(
        firestore: FirebaseFirestore
    ): TicketRepository {
        return TicketRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideSubEventRepository(
        firestore: FirebaseFirestore
    ): SubEventRepository {
        return SubEventRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideFcmApi(): FcmApi = Retrofit.Builder()
        .baseUrl("https://eventhivenotificationserver.onrender.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FcmApi::class.java)

    @Provides
    @Singleton
    fun provideCloudFunctionApi(): CloudFunctionApi = Retrofit.Builder()
        .baseUrl("https://us-central1-sit-event.cloudfunctions.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CloudFunctionApi::class.java)

    @Provides
    @Singleton
    fun provideFcmRepository(
        api: FcmApi,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
    ): NotificationRepository = NotificationRepositoryImpl(api, firestore, firebaseAuth)

    @Provides
    @Singleton
    fun provideBugReportRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): BugReportRepository = BugReportRepositoryImpl(firestore, storage)


}
