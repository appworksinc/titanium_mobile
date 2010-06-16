package ti.modules.titanium.admanager;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import android.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.widget.FrameLayout.LayoutParams;

public class AdManagerModule extends  TiModule {

	private static final String LCAT = "AdManagerModule";
	private static final boolean DBG = TiConfig.LOGD;
	
	private AdManagerProvider provider;
	
	public AdManagerModule(TiContext tiContext) {
		super(tiContext);
		Log.w(LCAT, "AdManagerModule Created");
	}

	public void test() {
		Log.w(LCAT, "Tested");		
	}
	
	public AdManagerProvider getProvider() {
		return provider;
	}
	
	public AdManagerProvider createProvider(TiDict args) {
		
		provider = new AdManagerProvider(getTiContext(), args);		
		
		return provider;
	}
	
	
	
}
