package ti.modules.titanium.admanager;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.os.Debug;

public class AdProxy extends TiViewProxy {
	
	private static final String LCAT = "AdProxy";
	private static final boolean DBG = TiConfig.LOGD;
	
	public AdProxy(TiContext tiContext, Object[] args) {
		super(tiContext, args);
		Log.w(LCAT, "adProxy Created");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public TiUIView createView(Activity activity) {
		
		TiDict props = this.getDynamicProperties();
		
		String adType = props.getString("adType");
		Log.w(LCAT, "[AdProxy]adType: "+adType);
		Log.w(LCAT, "[AdProxy]props: "+props.toString());
		
		if (adType.toLowerCase().equals("smaata")) {
			return new smaatoHelper(this, props);
		} else {
			return new admobHelper(this, props);
		}
		
	}
	
	public void test() {
		//
	}

}
