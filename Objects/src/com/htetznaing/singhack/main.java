package com.htetznaing.singhack;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "com.htetznaing.singhack", "com.htetznaing.singhack.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "com.htetznaing.singhack", "com.htetznaing.singhack.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "com.htetznaing.singhack.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        Object[] o;
        if (permissions.length > 0)
            o = new Object[] {permissions[0], grantResults[0] == 0};
        else
            o = new Object[] {"", false};
        processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.Timer _ad1 = null;
public static anywheresoftware.b4a.objects.Timer _ad2 = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imv = null;
public anywheresoftware.b4a.phone.Phone _ph = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b3 = null;
public anywheresoftware.b4a.objects.LabelWrapper _lfoot = null;
public anywheresoftware.b4a.admobwrapper.AdViewWrapper _banner = null;
public anywheresoftware.b4a.admobwrapper.AdViewWrapper.InterstitialAdWrapper _interstitial = null;
public anywheresoftware.b4a.objects.LabelWrapper _ab = null;
public static String _os = "";
public com.AB.ABZipUnzip.ABZipUnzip _zip = null;
public de.amberhome.objects.floatingactionbutton.FloatingActionButtonWrapper _fb = null;
public de.amberhome.objects.floatingactionbutton.FloatingActionButtonWrapper _fb1 = null;
public anywheresoftware.b4a.object.XmlLayoutBuilder _res = null;
public b4a.util.BClipboard _copy = null;
public MLfiles.Fileslib.MLfiles _ml = null;
public static String _rs = "";

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
int _height = 0;
anywheresoftware.b4a.objects.drawable.ColorDrawable _b1bg = null;
 //BA.debugLineNum = 38;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 39;BA.debugLine="Activity.LoadLayout(\"Lay1\")";
mostCurrent._activity.LoadLayout("Lay1",mostCurrent.activityBA);
 //BA.debugLineNum = 40;BA.debugLine="fb.Icon = res.GetDrawable(\"ic_add_white_24dp\")";
mostCurrent._fb.setIcon(mostCurrent._res.GetDrawable("ic_add_white_24dp"));
 //BA.debugLineNum = 41;BA.debugLine="fb.HideOffset = 100%y - fb.Top";
mostCurrent._fb.setHideOffset((int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-mostCurrent._fb.getTop()));
 //BA.debugLineNum = 42;BA.debugLine="fb.Hide(False)";
mostCurrent._fb.Hide(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 43;BA.debugLine="fb.Show(True)";
mostCurrent._fb.Show(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 45;BA.debugLine="fb1.Icon = res.GetDrawable(\"about\")";
mostCurrent._fb1.setIcon(mostCurrent._res.GetDrawable("about"));
 //BA.debugLineNum = 46;BA.debugLine="fb1.HideOffset = 100%y - fb.Top";
mostCurrent._fb1.setHideOffset((int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-mostCurrent._fb.getTop()));
 //BA.debugLineNum = 47;BA.debugLine="fb1.Hide(False)";
mostCurrent._fb1.Hide(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 48;BA.debugLine="fb1.Show(True)";
mostCurrent._fb1.Show(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 50;BA.debugLine="Banner.Initialize2(\"Banner\",\"ca-app-pub-417334857";
mostCurrent._banner.Initialize2(mostCurrent.activityBA,"Banner","ca-app-pub-4173348573252986/8554888553",mostCurrent._banner.SIZE_SMART_BANNER);
 //BA.debugLineNum = 51;BA.debugLine="Dim height As Int";
_height = 0;
 //BA.debugLineNum = 52;BA.debugLine="If GetDeviceLayoutValues.ApproximateScreenSize <";
if (anywheresoftware.b4a.keywords.Common.GetDeviceLayoutValues(mostCurrent.activityBA).getApproximateScreenSize()<6) { 
 //BA.debugLineNum = 54;BA.debugLine="If 100%x > 100%y Then height = 32dip Else height";
if (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)>anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)) { 
_height = anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (32));}
else {
_height = anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50));};
 }else {
 //BA.debugLineNum = 57;BA.debugLine="height = 90dip";
_height = anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (90));
 };
 //BA.debugLineNum = 59;BA.debugLine="Activity.AddView(Banner, 0dip, 100%y - height, 10";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._banner.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0)),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-_height),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),_height);
 //BA.debugLineNum = 60;BA.debugLine="Banner.LoadAd";
