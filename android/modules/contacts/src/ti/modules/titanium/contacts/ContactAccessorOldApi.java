package ti.modules.titanium.contacts;

import org.appcelerator.titanium.util.Log;

import android.content.Intent;
import android.net.Uri;
import android.provider.Contacts.People;

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
