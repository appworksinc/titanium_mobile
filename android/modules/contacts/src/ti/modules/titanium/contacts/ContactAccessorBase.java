package ti.modules.titanium.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;


/**
 * base class for resolving the type of interface to use on differing platforms
 */
public abstract class ContactAccessorBase {
	
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
                Class<? extends ContactAccessorBase> classSeeker =
                        Class.forName(ContactAccessorBase.class.getPackage() + "." + className).asSubclass(ContactAccessorBase.class);
                sInstance = classSeeker.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return sInstance;
    }
	
    public abstract Intent getContactPickerIntent();
    public abstract Uri fetchBaseURI();
}