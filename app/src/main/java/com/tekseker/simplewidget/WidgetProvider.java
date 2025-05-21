public class WidgetProvider extends AppWidgetProvider {
  private static final String ACTION_REFRESH = "com.yourpackage.REFRESH_FLAG";

  @Override
  public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
    for (int id : appWidgetIds) {
      updateFlag(context, mgr, id);
      setClickHandler(context, mgr, id);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    if (ACTION_REFRESH.equals(intent.getAction())) {
      AppWidgetManager mgr = AppWidgetManager.getInstance(context);
      int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                        AppWidgetManager.INVALID_APPWIDGET_ID);
      if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
        updateFlag(context, mgr, widgetId);
      }
    }
  }

  private void setClickHandler(Context context, AppWidgetManager mgr, int widgetId) {
    Intent refreshIntent = new Intent(context, WidgetProvider.class);
    refreshIntent.setAction(ACTION_REFRESH);
    refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

    PendingIntent pi = PendingIntent.getBroadcast(
      context, widgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );

    RemoteViews views = new RemoteViews(context.getPackageName(),
                                        R.layout.widget_layout);
    views.setOnClickPendingIntent(R.id.flagImageView, pi);
    mgr.updateAppWidget(widgetId, views);
  }

  private void updateFlag(Context context, AppWidgetManager mgr, int widgetId) {
    RemoteViews views = new RemoteViews(context.getPackageName(),
                                        R.layout.widget_layout);
    new Thread(() -> {
      try {
        // 1) Fetch public IP & country code
        URL url = new URL("https://api.myip.com");
        HttpURLConnection c = (HttpURLConnection)url.openConnection();
        InputStream in = c.getInputStream();
        String json = new Scanner(in).useDelimiter("\\A").next();
        JSONObject o = new JSONObject(json);
        String countryCode = o.getString("cc").toLowerCase();

        // 2) Load flag drawable via FlagKit
        int flagRes = FlagKit.getResId(context, countryCode);

        // 3) Apply to widget
        views.setImageViewResource(R.id.flagImageView, flagRes);
        mgr.updateAppWidget(widgetId, views);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }
            }
