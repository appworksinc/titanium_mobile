package ti.modules.titanium.admanager.smaato;

//ti.modules.titanium.admanager.admob.AdMobModule

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.util.TiConfig;
import ti.modules.titanium.admanager.AdManagerModule;

public class SmaatoModule extends TiModule {

	private static final String LCAT = "SmaatoModule";
	private static final boolean DBG = TiConfig.LOGD;

	private static TiDict constants;
	
	public SmaatoModule(TiContext tiContext) {
		super(tiContext);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TiDict getConstants() {
		if (constants == null) {
			constants = new TiDict();

			
		}

		return constants;
	}	
	
}
