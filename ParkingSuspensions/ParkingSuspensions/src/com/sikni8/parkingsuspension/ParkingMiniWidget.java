package com.sikni8.parkingsuspension;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class ParkingMiniWidget extends AppWidgetProvider {
	DateFormat df = new SimpleDateFormat("EEEEE, LLLL d", Locale.US);
	String[] suspendedDates = {
			"Tuesday, January 1",
			"Monday, January 21",
			"Sunday, February 10",
			"Tuesday, February 12",
			"Wednesday, February 13",
			"Monday, February 18",
			"Sunday, February 24",
			"Tuesday, March 26",
			"Wednesday, March 27",
			"Thursday, March 28",
			"Friday, March 29",
			"Monday, April 1",
			"Tuesday, April 2",
			"Thursday, May 2",
			"Friday, May 3",
			"Thursday, May 9",
			"Wednesday, May 15",
			"Thursday, May 16",
			"Monday, May 27",
			"Thursday, July 4",
			"Wednesday, August 7",
			"Thursday, August 8",
			"Friday, August 9",
			"Thursday, August 15",
			"Monday, September 2",
			"Thursday, September 5",
			"Friday, September 6",
			"Saturday, September 14",
			"Thursday, September 19",
			"Friday, September 20",
			"Thursday, September 26",
			"Friday, September 27",
			"Monday, October 14",
			"Tuesday, October 15",
			"Wednesday, October 16",
			"Thursday, October 17",
			"Friday, November 1",
			"Sunday, November 3",
			"Tuesday, November 5",
			"Monday, November 11",
			"Thursday, November 28",
			"Sunday, December 8",
			"Wednesday, December 25"
	};

	public static String CLOCK_WIDGET_UPDATE = "CLOCK_WIDGET_UPDATE";

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
			Toast.makeText(context, "onReceiver()", Toast.LENGTH_LONG).show();
		}
	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		Log.i("ExampleWidget",	"Updating widgets " + Arrays.asList(appWidgetIds));

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Create an Intent to launch MainActivity
			Intent intent = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,	intent, 0);

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widgetminilayout);
			
			views.setOnClickPendingIntent(R.id.ivCal, pendingIntent);
				
			if (Arrays.asList(suspendedDates).contains(df.format(Calendar.getInstance(Locale.US).getTime()))) {
				//parking is suspended
				views.setImageViewResource(R.id.ivCal, R.drawable.suspended);
			}
			else {
				//parking not suspended
				views.setImageViewResource(R.id.ivCal, R.drawable.notsuspended);
			}

			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	public static void updateAppWidget(Context context,	AppWidgetManager appWidgetManager, int appWidgetId) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),	R.layout.widgetminilayout);
		appWidgetManager.updateAppWidget(appWidgetId, updateViews);
	}
}