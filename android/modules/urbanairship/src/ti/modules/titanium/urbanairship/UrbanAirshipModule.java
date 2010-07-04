
package ti.modules.titanium.urbanairship;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;

import com.urbanairship.push.*;

public class UrbanAirshipModule extends TiModule 
  implements PushReceiver, APIDReceiver {
	
	private static final String LCAT = "UrbanAirshipModule";
	private static final boolean DBG = TiConfig.LOGD;
	private TiDict constants;

	public UrbanAirshipModule(TiContext tiContext) {
		super(tiContext);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public TiDict getConstants() {
		if (constants == null) {
			constants = new TiDict();

			constants.put("Titanium.Network.NOTIFICATION_TYPE_BADGE", 1);
			constants.put("Titanium.Network.NOTIFICATION_TYPE_ALERT", 2);
			constants.put("Titanium.Network.NOTIFICATION_TYPE_SOUND", 4);
		}

		return constants;
	}
	
	public void registerForPushNotifications(TiDict args) {
		
		TiContext tiContext = this.getTiContext();
		processProperties(args);
		
		Boolean enableDebug = args.optBoolean("debug", false);
		String appKey = (String)  args.getString("appKey");
		
		if (args.containsKey("success")) {
			this.addEventListener("success", args.get("success"));
		}

		if (args.containsKey("callback")) {
			this.addEventListener("callback", args.get("callback"));
		}
		
		
		if (appKey.equals("")) {
			Log.d(LCAT, "No AppKey Defined! Unable to register");			
		} else {
			Log.d(LCAT, "Props AppKey ["+appKey+"]");
			Log.d(LCAT, "Props enableDebug ["+enableDebug+"]");
			
			PushProperties props = new PushProperties(enableDebug, appKey);
			
			Registration.register(tiContext.getActivity().getBaseContext(), this, props);
			Registration.acceptPush(tiContext.getActivity().getBaseContext(), this);
		}
	}

	@Override
	public void onReceive(String message, String payload) {
		Log.d(LCAT, "Got message '" + message +"' and payload '" + payload + "'");
		
        TiDict event = new TiDict();
        
        // Create the data payload
        TiDict data = new TiDict();
        data.put("message", message);
        data.put("payload", payload);
        
        event.put("data",data);
        
		fireEvent("callback",event);		
	}

	@Override
	public void onReceive(String APID) {
		
        Log.d(LCAT, "Received ApID ["+APID+"]");		
		
        TiDict event = new TiDict();
        event.put("deviceToken", APID);
		fireEvent("success",event);		
	}

	@Override
	public void processProperties(TiDict d) {
		// TODO Auto-generated method stub
		super.processProperties(d);
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, TiProxy proxy) {
		// TODO Auto-generated method stub
		super.propertyChanged(key, oldValue, newValue, proxy);
	}

}
