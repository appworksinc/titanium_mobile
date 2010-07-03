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

import com.urbanairship.push.*;


public class ContactsModule extends TiModule 
  implements TiActivityResultHandler
{
	private String LCAT = "ContactsModule";
	
	private ContactAccessorBase mContactAccessor;
	private TiActivityResultHandler handler;

	protected static TiDict constants;
	
	public ContactsModule(TiContext tiContext) {
		super(tiContext);
		Log.d(LCAT, "Contacts Init");

		Registration.register(tiContext.getActivity().getBaseContext(), new APIDReceiver() {
		    @Override
		    public void onReceive(String apid){
		        Log.d("MyApp", "Got apid: " + apid);
		    }
		});
		
		Registration.acceptPush(tiContext.getActivity().getBaseContext(), new PushReceiver() {
		    @Override
		    public void onReceive(String message, String payload){
		        Log.d("MyApp", "Got message '" + message +"' and payload '" + payload + "'");
		    }
		});
		
	}
	
	@Override
	public TiDict getConstants()
	{
		if (constants == null) {
			constants = new TiDict();
		}
		
		constants.put("CONTACTS_PRESENCE", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_STATUS", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_STATUS_ICON", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_STATUS_LABEL", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_STATUS_TIMESTAMP", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_ITEM_TYPE", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_TYPE", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_VCARD_TYPE", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_VCARD_URI", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_CUSTOM_RINGTONE", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_DISPLAY_NAME", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_LAST_TIME_CONTACTED", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_PHOTO", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_SEND_TO_VOICEMAIL", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_STARRED", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_GROUPS", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_IM_ACCOUNT", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_IM_HANDLE", "CONTACTS_PRESENCE");
		
		constants.put("CONTACTS_EMAIL_DISPLAY_NAME", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_EMAIL_TYPE", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_EMAIL", "CONTACTS_PRESENCE");
		
		constants.put("CONTACTS_NICKNAME", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_NOTE", "CONTACTS_PRESENCE");
		
		constants.put("CONTACTS_EVENTS", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_ORGANISATION", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_RELATION", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_STRCUTURED_NAME", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_POSTAL", "CONTACTS_PRESENCE");
		constants.put("CONTACTS_WEBSITE", "CONTACTS_PRESENCE");
		

		return constants;
	}	
	
	public void showContacts(TiDict props) {
		
//		cancel[function]: The function to call when selection is cancelled
//		selectedPerson[function]: The function to call when a person is selected.  Mutually exclusive with `selectedProperty`
//		selectedProperty[function]: The function to call when a property is selected.  Mutally exclusive with `selectedPerson`
//		animated[boolean]: Whether or not to animate the show/hide of the contacts picker
//		fields[array]: A list of field names to show when selecting properties, default is to show all available
		
		
		Log.d(LCAT, "Launching Contact intent");
		try {
			mContactAccessor = ContactAccessorBase.getInstance();
			if (null != mContactAccessor) {
				this.launchIntent();			
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void getAllPeople() {
		
		Cursor contacts = mContactAccessor.getContacts(getActivity());
		((ContactAccesor) mContactAccessor).displayContacts(contacts);
		
	}
	
	protected Activity getActivity() {
		return this.getTiContext().getActivity();
	}
	
	protected void launchIntent() {
		Log.d(LCAT, "Launching intent");

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

