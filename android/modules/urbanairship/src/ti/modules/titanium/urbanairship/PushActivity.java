package ti.modules.titanium.urbanairship;

import org.appcelerator.titanium.TiActivity;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;

import android.content.Intent;
import android.os.Bundle;
import com.urbanairship.push.*;

/**
 * I'm not intending this to be used to handle taskbar notifications,
 * it's just a POC and a playground to see what's possible with extra logging to make the process more visible.
 * 
 * This Activity will only get activated if the right entries are in the AndroidManifest - so it's safe the leave the code in.
 * 
 * The AndroidManifest (or AndroidManifest.custom) entry needed is:
 * 		<activity android:name="ti.modules.titanium.urbanairship.PushActivity"
			android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
			android:allowTaskReparenting="true"
		>
			<intent-filter>
				<action android:name="com.urbanairship.airmail.NOTIFY" />
        		<category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
		</activity>
 * 
 * With the above in your manifest - it appears to launch the activity and attach to the correct App context.
 * 
 * @author dasher
 *
 */
public class PushActivity extends TiActivity {
	
	private static final String LCAT = "PushActivity";
	private static final boolean DBG = TiConfig.LOGD;
	
	private UrbanAirshipModule mPushModule;

	public PushActivity() {
		super();
        Log.d(LCAT, "Push Activity Started.");
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}
	
	public TiContext getContext() {
		
        Log.d(LCAT, "Working out the context");
        
		if (null == this.proxy || null==this.proxy.getTiContext()) {
	        Log.d(LCAT, "Proxy or proxy.getTiContext is null - so create one");
			TiContext c = new TiContext(this, null);
			return c;
		} else {
	        Log.d(LCAT, "We have a valid proxy");

			return this.proxy.getTiContext();
		}
	}
	
	public void linkPushModule() {
		
        Log.d(LCAT, "Link Push Module.");
		
        // Does the module exist?
		mPushModule = (UrbanAirshipModule) TiModule.getModule("UrbanAirshipModule");
		if (mPushModule == null) {
			/*
			 *  If not create it - this is just a short term approach
			 *  
			 *  Longer term - we should 
			 *   1) Work out if the app dev has registered a js file to handle the incoming notification
			 *   2) create the context in getContext with a path to the file
			 */
			mPushModule = new UrbanAirshipModule(getContext());
		}
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        Log.d(LCAT, "Push Activity onCreate.");
		
        linkPushModule();
        
        Intent intent = getIntent();

        String message = intent.getStringExtra("message");
        String payload = intent.getStringExtra("payload");

        /* This is not a Push */
        if (message == null){
            return;
        } else {
            /**
             * We setup the com.urbanairship.nc.NOTIFY intent in our manifest, which
             * happens after a push is received, when the user interacts with the
             * notification.
             *
             * Note that this is different from the acceptPush work flow which
             * happens right when the push is received.
             * */
            Log.d(LCAT, "Got message and payload: " + message + ", " + payload);	
            if (!(null == mPushModule)) {
                mPushModule.onReceive(message, payload);            	
            }
        }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        Log.d(LCAT, "Push Activity onResume");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        Log.d(LCAT, "Push Activity onResume");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        Log.d(LCAT, "Push Activity onResume");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
        Log.d(LCAT, "Push Activity onStart");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        Log.d(LCAT, "Push Activity onResume");
	}
	
}
