package ti.modules.titanium.admanager;

import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;
import android.widget.FrameLayout.LayoutParams;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;
import com.admob.android.ads.SimpleAdListener;

public class admobHelper extends TiUIView {
	
	private static final String LCAT = "admobHelper";
	private static final boolean DBG = TiConfig.LOGD;
	private AdView mAd;  // TODO You will need an AdView (this one is defined in res/layout/lunar_layout.xml)
	private RelativeLayout layout;
	private admobHelperListener adListener;
	private String publisherID;
	private TiDict providerProperties;
	LayoutAnimationController controller;
	
    private class admobHelperListener extends SimpleAdListener
    {

		@Override
		public void onFailedToReceiveAd(AdView adView)
		{
			Log.d(LCAT, "onFailedToReceiveAd in Listener");
			super.onFailedToReceiveAd(adView);
			
			proxy.fireEvent("failedToReceiveAd", new TiDict());
			
		}

		@Override
		public void onFailedToReceiveRefreshedAd(AdView adView)
		{
			Log.d(LCAT, "onFailedToReceiveRefreshedAd in Listener");
			super.onFailedToReceiveRefreshedAd(adView);
			
			proxy.fireEvent("failedToReceiveRefreshedAd", new TiDict());
		}

		@Override
		public void onReceiveAd(AdView adView)
		{
			Log.d(LCAT, "onReceiveAd in Listener");
			//mDisplayAd.onReceiveAd(adView);
			mAd.clearAnimation();
			mAd = buildAdContent(adView);			
			super.onReceiveAd(mAd);
			
			proxy.fireEvent("receiveAd", new TiDict());
		}

		@Override
		public void onReceiveRefreshedAd(AdView adView)
		{
			Log.d(LCAT, "onReceiveRefreshedAd in Listener");
			
			mAd = buildAdContent(adView);
			super.onReceiveRefreshedAd(mAd);
			proxy.fireEvent("receiveRefreshAd", new TiDict());
		}
    	
    }	
   
	public admobHelper(TiViewProxy proxy, TiDict args) {
		super(proxy);
		
		Log.d(LCAT, "Creating ad Holder");
		
		providerProperties = new TiDict();
		providerProperties.put("devMode", (Object)(new String[]{}));
		
		setupManager();
		buildAd();	
	}

	@Override
	public void release() {
		Log.d(LCAT, "Release called");
		super.release();
	}
	
	@Override
	public void hide() {
		Log.d(LCAT, "Hide called");
		super.hide();
	}
	
	private void setupManager() {
		
		String[] testDevices = (String[])providerProperties.get("devMode");
		
		AdManager.setTestDevices( testDevices );
		
		if (publisherID != null) {
			AdManager.setPublisherId(publisherID);			
		}
		
		try {
			Log.d(LCAT, "UserID "+AdManager.getUserId(this.getProxy().getContext()));			
		} catch (Exception e) {
			// TODO: handle exception
			e = null;
		}
		
		Boolean locationAllowed = providerProperties.optBoolean("useLocation", true);
		AdManager.setAllowUseOfLocation(locationAllowed);
		
	}
	
	private void buildAd() {
		
		layout = new RelativeLayout(getProxy().getContext());
		layout.setBackgroundColor(Color.argb(0, 0, 0, 0));
		layout.setGravity(Gravity.NO_GRAVITY);
		layout.setPadding(0, 0, 0, 0);

		mAd = buildAdContent(new AdView(getProxy().getContext(), null));
		
		RelativeLayout.LayoutParams params = buildLayoutParams();
		
		layout.addView(mAd, params);	
		layout.setGravity(Gravity.NO_GRAVITY);
		layout.setPadding(0, 0, 0, 0);
		setNativeView(layout);
		
	}

