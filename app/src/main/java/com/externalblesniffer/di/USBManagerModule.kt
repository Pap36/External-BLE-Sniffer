package com.externalblesniffer.di

import android.content.Context
import android.hardware.usb.UsbManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class USBManagerModule {
    companion object {
        @Provides
        @Singleton
        fun provideUSBManager(
            @ApplicationContext context: Context
        ): UsbManager {
            return (context.getSystemService(Context.USB_SERVICE) as UsbManager)
        }
    }
}