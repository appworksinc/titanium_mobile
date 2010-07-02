package ti.modules.titanium.contacts;

import org.appcelerator.titanium.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;


/**
 * base class for resolving the type of interface to use on differing platforms
 * 
 * @author dasher
 */
public abstract class ContactAccessorBase {
	
	private static String LCAT = "ContactAccessorBase";
	
	private static ContactAccessorBase sInstance;
	private final static int EclairBuildVersion = 5;

    public static ContactAccessorBase getInstance() {
    	
        if (sInstance == null) {
            String className;
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            
            if (sdkVersion < EclairBuildVersion) {
                className = "ContactAccessorOldApi";
            } else {
                className = "ContactAccesor";
            }
            try {
            	
            	Log.d(LCAT, "Searching for Package ["+ContactAccessorBase.class.getPackage()+"] Class ["+className+"]");
            	
                Class<? extends ContactAccessorBase> classSeeker =
                        Class.forName("ti.modules.titanium.contacts." + className).asSubclass(ContactAccessorBase.class);
                sInstance = classSeeker.newInstance();
            } catch (Exception e) {
            	e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
        return sInstance;
    }
	
    public abstract Intent getContactPickerIntent();
    public abstract Uri fetchBaseURI();
    public abstract Cursor getContacts(Activity activity);
    
}