mostCurrent._banner.LoadAd();
 //BA.debugLineNum = 62;BA.debugLine="Interstitial.Initialize(\"Interstitial\",\"ca-app-pu";
mostCurrent._interstitial.Initialize(mostCurrent.activityBA,"Interstitial","ca-app-pub-4173348573252986/1031621755");
 //BA.debugLineNum = 63;BA.debugLine="Interstitial.LoadAd";
mostCurrent._interstitial.LoadAd();
 //BA.debugLineNum = 65;BA.debugLine="ad1.Initialize(\"ad1\",100)";
_ad1.Initialize(processBA,"ad1",(long) (100));
 //BA.debugLineNum = 66;BA.debugLine="ad1.Enabled = False";
_ad1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 67;BA.debugLine="ad2.Initialize(\"ad2\",15000)";
_ad2.Initialize(processBA,"ad2",(long) (15000));
 //BA.debugLineNum = 68;BA.debugLine="ad2.Enabled = True";
_ad2.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 70;BA.debugLine="Select ph.SdkVersion";
switch (BA.switchObjectToInt(mostCurrent._ph.getSdkVersion(),(int) (2),(int) (3),(int) (4),(int) (5),(int) (6),(int) (7),(int) (8),(int) (9),(int) (10),(int) (11),(int) (12),(int) (13),(int) (14),(int) (15),(int) (16),(int) (17),(int) (18),(int) (19),(int) (21),(int) (22),(int) (23),(int) (24),(int) (25))) {
case 0: {
 //BA.debugLineNum = 71;BA.debugLine="Case 2 : OS = \"1.1\"";
mostCurrent._os = "1.1";
 break; }
case 1: {
 //BA.debugLineNum = 72;BA.debugLine="Case 3 : OS = \"1.5\"";
mostCurrent._os = "1.5";
 break; }
case 2: {
 //BA.debugLineNum = 73;BA.debugLine="Case 4 : OS = \"1.6\"";
mostCurrent._os = "1.6";
 break; }
case 3: {
 //BA.debugLineNum = 74;BA.debugLine="Case 5 : OS = \"2.0\"";
mostCurrent._os = "2.0";
 break; }
case 4: {
 //BA.debugLineNum = 75;BA.debugLine="Case 6 : OS = \"2.0.1\"";
mostCurrent._os = "2.0.1";
 break; }
case 5: {
 //BA.debugLineNum = 76;BA.debugLine="Case 7 : OS = \"2.1\"";
mostCurrent._os = "2.1";
 break; }
case 6: {
 //BA.debugLineNum = 77;BA.debugLine="Case 8 : OS = \"2.2\"";
mostCurrent._os = "2.2";
 break; }
case 7: {
 //BA.debugLineNum = 78;BA.debugLine="Case 9 : OS = \"2.3 - 2.3.2\"";
mostCurrent._os = "2.3 - 2.3.2";
 break; }
case 8: {
 //BA.debugLineNum = 79;BA.debugLine="Case 10 : OS = \"	2.3.3 - 2.3.7\" ' 2.3.3 or 2.3.";
mostCurrent._os = "	2.3.3 - 2.3.7";
 break; }
case 9: {
 //BA.debugLineNum = 80;BA.debugLine="Case 11 : OS = \"3.0\"";
mostCurrent._os = "3.0";
 break; }
case 10: {
 //BA.debugLineNum = 81;BA.debugLine="Case 12 : OS = \"3.1\"";
mostCurrent._os = "3.1";
 break; }
case 11: {
 //BA.debugLineNum = 82;BA.debugLine="Case 13 : OS = \"3.2\"";
mostCurrent._os = "3.2";
 break; }
case 12: {
 //BA.debugLineNum = 83;BA.debugLine="Case 14 : OS = \"	4.0.1 - 4.0.2\"";
mostCurrent._os = "	4.0.1 - 4.0.2";
 break; }
case 13: {
 //BA.debugLineNum = 84;BA.debugLine="Case 15 : OS = \"4.0.3 - 4.0.4\"";
mostCurrent._os = "4.0.3 - 4.0.4";
 break; }
case 14: {
 //BA.debugLineNum = 85;BA.debugLine="Case 16 : OS = \"	4.1.x\"";
mostCurrent._os = "	4.1.x";
 break; }
case 15: {
 //BA.debugLineNum = 86;BA.debugLine="Case 17 : OS = \"	4.2.x\"";
mostCurrent._os = "	4.2.x";
 break; }
case 16: {
 //BA.debugLineNum = 87;BA.debugLine="Case 18 : OS = 	\"4.3.x\"";
mostCurrent._os = "4.3.x";
 break; }
case 17: {
 //BA.debugLineNum = 88;BA.debugLine="Case 19 : OS = \"	4.4 - 4.4.4\"";
mostCurrent._os = "	4.4 - 4.4.4";
 break; }
case 18: {
 //BA.debugLineNum = 89;BA.debugLine="Case 21: OS = \"5.0\"";
mostCurrent._os = "5.0";
 break; }
case 19: {
 //BA.debugLineNum = 90;BA.debugLine="Case 22: OS = \"5.1\"";
mostCurrent._os = "5.1";
 break; }
case 20: {
 //BA.debugLineNum = 91;BA.debugLine="Case 23: OS = \"6.0\"";
mostCurrent._os = "6.0";
 break; }
case 21: {
 //BA.debugLineNum = 92;BA.debugLine="Case 24 : OS = \"	7.0\"";
mostCurrent._os = "	7.0";
 break; }
case 22: {
 //BA.debugLineNum = 93;BA.debugLine="Case 25 : OS = \"	7.1\"";
mostCurrent._os = "	7.1";
 break; }
default: {
 //BA.debugLineNum = 94;BA.debugLine="Case Else : OS = \"?\"";
mostCurrent._os = "?";
 break; }
}
;
 //BA.debugLineNum = 97;BA.debugLine="Activity.Color = Colors.White";
