package com.pm.ridepay.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

//  From Android 8.0 versions Android requires all notifications to belong to a Notification Channel.
// So that user can stop the notification if user wants.
// If we do not create a channel and post a notification android will block it and nothing will appear on screen

object NotificationChannels{
    const val PAYMENTS = "ridepay_payments"

    fun create(context: Context){
        val channel = NotificationChannel(
            PAYMENTS,
            "Payments and  rides", // channel-name
            NotificationManager.IMPORTANCE_HIGH // pops up as heads-up banner alerts with sound and vibration
        ).apply {
            description = "Ride completed and payment received alerts"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
        // this will tell android operating system to create and save the notification category during app startup
    }
}