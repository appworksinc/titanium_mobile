package ti.modules.titanium.admanager;
//package com.crucialdivide.modules.ads;
//package ti.modules.com.crucialdivide.modules.ads;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.admanager.AdManagerModule;

import android.app.Activity;

public class AdManagerProxy extends TiViewProxy {
	
	private static final String LCAT = "AdManagerProxy";
	private static final boolean DBG = TiConfig.LOGD;
	private AdManagerModule adManager;
		
	public AdManagerProxy(TiContext tiContext, Object[] args) {
		super(tiContext, args);
		Log.w(LCAT, "Create AdManagerProxy");
	}

	@Override
	public TiUIView createView(Activity activity) {
		// TODO Auto-generated method stub
		Log.w(LCAT, "Create View");
		return null;
	}



}
