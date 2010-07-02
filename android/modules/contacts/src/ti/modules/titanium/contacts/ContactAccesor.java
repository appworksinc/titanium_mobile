package ti.modules.titanium.contacts;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.appcelerator.titanium.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
//import android.provider.ContactsContract.Contacts;
//import android.widget.SimpleCursorAdapter;

public class ContactAccesor extends ContactAccessorBase {
	
	
	private static final String ANDROID_FIELD_CONTENT_URI = "CONTENT_URI";
	private static final String ANDROID_FIELD_CONTENT_FILTER_URI = "CONTENT_FILTER_URI";
	
	private static final String ANDROID_FIELD_COLUMN_DISPLAYNAME = "DISPLAY_NAME";
	private static final String ANDROID_FIELD_COLUMN_ID = "_ID";
	private static final String ANDROID_FIELD_IN_VISIBLE_GROUP = "IN_VISIBLE_GROUP";
	// android.provider.ContactsContract.Contacts.CONTENT_URI
	
	private static final String ANDROID_PROVIDER_CONTACTS_PHONE_LOOKUP = "android.provider.ContactsContract.PhoneLookup";
	private static final String ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS = "android.provider.ContactsContract$Contacts";
	private static final String ANDROID_PROVIDER_CONTACTS_CONTACTCOLUMNS = "android.provider.ContactsContract.ContactsColumns.Data";
	
	private String LCAT = "ContactAccesor";
	
	// Resolved Classes
	HashMap<String, Class<?> > cdefLookup;
	
	private Object thisInstance;

	public ContactAccesor() {
		cdefLookup =  new HashMap<String, Class<?>>();
	}
	
	
    public Cursor getContacts(Activity activity)
    {
    	Log.d(LCAT, "getContacts");
        // Run query
        //Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Uri uri = null;
		try {
			uri = fetchUri(ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS, ANDROID_FIELD_CONTENT_URI, false);
			
			String[] projection = new String[] {
                    (String)fetchFieldByNameByClassName(ANDROID_FIELD_COLUMN_DISPLAYNAME, ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS),
                    (String)fetchFieldByNameByClassName(ANDROID_FIELD_COLUMN_ID, ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS)
                 };
			
	        Log.d(LCAT, "projection: "+projection.toString());
	        
	        boolean showInvisible = true;

			String selection = (String) fetchFieldByNameByClassName(ANDROID_FIELD_IN_VISIBLE_GROUP, ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS) 
												+ " = '" + (showInvisible ? "0" : "1") + "'";
	        
	        Log.d(LCAT, "selection: "+selection.toString());
			
			
			//String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + (showInvisible ? "0" : "1") + "'";
	        String[] selectionArgs = null;
	        
	        String sortOrder = fetchFieldByNameByClassName(ANDROID_FIELD_COLUMN_DISPLAYNAME, ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS) 
	        									+ " ASC";
	        //String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
	        Log.d(LCAT, "sortOrder: "+sortOrder.toString());
	        
	        Cursor c =  activity.managedQuery(uri, projection, null, null, sortOrder);
	        displayContacts(c);
			
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
    
    
    public void displayContacts(Cursor c) {
    	
    	Log.d(LCAT, "displayContacts");
    	
    	try {
//    		Class<?> cdefContactsPhoneLookup = fetchClassByName(ANDROID_PROVIDER_CONTACTS_CONTACTCOLUMNS);
//    		thisInstance = cdefContactsPhoneLookup.newInstance();
//    		String result = (String) fetchFieldValueByName(ANDROID_FIELD_COLUMN_DISPLAYNAME, cdefContactsPhoneLookup, thisInstance);    	
//
//            String[] fields = new String[] { result };
            
            if (c.moveToFirst()) {

                String name; 
                String id; 
                
                //String s = ContactsContract.Contacts.DISPLAY_NAME;
                
                int nameColumn = c.getColumnIndex((String) fetchFieldByNameByClassName(ANDROID_FIELD_COLUMN_DISPLAYNAME, ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS)); 
                int idColumn = c.getColumnIndex((String) fetchFieldByNameByClassName(ANDROID_FIELD_COLUMN_ID, ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS));
                String imagePath; 
            
                do {
                    // Get the field values
                    name = c.getString(nameColumn);
                    id = c.getString(idColumn);

                    Log.d(LCAT, "testContacts: ID: '"+id+"' Display Name: '"+name+"'");

                } while (c.moveToNext());

            }            
            
            
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    }

	@Override
	public Intent getContactPickerIntent() {
		Log.d(LCAT, "fetching NEW Contact picker intent");
		
		try {
			
			//Uri t = Contacts.CONTENT_URI;
			Uri pickerUri;
			
			//Class<?> cdefContactsPhoneLookup = fetchClassByName(ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS);
			//pickerUri = (Uri) fetchFieldValueByName(ANDROID_FIELD_CONTENT_URI, cdefContactsPhoneLookup);
			pickerUri = fetchUri(ANDROID_PROVIDER_CONTACTSCONTRACT_CONTACTS, ANDROID_FIELD_CONTENT_URI, false);		
			
			return new Intent(Intent.ACTION_PICK, pickerUri);
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
//			cdefContactsPhoneLookup = fetchClassByName(ANDROID_PROVIDER_CONTACTS_PHONE_LOOKUP);
//			thisInstance = cdefContactsPhoneLookup.newInstance();
//			result = (Uri) fetchFieldValueByName(ANDROID_FIELD_CONTENT_FILTER_URI, cdefContactsPhoneLookup, thisInstance);
			return fetchUri(ANDROID_PROVIDER_CONTACTS_PHONE_LOOKUP, ANDROID_FIELD_CONTENT_FILTER_URI);
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
	
	
	private Uri fetchUri(String fromClass, String UriName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return fetchUri(fromClass, UriName, true);
	}
	
    private Uri fetchUri(String fromClass, String UriName, Boolean withInstance) 
		throws ClassNotFoundException, InstantiationException, IllegalAccessException 
	{
	
		Log.d(LCAT, "Fetch URI fromClass '"+fromClass+"' with URI '"+UriName+"'");
		
		Uri result = null;		
		try {
			Class<?> cdefContactsPhoneLookup = fetchClassByName(fromClass);
			if (withInstance) {
				thisInstance = cdefContactsPhoneLookup.newInstance();				
			} else {
				thisInstance = null;
			}
			result = (Uri) fetchFieldValueByName(UriName, cdefContactsPhoneLookup, thisInstance);
			
			return result;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw e;
			//e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			throw e;
			//e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			throw e;
			//e.printStackTrace();
		}
		
		//return null;    	
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
		
		Log.d(LCAT, "fetchClassByName '"+name+"'");
		
		synchronized (cdefLookup) {
			if (!cdefLookup.containsKey(name)) {
				Class<?> cDef = Class.forName(name);		
				cdefLookup.put(name, cDef);
			}
		}
		
		return cdefLookup.get(name);
	}
	
	private Object fetchFieldByNameByClassName(String fieldName, String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		Class<?> cDef = fetchClassByName(className);
		//thisInstance = cDef.newInstance();
		return fetchFieldValueByName(fieldName, cDef);		
	}
	
	private Object fetchFieldValueByName(String name, Class<?> cDef) {
		
		try {
			return cDef.getField(name).get(null);
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
		return null;		
	}
	
	
	private Object fetchFieldValueByName(String name, Class<?> cDef, Object instance) {
		
		try {
			return cDef.getField(name).get(instance);
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
		
		return null;
	}
	
}	
