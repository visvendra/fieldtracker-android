package com.allsmart.fieldtracker.utils;

import android.text.format.DateUtils;

import com.allsmart.fieldtracker.model.SimpleGeofence;
import com.google.android.gms.location.Geofence;

import java.util.HashMap;

public class SimpleGeofenceStore
{
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;
	protected HashMap<String, SimpleGeofence> geofences = new HashMap<String, SimpleGeofence>();
	public static SimpleGeofenceStore getInstance(String strStoreName,Double latitute,Double longitute,int radius){
		return new SimpleGeofenceStore(strStoreName,latitute,longitute,radius);
	}
	private SimpleGeofenceStore(String strStoreName,Double latitute,Double longitute,int radius) {
		geofences.put(strStoreName, new SimpleGeofence(strStoreName, latitute, longitute,radius, GEOFENCE_EXPIRATION_IN_MILLISECONDS, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT));
	}
	public HashMap<String, SimpleGeofence> getSimpleGeofences() {
		return this.geofences;
	}
}