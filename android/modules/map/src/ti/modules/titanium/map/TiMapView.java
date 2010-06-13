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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Path.FillType;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;
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
	public boolean onTap(int index);
	public boolean onTap(Object overlay, int index);
	public boolean onTouchEvent(android.view.MotionEvent event, MapView mapView);	
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
	
	public static final int MAP_LAYAR_TYPE_DEFAULT = 1;
	public static final int MAP_LAYAR_TYPE_POLYGON = 2;
	
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
//  Precursor to enabling selective shadows & userLocation icon
//	private boolean enableShadow = true;
//	private int userLocationPinColor = Color.RED;

	private LocalMapView view;
	private Window mapWindow;
//	private List<Overlay> overlays;
	private HashMap<String, TitaniumOverlay> overlays;
//	private TitaniumOverlay overlay;
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
		boolean enableShadow = true;
		int overlayType = TiMapView.MAP_LAYAR_TYPE_DEFAULT;

		public TitaniumOverlay(Drawable defaultDrawable, TitaniumOverlayListener listener) {
			super(defaultDrawable);
			this.listener = listener;
		}
		
		public void setAnnotation(AnnotationProxy annotation) {
			
			if (this.annotations == null) {
				this.annotations = new ArrayList<AnnotationProxy>();
			}
			
			this.annotations.add(annotation);
		}
		
		public AnnotationProxy getAnnotation(int index) {
			return this.annotations.get(index);
		}
		
		public ArrayList<AnnotationProxy> getAnnontations() {
			return this.annotations;
		}

		public void setAnnotations(ArrayList<AnnotationProxy> annotations) {
			this.annotations = new ArrayList<AnnotationProxy>(annotations);

			doPopulate();
		}
		
		public int getOverlayType() {
			return this.overlayType;
		}
		
		public void doPopulate() {
			populate();
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
					// Pushed the conversion to it's own function to allow reuse

					item.setMarker(makeMarker(toColor(a.get("pincolor"))));					
//					Object value = a.get("pincolor");
//					item.setMarker(makeMarker(toColor(value)));							
					
//					try {
//						if (value instanceof String) {							
//							// Supported strings: Supported formats are: 
//							//     #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'
//							int markerColor = TiConvert.toColor((String) value);
//							item.setMarker(makeMarker(toColor(value)));							
//						} else {
//							// Assume it's a numeric
//							switch(a.getInt("pincolor")) {
//								case 1 : // RED
//									item.setMarker(makeMarker(Color.RED));
//									break;
//								case 2 : // GRE
//									item.setMarker(makeMarker(Color.GREEN));
//									break;
//								case 3 : // PURPLE
//									item.setMarker(makeMarker(Color.argb(255,192,0,192)));
//								break;
//								
//							}						
//						}										
//					} catch (Exception e) {
//						// May as well catch all errors 
//						Log.w(LCAT, "Unable to parse color [" + a.getString("pincolor")+"] for item ["+i+"]");							
//					}
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
			return (this.annotations == null) ? 0 : this.annotations.size();
		}

		@Override
		protected boolean onTap(int index) {			
			Log.d(LCAT, "TitaniumOverlay:onTap - Start");
			boolean handled = super.onTap(index);
			if(!handled ) {
				Log.d(LCAT, "TitaniumOverlay:onTap - !handled");
				handled = listener.onTap(this,index);
			} else {
				Log.d(LCAT, "TitaniumOverlay:onTap - Handled by Super");
			}

			return handled;
		}
		