mostCurrent._activity.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 98;BA.debugLine="ph.SetScreenOrientation(1)";
mostCurrent._ph.SetScreenOrientation(processBA,(int) (1));
 //BA.debugLineNum = 100;BA.debugLine="imv.Initialize(\"imv\")";
mostCurrent._imv.Initialize(mostCurrent.activityBA,"imv");
 //BA.debugLineNum = 101;BA.debugLine="imv.Bitmap = LoadBitmap(File.DirAssets,\"icon.png\")";
mostCurrent._imv.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"icon.png").getObject()));
 //BA.debugLineNum = 102;BA.debugLine="imv.Gravity = Gravity.FILL";
mostCurrent._imv.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.FILL);
 //BA.debugLineNum = 103;BA.debugLine="Activity.AddView(imv,50%x - 50dip  , 10dip ,  110d";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imv.getObject()),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50))),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (110)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (110)));
 //BA.debugLineNum = 105;BA.debugLine="b1.Initialize(\"b1\")";
mostCurrent._b1.Initialize(mostCurrent.activityBA,"b1");
 //BA.debugLineNum = 106;BA.debugLine="Dim b1bg As ColorDrawable";
_b1bg = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 107;BA.debugLine="b1bg.Initialize(Colors.Black,10)";
_b1bg.Initialize(anywheresoftware.b4a.keywords.Common.Colors.Black,(int) (10));
 //BA.debugLineNum = 108;BA.debugLine="b1.Text = \"Install Smule\"";
mostCurrent._b1.setText((Object)("Install Smule"));
 //BA.debugLineNum = 109;BA.debugLine="b1.Background = b1bg";
mostCurrent._b1.setBackground((android.graphics.drawable.Drawable)(_b1bg.getObject()));
 //BA.debugLineNum = 110;BA.debugLine="Activity.AddView(b1,20%x,(imv.Top+imv.Height)+3%y,";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._b1.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (20),mostCurrent.activityBA),(int) ((mostCurrent._imv.getTop()+mostCurrent._imv.getHeight())+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (3),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 112;BA.debugLine="b2.Initialize(\"b2\")";
mostCurrent._b2.Initialize(mostCurrent.activityBA,"b2");
 //BA.debugLineNum = 113;BA.debugLine="b2.Text = \"Install Smule Patcher\"";
mostCurrent._b2.setText((Object)("Install Smule Patcher"));
 //BA.debugLineNum = 114;BA.debugLine="b2.Background = b1bg";
mostCurrent._b2.setBackground((android.graphics.drawable.Drawable)(_b1bg.getObject()));
 //BA.debugLineNum = 115;BA.debugLine="Activity.AddView(b2,20%x,(b1.Top+b1.Height)+1%y,60";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._b2.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (20),mostCurrent.activityBA),(int) ((mostCurrent._b1.getTop()+mostCurrent._b1.getHeight())+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (1),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 117;BA.debugLine="b3.Initialize(\"b3\")";
