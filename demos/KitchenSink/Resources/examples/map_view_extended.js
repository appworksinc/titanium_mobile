var win = Titanium.UI.currentWindow;

//41.381574, 2.18536
var poiOne = Titanium.Map.createAnnotation({
    latitude:41.381574,
    longitude:2.18536,
    title:"Hola Buonita",
    subtitle:'Saturday Night Parties in Barcelona',
    pincolor:"#08FF00F0",  // Supports #ARGB, #AARRGGBB, #RGB, #RRGGBB & Color Constants (Titanium.Map.ANNOTATION_GREEN, etc)
    animate:true,
    leftButton: 'app://images/KS_nav_views.png',
    myid:1,
    layarName: "default", // Optional - if not passed it'll default to default or default-poly
    layarType: Titanium.Map.MAP_LAYAR_TYPE_DEFAULT // Titanium.Map.MAP_LAYAR_TYPE_DEFAULT & MAP_LAYAR_TYPE_POLYGON 
});

var polyOne = Titanium.Map.createAnnotation({
    latitude:41.381083,
    longitude:2.18185,
    title:"PolyOne",
    subtitle:'Extended Polygon',
    animate:false,
    leftButton: 'app://buttons/medium-green-round-building.png',
    myid:11, 
    // The points structure allows us to define extra information that should be passed when creating the polygon
    points: {
		// data - the actual points to use when making the shape, these are standard map coords
		data:[
		      {latitude:41.381083, longitude:2.18185},
		      {latitude:41.381582, longitude:2.18042},
		      {latitude:41.382323, longitude:2.18094},
		      {latitude:41.382637, longitude:2.18119},
		      {latitude:41.382532, longitude:2.18166},
		      {latitude:41.382363, longitude:2.18226},
		      {latitude:41.381719, longitude:2.18177},
		      {latitude:41.381389, longitude:2.18213},
		     ],
		lineColor: "#ff669900",  // lineColor supports the color in the format outlined above
		lineWidth: 1,			 // Pixel width of the line
		antiAlias: true,		 // Smooth or chunky
		fillColor: "#80669900",  // The color to use for fill, supports colors in the format outlined above
		complete: true			 // Should the shape be automatically closed or not - allows defining of lines
	},
    layarType: Titanium.Map.MAP_LAYAR_TYPE_POLYGON,  // This is a polygon
    layarName: "poly"								 // Give it a friendly name
})

var polyTwo = Titanium.Map.createAnnotation({
    latitude:41.380133,
    longitude:2.18962,
    title:"Index",
    subtitle:'IndexVentures - Sector 32',
    animate:false,
    leftButton: 'app://buttons/medium-green-round-building.png',
    myid:10, 
    points: {
		data:[
		      {longitude:2.18962,latitude:41.380133},
		      {longitude:2.18905,latitude:41.381743},
		      {longitude:2.19134,latitude:41.382267},
		      {longitude:2.19198,latitude:41.380624},
		     ],
		lineColor: "#ff669900",
		lineWidth: 1,
		antiAlias: true,
		fillColor: "#80669900",
		complete: true
	},
    layarType: Titanium.Map.MAP_LAYAR_TYPE_POLYGON,
    layarName: "poly"
});

//
// CREATE MAP VIEW
//
var mapview = Titanium.Map.createView({
	mapType: Titanium.Map.STANDARD_TYPE,
	region:{latitude:41.381574, longitude:2.18536, latitudeDelta:0.5, longitudeDelta:0.5},
	animate:true,
	regionFit:true,
	userLocation:true,
	annotations:[poiOne, polyOne, polyTwo]
});

win.add(mapview);