	private AdView buildAdContent(AdView ad) {
		ad.setId(103);
		ad.setTag("ad");
		
		TiDict props = proxy.getDynamicProperties();
		
		int refreshInterval = 60;
		if (props.containsKey("refreshInterval")) {
			refreshInterval = props.getInt("refreshInterval");
		}
		ad.setRequestInterval(refreshInterval);
		
		if (props.containsKey("keywords")) {
			ad.setKeywords(props.getString("keywords"));
		}
		
		if (props.containsKey("enableSoundEffects") && (props.get("enableSoundEffects") instanceof Boolean)) {
			ad.setSoundEffectsEnabled(props.getBoolean("enableSoundEffects"));			
		}
		
		if (props.containsKey("searchQuery") && (props.get("searchQuery") instanceof String)) {
			ad.setSearchQuery(props.getString("searchQuery"));						
			//ad.setLayoutAnimation(controller)
		}

		getAnimation();
		if (controller != null) {
			ad.setLayoutAnimation(controller);			
		}
		
		if (adListener == null) {
			adListener = new admobHelperListener();
		}
		ad.setAdListener(adListener);	
		
		return ad;
	}
	
	private LayoutAnimationController getAnimation() {
		
//		Animation inFromR = AnimationUtils.makeInAnimation(this.getProxy().getContext(), true);
//		inFromR.setInterpolator(this.getProxy().getContext(), R.anim.accelerate_interpolator);
//		inFromR.setDuration(2000);
//		
//		if (controller == null) {
//			controller = new LayoutAnimationController(inFromR);
//		} else {
//			controller.setAnimation(inFromR);
//		}
//		
//		return controller; 
		return null;
	}
	
	private RelativeLayout.LayoutParams buildLayoutParams() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (Integer.parseInt(Build.VERSION.SDK) > 3) {
			params.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		params.addRule(RelativeLayout.RIGHT_OF, 101);
		params.setMargins(0, 0, 0, 0);
		return params;
	}
	
	@Override
	public void processProperties(TiDict d)
	{
		super.processProperties(d);
		Log.d(LCAT, "ProcessProperties");
				
		if (d.containsKey("properties")) {
			providerProperties = d.getTiDict("properties");
			Log.d(LCAT, "ProcessProperties - has properties key");
			
			if (providerProperties.containsKey("publisherID")) {
				publisherID = providerProperties.getString("publisherID");
			}
			
			if (providerProperties.containsKey("devMode")) {
				String[] testDevices = new String[] {};
								
				if (providerProperties.get("devMode") instanceof Boolean) {
					Boolean bTest = providerProperties.optBoolean("devMode", false);
					
					if (bTest) {
						testDevices = new String[] { AdManager.TEST_EMULATOR } ;
						Log.d(LCAT, "Dev Mode enabled via boolean");
					}
				} else {					
					testDevices = providerProperties.getStringArray("devMode");
				}
				
				providerProperties.put("devMode", testDevices);
			}
			
			setupManager();			
		}

		layout.invalidate();
	}	

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, TiProxy proxy)
	{
		if (DBG) {
			Log.d(LCAT, "Property: " + key + " old: " + oldValue + " new: " + newValue);
		}
		
		if (((TiDict)newValue).containsKey("devMode")) {
			providerProperties.put("devMode", ((TiDict)newValue).optBoolean("devMode", false));
		}		
		
		if (key.equals("properties")) {
			if (newValue instanceof TiDict) {				
				TiDict testProperties = (TiDict)newValue;
				
				if (testProperties.containsKey("devMode")) {
					String[] testDevices = new String[] {};
					
					if (testProperties.get("devMode") instanceof Boolean) {
						if (testProperties.getBoolean("devMode")) {
							testDevices = new String[] { AdManager.TEST_EMULATOR } ;
							Log.d(LCAT, "Dev Mode enabled via boolean");
						}
					} else {					
						testDevices = providerProperties.getStringArray("devMode");
					}
					
					providerProperties.put("devMode", testDevices);
				}
			} else {
				Log.d(LCAT, "Property: " + key + " wasn't a TiDict");				
			}
			
			setupManager();			
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
		
	}	
	

}
