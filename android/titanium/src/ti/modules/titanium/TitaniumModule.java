/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.kroll.KrollCallback;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

public class TitaniumModule
	extends TiModule
{
	private static final String LCAT = "TitaniumModule";
	private static TiDict constants;
	private Stack<String> basePath;

	public TitaniumModule(TiContext tiContext) {
		super(tiContext);
		basePath = new Stack<String>();
		basePath.push(tiContext.getBaseUrl());
		
		tiContext.addOnLifecycleEventListener(this);
	}
	
	@Override
	public TiDict getConstants()
	{
		if (constants == null) {
			constants = new TiDict();

			constants.put("version", getTiContext().getTiApp().getTiBuildVersion());
			constants.put("buildTimestamp", getTiContext().getTiApp().getTiBuildTimestamp());
		}

		return constants;
	}

	public void include(TiContext tiContext, Object[] files) {
		for(Object filename : files) {
			try {
				// we need to make sure paths included from sub-js files are actually relative
				boolean popContext = false;
				if (!basePath.contains(tiContext.getBaseUrl())) {
					basePath.push(tiContext.getBaseUrl());
					popContext = true;
				}
				String resolved = tiContext.resolveUrl(null, TiConvert.toString(filename), basePath.peek());
				basePath.push(resolved.substring(0, resolved.lastIndexOf('/')+1));
				tiContext.evalFile(resolved);
				basePath.pop();
				
				if (popContext) {
					basePath.pop();
				}
			} catch (IOException e) {
				Log.e(LCAT, "Error while evaluating: " + filename, e);
			}
		}
	}

	private HashMap<Integer, Timer> timers = new HashMap<Integer, Timer>();
	private int currentTimerId;

	private int createTimer(Object fn, long timeout, final Object[] args, final boolean interval)
		throws IllegalArgumentException
	{
		// TODO: we should handle evaluatable code eventually too..
		if (fn instanceof KrollCallback) {
			final KrollCallback callback = (KrollCallback) fn;
			Timer timer = new Timer();
			final int timerId = currentTimerId++;

			timers.put(timerId, timer);
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					Log.d(LCAT, "calling " + (interval?"interval":"timeout") + " timer " + timerId + " @" + new Date().getTime());
					callback.call(args);
				}
			};

			if (interval) {
				timer.schedule(task, timeout, timeout);
			} else {
				timer.schedule(task, timeout);
			}

			return timerId;
		}
		else throw new IllegalArgumentException("Don't know how to call callback of type: " + fn.getClass().getName());
	}

	public int setTimeout(Object fn, long timeout, final Object[] args)
		throws IllegalArgumentException
	{
		return createTimer(fn, timeout, args, false);
	}

	public void clearTimeout(int timerId) {
		if (timers.containsKey(timerId)) {
			Timer timer = timers.remove(timerId);
			timer.cancel();
		}
	}

	public int setInterval(Object fn, long timeout, final Object[] args)
		throws IllegalArgumentException
	{
		return createTimer(fn, timeout, args, true);
	}

	public void clearInterval(int timerId) {
		clearTimeout(timerId);
	}

	public void alert(Object message) {
		String msg = (message == null? null : message.toString());
		Log.i("ALERT", msg);
		TiUIHelper.doOkDialog(getTiContext().getActivity(), "Alert", msg, null);
	}
	
	@Override
	public void onDestroy() {
		cancelTimers();
		super.onDestroy();
	}
	
	@Override
	public void onStop() {
		cancelTimers();
		super.onStop();
	}
	
	public void cancelTimers() {
		for (Timer timer: timers.values()) {
			if (timer != null) {
				timer.cancel();
			}
		}
		timers.clear();
	}
	
}