package com.example.android.mygarden;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider
{
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void updateAppWidget( Context context,
                                        AppWidgetManager appWidgetManager,
                                        int imgRes,
                                        long plantId,
                                        boolean showWater,
                                        int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        RemoteViews rv = (width < 300) ?
                    getSinglePlantRemoveView(context, imgRes, plantId, showWater):
                    getGardenGridRemoteView(context);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    private static RemoteViews getGardenGridRemoteView(Context context)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.widget_grid_view, intent);

        Intent appIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_grid_view, appPendingIntent);
        views.setEmptyView(R.id.widget_grid_view, R.id.empty_view);
        return views;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager,
                                          int appWidgetId,
                                          Bundle newOptions)
    {
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    // TODO (2): Modify updatePlantWidgets and updateAppWidget to pass the plant ID as well as a boolean
    // to show/hide the water button
    public static void updatePlantWidgets( Context context,
                                           AppWidgetManager appWidgetManager,
                                           int imgRes,
                                           int[] appWidgetIds,
                                           long plantId,
                                           boolean showWaterButton)
    {
        for(int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, imgRes, plantId, showWaterButton, appWidgetId);
        }
    }

    private static RemoteViews getSinglePlantRemoveView(Context context,
                                                        int imgRes,
                                                        long plantId,
                                                        boolean showWaterButton)
    {
        Intent intent;
        if(PlantContract.INVALID_PLANT_ID == plantId)
        {
            intent = new Intent(context, MainActivity.class);
        }
        else
        {
            Log.d(PlantWidgetProvider.class.getSimpleName(), "plantId="+plantId);
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }



        // Construct the RemoteViews object
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        int visibility = (showWaterButton)?
                            View.VISIBLE:
                            View.INVISIBLE;

        views.setViewVisibility(R.id.widget_water_button, visibility);
        views.setImageViewResource(R.id.widget_plant_image, imgRes);
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        views.setTextViewText(R.id.widget_plant_id, String.valueOf(plantId));

        // Add the wateringservice click handler
        Intent wateringIntent = new Intent(context, PlantWateringService.class);
        wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
        wateringIntent.putExtra(PlantWateringService.EXTRA_PLANT_ID, plantId);
        PendingIntent wateringPendingIntent = PendingIntent.getService(
                context,
                0,
                wateringIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);
        return views;
    }

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        // for (int appWidgetId : appWidgetIds)
        //{
        //    updateAppWidget(context, appWidgetManager, R.layout.plant_widget, appWidgetId);
        //}

        //Start the intent service update widget action, the service takes care of updating the widgets UI
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    @Override
    public void onEnabled(Context context)
    {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }
}

