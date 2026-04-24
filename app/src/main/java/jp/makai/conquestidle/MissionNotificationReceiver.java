package com.makaijr;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public final class MissionNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "mission_complete";
    private static final int REQUEST_CODE = 2001;
    private static final int NOTIFICATION_ID = 2002;

    @Override
    public void onReceive(Context context, Intent intent) {
        GameState gameState = new GameState(context);
        MissionEngine.syncActiveMission(gameState, System.currentTimeMillis());

        String pendingReport = gameState.getPendingMissionReport();
        if (pendingReport.isEmpty()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "遠征完了通知",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("出撃が完了した時に通知する。");
            notificationManager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, TitleActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = extractNotificationTitle(pendingReport);
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText("司令部へ戻って戦果を確認する。")
                    .setStyle(new Notification.BigTextStyle().bigText(pendingReport))
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build();
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText("司令部へ戻って戦果を確認する。")
                    .setStyle(new Notification.BigTextStyle().bigText(pendingReport))
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build();
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    static void schedule(Context context, ActiveMission mission) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null || mission.expectedCompletionAtEpochMillis <= 0L) {
            return;
        }

        PendingIntent pendingIntent = buildPendingIntent(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    mission.expectedCompletionAtEpochMillis,
                    pendingIntent
            );
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    mission.expectedCompletionAtEpochMillis,
                    pendingIntent
            );
            return;
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, mission.expectedCompletionAtEpochMillis, pendingIntent);
    }

    static void cancel(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(buildPendingIntent(context));
    }

    private static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, MissionNotificationReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static String extractNotificationTitle(String pendingReport) {
        String[] lines = pendingReport.split("\n");
        if (lines.length > 0 && !lines[0].isEmpty()) {
            return "遠征完了: " + lines[0];
        }
        return "遠征完了";
    }
}
