package com.urbanairship.push;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class Registration {
    final static String LOG_TAG = "UA.push";

	/**
	 * Register an application by sending a broadcast to AirMail
	 * and setup broadcast receiver with <code>apidReceiver</code> to accept
	 * apid.
	 * 
	 * @param ctx  Application Context
	 * @param apidReceiver Callback that accepts apid
	 */
	public static void register(Context ctx, final APIDReceiver apidReceiver) {
	    Log.d("app", "Registering");
    	Intent intent = new Intent("com.urbanairship.airmail.START_REGISTER");
    	intent.setClassName("com.urbanairship.airmail", "com.urbanairship.airmail.CoreReceiver");
        
    	String localIntentName = makeIntentName(ctx, "END_REGISTER");
	    Intent localIntent = new Intent(localIntentName);
	    
		BroadcastReceiver receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	Log.d(LOG_TAG, "Broadcast Received: " + intent.getAction());
		    	final String apid = intent.getStringExtra("apid");
		    	UA.setApid(apid);
		    	apidReceiver.onReceive(apid);
		    }
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(localIntentName);
		
		ctx.registerReceiver(receiver, filter);
	    
		Properties props = new Properties(ctx);
		intent.putExtra("appKey", props.appKey());
        intent.putExtra("app", PendingIntent.getBroadcast(ctx, 0, localIntent, 0));
        
        Log.d(LOG_TAG, "AppID: "+props.appKey().toString());
        
        Log.d(LOG_TAG, "Sending broadcast");
        ctx.sendBroadcast(intent);
        
    }
	
	/**
	 * Set up callback that receives incoming push notifications
	 * 
	 * @param ctx  Application Context
	 * @param pushReceiver PushReceiver that accepts incoming push notification
	 * @see PushReceiver#onReceive
	 */
	public static void acceptPush(Context ctx, final PushReceiver pushReceiver){
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Broadcast Received: " + intent.getAction());
                pushReceiver.onReceive(intent.getStringExtra("message"), intent.getStringExtra("payload"));
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(makeIntentName(ctx, "ACCEPT_PUSH"));
        
        ctx.registerReceiver(receiver, filter);
	}
	
	private static String makeIntentName(Context ctx, String label){
	    return ctx.getPackageName() + "." + label;
	}
}
