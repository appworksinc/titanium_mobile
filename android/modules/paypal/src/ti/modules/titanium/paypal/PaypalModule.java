/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.paypal;

import java.util.concurrent.Semaphore;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiPlatformHelper;

import android.app.Activity;
import android.content.Intent;

import com.paypal.android.MEP.*;

public class PaypalModule extends TiModule
	implements TiActivityResultHandler
{

	private static final String LCAT = "PaypalModule";
	private static final boolean DBG = TiConfig.LOGD;

	private static TiDict constants;
	private TiActivityResultHandler handler;

	//protected DisplayCapsProxy displayCaps;

	public PaypalModule(TiContext tiContext) {
		super(tiContext);
		
		setActivityResultHandler(this);
	}

	@Override
	public TiDict getConstants()
	{
		if (constants == null) {
			constants = new TiDict();

			constants.put("PAYPAL_ENV_LIVE", PayPal.ENV_LIVE);
			constants.put("PAYPAL_ENV_SANDBOX", PayPal.ENV_SANDBOX);
			constants.put("PAYPAL_ENV_NONE", PayPal.ENV_NONE);
			
			constants.put("PAYMENT_TYPE_HARD_GOODS", PayPal.PAYMENT_TYPE_HARD_GOODS);
			constants.put("PAYMENT_TYPE_DONATION", PayPal.PAYMENT_TYPE_DONATION);
			constants.put("PAYMENT_TYPE_PERSONAL", PayPal.PAYMENT_TYPE_PERSONAL);
			constants.put("PAYMENT_TYPE_SERVICE", PayPal.PAYMENT_TYPE_SERVICE);
			
			constants.put("BUTTON_68x24", PayPal.BUTTON_68x24);
			constants.put("BUTTON_68x33", PayPal.BUTTON_68x33);
			constants.put("BUTTON_118x24", PayPal.BUTTON_118x24);
			constants.put("BUTTON_152x33", PayPal.BUTTON_152x33);
			constants.put("BUTTON_194x37", PayPal.BUTTON_194x37);
			constants.put("BUTTON_278x43", PayPal.BUTTON_278x43);
			constants.put("BUTTON_294x43", PayPal.BUTTON_294x43);			
		}

		return constants;
	}
	
	public void executePayment(PayPalPayment newPayment) {
		
		Log.d(LCAT, "EXECUTE Payment CALLED");
		
		Activity activity = getTiContext().getActivity();
		TiActivitySupport activitySupport = (TiActivitySupport) activity;
		final int resultCode = activitySupport.getUniqueResultCode();
		
		// This should handler disconnected clients I think
		if (null == handler) {
			setActivityResultHandler(this);
		}

		Intent checkoutIntent = new Intent(activity, PayPalActivity.class);
		checkoutIntent.putExtra(PayPalActivity.EXTRA_PAYMENT_INFO, newPayment);		
		activitySupport.launchActivityForResult(checkoutIntent, resultCode, handler);		
		
	}
	
	public void setActivityResultHandler(TiActivityResultHandler handler) {
		this.handler = handler;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
		
		Log.d(LCAT, "Paypal-Module - Default onResult called");
		
		if (requestCode != 1) {
			return;
		}
		
		switch(resultCode) {
		case Activity.RESULT_OK:
			String transactionID = data.getStringExtra(PayPalActivity.EXTRA_TRANSACTION_ID);
			Log.d(LCAT, "Paypal Result ok ["+transactionID+"]");
			
		break;
		case Activity.RESULT_CANCELED:
			Log.d(LCAT, "Paypal Cancelled");
		break;
		case PayPalActivity.RESULT_FAILURE:
			String errorID = data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
			String errorMessage = data.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
			Log.d(LCAT, "Paypal Failed: ["+errorID+"] with "+errorMessage);
		break;
		default:
			Log.d(LCAT, "Other Result code ["+resultCode+"]");
		break;
		}		
	}
}