mostCurrent._b3.Initialize(mostCurrent.activityBA,"b3");
 //BA.debugLineNum = 118;BA.debugLine="b3.Text = \"Tutorial\"";
mostCurrent._b3.setText((Object)("Tutorial"));
 //BA.debugLineNum = 119;BA.debugLine="b3.Background = b1bg";
mostCurrent._b3.setBackground((android.graphics.drawable.Drawable)(_b1bg.getObject()));
 //BA.debugLineNum = 120;BA.debugLine="Activity.AddView(b3,20%x,(b2.Top+b2.Height)+1%y,60";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._b3.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (20),mostCurrent.activityBA),(int) ((mostCurrent._b2.getTop()+mostCurrent._b2.getHeight())+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (1),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 122;BA.debugLine="ml.GetRoot";
mostCurrent._ml.GetRoot();
 //BA.debugLineNum = 123;BA.debugLine="If ml.HaveRoot Then";
if (mostCurrent._ml.HaveRoot) { 
 //BA.debugLineNum = 124;BA.debugLine="rs = \"Rooted\"";
mostCurrent._rs = "Rooted";
 }else {
 //BA.debugLineNum = 126;BA.debugLine="rs = \"No Root\"";
mostCurrent._rs = "No Root";
 };
 //BA.debugLineNum = 128;BA.debugLine="ab.Initialize(\"\")";
mostCurrent._ab.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 129;BA.debugLine="ab.Text =\"Brand Name : \" & ph.Manufacturer & CRLF";
mostCurrent._ab.setText((Object)("Brand Name : "+mostCurrent._ph.getManufacturer()+anywheresoftware.b4a.keywords.Common.CRLF+"Device Name : "+mostCurrent._ph.getModel()+anywheresoftware.b4a.keywords.Common.CRLF+"Android Version : "+mostCurrent._os+anywheresoftware.b4a.keywords.Common.CRLF+"Produck Name : "+mostCurrent._ph.getProduct()+anywheresoftware.b4a.keywords.Common.CRLF+"Root Status : "+mostCurrent._rs));
 //BA.debugLineNum = 130;BA.debugLine="ab.TextColor = Colors.RGB(0,121,107)";
mostCurrent._ab.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (121),(int) (107)));
 //BA.debugLineNum = 131;BA.debugLine="ab.Gravity = Gravity.CENTER";
mostCurrent._ab.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 132;BA.debugLine="Activity.AddView(ab,0%x,(b3.Top+b3.Height)+1%y,100";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._ab.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),(int) ((mostCurrent._b3.getTop()+mostCurrent._b3.getHeight())+anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (1),mostCurrent.activityBA)),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (30),mostCurrent.activityBA));
 //BA.debugLineNum = 134;BA.debugLine="lfoot.Initialize(\"ft\")";
mostCurrent._lfoot.Initialize(mostCurrent.activityBA,"ft");
 //BA.debugLineNum = 135;BA.debugLine="lfoot.Text = \"Powered By Myanmar Android Apps\"";
mostCurrent._lfoot.setText((Object)("Powered By Myanmar Android Apps"));
 //BA.debugLineNum = 136;BA.debugLine="lfoot.TextColor = Colors.Magenta";
mostCurrent._lfoot.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Magenta);
 //BA.debugLineNum = 137;BA.debugLine="lfoot.Gravity = Gravity.CENTER";
mostCurrent._lfoot.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 138;BA.debugLine="Activity.AddView(lfoot,0%x,100%y - 80dip,100%x,5%y";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lfoot.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (80))),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA));
 //BA.debugLineNum = 139;BA.debugLine="End Sub";
