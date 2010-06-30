package ti.modules.titanium.paypal;


import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiModule;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.paypal.PaypalModule;
import com.paypal.android.MEP.*;

import android.R;
import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.StateSet;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class PaypalButton extends TiUIView 
	implements OnClickListener, TiActivityResultHandler
{
	// Debug helpers
	private static final String LCAT = "PaypalButton";
	private static final boolean DBG = TiConfig.LOGD;
	
	// Events
	public static final String EVENT_PAYMENT_COMPLETE = "paymentSuccess";
	public static final String EVENT_PAYMENT_ERROR = "paymentError";
	public static final String EVENT_PAYMENT_CANCELLED = "paymentCancelled";

	// Module vars
	private PaypalModule ppModule;	
	private PayPal ppObj;

	public PaypalButton(TiViewProxy proxy) {
		super(proxy);
		
		Log.i(LCAT, "Construct");
		
		// Lets grab those dynamic properties
		TiDict props = proxy.getDynamicProperties();
		
		// A little bloat for the props - but better to have some proper defaults that are retrievable in the JS
		if (!props.containsKey("paypalEnvironment")) {
			props.put("paypalEnvironment", PayPal.ENV_SANDBOX);
		}
		
		if (ppObj == null) {
			ppObj = PayPal.initWithAppID(
					proxy.getContext(), 
					props.optString("appId", "APP-80W284485P519543T"), 
					props.getInt("paypalEnvironment"));
			//ppObj = PayPal.initWithAppID(proxy.getContext(), "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
		}
		
		// Check for the mandatory properties early - we want to fail upfront rather than creating the button
		if (hasValidDefaults(props)) {
			CheckoutButton btn = buildButton();
			
			if (props.containsKey("feePaidByReceiver") && props.getBoolean("feePaidByReceiver")==true) {
				
				if (props.getInt("transactionType") == PayPal.PAYMENT_TYPE_PERSONAL) {
					ppObj.feePaidByReceiver();					
				} else {
					Log.w(LCAT, "Ignored parameter feePaidByReceiver - only valid for Personal Payments");
				}				
			}
			
			// Have a bad feeling that this is too tightly coupled
			// TODO Check with Don/KW
			setNativeView(btn);
		}
	}

	
	private CheckoutButton buildButton() {
		
		TiViewProxy proxy = this.getProxy();
		TiDict props = proxy.getDynamicProperties();
		
		if (!props.containsKey("buttonStyle")) {
			props.put("buttonStyle", PayPal.BUTTON_68x24);
		}
		
		if (!props.containsKey("transactionType")) {
			props.put("transactionType", PayPal.PAYMENT_TYPE_DONATION);
		}
		
		CheckoutButton btn = (CheckoutButton) ppObj.getPaymentButton(
				props.getInt("buttonStyle"), 
				proxy.getContext(),
				props.getInt("transactionType"));
		
		btn.setOnClickListener(this);
		btn.setPadding(0,0,0,0);
		
		return btn;		
	}
	
	private Boolean hasValidDefaults(TiDict d) {
		
		Boolean isValid = true;
		
		Log.i(LCAT,d.toString());
		
		if (!d.containsKey("payment")) {
			// Fail hard
			throw new IllegalArgumentException("You can't create a PayPal payment button without payment details.");
		}
		
		TiDict props = d.getTiDict("payment");
		
		if (!props.containsKey("amount")) {
			isValid = false;
			Log.e(LCAT, "PayPal payment button MUST contain an amount");
		}
		
		if (!props.containsKey("currency")) {
			isValid = false;
			Log.e(LCAT, "PayPal payment button MUST contain a currency");
		}
		
		if (!props.containsKey("recipient")) {
			isValid = false;
			Log.e(LCAT, "PayPal payment button MUST contain a recipient");
		}
		
		if (!isValid) {
			throw new IllegalArgumentException("The PayPal button must contain 'amount', 'currency' and 'recipient.");
		}
		
		// Technically not needed as an exception will occur if it's not :)
		return isValid;
	}
	
	@Override
	public void onClick(View arg0) {
		
		Log.d(LCAT, "Paypal button clicked");
		
		TiViewProxy proxy = this.getProxy();
		TiDict buttonProperties = proxy.getDynamicProperties();
		TiDict paymentProperties = buttonProperties.getTiDict("payment");
				
		PayPalPayment newPayment = new PayPalPayment();
		
		// Start with the mandatory props
		newPayment.setAmount((float) TiConvert.toFloat(paymentProperties, "amount"));
		newPayment.setCurrency(paymentProperties.getString("currency"));
		newPayment.setRecipient(paymentProperties.getString("recipient"));
		
		if (paymentProperties.containsKey("itemDescription")) {
			newPayment.setItemDescription(paymentProperties.getString("itemDescription"));
		}
		
		if (paymentProperties.containsKey("merchantName")) {
			newPayment.setMerchantName(paymentProperties.getString("merchantName"));
		}
		
		if (paymentProperties.containsKey("senderEmailOrPhone")) {
			newPayment.setSenderEmail(paymentProperties.getString("senderEmailOrPhone"));
		}
		
		if (paymentProperties.containsKey("tax")) {
			newPayment.setTax(TiConvert.toFloat(paymentProperties,"tax"));
		}
		
		if (paymentProperties.containsKey("shipping")) {
			newPayment.setShipping(TiConvert.toFloat(paymentProperties,"shipping"));
		}
		
		ppModule.setActivityResultHandler(this);			
		ppModule.executePayment(newPayment);		
	}
	
	@Override
	public void listenerAdded(String type, int count, TiProxy proxy) {
		// TODO Auto-generated method stub
		super.listenerAdded(type, count, proxy);
	}


	@Override
	public void listenerRemoved(String type, int count, TiProxy proxy) {
		// TODO Auto-generated method stub
		super.listenerRemoved(type, count, proxy);
	}

	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		// Error with the intent
		Log.e(LCAT, "Error connecting to the Paypal Intent ["+e.getMessage()+"]");
	}

	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
		Log.d(LCAT, "Paypal-Button - onResult called");
		
		if (requestCode != 1) {
			return;
		}
		
		TiDict eventData = new TiDict();
		String eventType = "";
		
		switch(resultCode) {
		case Activity.RESULT_OK:
			String transactionID = data.getStringExtra(PayPalActivity.EXTRA_TRANSACTION_ID);
			Log.d(LCAT, "Paypal Result ok ["+transactionID+"]");
			
			eventType = EVENT_PAYMENT_COMPLETE;
			eventData.put("type", EVENT_PAYMENT_COMPLETE);
			eventData.put("transactionID", transactionID);
			
		break;
		case Activity.RESULT_CANCELED:
			Log.d(LCAT, "Paypal Cancelled");
			eventType = EVENT_PAYMENT_CANCELLED;
			eventData.put("type", EVENT_PAYMENT_CANCELLED);
		break;
		case PayPalActivity.RESULT_FAILURE:
			String errorID = data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
			String errorMessage = data.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
			Log.d(LCAT, "Paypal Failed: ["+errorID+"] with "+errorMessage);
			eventType = EVENT_PAYMENT_ERROR;
			eventData.put("type", EVENT_PAYMENT_ERROR);
			eventData.put("errorCode", errorID);
			eventData.put("errorMessage", errorMessage);
		break;
		default:
			Log.d(LCAT, "Other Result code ["+resultCode+"]");
		break;
		}
		
		this.proxy.fireEvent(eventType, eventData);
	}	
	
	@Override
	public void processProperties(TiDict d) {
		super.processProperties(d);
		
		ppModule = (PaypalModule) TiModule.getModule("Paypal");
		if (ppModule == null) {
			ppModule = new PaypalModule(getProxy().getTiContext());
		}
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, TiProxy proxy) {
		super.propertyChanged(key, oldValue, newValue, proxy);
	}	
	
	
}
