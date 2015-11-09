package barqsoft.footballscores.service;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by hojin on 15. 10. 26.
 */
public class WidgetRemoteViewService extends RemoteViewsService {
    public WidgetRemoteViewService() {

    }

    private static final String[] SCORE_COLUMNS={
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };

    static final int INDEX_DATE_COL=1;
    static final int INDEX_TIME_COL=2;
    static final int INDEX_LEAGUE_COL=4;
    static final int INDEX_HOME_COL=5;
    static final int INDEX_AWAY_COL=6;
    static final int INDEX_HOME_GOALS_COL=7;
    static final int INDEX_AWAY_GOALS_COL=8;

    private static final String SCORES_FROM_DATE=
            DatabaseContract.scores_table.DATE_COL + " >= ?";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor cursor=null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(cursor !=null){
                    cursor.close();
                }

                final long identityToken= Binder.clearCallingIdentity();

                String[] fromDateArray=new String[1];
                Date fromDate=new Date(System.currentTimeMillis()+((0-7)*86400000));
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
                fromDateArray[0]=simpleDateFormat.format(fromDate);

                Uri AllScoresUri=DatabaseContract.scores_table.buildScores();
                cursor= getContentResolver().query(AllScoresUri,
                        SCORE_COLUMNS,
                        SCORES_FROM_DATE,
                        fromDateArray,
                        DatabaseContract.scores_table.DATE_COL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }


            @Override
            public void onDestroy() {
                if(cursor !=null){
                    cursor.close();
                    cursor=null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if(position == AdapterView.INVALID_POSITION ||
                        cursor==null || !cursor.moveToPosition(position)){
                    return null;
                }
                RemoteViews remoteViews=new RemoteViews(getPackageName(), R.layout.widget_list_item);

                String formattedScore=Utilies.getScores(cursor.getInt(INDEX_HOME_GOALS_COL), cursor.getInt(INDEX_AWAY_GOALS_COL));

                remoteViews.setTextViewText(R.id.widget_date, cursor.getString(INDEX_DATE_COL));
                remoteViews.setTextViewText(R.id.widget_time, cursor.getString(INDEX_TIME_COL));
                remoteViews.setTextViewText(R.id.widget_league, Utilies.getLeague(cursor.getInt(INDEX_LEAGUE_COL)));
                remoteViews.setTextViewText(R.id.widget_home_team, cursor.getString(INDEX_HOME_COL));
                remoteViews.setTextViewText(R.id.widget_score, formattedScore);
                remoteViews.setTextViewText(R.id.widget_away_team, cursor.getString(INDEX_AWAY_COL));

                final Intent fillInIntent=new Intent();
                Uri AllScoresUri1=DatabaseContract.scores_table.buildScores();
                fillInIntent.setData(AllScoresUri1);
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}


























