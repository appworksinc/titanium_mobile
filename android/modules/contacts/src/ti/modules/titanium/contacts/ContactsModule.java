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
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public TiDict getConstants()
	{
		if (constants == null) {
			constants = new TiDict();
		}

		return constants;
	}	
	
	public void showContacts() {
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
	
	public void getContacts() {
		
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

