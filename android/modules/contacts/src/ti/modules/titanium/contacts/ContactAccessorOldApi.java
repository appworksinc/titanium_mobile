package ti.modules.titanium.contacts;

import org.appcelerator.titanium.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
// We can leave this in - as the interface appears in Android 2+ builds and it facilitates development
import android.provider.Contacts.People;

/**
 * Accessor class for Contacts for Android pre 2.0 
 * 
 * @author dasher@inspiredthinking.co.uk
 *
 */
@SuppressWarnings("deprecation")
public class ContactAccessorOldApi extends ContactAccessorBase {
	
	private String LCAT = "ContactAccessorOldApi";

	@Override
	public Intent getContactPickerIntent() {
		Log.d(LCAT, "fetching OLD Contact picker intent");
		return new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
	}

	@Override
	public Uri fetchBaseURI() {
		// TODO Auto-generated method stub
		Log.d(LCAT, "fetchBaseURI");
		return People.CONTENT_FILTER_URI;
	}

	@Override
	public Cursor getContacts(Activity activity) {
		// TODO Auto-generated method stub
		Log.d(LCAT, "getContacts");
		return null;
	}
	
}
