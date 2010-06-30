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
import android.widget.ImageView.ScaleType;

import com.smaato.SOMA.SOMABanner;

public class smaatoHelper extends TiUIView {
	
	private static final String LCAT = "smaatoHelper";
	private static final boolean DBG = TiConfig.LOGD;
	private SOMABanner mAd;  // TODO You will need an AdView (this one is defined in res/layout/lunar_layout.xml)
	private RelativeLayout layout;
	private String publisherID;
	private String adSpaceId;
	private Boolean devMode = false;
	private Integer refreshInterval = 60;
	private String keywords = "";
	private TiDict providerProperties;
	LayoutAnimationController controller;
   
	public smaatoHelper(TiViewProxy proxy, TiDict args) {
		super(proxy);
		
		Log.d(LCAT, "Creating ad Holder");
		
		providerProperties = new TiDict();
		
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
		mAd = new SOMABanner(this.proxy.getContext());
		SOMABanner.setPubID(this.publisherID);
		SOMABanner.setAdID(this.adSpaceId);
		mAd.setKeywordSearch(this.keywords);
		mAd.nextAd(this.refreshInterval);
		mAd.setDevMode(this.devMode);
		mAd.setScaleType(ScaleType.CENTER_INSIDE);
		//mAd.setLocation(41.3853, 2.1701);
		mAd.fetchDrawableOnThread();
	}
	
	private void buildAd() {
		
		layout = new RelativeLayout(getProxy().getContext());
		layout.setBackgroundColor(Color.argb(0, 0, 0, 0));
		layout.setGravity(Gravity.NO_GRAVITY);
		layout.setPadding(0, 0, 0, 0);
		
		RelativeLayout.LayoutParams params = buildLayoutParams();
		
		layout.addView(mAd, params);	
		layout.setGravity(Gravity.NO_GRAVITY);
		layout.setPadding(0, 0, 0, 0);
		setNativeView(layout);
		
	}
	
	private RelativeLayout.LayoutParams buildLayoutParams() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
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
			
		if (d.containsKey("refreshInterval")) {
			this.refreshInterval = d.getInt("refreshInterval");
		}
		
		if (d.containsKey("keywords")) {
			this.keywords = d.getString("keywords");
		}
		
		if (d.containsKey("properties")) {
			providerProperties = d.getTiDict("properties");
			Log.d(LCAT, "ProcessProperties - has properties key");
			
			if (providerProperties.containsKey("publisherID")) {
				this.publisherID = providerProperties.getString("publisherID");
			}
			
			if (providerProperties.containsKey("adSpaceID")) {
				this.adSpaceId = providerProperties.getString("adSpaceID");
			}
			
			if (providerProperties.containsKey("devMode")) {
				this.devMode = providerProperties.getBoolean("devMode");
			}			
		}

		setupManager();
		buildAd();
		layout.invalidate();
	}	

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, TiProxy proxy)
	{
		if (DBG) {
			Log.d(LCAT, "Property: " + key + " old: " + oldValue + " new: " + newValue);
		}
		
		if (((TiDict)newValue).containsKey("devMode")) {
//			providerProperties.put("devMode", ((TiDict)newValue).optBoolean("devMode", false));
		}		
		
		if (key.equals("properties")) {
//			if (newValue instanceof TiDict) {				
//				TiDict testProperties = (TiDict)newValue;
//				
//				if (testProperties.containsKey("devMode")) {
//					String[] testDevices = new String[] {};
//					
//					if (testProperties.get("devMode") instanceof Boolean) {
//						if (testProperties.getBoolean("devMode")) {
//							testDevices = new String[] { AdManager.TEST_EMULATOR } ;
//							Log.d(LCAT, "Dev Mode enabled via boolean");
//						}
//					} else {					
//						testDevices = providerProperties.getStringArray("devMode");
//					}
//					
//					providerProperties.put("devMode", testDevices);
//				}
//			} else {
//				Log.d(LCAT, "Property: " + key + " wasn't a TiDict");				
//			}
//			
//			setupManager();			
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
		
	}	
	

}
