package ti.modules.titanium.contacts;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.appcelerator.titanium.util.Log;

import android.content.Intent;
import android.net.Uri;
//import android.provider.ContactsContract.Contacts;

public class ContactAccesor extends ContactAccessorBase {
	
	private static final String CONTENT_FILTER_URI = "CONTENT_FILTER_URI";
	
	private static final String ANDROID_PROVIDER_CONTACTS_PHONE_LOOKUP = "android.provider.ContactsContract.PhoneLookup";
	private static final String ANDROID_PROVIDER_CONTACTS_CONTENT_URI = "android.provider.ContactsContract.Contacts";
	
	private String LCAT = "ContactAccesor";
	
	// Resolved Classes
	HashMap<String, Class<?> > cdefLookup;
	Class<?> cdefContactsPhoneLookup;
	
	private Object thisInstance;

	public ContactAccesor() {
		cdefLookup =  new HashMap<String, Class<?>>();
	}

	@Override
	public Intent getContactPickerIntent() {
		Log.d(LCAT, "fetching NEW Contact picker intent");
		
		Uri result = null;		
		try {
			cdefContactsPhoneLookup = fetchClassByName(ANDROID_PROVIDER_CONTACTS_CONTENT_URI);
			thisInstance = cdefContactsPhoneLookup.newInstance();
			result = (Uri) fetchFieldValueByName(CONTENT_FILTER_URI, cdefContactsPhoneLookup, thisInstance);
			
			return new Intent(Intent.ACTION_PICK, result);			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;		
	}

	@Override
	public Uri fetchBaseURI() {
		
		Uri result = null;
		try {
			cdefContactsPhoneLookup = fetchClassByName(ANDROID_PROVIDER_CONTACTS_PHONE_LOOKUP);
			thisInstance = cdefContactsPhoneLookup.newInstance();
			result = (Uri) fetchFieldValueByName(CONTENT_FILTER_URI, cdefContactsPhoneLookup, thisInstance);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;			
	}
	
	
	/**
	 * Fetches the named class.
	 * If it exists in the local lookup table - then that's returned - otherwise it will locate the class
	 * and cache the result before returning it
	 * 
	 * @param name  The class to locate
	 * @return Class<?> The resolved class definition
	 * @throws ClassNotFoundException
	 */
	private Class<?> fetchClassByName(String name) throws ClassNotFoundException {
		
		synchronized (cdefLookup) {
			if (!cdefLookup.containsKey(name)) {
				Class<?> cDef = Class.forName(name);		
				cdefLookup.put(name, cDef);
			}
		}
		
		return cdefLookup.get(name);
	}
	
	private Object fetchFieldValueByName(String name, Class<?> cDef, Object instance) {
		
		Field fld;
		Object result = null;
		
		try {
			fld = cDef.getField(name);
			result = fld.get(instance);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
}	
