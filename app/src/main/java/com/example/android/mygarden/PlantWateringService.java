package com.example.android.mygarden;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

/**
 * Created by ctyeung on 2/3/18.
 */

public class PlantWateringService extends IntentService
{
    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";
    public static final String ACITON_UPDATE_PLANT_WIDGETS = "com.example.android.mygarden.action.update_plant_widgets";

    public PlantWateringService()
    {
        super("PlantWateringService");
    }

    public static void startActionUpdatePlantWidgets(Context context)
    {
        startAction(context, ACITON_UPDATE_PLANT_WIDGETS);
    }

    public static void startActionWaterPlants(Context context)
    {
        startAction(context, ACTION_WATER_PLANTS);
    }

    protected static void startAction(Context context, String action)
    {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(null != intent)
        {
            final String action = intent.getAction();
            if(ACTION_WATER_PLANTS.equals(action))
            {
                handleActionWaterPlants();
            }
            else if(ACITON_UPDATE_PLANT_WIDGETS.equals(action)) {
                handleActionUpdatePlantWidgets();
            }
        }
    }

    protected void handleActionUpdatePlantWidgets()
    {
        Uri PLANTS_URI = getPlantsUri();
        Cursor cursor = getContentResolver().query(PLANTS_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        // extract plant detail
        int imgRes = R.drawable.grass;
        if(null!= cursor && cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

            long timeNow = System.currentTimeMillis();
            long wateredAt = cursor.getLong(waterTimeIndex);
            long createdAt = cursor.getLong(createTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);

            cursor.close();
            imgRes = PlantUtils.getPlantImageRes(this,
                                                (timeNow-createdAt),
                                                (timeNow-wateredAt),
                                                plantType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));
        PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, imgRes, appWidgetIds);
    }

    protected void handleActionWaterPlants()
    {
        Uri PLANTS_URI = getPlantsUri();
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // Update only plants that are still alive
        getContentResolver().update(PLANTS_URI,
                            contentValues,
                            PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",
                            new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }


    protected  Uri getPlantsUri()
    {
        Uri PLANTS_URI = com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(com.example.android.mygarden.provider.PlantContract.PATH_PLANTS).build();

        return PLANTS_URI;
    }
}
