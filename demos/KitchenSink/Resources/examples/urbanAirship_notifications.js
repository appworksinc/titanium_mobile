var win = Titanium.UI.currentWindow;

Titanium.UI.setBackgroundColor('#000');

// Lets keep a count of how many messages we have received
var notificationCount = 0;

// Create some labels to show the user
var lStatus = Titanium.UI.createLabel({
	color:'#999',
	text:'Status: Unregistered',
	font:{fontSize:20,fontFamily:'Helvetica Neue'},
	textAlign:'center',
	width:'auto',
	top: 20
});

var lDeviceToken = Titanium.UI.createLabel({
	color:'#999',
	text:'DeviceToken: Unknown',
	font:{fontSize:12,fontFamily:'Helvetica Neue'},
	textAlign:'center',
	width:'auto',
	top: 60
});

var lMessage = Titanium.UI.createLabel({
	color:'#999',
	text:'Waiting for Notification',
	font:{fontSize:20,fontFamily:'Helvetica Neue'},
	textAlign:'center',
	width:'auto',
	top: 160
});

// Add them to the window
win.add(lStatus);
win.add(lDeviceToken);
win.add(lMessage);

// Open the window
win.open();

/**
 * The API is pretty much the same as outlined - http://blog.urbanairship.com/2010/05/26/appcelerator-and-urban-airship/
 * The 2 main differences are:
 * 	Properties:
 * 		appKey & debug
 * 		appKey allows us to pass the appKey issues by UA
 * 		debug allows us to enable/disable debug mode
 * 
 */
// Setup the registration to UrbanAirship
Titanium.UrbanAirship.registerForPushNotifications({
	// We pass in our appKey that's been given to us from the UrbanAirship console
	appKey: "Y2KMAbHURFmcnlrW2x2T6Q",
	// Enable debug mode
	debug: true,
	// Setup some functions to handle UA events
	// You can define these inline (as below) or BEFORE you register for notifications (ie directly before this function)
	success: function(e){
		// Log the output
		Ti.API.log("JS TestHarness","Sucessfully register: "+e.deviceToken);
		// Update our labels
		lStatus.text = "Status: Registered";
		lStatus.color = "#0f0";
		lDeviceToken.text = "DeviceToken: "+e.deviceToken;
	},
	callback: function(e){
		// Log the output
		Ti.API.log("JS TestHarness","Received Push Notification");
		Ti.API.log("JS TestHarness","Push Notification Message: "+e.data.message);
		Ti.API.log("JS TestHarness","Received Push Notification: "+e.data.payload);
		
		notificationCount = notificationCount+1; 
		
		lMessage.text = "Messages Received: "+notificationCount;
		
		// Display the notification as an alert
		var a = Titanium.UI.createAlertDialog({
			title:e.data.message,
			message:e.data.payload
		});
		
		a.show();
	}
});


