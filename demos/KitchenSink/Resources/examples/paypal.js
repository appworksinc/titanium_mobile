var win = Titanium.UI.currentWindow;
win.backgroundColor = '#F00';

/**
 * Environment Constants
 * 		Titanium.Paypal.PAYPAL_ENV_LIVE
 * 		Titanium.Paypal.PAYPAL_ENV_SANDBOX
 * 		Titanium.Paypal.PAYPAL_ENV_NONE
 * 	
 * Transaction Type Constants
 * 		Titanium.Paypal.PAYMENT_TYPE_HARD_GOODS
 * 		Titanium.Paypal.PAYMENT_TYPE_DONATION
 * 		Titanium.Paypal.PAYMENT_TYPE_PERSONAL
 * 		Titanium.Paypal.PAYMENT_TYPE_SERVICE
 * 	
 * Button Style Constants
 * 		Titanium.Paypal.BUTTON_68x24
 * 		Titanium.Paypal.BUTTON_68x33
 * 		Titanium.Paypal.BUTTON_118x24
 * 		Titanium.Paypal.BUTTON_152x33
 * 		Titanium.Paypal.BUTTON_194x37
 * 		Titanium.Paypal.BUTTON_278x43
 * 		Titanium.Paypal.BUTTON_294x43	
 */

/**
 * Create the PayPal button is simple enough and as you pass all the data into the button - you can have multiple buttons in one screen
 */
var ppButton = Titanium.Paypal.createPaypalButton(
{
	// Button Details
	// Note - you can override the button size as defined by Paypal - but it's not a good idea
	height:30,
	width:100,
	top: 10,
	left: 10,
	appId: "APP-80W284485P519543T",							 // The appID issued by Paypal for your application - APP-80W284485P519543T is the default Paypal test ID
	buttonStyle: Titanium.Paypal.BUTTON_68x24,  			 // The style & size of the button
	paypalEnvironment: Titanium.Paypal.PAYPAL_ENV_SANDBOX,   // Sandbox, None or Live
	feePaidByReceiver: false, 								 // This will only be applied when the transaction type is Personal
	transactionType: Titanium.Paypal.PAYMENT_TYPE_DONATION,	 // The type of payment
	// The payment itself
	payment: 
	{
		amount: 12.99,
		tax: 0.00,
		shipping: 0.00,
		currency: "USD",
		recipient: "moneyBags@biz.near.me",
		itemDescription: "Donation",
		merchantName: "Dev Tools",
		senderEmailOrPhone: "joe@spendThrift.com"
	}
});

// Events available
ppButton.addEventListener("paymentCancelled", function(e){
	Titanium.API.log("1","Payment Cancelled");
});

ppButton.addEventListener("paymentSuccess", function(e){
	Titanium.API.log("1","Payment Success.  TransactionID: "+e.transactionID);
});

ppButton.addEventListener("paymentError", function(e){
	Titanium.API.log("1","Payment Error");
	Titanium.API.log("1","errorCode: "+e.errorCode);
	Titanium.API.log("1","errorMessage: "+e.errorMessage);
});

// Add it to the window
win.add(ppButton);
// Open the window
win.open();