return "";
}
public static boolean  _activity_keypress(int _keycode) throws Exception{
int _answ = 0;
anywheresoftware.b4a.objects.IntentWrapper _facebook = null;
anywheresoftware.b4a.objects.IntentWrapper _i = null;
 //BA.debugLineNum = 245;BA.debugLine="Sub Activity_KeyPress (KeyCode As Int) As Boolean";
 //BA.debugLineNum = 246;BA.debugLine="Dim Answ As Int";
_answ = 0;
 //BA.debugLineNum = 247;BA.debugLine="If KeyCode = KeyCodes.KEYCODE_BACK Then";
if (_keycode==anywheresoftware.b4a.keywords.Common.KeyCodes.KEYCODE_BACK) { 
 //BA.debugLineNum = 248;BA.debugLine="Answ = Msgbox2(\"If you want to get new updates";
_answ = anywheresoftware.b4a.keywords.Common.Msgbox2("If you want to get new updates on  Facebook? Please Like "+anywheresoftware.b4a.keywords.Common.CRLF+"Myanmar Android Apps Page!","Attention!","Yes","","No",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"fb.png").getObject()),mostCurrent.activityBA);
 //BA.debugLineNum = 249;BA.debugLine="If Answ = DialogResponse.NEGATIVE Then";
if (_answ==anywheresoftware.b4a.keywords.Common.DialogResponse.NEGATIVE) { 
 //BA.debugLineNum = 250;BA.debugLine="ad1.Enabled = True";
_ad1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 251;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 };
 };
 //BA.debugLineNum = 254;BA.debugLine="If Answ = DialogResponse.POSITIVE Then";
if (_answ==anywheresoftware.b4a.keywords.Common.DialogResponse.POSITIVE) { 
 //BA.debugLineNum = 255;BA.debugLine="ad1.Enabled = True";
_ad1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 256;BA.debugLine="Try";
try { //BA.debugLineNum = 258;BA.debugLine="Dim Facebook As Intent";
_facebook = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 260;BA.debugLine="Facebook.Initialize(Facebook.ACTION_VIEW, \"fb";
_facebook.Initialize(_facebook.ACTION_VIEW,"fb://page/627699334104477");
 //BA.debugLineNum = 261;BA.debugLine="StartActivity(Facebook)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_facebook.getObject()));
 } 
       catch (Exception e16) {
			processBA.setLastException(e16); //BA.debugLineNum = 265;BA.debugLine="Dim i As Intent";
_i = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 266;BA.debugLine="i.Initialize(i.ACTION_VIEW, \"https://m.facebo";
_i.Initialize(_i.ACTION_VIEW,"https://m.facebook.com/MmFreeAndroidApps");
 //BA.debugLineNum = 268;BA.debugLine="StartActivity(i)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_i.getObject()));
 };
 //BA.debugLineNum = 271;BA.debugLine="Return False";
if (true) return anywheresoftware.b4a.keywords.Common.False;
 };
 //BA.debugLineNum = 273;BA.debugLine="End Sub";
return false;
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 241;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 243;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 228;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 229;BA.debugLine="ml.rmrf(File.DirRootExternal & \"/MyanmarAndroidAp";
mostCurrent._ml.rmrf(anywheresoftware.b4a.keywords.Common.File.getDirRootExternal()+"/MyanmarAndroidApps");
 //BA.debugLineNum = 230;BA.debugLine="End Sub";
