package ti.modules.titanium.admanager.admob;

//ti.modules.titanium.admanager.admob.AdMobModule

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.util.TiConfig;

import com.admob.android.ads.AdManager;

public class AdMobModule extends TiModule {

	private static final String LCAT = "adMobModule";
	private static final boolean DBG = TiConfig.LOGD;

	private static TiDict constants;
	
	public AdMobModule(TiContext tiContext) {
		super(tiContext);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TiDict getConstants() {
		if (constants == null) {
			constants = new TiDict();

			constants.put("TEST_EMULATOR", AdManager.TEST_EMULATOR);			
		}

		return constants;
	}	
	
}
