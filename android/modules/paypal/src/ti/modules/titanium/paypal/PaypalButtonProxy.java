/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.paypal;

import java.lang.ref.SoftReference;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

public class PaypalButtonProxy extends TiViewProxy {
	
	private static final String LCAT = "PaypalButtonProxy";
	private static final boolean DBG = TiConfig.LOGD;	
	
	public PaypalButtonProxy(TiContext tiContext, Object[] args) {
		super(tiContext, args);
		Log.d(LCAT, "Construct");
	}

	@Override
	public TiUIView createView(Activity activity) {
		
		Log.i(LCAT, "Creating Paypal Button");		
		return new PaypalButton(this);
	}
}