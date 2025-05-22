package com.tekseker.simplewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.murgupluoglu.flagkit.FlagKit;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WidgetProvider extends AppWidgetProvider {
    private static final String ACTION_REFRESH = "com.tekseker.simplewidget.REFRESH_FLAG";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateFlag(context, appWidgetManager, widgetId);
            setClickHandler(context, appWidgetManager, widgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                              AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateFlag(context, appWidgetManager, widgetId);
            }
        }
    }

    private void setClickHandler(Context context,
                                 AppWidgetManager appWidgetManager,
                                 int widgetId) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(ACTION_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pi = PendingIntent.getBroadcast(
            context, widgetId, intent, flags
        );

        RemoteViews views = new RemoteViews(
            context.getPackageName(),
            R.layout.widget_layout
        );
        views.setOnClickPendingIntent(R.id.flagImageView, pi);
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    private void updateFlag(Context context,
                            AppWidgetManager appWidgetManager,
                            int widgetId) {
        RemoteViews views = new RemoteViews(
            context.getPackageName(),
            R.layout.widget_layout
        );

        new Thread(() -> {
            try {
                URL url = new URL("https://api.myip.com");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                String json = new Scanner(in).useDelimiter("\\A").next();
                JSONObject obj = new JSONObject(json);
                String cc = obj.getString("cc").toLowerCase();

                int resId = FlagKit.getResId(context, cc);
                views.setImageViewResource(R.id.flagImageView, resId);
                appWidgetManager.updateAppWidget(widgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
            }
