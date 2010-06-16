package ti.modules.titanium.admanager;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;

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
		Log.w(LCAT, "Create View");
		
		TiDict props = this.getDynamicProperties();
		
		return new admobHelper(this, props);
		//return new admobHelper(this, props);
	}
	
	public void test() {
		//
	}

}
