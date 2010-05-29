/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package ti.modules.titanium.map;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiProperties;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.TiContext.OnLifecycleEvent;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

interface TitaniumOverlayListener {
	public void onTap(int index);
}

public class TiMapView extends TiUIView
	implements Handler.Callback, TitaniumOverlayListener
{
	private static final String LCAT = "TiMapView";
	private static final boolean DBG = TiConfig.LOGD;

	private static final String TI_DEVELOPMENT_KEY = "0Rq5tT4bUSXcVQ3F0gt8ekVBkqgn05ZJBQMj6uw";
	private static final String OLD_API_KEY = "ti.android.google.map.api.key";
	private static final String DEVELOPMENT_API_KEY = "ti.android.google.map.api.key.development";
	private static final String PRODUCTION_API_KEY = "ti.android.google.map.api.key.production";

	public static final String EVENT_CLICK = "click";
	public static final String EVENT_REGION_CHANGED = "regionChanged";

	public static final int MAP_VIEW_STANDARD = 1;
	public static final int MAP_VIEW_SATELLITE = 2;
	public static final int MAP_VIEW_HYBRID = 3;

	private static final int MSG_SET_LOCATION = 300;
	private static final int MSG_SET_MAPTYPE = 301;
	private static final int MSG_SET_REGIONFIT = 302;
	private static final int MSG_SET_ANIMATE = 303;
	private static final int MSG_SET_USERLOCATION = 304;
	private static final int MSG_SET_SCROLLENABLED = 305;
	private static final int MSG_CHANGE_ZOOM = 306;
	private static final int MSG_ADD_ANNOTATION = 307;
	private static final int MSG_REMOVE_ANNOTATION = 308;
	private static final int MSG_SELECT_ANNOTATION = 309;
	private static final int MSG_REMOVE_ALL_ANNOTATIONS = 310;

	//private MapView view;
	private boolean scrollEnabled;
	private boolean regionFit;
	private boolean animate;
	private boolean userLocation;
	private boolean enableShadow = true;
	private int userLocationPinColor = Color.RED;

	private LocalMapView view;
	private Window mapWindow;
	private TitaniumOverlay overlay;
	private MyLocationOverlay myLocation;
	private TiOverlayItemView itemView;
	private ArrayList<AnnotationProxy> annotations;
	private Handler handler;

	class LocalMapView extends MapView
	{
		private boolean scrollEnabled;
		private int lastLongitude;
		private int lastLatitude;
		private int lastLatitudeSpan;
		private int lastLongitudeSpan;

		public LocalMapView(Context context, String apiKey) {
			super(context, apiKey);
			scrollEnabled = false;
		}

		public void setScrollable(boolean enable) {
			scrollEnabled = enable;
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			if (!scrollEnabled && ev.getAction() == MotionEvent.ACTION_MOVE) {
				return true;
			}
			return super.dispatchTouchEvent(ev);
		}

		@Override
		public boolean dispatchTrackballEvent(MotionEvent ev) {
			if (!scrollEnabled && ev.getAction() == MotionEvent.ACTION_MOVE) {
				return true;
			}
			return super.dispatchTrackballEvent(ev);
		}

		@Override
		public void computeScroll() {
			super.computeScroll();

			GeoPoint center = getMapCenter();
			if (lastLatitude != center.getLatitudeE6() || lastLongitude != center.getLongitudeE6() ||
					lastLatitudeSpan != getLatitudeSpan() || lastLongitudeSpan != getLongitudeSpan())
			{
				lastLatitude = center.getLatitudeE6();
				lastLongitude = center.getLongitudeE6();
				lastLatitudeSpan = getLatitudeSpan();
				lastLongitudeSpan = getLongitudeSpan();

				TiDict d = new TiDict();
				d.put("latitude", scaleFromGoogle(lastLatitude));
				d.put("longitude", scaleFromGoogle(lastLongitude));
				d.put("latitudeDelta", scaleFromGoogle(lastLatitudeSpan));
				d.put("longitudeDelta", scaleFromGoogle(lastLongitudeSpan));

				proxy.fireEvent("regionChanged", d);
			}

		}
	}

	class TitaniumOverlay extends ItemizedOverlay<TiOverlayItem>
	{
		ArrayList<AnnotationProxy> annotations;
		TitaniumOverlayListener listener;

		public TitaniumOverlay(Drawable defaultDrawable, TitaniumOverlayListener listener) {
			super(defaultDrawable);
			this.listener = listener;
		}

		public void setAnnotations(ArrayList<AnnotationProxy> annotations) {
			this.annotations = new ArrayList<AnnotationProxy>(annotations);

			populate();
		}
		
		public void setShadow(boolean v) {
			enableShadow = v;
		}
		
		public boolean getShadow() {
			return enableShadow;
		}

		@Override
		public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, false);
		}
		
		@Override
		protected TiOverlayItem createItem(int i) {
			TiOverlayItem item = null;

			AnnotationProxy p = annotations.get(i);
			TiDict a = p.getDynamicProperties();
			if (a.containsKey("latitude") && a.containsKey("longitude")) {
				String title = a.optString("title", "");
				String subtitle = a.optString("subtitle", "");

				GeoPoint location = new GeoPoint(scaleToGoogle(a.getDouble("latitude")), scaleToGoogle(a.getDouble("longitude")));
				item = new TiOverlayItem(location, title, subtitle, p);

				//prefer pinImage to pincolor.
				if (a.containsKey("pinImage"))
				{
					String imagePath = a.getString("pinImage");
					Drawable marker = makeMarker(imagePath);
					boundCenterBottom(marker);
					item.setMarker(marker);
				} else if (a.containsKey("pincolor")) {					
					item.setMarker(makeMarker(toColor(a.get("pincolor"))));
				}

				if (a.containsKey("leftButton")) {
					item.setLeftButton(proxy.getTiContext().resolveUrl(null, a.getString("leftButton")));
				}
				if (a.containsKey("rightButton")) {
					item.setRightButton(proxy.getTiContext().resolveUrl(null, a.getString("rightButton")));
				}
			} else {
				Log.w(LCAT, "Skipping annotation: No coordinates #" + i);
			}
			return item;
		}

		@Override
		public int size() {
			return (annotations == null) ? 0 : annotations.size();
		}

		@Override
		protected boolean onTap(int index)
		{
			boolean handled = super.onTap(index);
			if(!handled ) {
				listener.onTap(index);
			}

			return handled;
		}
	}

	public TiMapView(TiViewProxy proxy, Window mapWindow)
	{
		super(proxy);

		this.mapWindow = mapWindow;
		this.handler = new Handler(this);
		this.annotations = new ArrayList<AnnotationProxy>();

		//TODO MapKey
		TiApplication app = proxy.getTiContext().getTiApp();
		TiProperties appProperties = app.getSystemProperties();
		String oldKey = appProperties.getString(OLD_API_KEY, TI_DEVELOPMENT_KEY);
		String developmentKey = appProperties.getString(DEVELOPMENT_API_KEY, oldKey);
		String productionKey = appProperties.getString(PRODUCTION_API_KEY, oldKey);

		String apiKey = developmentKey;
		if (app.getDeployType().equals(TiApplication.DEPLOY_TYPE_PRODUCTION)) {
			apiKey = productionKey;
		}

		view = new LocalMapView(mapWindow.getContext(), apiKey);
		TiMapActivity ma = (TiMapActivity) mapWindow.getContext();

		ma.setLifecycleListener(new OnLifecycleEvent()
			{
				public void onPause() {
					if (myLocation != null) {
						if (DBG) {
							Log.d(LCAT, "onPause: Disabling My Location");
						}
						myLocation.disableMyLocation();
					}
				}

				public void onResume() {
					if (myLocation != null && userLocation) {
						if (DBG) {
							Log.d(LCAT, "onResume: Enabling My Location");
						}
						myLocation.enableMyLocation();
					}
				}

				public void onDestroy() {
				}

				public void onStart() {
				}

				public void onStop() {
				}
			});
		view.setBuiltInZoomControls(true);
		view.setScrollable(true);
		view.setClickable(true);

		setNativeView(view);

		this.regionFit =true;
		this.animate = false;

		final TiViewProxy fproxy = proxy;

		itemView = new TiOverlayItemView(proxy.getContext());
		itemView.setOnOverlayClickedListener(new TiOverlayItemView.OnOverlayClicked(){
			public void onClick(int lastIndex, String clickedItem) {
				TiOverlayItem item = overlay.getItem(lastIndex);
				if (item != null) {
					TiDict d = new TiDict();
					d.put("title", item.getTitle());
					d.put("subtitle", item.getSnippet());
					d.put("latitude", scaleFromGoogle(item.getPoint().getLatitudeE6()));
					d.put("longitude", scaleFromGoogle(item.getPoint().getLongitudeE6()));
					d.put("annotation", item.getProxy());
					d.put("clicksource", clickedItem);

					fproxy.fireEvent(EVENT_CLICK, d);
				}
			}
		});
	}

	private LocalMapView getView() {
		return view;
	}

	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case MSG_SET_LOCATION : {
				doSetLocation((TiDict) msg.obj);
				return true;
			}
			case MSG_SET_MAPTYPE : {
				doSetMapType(msg.arg1);
				return true;
			}
			case MSG_SET_REGIONFIT :
				regionFit = msg.arg1 == 1 ? true : false;
				return true;
			case MSG_SET_ANIMATE :
				animate = msg.arg1 == 1 ? true : false;
				return true;
			case MSG_SET_SCROLLENABLED :
				animate = msg.arg1 == 1 ? true : false;
				if (view != null) {
					view.setScrollable(scrollEnabled);
				}
				return true;
			case MSG_SET_USERLOCATION :
				userLocation = msg.arg1 == 1 ? true : false;
				doUserLocation(userLocation);
				return true;
			case MSG_CHANGE_ZOOM :
				MapController mc = view.getController();
				if (mc != null) {
					mc.setZoom(view.getZoomLevel() + msg.arg1);
				}
				return true;
			case MSG_ADD_ANNOTATION :
				doAddAnnotation((AnnotationProxy) msg.obj);
				return true;
			case MSG_REMOVE_ANNOTATION :
				doRemoveAnnotation((String) msg.obj);
				return true;
			case MSG_SELECT_ANNOTATION :
				boolean select = msg.arg1 == 1 ? true : false;
				boolean animate = msg.arg2 == 1 ? true : false;
				String title = (String) msg.obj;
				doSelectAnnotation(select, title, animate);
				return true;
			case MSG_REMOVE_ALL_ANNOTATIONS :
				annotations.clear();
				doSetAnnotations(annotations);
				return true;
		}

		return false;
	}

	private void hideAnnotation()
	{
		if (view != null && itemView != null) {
			view.removeView(itemView);
			itemView.clearLastIndex();
		}
	}

	private void showAnnotation(int index, TiOverlayItem item)
	{
		if (view != null && itemView != null && item != null) {
			itemView.setItem(index, item);
			//Make sure the atonnation is always on top of the marker
			int y = -1*item.getMarker(TiOverlayItem.ITEM_STATE_FOCUSED_MASK).getIntrinsicHeight();
			MapView.LayoutParams params = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, item.getPoint(), 0, y, MapView.LayoutParams.BOTTOM_CENTER);
			params.mode = MapView.LayoutParams.MODE_MAP;

			view.addView(itemView, params);
		}
	}

	public void onTap(int index)
	{
		if (overlay != null) {
			synchronized(overlay) {
				TiOverlayItem item = overlay.getItem(index);

				if (itemView != null && index == itemView.getLastIndex() && itemView.getVisibility() == View.VISIBLE) {
					hideAnnotation();
					return;
				}

				if (item.hasData())
				{
					hideAnnotation();
					showAnnotation(index, item);
				} else {
					Toast.makeText(proxy.getContext(), "No information for location", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	public void processProperties(TiDict d)
	{
		LocalMapView view = getView();

		if (d.containsKey("mapType")) {
			doSetMapType(TiConvert.toInt(d, "mapType"));
		}
		if (d.containsKey("zoomEnabled")) {
			view.setBuiltInZoomControls(TiConvert.toBoolean(d,"zoomEnabled"));
		}
		if (d.containsKey("scrollEnabled")) {
			view.setScrollable(TiConvert.toBoolean(d, "scrollEnabled"));
		}
		if (d.containsKey("region")) {
			doSetLocation(d.getTiDict("region"));
		}
		if (d.containsKey("regionFit")) {
			regionFit = d.getBoolean("regionFit");
		}
		if (d.containsKey("animate")) {
			animate = d.getBoolean("animate");
		}
		if (d.containsKey("userLocation")) {
			doUserLocation(d.getBoolean("userLocation"));
		}
		if (d.containsKey("userLocationPinColor")) {
			userLocationPinColor = toColor(d.get("userLocationPinColor"));
		}
		if (d.containsKey("annotations")) {
			proxy.internalSetDynamicValue("annotations", d.get("annotations"), false);
			Object [] annotations = (Object[]) d.get("annotations");
			for(int i = 0; i < annotations.length; i++) {
				AnnotationProxy ap = (AnnotationProxy) annotations[i];
				this.annotations.add(ap);
			}
			doSetAnnotations(this.annotations);
		}

		super.processProperties(d);
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, TiProxy proxy)
	{

		if (key.equals("location")) {
			if (newValue != null) {
				if (newValue instanceof AnnotationProxy) {
					AnnotationProxy ap = (AnnotationProxy) newValue;
					doSetLocation(ap.getDynamicProperties());
				} else if (newValue instanceof TiDict) {
					doSetLocation((TiDict) newValue);
				}
			}
		} else if (key.equals("mapType")) {
			if (newValue == null) {
				doSetMapType(MAP_VIEW_STANDARD);
			} else {
				doSetMapType(TiConvert.toInt(newValue));
			}
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
	}

	public void doSetLocation(TiDict d)
	{
		LocalMapView view = getView();

		if (d.containsKey("longitude") && d.containsKey("latitude")) {
			GeoPoint gp = new GeoPoint(scaleToGoogle(d.getDouble("latitude")), scaleToGoogle(d.getDouble("longitude")));
			boolean anim = false;
			if (d.containsKey("animate")) {
				anim = TiConvert.toBoolean(d, "animate");
			}
			if (anim) {
				view.getController().animateTo(gp);
			} else {
				view.getController().setCenter(gp);
			}
		}
		if (regionFit && d.containsKey("longitudeDelta") && d.containsKey("latitudeDelta")) {
			view.getController().zoomToSpan(scaleToGoogle(d.getDouble("latitudeDelta")), scaleToGoogle(d.getDouble("longitudeDelta")));
		} else {
			Log.w(LCAT, "span must have longitudeDelta and latitudeDelta");
		}
	}

	public void doSetMapType(int type)
	{
		if (view != null) {
			switch(type) {
			case MAP_VIEW_STANDARD :
				view.setSatellite(false);
				view.setTraffic(false);
				view.setStreetView(false);
				break;
			case MAP_VIEW_SATELLITE:
				view.setSatellite(true);
				view.setTraffic(false);
				view.setStreetView(false);
				break;
			case MAP_VIEW_HYBRID :
				view.setSatellite(false);
				view.setTraffic(false);
				view.setStreetView(true);
				break;
			}
		}
	}

	public void doSetAnnotations(ArrayList<AnnotationProxy> annotations)
	{
		if (annotations != null) {

			this.annotations = annotations;
			List<Overlay> overlays = view.getOverlays();

			synchronized(overlays) {
				if (overlays.contains(overlay)) {
					overlays.remove(overlay);
					overlay = null;
				}

				if (annotations.size() > 0) {
					overlay = new TitaniumOverlay(makeMarker(userLocationPinColor), this);
					overlay.setAnnotations(annotations);
					overlays.add(overlay);
				}

				view.invalidate();
			}
		}
	}

	public void addAnnotation(AnnotationProxy annotation) {
		handler.obtainMessage(MSG_ADD_ANNOTATION, annotation).sendToTarget();
	};

	public void doAddAnnotation(AnnotationProxy annotation)
	{
		if (annotation != null && view != null) {

			annotations.add(annotation);
			doSetAnnotations(annotations);
		}
	};

	public void removeAnnotation(String title) {
		handler.obtainMessage(MSG_REMOVE_ANNOTATION, title).sendToTarget();
	};

	public void removeAllAnnotations() {
		handler.obtainMessage(MSG_REMOVE_ALL_ANNOTATIONS).sendToTarget();
	}

	private int findAnnotation(String title)
	{
		int existsIndex = -1;
		// Check for existence
		int len = annotations.size();
		for(int i = 0; i < len; i++) {
			AnnotationProxy a = annotations.get(i);
			String t = (String) a.getDynamicValue("title");

			if (t != null) {
				if (title.equals(t)) {
					if (DBG) {
						Log.d(LCAT, "Annotation found at index: " + " with title: " + title);
					}
					existsIndex = i;
					break;
				}
			}
		}

		return existsIndex;
	}

	public void doRemoveAnnotation(String title)
	{
		if (title != null && view != null && annotations != null) {
			int existsIndex = findAnnotation(title);
			// If found, build a new annotation list
			if (existsIndex > -1) {
				annotations.remove(existsIndex);

				doSetAnnotations(annotations);
			}
		}
	};

	public void selectAnnotation(boolean select, String title, boolean animate)
	{
		if (title != null) {
			handler.obtainMessage(MSG_SELECT_ANNOTATION, select ? 1 : 0, animate ? 1 : 0, title).sendToTarget();
		}
	}

	public void doSelectAnnotation(boolean select, String title, boolean animate)
	{
		if (title != null && view != null && annotations != null && overlay != null) {
			int index = findAnnotation(title);
			if (index > -1) {
				if (overlay != null) {
					synchronized(overlay) {
						TiOverlayItem item = overlay.getItem(index);

						if (select) {
							if (itemView != null && index == itemView.getLastIndex() && itemView.getVisibility() != View.VISIBLE) {
								showAnnotation(index, item);
								return;
							}

							hideAnnotation();

							MapController controller = view.getController();
							if (animate) {
								controller.animateTo(item.getPoint());
							} else {
								controller.setCenter(item.getPoint());
							}
							showAnnotation(index, item);
						} else {
							hideAnnotation();
						}
					}
				}
			}
		}
	}

	public void doUserLocation(boolean userLocation)
	{
		if (view != null) {
			if (userLocation) {
				if (myLocation == null) {
					myLocation = new MyLocationOverlay(proxy.getContext(), view);
				}

				List<Overlay> overlays = view.getOverlays();
				synchronized(overlays) {
					if (!overlays.contains(myLocation)) {
						overlays.add(myLocation);
					}
				}

				myLocation.enableMyLocation();

			} else {
				if (myLocation != null) {
					List<Overlay> overlays = view.getOverlays();
					synchronized(overlays) {
						if (overlays.contains(myLocation)) {
							overlays.remove(myLocation);
						}
						myLocation.disableMyLocation();
					}
				}
			}
		}
	}

	public void changeZoomLevel(int delta) {
		handler.obtainMessage(MSG_CHANGE_ZOOM, delta, 0).sendToTarget();
	}

	private int toColor(Object c) {
		String classType = c.getClass().getSimpleName().toLowerCase();
		
		if (classType.equals("string")) {
			try {
				return Color.parseColor(TiConvert.toString(c));
			} catch (Exception e) {
				Log.w(LCAT, "Unable to parse color: " + TiConvert.toString(c));	
				return 0;
			}
		} else {
			// Assume it's an int
			int pinColor = TiConvert.toInt(c);
			switch(pinColor) {
				case 1 : // RED
					return Color.RED;
				case 2 : // GRE
					return Color.GREEN;
				case 3 : // PURPLE
					return Color.argb(255,192,0,192);
				default:
					// TODO - Default?
					return 0;
			}						
		}
	}
	
	private Drawable makeMarker(int c)
	{
		OvalShape s = new OvalShape();
		s.resize(1.0f, 1.0f);
		ShapeDrawable d = new ShapeDrawable(s);
		d.setBounds(0, 0, 15, 15);
		d.getPaint().setColor(c);

		return d;
	}

	private Drawable makeMarker(String pinImage)
	{
		String url = proxy.getTiContext().resolveUrl(null, pinImage);
		TiBaseFile file = TiFileFactory.createTitaniumFile(proxy.getTiContext(), new String[] { url }, false);
		try {
			Drawable d = new BitmapDrawable(TiUIHelper.createBitmap(file.getInputStream()));
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			return d;
		} catch (IOException e) {
			Log.e(LCAT, "Error creating drawable from path: " + pinImage.toString(), e);
		}
		return null;
	}
	private double scaleFromGoogle(int value) {
		return (double)value / 1000000.0;
	}

	private int scaleToGoogle(double value) {
		return (int)(value * 1000000);
	}
}
