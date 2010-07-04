/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package ti.modules.titanium.accelerometer;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiSensorHelper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerModule
	extends TiModule
{
	private static final String LCAT = "AccelerometerModule";
	private static final boolean DBG = TiConfig.LOGD;

	public static final String EVENT_UPDATE = "update";

	protected TiSensorHelper sensorHelper;

	protected SensorEventListener updateListener;

	protected boolean sensorAttached;
	protected boolean listeningForUpdate;

	protected long lastEventInUpdate;

	protected float last_x;
	protected float last_y;
	protected float last_z;

	public AccelerometerModule(TiContext tiContext)
	{
		super(tiContext);

		sensorHelper = new TiSensorHelper();
		updateListener = createUpdateListener();

		sensorAttached = false;
		listeningForUpdate = false;

		tiContext.addOnEventChangeListener(this);
	}

	@Override
	public TiDict getConstants() {
		return null;
	}

	protected SensorEventListener createUpdateListener() {
		return new SensorEventListener()
		{

			public void onAccuracyChanged(Sensor sensor, int accuracy)
			{

			}

			public void onSensorChanged(SensorEvent event)
			{
				if (event.timestamp - lastEventInUpdate > 100) {
					lastEventInUpdate = event.timestamp;

					float x = event.values[SensorManager.DATA_X];
					float y = event.values[SensorManager.DATA_Y];
					float z = event.values[SensorManager.DATA_Z];

					TiDict data = new TiDict();
					data.put("type", EVENT_UPDATE);
					data.put("timestamp", lastEventInUpdate);
					data.put("x", x);
					data.put("y", y);
					data.put("z", z);
					fireEvent(EVENT_UPDATE, data);
				}
			}
		};
	}

	@Override
	public void listenerAdded(String eventName, int count, TiProxy proxy) {
		super.listenerAdded(eventName, count, proxy);

		if (eventName != null && eventName.equals(EVENT_UPDATE)) {
			if (proxy != null && proxy.equals(this)) {
				if (!listeningForUpdate) {
					sensorAttached = sensorHelper.attach(getTiContext().getActivity());

					manageUpdateListener(true);
				}
			}
		}
	}

	@Override
	public void listenerRemoved(String eventName, int count, TiProxy proxy) {
		super.listenerRemoved(eventName, count, proxy);
		if (eventName != null && eventName.equals(EVENT_UPDATE)) {
			if (proxy != null && proxy.equals(this)) {
				if (count == 0) {
					if (sensorAttached) {
						manageUpdateListener(false);

						sensorHelper.detach();
						sensorAttached = false;
					}
				}
			}
		}
	}

	protected void manageUpdateListener(boolean register)
	{
		if (sensorAttached) {
			if (register) {
				sensorHelper.registerListener(Sensor.TYPE_ACCELEROMETER,
						updateListener, SensorManager.SENSOR_DELAY_UI);
				listeningForUpdate = true;
			} else {
				if (listeningForUpdate) {
					sensorHelper.unregisterListener(Sensor.TYPE_ACCELEROMETER, updateListener);
					listeningForUpdate = false;
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		sensorAttached = sensorHelper.attach(getTiContext().getActivity());

		if (sensorAttached) {
			if (getTiContext().hasEventListener(EVENT_UPDATE, this)) {
				manageUpdateListener(true);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (sensorAttached) {
			manageUpdateListener(false);

			sensorHelper.detach();
			sensorAttached = false;
		}
	}
}