package com.evancharlton.mileage.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evancharlton.mileage.models.ServiceInterval;

public class IntervalReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		long id = intent.getExtras().getLong(ServiceInterval._ID, -1L);

		ServiceInterval interval = new ServiceInterval(id);

		interval.raiseNotification(context);
	}
}