return "";
}
public static String  _ad1_tick() throws Exception{
 //BA.debugLineNum = 232;BA.debugLine="Sub ad1_Tick";
 //BA.debugLineNum = 233;BA.debugLine="If Interstitial.Ready Then Interstitial.Show Else";
if (mostCurrent._interstitial.getReady()) { 
mostCurrent._interstitial.Show();}
else {
mostCurrent._interstitial.LoadAd();};
 //BA.debugLineNum = 234;BA.debugLine="ad1.Enabled = False";
_ad1.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 235;BA.debugLine="End Sub";
return "";
}
public static String  _ad2_tick() throws Exception{
 //BA.debugLineNum = 237;BA.debugLine="Sub ad2_Tick";
 //BA.debugLineNum = 238;BA.debugLine="If Interstitial.Ready Then Interstitial.Show Else";
if (mostCurrent._interstitial.getReady()) { 
mostCurrent._interstitial.Show();}
else {
mostCurrent._interstitial.LoadAd();};
 //BA.debugLineNum = 239;BA.debugLine="End Sub";
return "";
}
public static String  _b1_click() throws Exception{
String _sd = "";
anywheresoftware.b4a.objects.IntentWrapper _i = null;
 //BA.debugLineNum = 199;BA.debugLine="Sub b1_Click";
 //BA.debugLineNum = 200;BA.debugLine="ad1.Enabled = True";
_ad1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 201;BA.debugLine="File.Copy(File.DirAssets,\"My.zip\", File.DirRootEx";
anywheresoftware.b4a.keywords.Common.File.Copy(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"My.zip",anywheresoftware.b4a.keywords.Common.File.getDirRootExternal(),"My.zip");
 //BA.debugLineNum = 202;BA.debugLine="Dim sd As String = File.DirRootExternal & \"/\"";
_sd = anywheresoftware.b4a.keywords.Common.File.getDirRootExternal()+"/";
 //BA.debugLineNum = 203;BA.debugLine="zip.ABUnzip(sd & \"My.zip\",sd & \"MyanmarAndroidApp";
mostCurrent._zip.ABUnzip(_sd+"My.zip",_sd+"MyanmarAndroidApps");
 //BA.debugLineNum = 204;BA.debugLine="File.Delete(File.DirRootExternal , \"My.zip\")";
anywheresoftware.b4a.keywords.Common.File.Delete(anywheresoftware.b4a.keywords.Common.File.getDirRootExternal(),"My.zip");
 //BA.debugLineNum = 205;BA.debugLine="Dim i As Intent";
_i = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 206;BA.debugLine="i.Initialize(i.ACTION_VIEW,\"file:///\"&File.DirRoo";
_i.Initialize(_i.ACTION_VIEW,"file:///"+anywheresoftware.b4a.keywords.Common.File.getDirRootExternal()+"/MyanmarAndroidApps/My.apk");
 //BA.debugLineNum = 207;BA.debugLine="i.SetType(\"application/vnd.android.package-archiv";
_i.SetType("application/vnd.android.package-archive");
 //BA.debugLineNum = 208;BA.debugLine="StartActivity(i)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_i.getObject()));
 //BA.debugLineNum = 209;BA.debugLine="End Sub";
return "";
}
public static String  _b2_click() throws Exception{
String _sd = "";
anywheresoftware.b4a.objects.IntentWrapper _i = null;
 //BA.debugLineNum = 211;BA.debugLine="Sub b2_Click";
 //BA.debugLineNum = 212;BA.debugLine="ad1.Enabled = True";
_ad1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 213;BA.debugLine="File.Copy(File.DirAssets,\"LP.zip\", File.DirRootEx";
anywheresoftware.b4a.keywords.Common.File.Copy(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"LP.zip",anywheresoftware.b4a.keywords.Common.File.getDirRootExternal(),"LP.zip");
 //BA.debugLineNum = 214;BA.debugLine="Dim sd As String = File.DirRootExternal & \"/\"";
_sd = anywheresoftware.b4a.keywords.Common.File.getDirRootExternal()+"/";
 //BA.debugLineNum = 215;BA.debugLine="zip.ABUnzip(sd & \"LP.zip\",sd & \"MyanmarAndroidApp";
mostCurrent._zip.ABUnzip(_sd+"LP.zip",_sd+"MyanmarAndroidApps");
 //BA.debugLineNum = 216;BA.debugLine="File.Delete(File.DirRootExternal , \"LP.zip\")";
anywheresoftware.b4a.keywords.Common.File.Delete(anywheresoftware.b4a.keywords.Common.File.getDirRootExternal(),"LP.zip");
 //BA.debugLineNum = 217;BA.debugLine="Dim i As Intent";
_i = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 218;BA.debugLine="i.Initialize(i.ACTION_VIEW,\"file:///\"&File.DirRoo";
_i.Initialize(_i.ACTION_VIEW,"file:///"+anywheresoftware.b4a.keywords.Common.File.getDirRootExternal()+"/MyanmarAndroidApps/LP.apk");
 //BA.debugLineNum = 219;BA.debugLine="i.SetType(\"application/vnd.android.package-archiv";
_i.SetType("application/vnd.android.package-archive");
 //BA.debugLineNum = 220;BA.debugLine="StartActivity(i)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_i.getObject()));
 //BA.debugLineNum = 221;BA.debugLine="End Sub";
return "";
}
public static String  _b3_click() throws Exception{
anywheresoftware.b4a.phone.Phone.PhoneIntents _pi = null;
 //BA.debugLineNum = 223;BA.debugLine="Sub b3_Click";
 //BA.debugLineNum = 224;BA.debugLine="Dim pi As PhoneIntents";
_pi = new anywheresoftware.b4a.phone.Phone.PhoneIntents();
 //BA.debugLineNum = 225;BA.debugLine="StartActivity(pi.OpenBrowser(\"http://www.htetznai";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_pi.OpenBrowser("http://www.htetznaing.com/smule_4-1-1_full/")));
 //BA.debugLineNum = 226;BA.debugLine="End Sub";
return "";
}
public static String  _fb_click() throws Exception{
anywheresoftware.b4a.objects.IntentWrapper _facebook = null;
anywheresoftware.b4a.objects.IntentWrapper _i = null;
 //BA.debugLineNum = 141;BA.debugLine="Sub fb_Click";
 //BA.debugLineNum = 142;BA.debugLine="ToastMessageShow(\"App Update နွင့္ App အသစ္ေတြ \"";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("App Update နွင့္ App အသစ္ေတြ "+anywheresoftware.b4a.keywords.Common.CRLF+"Facebook ကေနဖတ္ရူ့သိနိုင္ဖို့"+anywheresoftware.b4a.keywords.Common.CRLF+"Myanmar Android Apps ကို Like ထားလိုက္ပါ။",anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 143;BA.debugLine="Try";
try { //BA.debugLineNum = 145;BA.debugLine="Dim Facebook As Intent";
_facebook = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 147;BA.debugLine="Facebook.Initialize(Facebook.ACTION_VIEW, \"fb";
_facebook.Initialize(_facebook.ACTION_VIEW,"fb://page/627699334104477");
 //BA.debugLineNum = 148;BA.debugLine="StartActivity(Facebook)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_facebook.getObject()));
 } 
       catch (Exception e7) {
			processBA.setLastException(e7); //BA.debugLineNum = 152;BA.debugLine="Dim i As Intent";
_i = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 153;BA.debugLine="i.Initialize(i.ACTION_VIEW, \"https://m.facebo";
_i.Initialize(_i.ACTION_VIEW,"https://m.facebook.com/MmFreeAndroidApps");
 //BA.debugLineNum = 155;BA.debugLine="StartActivity(i)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_i.getObject()));
 };
 //BA.debugLineNum = 158;BA.debugLine="End Sub";
return "";
}
public static String  _fb1_click() throws Exception{
anywheresoftware.b4a.objects.IntentWrapper _shareit = null;
 //BA.debugLineNum = 160;BA.debugLine="Sub fb1_Click";
 //BA.debugLineNum = 161;BA.debugLine="Dim ShareIt As Intent";
_shareit = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 162;BA.debugLine="copy.clrText";
mostCurrent._copy.clrText(mostCurrent.activityBA);
 //BA.debugLineNum = 163;BA.debugLine="copy.setText(\"#SmuleVipUnlocked For All Android De";
mostCurrent._copy.setText(mostCurrent.activityBA,"#SmuleVipUnlocked For All Android Deveices!"+anywheresoftware.b4a.keywords.Common.CRLF+"Download Free at : www.htetznaing.com"+anywheresoftware.b4a.keywords.Common.CRLF+" #Ht3tzN4ing"+anywheresoftware.b4a.keywords.Common.CRLF+" #MyanmarAndroidApps");
 //BA.debugLineNum = 164;BA.debugLine="ShareIt.Initialize (ShareIt.ACTION_SEND,\"\")";
_shareit.Initialize(_shareit.ACTION_SEND,"");
 //BA.debugLineNum = 165;BA.debugLine="ShareIt.SetType (\"text/plain\")";
_shareit.SetType("text/plain");
 //BA.debugLineNum = 166;BA.debugLine="ShareIt.PutExtra (\"android.intent.extra.TEXT\",";
_shareit.PutExtra("android.intent.extra.TEXT",(Object)(mostCurrent._copy.getText(mostCurrent.activityBA)));
 //BA.debugLineNum = 167;BA.debugLine="ShareIt.PutExtra (\"android.intent.extra.SUBJEC";
_shareit.PutExtra("android.intent.extra.SUBJECT",(Object)("Get Free!!"));
 //BA.debugLineNum = 168;BA.debugLine="ShareIt.WrapAsIntentChooser(\"Share App Via...\"";
_shareit.WrapAsIntentChooser("Share App Via...");
 //BA.debugLineNum = 169;BA.debugLine="StartActivity (ShareIt)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_shareit.getObject()));
 //BA.debugLineNum = 170;BA.debugLine="End Sub";
return "";
}
public static String  _ft_click() throws Exception{
anywheresoftware.b4a.objects.IntentWrapper _facebook = null;
anywheresoftware.b4a.objects.IntentWrapper _i = null;
 //BA.debugLineNum = 177;BA.debugLine="Sub ft_Click";
 //BA.debugLineNum = 178;BA.debugLine="Try";
try { //BA.debugLineNum = 180;BA.debugLine="Dim Facebook As Intent";
_facebook = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 182;BA.debugLine="Facebook.Initialize(Facebook.ACTION_VIEW, \"fb";
_facebook.Initialize(_facebook.ACTION_VIEW,"fb://page/627699334104477");
 //BA.debugLineNum = 183;BA.debugLine="StartActivity(Facebook)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_facebook.getObject()));
 } 
       catch (Exception e6) {
			processBA.setLastException(e6); //BA.debugLineNum = 187;BA.debugLine="Dim i As Intent";
_i = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 188;BA.debugLine="i.Initialize(i.ACTION_VIEW, \"https://m.facebo";
_i.Initialize(_i.ACTION_VIEW,"https://m.facebook.com/MmFreeAndroidApps");
 //BA.debugLineNum = 190;BA.debugLine="StartActivity(i)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_i.getObject()));
 };
 //BA.debugLineNum = 193;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 21;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 22;BA.debugLine="Dim imv As ImageView";
mostCurrent._imv = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 23;BA.debugLine="Dim ph As Phone";
mostCurrent._ph = new anywheresoftware.b4a.phone.Phone();
 //BA.debugLineNum = 24;BA.debugLine="Dim b1,b2,b3 As Button";
mostCurrent._b1 = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._b2 = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._b3 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Dim lfoot As Label";
mostCurrent._lfoot = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Dim Banner As AdView";
mostCurrent._banner = new anywheresoftware.b4a.admobwrapper.AdViewWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Dim Interstitial As InterstitialAd";
mostCurrent._interstitial = new anywheresoftware.b4a.admobwrapper.AdViewWrapper.InterstitialAdWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Dim ab As Label";
mostCurrent._ab = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Dim OS As String";
mostCurrent._os = "";
 //BA.debugLineNum = 30;BA.debugLine="Dim zip As ABZipUnzip";
mostCurrent._zip = new com.AB.ABZipUnzip.ABZipUnzip();
 //BA.debugLineNum = 31;BA.debugLine="Dim fb ,fb1 As FloatingActionButton";
mostCurrent._fb = new de.amberhome.objects.floatingactionbutton.FloatingActionButtonWrapper();
mostCurrent._fb1 = new de.amberhome.objects.floatingactionbutton.FloatingActionButtonWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Dim res As XmlLayoutBuilder";
mostCurrent._res = new anywheresoftware.b4a.object.XmlLayoutBuilder();
 //BA.debugLineNum = 33;BA.debugLine="Dim copy As BClipboard";
mostCurrent._copy = new b4a.util.BClipboard();
 //BA.debugLineNum = 34;BA.debugLine="Dim ml As MLfiles";
mostCurrent._ml = new MLfiles.Fileslib.MLfiles();
 //BA.debugLineNum = 35;BA.debugLine="Dim rs As String";
mostCurrent._rs = "";
 //BA.debugLineNum = 36;BA.debugLine="End Sub";
return "";
}
public static String  _imv_click() throws Exception{
 //BA.debugLineNum = 195;BA.debugLine="Sub imv_Click";
 //BA.debugLineNum = 196;BA.debugLine="ad1.Enabled = True";
_ad1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 197;BA.debugLine="End Sub";
return "";
}
public static String  _l_click() throws Exception{
anywheresoftware.b4a.phone.Phone.PhoneIntents _ngoma = null;
 //BA.debugLineNum = 172;BA.debugLine="Sub l_Click";
 //BA.debugLineNum = 173;BA.debugLine="Dim ngoma As PhoneIntents";
_ngoma = new anywheresoftware.b4a.phone.Phone.PhoneIntents();
 //BA.debugLineNum = 174;BA.debugLine="StartActivity(ngoma.OpenBrowser(\"http://root.360.";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(_ngoma.OpenBrowser("http://root.360.cn")));
 //BA.debugLineNum = 175;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 17;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim ad1,ad2 As Timer";
_ad1 = new anywheresoftware.b4a.objects.Timer();
_ad2 = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 19;BA.debugLine="End Sub";
return "";
}
}
