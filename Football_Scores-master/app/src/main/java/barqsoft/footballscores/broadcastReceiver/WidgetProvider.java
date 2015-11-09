package barqsoft.footballscores.broadcastReceiver;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.WidgetRemoteViewService;

/**
 * Created by hojin on 15. 10. 27.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetProvider extends AppWidgetProvider {
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        for(int appWidgetId : appWidgetIds){
            RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.widget_listview);

            Intent intent=new Intent(context, MainActivity.class);
            PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                setRemoteAdapter(context, remoteViews);
            }else{
                setRemoteAdapterV11(context, remoteViews);
            }

            Intent intent1=new Intent(context, MainActivity.class);
            PendingIntent pendingIntent1= android.app.TaskStackBuilder.create(context).addNextIntentWithParentStack(intent1).getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_scores_list, pendingIntent1);

            appWidgetManager.updateAppWidget(appWidgetId,remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
        int[] appWidgetIds=appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_scores_list);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews remoteViews){
        remoteViews.setRemoteAdapter(R.id.widget_scores_list, new Intent(context, WidgetRemoteViewService.class));
    }

    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews remoteViews){
        remoteViews.setRemoteAdapter(0, R.id.widget_scores_list, new Intent(context, WidgetRemoteViewService.class));
    }
}
