package ti.modules.titanium.zendesk;

import org.apache.http.MethodNotSupportedException;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiProxy;
import com.zendesk.ZendeskDialog;

public class ZendeskProxy extends TiProxy {
	
	private ZendeskModule dialog; 

	public ZendeskProxy(TiContext tiContext) {
		super(tiContext);
		// TODO Auto-generated constructor stub
		this.dialog = new ZendeskModule(tiContext);
	}


}