//		@Override
//		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
//			
//			Log.d(LCAT, "TitaniumOverlay:onTouchEvent - Start");
//			
//			boolean handled = super.onTouchEvent(event, mapView);
//			if(!handled ) {
//				Log.d(LCAT, "TitaniumOverlay:onTouchEvent - not handled by super - passing to the listener.");
//				handled = listener.onTouchEvent(event, mapView);
//			} else {
//				Log.d(LCAT, "TitaniumOverlay:onTouchEvent - handled by super");				
//			}
//
//			return handled;
//		}				
		
	}

	public class TitaniumOverlayPolygon extends TitaniumOverlay {

		private boolean enableShadow = true;
		// Defaults
		private static final int lineWidthDefault = 2;
		private static final int lineColorDefault = 0xFF097286;
		private static final int fillColorDefault = 0x80FF0000;
		private static final boolean antiAliasDefault = true;
		private static final boolean completeDefault = true;
		
		public void setShadow(boolean v) {
			enableShadow = v;
		}
		
		public boolean getShadow() {
			return enableShadow;
		}		
		
		public TitaniumOverlayPolygon(Drawable defaultDrawable, TitaniumOverlayListener listener) {
			super(defaultDrawable, listener);
			this.overlayType = TiMapView.MAP_LAYAR_TYPE_POLYGON;
		}
		
		
		@Override
		protected boolean onTap(int index)
		{
			Log.d(LCAT, "TitaniumOverlayPolygon:onTap - Start");
			
			boolean handled = false;
			if(!handled ) {
				Log.d(LCAT, "TitaniumOverlayPolygon:onTap - Not Handled");
				handled = super.listener.onTap(this,index);
			}

			return handled;
		}	
		
//		@Override
//		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
//			
//			Log.d(LCAT, "TitaniumOverlayPolygon:onTouchEvent - Start");			
//			AnnotationProxy foundItem = getHitMapLocation(mapView,event);
//			
//			boolean handled = false;
//			
//			if (foundItem !=null) {				
//				// Just for debugging
//				TiDict props = foundItem.getDynamicProperties();
//				Log.d(LCAT, "TitaniumOverlayPolygon:onTouchEvent - Item Found: " + props.getString("title"));
//				
//				// We found something - so find the index so we can bubble the onTap
//				int index = super.annotations.indexOf(foundItem);
//				
//				// Bubble up the event to the listener
//				handled = super.listener.onTap(this,index);
//				mapView.invalidate();
//				return handled;
//			} else {
//				Log.d(LCAT, "TitaniumOverlayPolygon:onTouchEvent - No Item Found!");
//				return false;
//			}
//		}
		
		private AnnotationProxy getHitMapLocation(MapView mapView, MotionEvent event) {

			// Track which MapLocation was hit…if any
			AnnotationProxy hitMapLocation = null;

			Iterator<AnnotationProxy> iterator = annotations.iterator();
			while(iterator.hasNext()) {

				AnnotationProxy testLocation = iterator.next();
				
				TiDict props = testLocation.getDynamicProperties();
		        Path shapePath = new Path();
		      
		        // We need to do this more efficiently - but for now extract the data in each function
				if (props.containsKey("points")) {
					TiDict itemAttributes = props.getTiDict("points");
					if (itemAttributes.containsKey("data")) {
						
						Object[] dataPoints = (Object [])itemAttributes.get("data");
						
						// Build the shape
						for(int j = 0; j < dataPoints.length; j++) {
							
							TiDict dataPoint = (TiDict) dataPoints[j];
					        GeoPoint gp1 = new GeoPoint(scaleToGoogle(dataPoint.getDouble("latitude")), scaleToGoogle(dataPoint.getDouble("longitude")));
					        Point p1 = new Point();
					        view.getProjection().toPixels(gp1, p1);
					        if (j == 0) {
						        shapePath.moveTo(p1.x, p1.y);
					        } else {
						        shapePath.lineTo(p1.x, p1.y);
					        }
						}
						
						boolean shouldComplete = itemAttributes.optBoolean("complete", completeDefault);
						
						if (shouldComplete) {
							shapePath.close();
						} 
						
				        // Determine the bounds
				        RectF bounds = new RectF();
				        shapePath.computeBounds(bounds, true);
				        
				        // We find anything?
				        if (bounds.contains(event.getX(),event.getY())) {
				        	hitMapLocation = testLocation;
				        	break;
				        }				        
					}
				}
			}

			return hitMapLocation;
		}
		
		@Override
		protected TiOverlayItem createItem(int i) {
			TiOverlayItemPolygon item = null;

			Log.d(LCAT, "TitaniumOverlayPolygon - Index [" + i +"]");
			
			AnnotationProxy p = super.annotations.get(i);
			TiDict a = p.getDynamicProperties();
			if (a.containsKey("latitude") && a.containsKey("longitude")) {
				String title = a.optString("title", "");
				String subtitle = a.optString("subtitle", "");

				GeoPoint location = new GeoPoint(scaleToGoogle(a.getDouble("latitude")), scaleToGoogle(a.getDouble("longitude")));
				if (a.containsKey("points")) {
					item = new TiOverlayItemPolygon(location, title, subtitle, p);
					item.setDataPoints(a.getTiDict("points"));
				}

			} else {
				Log.w(LCAT, "Skipping annotation: No coordinates #" + i);
			}
			return item;
		}		
		
		@Override
		public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
			
			// We're not doing anything smart yet with shadows
			if (shadow == false) {
				
				for (int i = 0; i < super.annotations.size(); i++) {
					AnnotationProxy thisItem = super.annotations.get(i);
					
					TiDict props = thisItem.getDynamicProperties();
			        Path shapePath = new Path();
					
					if (props.containsKey("points")) {
						TiDict itemAttributes = props.getTiDict("points");
						if (itemAttributes.containsKey("data")) {
							
							// Grab the actual data
							Object[] dataPoints = (Object [])itemAttributes.get("data");
							
							// Build the path
							for(int j = 0; j < dataPoints.length; j++) {
								
								TiDict dataPoint = (TiDict) dataPoints[j];
						        GeoPoint gp1 = new GeoPoint(scaleToGoogle(dataPoint.getDouble("latitude")), scaleToGoogle(dataPoint.getDouble("longitude")));
						        Point p1 = new Point();
						        view.getProjection().toPixels(gp1, p1);
						        if (j == 0) {
						        	// The first item we move to - currently we're not using the initial lat/long from the Annotation
							        shapePath.moveTo(p1.x, p1.y);
						        } else {
							        shapePath.lineTo(p1.x, p1.y);
						        }
							}
							
							// Auto close the shape?
							boolean shouldComplete = itemAttributes.optBoolean("complete", completeDefault);
							
							if (shouldComplete) {
								shapePath.close();
							} 
							
					        RectF bounds = new RectF();
					        shapePath.computeBounds(bounds, true);
					        
					        // Start actually drawing the shape - with the fill
					        // We're currently not handling unfilled shapes
					        Paint mPaint = new Paint();
					        
					        // Handle passed properties					        
					        if (itemAttributes.containsKey("lineWidth")) {
						        mPaint.setStrokeWidth(itemAttributes.getInt("lineWidth"));
					        } else {
						        mPaint.setStrokeWidth(lineWidthDefault);
					        }
					        
					        // I can do this via itemAttributes.opt I think
					        if (itemAttributes.containsKey("fillColor")) {
						        mPaint.setColor(toColor(itemAttributes.get("fillColor"))); 
					        } else {
						        mPaint.setColor(toColor(fillColorDefault)); 
					        }
					        					        
					        if (itemAttributes.containsKey("antiAlias")) {
						        mPaint.setAntiAlias(itemAttributes.getBoolean("antiAlias"));
					        } else {
						        mPaint.setAntiAlias(antiAliasDefault);
					        }
					        
					        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);					        
					        canvas.drawPath(shapePath, mPaint);	
					        
					        // Draw the border only
					        mPaint = new Paint();
					        
					        // Handle passed properties
					        if (itemAttributes.containsKey("lineWidth")) {
						        mPaint.setStrokeWidth(itemAttributes.getInt("lineWidth"));
					        } else {
						        mPaint.setStrokeWidth(lineWidthDefault);
					        }
					        
					        if (itemAttributes.containsKey("lineColor")) {
						        mPaint.setColor(toColor(itemAttributes.get("lineColor"))); //tealish with no transparency
					        } else {
						        mPaint.setColor(toColor(lineColorDefault)); //tealish with no transparency
					        }
					        					        
					        if (itemAttributes.containsKey("antiAlias")) {
						        mPaint.setAntiAlias(itemAttributes.getBoolean("antiAlias"));
					        } else {
						        mPaint.setAntiAlias(antiAliasDefault);
					        }
					        
					        mPaint.setStyle(Paint.Style.STROKE);
					        
					        canvas.drawPath(shapePath, mPaint);	
							
						}	
					}					
				}
			}
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
				
				TitaniumOverlay overlay = getDefaultOverlay();				
				TiOverlayItem item = overlay.getItem(lastIndex);
				
				if (item != null) {
					Log.d(LCAT, "TiMapView:onClick - item is found - index ["+lastIndex+"] & clickedItem ["+clickedItem+"]");
					TiDict d = new TiDict();
					d.put("title", item.getTitle());
					d.put("subtitle", item.getSnippet());
					d.put("latitude", scaleFromGoogle(item.getPoint().getLatitudeE6()));
					d.put("longitude", scaleFromGoogle(item.getPoint().getLongitudeE6()));
					d.put("annotation", item.getProxy());
					d.put("clicksource", clickedItem);

					fproxy.fireEvent(EVENT_CLICK, d);
				} else {
					// item is null
					Log.d(LCAT, "TiMapView:onClick - item is null - index ["+lastIndex+"] & clickedItem ["+clickedItem+"]");
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
			//Make sure the annotation is always on top of the marker
			
			Log.d(LCAT, "showAnnotation index ["+index+"]");
			
			TiDict props = item.getProxy().getDynamicProperties();
			int y=0;
			
			switch (props.getInt("layarType")) {
				case TiMapView.MAP_LAYAR_TYPE_POLYGON:
					Log.d(LCAT, "showAnnotation layarType is Poly");
					y = -20;					
				break;
	
				default:
					Log.d(LCAT, "showAnnotation layarType is default");
					Drawable itemMarker = item.getMarker(TiOverlayItem.ITEM_STATE_FOCUSED_MASK);
					if (itemMarker != null) {
						y = -1*itemMarker.getIntrinsicHeight();
					}				
				break;
			}
						
			MapView.LayoutParams params = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT, item.getPoint(), 0, y, MapView.LayoutParams.BOTTOM_CENTER);
			params.mode = MapView.LayoutParams.MODE_MAP;

			view.addView(itemView, params);
		} else {
			Log.d(LCAT, "showAnnotation view, ItemView or Item is null.");			
		}
	}
	
	public boolean onTap(Object overlay, int index) {
		
		synchronized (overlay) {
			Log.d(LCAT, "TiMapView:onTap with overlay & index ["+index+"]");			
			TiOverlayItem item = ((TitaniumOverlay)overlay).getItem(index);
			
			if (itemView != null) {
				if (itemView.getLastItem() !=null && itemView.getLastItem().equals(item)) {					
					if (itemView.getVisibility() == View.VISIBLE) {
						Log.d(LCAT, "TiMapView:onTap hideAnnotation");			
						hideAnnotation();	
						return true;
					} else {
						Log.d(LCAT, "TiMapView:onTap isn't Visible");									
					}
				} else {
					if (itemView.getLastItem() == null) {
						Log.d(LCAT, "TiMapView:onTap LastItem is null");						
					} else {
						Log.d(LCAT, "TiMapView:onTap !equals");	
						Log.d(LCAT, "TiMapView:onTap lastItemHash - "+itemView.getLastItem().hashCode());	
						Log.d(LCAT, "TiMapView:onTap lastItemHash - "+item.hashCode());	
					}					
				}
			}
//			} else {
				if (item.hasData()) {
					Log.d(LCAT, "TiMapView:onTap hasData");			

					hideAnnotation();
					showAnnotation(index, item);
					return true;
				} else {
					Log.d(LCAT, "TiMapView:onTap hasData is FALSE");
					//Toast.makeText(proxy.getContext(), "No information for location", Toast.LENGTH_SHORT).show();
					Toast.makeText(this.mapWindow.getContext(), "No information for location", Toast.LENGTH_SHORT).show();					
				}	
//			}
		}
		return false;
			
//			if (itemView != null && index == itemView.getLastIndex() && itemView.getVisibility() == View.VISIBLE) {
//				Log.d(LCAT, "onTap hideAnnotation");			
//				hideAnnotation();
//			} else {
//				if (item.hasData()) {
//					Log.d(LCAT, "onTap hasData");			
//
//					hideAnnotation();
//					showAnnotation(index, item);
//					return true;
//				} else {
//					Log.d(LCAT, "onTap hasData is FALSE");
//					//Toast.makeText(proxy.getContext(), "No information for location", Toast.LENGTH_SHORT).show();
//					Toast.makeText(this.mapWindow.getContext(), "No information for location", Toast.LENGTH_SHORT).show();					
//				}				
//			}
//			return false;
	}		
	
	

	public boolean onTap(int index)
	{
		TitaniumOverlay thisOverlay = getDefaultOverlay();
		
		Log.d(LCAT, "TiMapView:onTap with index ["+index+"]");			
		
		if (thisOverlay != null) {
			Log.d(LCAT, "TiMapView:onTap found a default Overlay");			

			synchronized(thisOverlay) {
				// TODO: We should really use the global annotations and work out which overlay it's actually assoc with
				TiOverlayItem item = thisOverlay.getItem(index);

				if (itemView != null && index == itemView.getLastIndex() && itemView.getVisibility() == View.VISIBLE) {
					Log.d(LCAT, "TiMapView:onTap hideAnnotation");			
					hideAnnotation();
				} else {
					if (item.hasData()) {
						Log.d(LCAT, "TiMapView:onTap hasData");			
						hideAnnotation();
						showAnnotation(index, item);
						return true;
					} else {
						Log.d(LCAT, "TiMapView:onTap hasData is FALSE");
						//Toast.makeText(proxy.getContext(), "No information for location", Toast.LENGTH_SHORT).show();
						Toast.makeText(this.mapWindow.getContext(), "No information for location", Toast.LENGTH_SHORT).show();					
					}
				}
			}
		} else {
			Log.d(LCAT, "TiMapView:onTap with no Default Overlay");			
		}
		return false;
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
		if (d.containsKey("annotations")) {
			proxy.internalSetDynamicValue("annotations", d.get("annotations"), false);
			Object [] annotations = (Object[]) d.get("annotations");
			
			Log.e(LCAT, "Annotations - start data - Length:" + annotations.length);
			
			for(int i = 0; i < annotations.length; i++) {
				
				Log.e(LCAT, "Annotations - Annotation ["+i+"]- Type:" + (annotations[i]).getClass().getSimpleName());

				try {
					AnnotationProxy ap = (AnnotationProxy) annotations[i];
					this.annotations.add(ap);
				} catch (Exception e) {
					// TODO: handle exception
					Log.e(LCAT, "ProcessProperties:ProcessAnnotations Problem with index ["+i+"]" ,e);
				}
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
			overlays = new HashMap<String, TitaniumOverlay>();
			List<Overlay> currentOverlays = view.getOverlays();

			synchronized(currentOverlays) {
				
				// Legacy - not sure if this is needed - but clear out the old overlays
				Collection<TitaniumOverlay> colValues = overlays.values();
				Iterator<TitaniumOverlay> iterator = colValues.iterator();
				
				while(iterator.hasNext()) {
					TitaniumOverlay thisOverlay = iterator.next();
					
					if (currentOverlays.contains(thisOverlay)) {
						overlays.remove(thisOverlay);
					}					
				}				


				if (annotations.size() > 0) {
					//overlay = new TitaniumOverlay(makeMarker(Color.BLUE), this);
					//TitaniumOverlayPolygon overlayPoly = new TitaniumOverlayPolygon(makeMarker(Color.BLUE), this);
					
					for (int i = 0; i < annotations.size(); i++) {
						AnnotationProxy thisItem = annotations.get(i);						
						TiDict props = thisItem.getDynamicProperties();
						
						String layerName = props.optString("layarName", "default").toLowerCase();
						Integer layarType = props.optInt("layarType", TiMapView.MAP_LAYAR_TYPE_DEFAULT);
						
						// TODO: Implement named layers
						TitaniumOverlay thisOverlay = null;
						
						if (overlays.containsKey(layerName)) {
							// We have something to work with
							thisOverlay = (TitaniumOverlay) overlays.get(layerName);							
						} else {
							switch (layarType) {
								case TiMapView.MAP_LAYAR_TYPE_POLYGON:
									// Validate
									if (thisOverlay == null) {
										thisOverlay = new TitaniumOverlayPolygon(makeMarker(Color.BLUE), this);
									}
									if (!props.containsKey("points")) {
										Log.e(LCAT, "Invalid Annotation - required property 'points' not found ");
									}								
								break;
		
								default:
									if (thisOverlay == null) {
										thisOverlay = new TitaniumOverlay(makeMarker(Color.BLUE), this);
									}
								break;
							}							
							overlays.put(layerName,thisOverlay);
							currentOverlays.add(thisOverlay);
						}						
						thisOverlay.setAnnotation(thisItem);
					}
					
					colValues = overlays.values();
					iterator = colValues.iterator();
					
					while(iterator.hasNext()) {
						TitaniumOverlay thisOverlay = iterator.next();
						thisOverlay.doPopulate();
					}					
				}

				// Trigger the update of the view
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
		TitaniumOverlay overlay = getDefaultOverlay();
		
		if (title != null && view != null && annotations != null && overlay != null) {
			int index = findAnnotation(title);
			if (index > -1) {
				
				Log.d(LCAT, "TiMapView:doSelectAnnotation - item is null - title ["+title+"]");
				
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

	private TitaniumOverlay getDefaultOverlay() {
		return getOverlay(TiMapView.MAP_LAYAR_TYPE_DEFAULT);
	}
		
	/**
	 * Return the first overlay found of the passed type
	 * @param mapOverlayType
	 * @return TitaniumOverlay
	 */
	private TitaniumOverlay getOverlay(int mapOverlayType) {
		
		synchronized (overlays) {

			Collection<TitaniumOverlay> colValues = overlays.values();
			Iterator<TitaniumOverlay> iterator = colValues.iterator();
			
			while(iterator.hasNext()) {
				TitaniumOverlay theOverlay = iterator.next();
				if (theOverlay.getOverlayType() == mapOverlayType) {
					return theOverlay;
				}
			}			
		}
		
		return null;
	}
	
	/**
	 * Return the overlay associated with the passed name
	 * 
	 * @param mapOverlayName
	 * @return TitaniumOverlay 
	 */
	private TitaniumOverlay getOverlay(String mapOverlayName) {
		
		synchronized (overlays) {
			try {
				return overlays.get(mapOverlayName);
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}
		}		
	}
		
	/**
	 * Takes the passed value object and acts accordingly based on the passed type
	 * @param value Object The color value to parse
	 * @return int the converted color
	 */
	private int toColor(Object value) {
		
		try {
			if (value instanceof String) {				
				// Supported strings: Supported formats are: 
				//     #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'
				return TiConvert.toColor((String) value);
			} else {
				// Assume it's a numeric
				switch(TiConvert.toInt(value)) {
					case 1 : // RED
						return Color.RED;
					case 2 : // GREEN
						return Color.GREEN;
					case 3 : // PURPLE
						return Color.argb(255,192,0,192);
				}						
			}										
		} catch (Exception e) {
			// May as well catch all errors 
			Log.d(LCAT, "TiMapView - Unable to parse color [" + (TiConvert.toString(value)) +"]");							
		}
		// Handle unknown passed ints and exceptions
		return 0;		
	}
	
	private Drawable makeMarker(int c)
	{
		OvalShape s = new OvalShape();
		s.resize(1.0f, 1.0f);
		ShapeDrawable d = new ShapeDrawable(s);
		d.setBounds(0, 0, 15, 15);
		d.getPaint().setColor(c);
		d.getPaint().clearShadowLayer();

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

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		// TODO Auto-generated method stub
		return false;
	}
}
