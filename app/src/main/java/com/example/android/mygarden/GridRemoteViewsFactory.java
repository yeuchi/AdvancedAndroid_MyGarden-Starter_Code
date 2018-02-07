package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

/**
 * Created by ctyeung on 2/7/18.
 */

public class GridWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

public class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
    Context mContext;
    Cursor mCursor;

    public GridRemoteViewsFactory(Context applicationContext)
    {
        mContext = applicationContext;
    }

    @Override
    public void onCreate()
    {

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position)
    {
        if(null==mCursor || 0==mCursor.getCount())
            return null;

        mCursor.moveToPosition(position);

        int idIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int createTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

        long plantId = mCursor.getLong(idIndex);
        int plantType = mCursor.getInt(plantTypeIndex);
        long createdAt = mCursor.getLong(createTimeIndex);
        long wateredAt = mCursor.getLong(waterTimeIndex);
        long timeNow = System.currentTimeMillis();

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        //update plant image
        int imgRes = PlantUtils.getPlantImageRes(mContext,
                                        timeNow-createdAt,
                                        timeNow-wateredAt,
                                                 plantType);
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setTextViewText(R.id.widget_plant_id, String.valueOf(plantId));
        views.setViewVisibility(R.id.widget_water_button, View.GONE);
        return views;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public void onDataSetChanged()
    {
        Uri PLANT_URI = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PlantContract.PATH_PLANTS).build();
        if(null!=mCursor)
            mCursor.close();

        mCursor = mContext.getContentResolver().query(PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME);
    }

    @Override
    public void onDestroy()
    {
        mCursor.close();
    }

    @Override
    public int getCount()
    {
        return (null==mCursor)?
                0:
                mCursor.getCount();
    }
}
