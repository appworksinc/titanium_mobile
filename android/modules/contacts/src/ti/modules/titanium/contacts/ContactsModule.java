package ti.modules.titanium.contacts;

import java.util.concurrent.Semaphore;

import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiPlatformHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;


@SuppressWarnings("deprecation")
public class ContactsModule extends TiModule 
  implements TiActivityResultHandler
{
	private String LCAT = "ContactsModule";
	
	private final ContactAccessorBase mContactAccessor;
	private TiActivityResultHandler handler;
	
	// Android Compatability Layer
	
	/**
	 * base class for resolving the type of interface to use on differing platforms
	 */
	public abstract static class ContactAccessorBase {
		
		private static ContactAccessorBase sInstance;

	    public static ContactAccessorBase getInstance() {
	        if (sInstance == null) {
	            String className;
	            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
	            if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
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
	
	public class ContactAccessorOldApi extends ContactAccessorBase {
		
		private String LCAT = "ContactAccessorOldApi";

		@SuppressWarnings("deprecation")
		@Override
		public Intent getContactPickerIntent() {
			Log.d(LCAT, "fetching OLD Contact picker intent");
			return new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
		}

		@SuppressWarnings("deprecation")
		@Override
		public Uri fetchBaseURI() {
			// TODO Auto-generated method stub
			return People.CONTENT_FILTER_URI;
		}
		
	}
	
	public class ContactAccesor extends ContactAccessorBase {
		
		private String LCAT = "ContactAccesor";

		@Override
		public Intent getContactPickerIntent() {
			Log.d(LCAT, "fetching NEW Contact picker intent");
			return new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
		}

		@Override
		public Uri fetchBaseURI() {
			return android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI;			
		}
		
	}	

	public ContactsModule(TiContext tiContext) {
		super(tiContext);
		mContactAccessor = ContactAccessorBase.getInstance();
		// TODO Auto-generated constructor stub
	}
	
	public void showContacts() {
		this.launchIntent();
	}
	
	
	protected void launchIntent() {
		Log.d(LCAT, "Launching Contact picker intent");

		Activity activity = getTiContext().getActivity();
		TiActivitySupport activitySupport = (TiActivitySupport) activity;
		final int resultCode = activitySupport.getUniqueResultCode();

		// This should handler disconnected clients I think
		if (null == handler) {
			setActivityResultHandler(this);
		}

		Intent contactsIntent = mContactAccessor.getContactPickerIntent();
		activitySupport.launchActivityForResult(contactsIntent, resultCode, handler);	
	}

	private void setActivityResultHandler(TiActivityResultHandler handler) {
		// TODO Auto-generated method stub
		this.handler = handler;		
	}

	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
			case (Activity.RESULT_OK) :
				//Uri baseUri = mContactAccessor.fetchBaseURI();
			 	Uri contactData = data.getData();
				Log.d(LCAT, "Data from Intent: "+contactData.toString());
			break;
		  }				
	}



}
