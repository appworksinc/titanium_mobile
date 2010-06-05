package ti.modules.titanium.map;

import org.appcelerator.titanium.TiDict;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class TiOverlayItemPolygon extends TiOverlayItem {

	private AnnotationProxy proxy;
	private TiDict dataPoints;

	public TiOverlayItemPolygon(GeoPoint location, String title, String snippet, AnnotationProxy proxy) {
		super(location,title,snippet, proxy);
		this.proxy = proxy;
	}
	
	public void setDataPoints(TiDict data) {
		this.dataPoints = data;
	}
	
	public TiDict getDataPoints() {
		return this.dataPoints;
	}

	public AnnotationProxy getProxy() {
		return proxy;
	}
	public boolean hasData() {
		return getDataPoints() != null;
	}

}
