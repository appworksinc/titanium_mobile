package ti.modules.titanium.admanager;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;

import com.admob.android.ads.AdManager;

public class AdManagerProvider extends TiModule {
	
	private static final String LCAT = "AdManagerProvider";
	private static final boolean DBG = TiConfig.LOGD;
	private admobHelper adProvider;
	private Object proxy;
	private TiDict props;
	
	public AdManagerProvider(TiContext tiContext, TiDict args) {
		super(tiContext);
		Log.w(LCAT, "AdManagerProvider Created");
		props = args;
	}
	
	public void test() {
		Log.w(LCAT, "Tested");		
	}
	
	
	@Override
	public Object createProxy(TiContext tiContext, Object[] args, String name) {
		
		proxy = super.createProxy(tiContext, args, name);
		Log.w(LCAT, "AdManagerProvider Proxy Created for ["+name+"]");
				
		if (props.containsKey("properties")) {
			Log.w(LCAT, "AdManagerProvider setting dynamic value for properties");
			((AdProxy)proxy).internalSetDynamicValue("properties", props.getTiDict("properties"), true);
		}
		
		((AdProxy)proxy).internalSetDynamicValue("adType", props.getString("type"),true);
		
		
		return proxy;		
	};
	
//	private Object createInstanceFromString(String className) {
//		
//		Class<?> c;
//		
//		try {
//			c = Class.forName(className);
//			
//			Object o = c.newInstance(); // InstantiationException
//			return o;
//		} catch (ClassNotFoundException e) {
//			return null;
//		} catch (IllegalAccessException e) {
//			return null;
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			return null;
//		}
//      
//	}
//	
//	private Boolean resolvable(String className) {
//		try {
//		      Class<?> c = Class.forName(className);
//		      Object o = c.newInstance(); // InstantiationException
//		      
//		      return true;
//
//		      // production code should handle these exceptions more gracefully
//		    } catch (ClassNotFoundException x) {
//		      return false;
//		    } catch (InstantiationException x) {
//		      return false;
//		    } catch (IllegalAccessException x) {
//		      return false;
//		    }
//	}
	
		
	@Override
	public void processProperties(TiDict d) {
		
		Log.w(LCAT, "processProperties");
		super.processProperties(d);
		
		// Type
		if (d.containsKey("type")) {
			Object adManagerType = d.get("type");
			
			if (adManagerType instanceof String) {
				
				if (((String)adManagerType).toLowerCase().equals("admob")) {
					//
				} else if (((String)adManagerType).toLowerCase().equals("smaato")) {
					
				}
				
			}
		} 
		
		if (d.containsKey("properties")) {
			TiDict properties = d.getTiDict("properties");
		}		
	}	

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, TiProxy proxy) {
		// TODO Auto-generated method stub
		
		Log.w(LCAT, "propertyChanged ["+key+"]");
		
		if (key.equals("properties")) {			
			((AdProxy)this.proxy).internalSetDynamicValue("properties", (TiDict)newValue, true);
		}
		
		super.propertyChanged(key, oldValue, newValue, proxy);
	}

}
