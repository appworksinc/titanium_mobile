package ti.modules.titanium.urbanairship;

import android.content.Context;

import com.urbanairship.push.Properties;


/**
 * Allows passing of custom properties when constructing the connection to
 * the UrbanAirship API
 * 
 * @author dasher
 */
public class PushProperties extends Properties {

	private Boolean isDebug = false;
	private String appKey;
	
	public PushProperties(Boolean debug, String appKey) {
		this.isDebug = debug;
		this.appKey = appKey;
	}

	@Override
	public String appKey() {
		// TODO Auto-generated method stub
		return this.appKey;
	}

	@Override
	public Boolean isDebug() {
		// TODO Auto-generated method stub
		return this.isDebug;
	}

}
