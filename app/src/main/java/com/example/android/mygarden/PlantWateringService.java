package com.example.android.mygarden;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

    public static void startActionWaterPlants(Context context)
    {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }

    public PlantWateringService()
    {
        super("PlantWateringService");
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
        }
    }

    protected void handleActionWaterPlants()
    {
        Uri PLANTS_URI = com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI.buildUpon()
                        .appendPath(com.example.android.mygarden.provider.PlantContract.PATH_PLANTS).build();

        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // Update only plants that are still alive
        getContentResolver().update(PLANTS_URI,
                            contentValues,
                            PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",
                            new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
    }


}
