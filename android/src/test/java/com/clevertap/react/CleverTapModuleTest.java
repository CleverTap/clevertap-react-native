package com.clevertap.react;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.clevertap.android.sdk.CTExperimentsListener;
import com.clevertap.android.sdk.CTFeatureFlagsListener;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.InAppNotificationButtonListener;
import com.clevertap.android.sdk.InAppNotificationListener;
import com.clevertap.android.sdk.InboxMessageButtonListener;
import com.clevertap.android.sdk.SyncListener;
import com.clevertap.android.sdk.displayunits.DisplayUnitListener;
import com.clevertap.android.sdk.product_config.CTProductConfigListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.CallbackImpl;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import org.junit.*;
import org.junit.runner.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest({CleverTapAPI.class,Log.class,CallbackImpl.class, Arguments.class})
public class CleverTapModuleTest {

    private static final String TAG = "CleverTapReact";
    public static CleverTapAPI clevertap;
    public static ReactApplicationContext context;

    public static CleverTapModule cleverTapModule;
    String channelId="offers123";
    String channelName="Offers";
    String channelDesc="diwali offers";
    String channelSound="diwali.mp3";
    String groupId="offersGroupDiwali2030";
    String groupName="offersGroupDiwali";

    @BeforeClass
    public static void init() {
        mockStatic(CleverTapAPI.class, Log.class);

        clevertap = PowerMockito.mock(CleverTapAPI.class);
        context = PowerMockito.mock(ReactApplicationContext.class);

        when(CleverTapAPI.getDefaultInstance(context)).thenReturn(clevertap);
        cleverTapModule = new CleverTapModule(context);

    }

    @Before
    public void setUp() {
        mockStatic(CleverTapAPI.class,Log.class);
    }

    @Test
    public void testGetCleverTapAPI() throws Exception {

        Whitebox.invokeMethod(cleverTapModule, "getCleverTapAPI");

        //verify all listeners are set, test will fail if not found any
        verify(clevertap).setInAppNotificationListener(any(InAppNotificationListener.class));
        verify(clevertap).setSyncListener(any(SyncListener.class));
        verify(clevertap).setCTNotificationInboxListener(any(CTInboxListener.class));
        verify(clevertap).setCTExperimentsListener(any(CTExperimentsListener.class));
        verify(clevertap).setInboxMessageButtonListener(any(InboxMessageButtonListener.class));
        verify(clevertap).setInAppNotificationButtonListener(any(InAppNotificationButtonListener.class));
        verify(clevertap).setDisplayUnitListener(any(DisplayUnitListener.class));
        verify(clevertap).setCTProductConfigListener(any(CTProductConfigListener.class));
        verify(clevertap).setCTFeatureFlagsListener(any(CTFeatureFlagsListener.class));
        verify(clevertap).setLibrary(anyString());
    }

    @Test
    public void testSetPushTokenAsStringWhenTypeOrTokenIsNull() {
        String token = "1234";
        String type = "FCM";

        //test case 1 - when token null
        cleverTapModule.setPushTokenAsString(null, type);
        verify(clevertap,times(0)).pushFcmRegistrationId(anyString(),anyBoolean());

        //test case 2 - when type null
        cleverTapModule.setPushTokenAsString(token, null);
        verify(clevertap,times(0)).pushFcmRegistrationId(anyString(),anyBoolean());

        //test case 3 - when type and token null
        cleverTapModule.setPushTokenAsString(null, null);
        verify(clevertap,times(0)).pushFcmRegistrationId(anyString(),anyBoolean());
    }

    @Test
    public void testSetPushTokenAsString() {
        String token = "1234";

        //test case 1 - type FCM
        cleverTapModule.setPushTokenAsString(token, "FCM");
        verify(clevertap).pushFcmRegistrationId(token,true);

        //test case 2 - type XPS
        cleverTapModule.setPushTokenAsString(token, "XPS");
        verify(clevertap).pushXiaomiRegistrationId(token,true);

        //test case 3 - type BPS
        cleverTapModule.setPushTokenAsString(token, "BPS");
        verify(clevertap).pushBaiduRegistrationId(token,true);

        //test case 4 - type HPS
        cleverTapModule.setPushTokenAsString(token, "HPS");
        verify(clevertap).pushHuaweiRegistrationId(token,true);
    }

    @Test
    public void testSetUIEditorConnectionEnabled(){

        cleverTapModule.setUIEditorConnectionEnabled(true);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.setUIEditorConnectionEnabled(true);

        verifyStatic(Log.class);
        Log.i(TAG,"UI Editor connection enabled - true");
    }

    @Test
    public void testCreateNotificationChannelWithSoundWhenParamsNull(){

        //test case 1 - when channelId null
        cleverTapModule.createNotificationChannelWithSound(null,channelName,channelDesc,1,true,channelSound);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean(),anyString());

        //test case 2 - when channelName null
        cleverTapModule.createNotificationChannelWithSound(channelId,null,channelDesc,1,true,channelSound);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean(),anyString());

        //test case 3 - when channelDesc null
        cleverTapModule.createNotificationChannelWithSound(channelId,channelName,null,1,true,channelSound);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean(),anyString());

        //test case 4 - when sound null
        cleverTapModule.createNotificationChannelWithSound(channelId,channelName,channelDesc,1,true,null);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean(),anyString());

    }

    @Test
    public void testCreateNotificationChannelWithSound(){
        cleverTapModule.createNotificationChannelWithSound(channelId,channelName,channelDesc,1,true,channelSound);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean(),anyString());

    }

    @Test
    public void testCreateNotificationChannelWithGroupIdWhenParamsNull(){

        //test case 1 - when channelId null
        cleverTapModule.createNotificationChannelWithGroupId(null,channelName,channelDesc,1,groupId,true);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean());

        //test case 2 - when channelName null
        cleverTapModule.createNotificationChannelWithGroupId(channelId,null,channelDesc,1,groupId,true);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean());

        //test case 3 - when channelDesc null
        cleverTapModule.createNotificationChannelWithGroupId(channelId,channelName,null,1,groupId,true);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean());

        //test case 4 - when groupId null
        cleverTapModule.createNotificationChannelWithGroupId(channelId,channelName,channelDesc,1,null,true);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean());

    }

    @Test
    public void testCreateNotificationChannelWithGroupId(){
        cleverTapModule.createNotificationChannelWithGroupId(channelId,channelName,channelDesc,1,groupId,true);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean());

    }

    @Test
    public void testCreateNotificationChannelWithGroupIdAndSoundWhenParamsNull(){

        //test case 1 - when channelId null
        cleverTapModule.createNotificationChannelWithGroupIdAndSound(null,channelName,channelDesc,1,groupId,true,channelSound);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean(),anyString());

        //test case 2 - when channelName null
        cleverTapModule.createNotificationChannelWithGroupIdAndSound(channelId,null,channelDesc,1,groupId,true,channelSound);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean(),anyString());

        //test case 3 - when channelDesc null
        cleverTapModule.createNotificationChannelWithGroupIdAndSound(channelId,channelName,null,1,groupId,true,channelSound);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean(),anyString());

        //test case 4 - when groupId null
        cleverTapModule.createNotificationChannelWithGroupIdAndSound(channelId,channelName,channelDesc,1,null,true,channelSound);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean(),anyString());

        //test case 5 - when sound null
        cleverTapModule.createNotificationChannelWithGroupIdAndSound(channelId,channelName,channelDesc,1,groupId,true,null);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean(),anyString());

    }

    @Test
    public void testCreateNotificationChannelWithGroupIdAndSound(){
        cleverTapModule.createNotificationChannelWithGroupIdAndSound(channelId,channelName,channelDesc,1,groupId,true,channelSound);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyString(),anyBoolean(),anyString());

    }

    @Test
    public void testCreateNotificationChannelWhenParamsNull(){

        //test case 1 - when channelId null
        cleverTapModule.createNotificationChannel(null,channelName,channelDesc,1,true);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean());

        //test case 2 - when channelName null
        cleverTapModule.createNotificationChannel(channelId,null,channelDesc,1,true);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean());

        //test case 3 - when channelDesc null
        cleverTapModule.createNotificationChannel(channelId,channelName,null,1,true);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean());

    }

    @Test
    public void testCreateNotificationChannel(){
        cleverTapModule.createNotificationChannel(channelId,channelName,channelDesc,1,true);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.createNotificationChannel(any(Context.class),anyString(),anyString(),anyString(),anyInt(),anyBoolean());

    }

    @Test
    public void testCreateNotificationChannelGroupWhenParamsNull(){

        //test case 1 - when groupId null
        cleverTapModule.createNotificationChannelGroup(null,groupName);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannelGroup(any(Context.class),anyString(),anyString());

        //test case 2 - when groupName null
        cleverTapModule.createNotificationChannelGroup(groupId,null);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.createNotificationChannelGroup(any(Context.class),anyString(),anyString());

    }

    @Test
    public void testCreateNotificationChannelGroup(){
        cleverTapModule.createNotificationChannelGroup(groupId,groupName);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.createNotificationChannelGroup(any(Context.class),anyString(),anyString());

    }

    @Test
    public void testDeleteNotificationChannelWhenParamsNull(){

        //when channelId null
        cleverTapModule.deleteNotificationChannel(null);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.deleteNotificationChannel(any(Context.class),anyString());

    }

    @Test
    public void testDeleteNotificationChannel(){
        cleverTapModule.deleteNotificationChannel(channelId);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.deleteNotificationChannel(any(Context.class),anyString());

    }

    @Test
    public void testDeleteNotificationChannelGroupWhenParamsNull(){

        //when groupId null
        cleverTapModule.deleteNotificationChannelGroup(null);

        verifyStatic(CleverTapAPI.class,never());
        CleverTapAPI.deleteNotificationChannelGroup(any(Context.class),anyString());

    }

    @Test
    public void testDeleteNotificationChannelGroup(){
        cleverTapModule.deleteNotificationChannelGroup(groupId);

        verifyStatic(CleverTapAPI.class);
        CleverTapAPI.deleteNotificationChannelGroup(any(Context.class),anyString());

    }

    @Test
    public void testSetOptOut(){
        cleverTapModule.setOptOut(true);
        verify(clevertap).setOptOut(true);
    }

    @Test
    public void testSetOffline(){
        cleverTapModule.setOffline(true);
        verify(clevertap).setOffline(true);
    }

    @Test
    public void testEnableDeviceNetworkInfoReporting(){
        cleverTapModule.enableDeviceNetworkInfoReporting(true);
        verify(clevertap).enableDeviceNetworkInfoReporting(true);
    }

    @Test
    public void testEnablePersonalization(){
        cleverTapModule.enablePersonalization();
        verify(clevertap).enablePersonalization();
    }

    @Test
    public void testDisablePersonalization(){
        cleverTapModule.disablePersonalization();
        verify(clevertap).disablePersonalization();
    }

    @Test
    public void testRecordScreenView(){
        String screenName="cart";
        cleverTapModule.recordScreenView(screenName);
        verify(clevertap).recordScreen(screenName);
    }

    @Test
    public void testEventGetFirstTime(){

        String eventName="cart viewed";
        CallbackImpl callback = PowerMockito.mock(CallbackImpl.class);

        when(clevertap.getFirstTime(anyString())).thenReturn(230000);

        cleverTapModule.eventGetFirstTime(eventName,callback);

        verify(clevertap).getFirstTime(eventName);
        verify(callback).invoke(null,230000);

    }

    @Test
    public void testEventGetLastTime(){

        String eventName="cart viewed";
        CallbackImpl callback = PowerMockito.mock(CallbackImpl.class);

        when(clevertap.getLastTime(anyString())).thenReturn(10000);

        cleverTapModule.eventGetLastTime(eventName,callback);

        verify(clevertap).getLastTime(eventName);
        verify(callback).invoke(null,10000);

    }

    @Test
    public void testEventGetOccurrences(){

        String eventName="cart viewed";
        CallbackImpl callback = PowerMockito.mock(CallbackImpl.class);

        when(clevertap.getCount(anyString())).thenReturn(4);

        cleverTapModule.eventGetOccurrences(eventName,callback);

        verify(clevertap).getCount(eventName);
        verify(callback).invoke(null,4);

    }

    @Test
    public void testEventGetDetail(){
        mockStatic(Arguments.class);

        String eventName="cart viewed";
        CallbackImpl callback = PowerMockito.mock(CallbackImpl.class);
        WritableMap writableMap = PowerMockito.mock(WritableMap.class);

        PowerMockito.when(Arguments.createMap()).thenReturn(writableMap);
        cleverTapModule.eventGetDetail(eventName,callback);

        verify(callback).invoke(null,writableMap);

    }

    @Test
    public void testGetEventHistory(){
        mockStatic(Arguments.class);

        CallbackImpl callback = PowerMockito.mock(CallbackImpl.class);
        WritableMap writableMap = PowerMockito.mock(WritableMap.class);

        PowerMockito.when(Arguments.createMap()).thenReturn(writableMap);
        cleverTapModule.getEventHistory(callback);

        verify(callback).invoke(null,writableMap);

    }

    @Test
    public void testSetLocation(){
        double latitude=1.234;
        double longitude=1.244;

        cleverTapModule.setLocation(latitude,longitude);
        verify(clevertap).setLocation(any(Location.class));

    }

    @Test
    public void testProfileGetCleverTapAttributionIdentifier(){
        String attributionId="123";

        CallbackImpl callback = PowerMockito.mock(CallbackImpl.class);

        when(clevertap.getCleverTapAttributionIdentifier()).thenReturn(attributionId);

        cleverTapModule.profileGetCleverTapAttributionIdentifier(callback);

        verify(clevertap).getCleverTapAttributionIdentifier();
        verify(callback).invoke(null,attributionId);

    }

    @Test
    public void testProfileGetCleverTapID(){
        String ctId="123";

        CallbackImpl callback = PowerMockito.mock(CallbackImpl.class);

        when(clevertap.getCleverTapID()).thenReturn(ctId);

        cleverTapModule.profileGetCleverTapID(callback);

        verify(clevertap).getCleverTapID();
        verify(callback).invoke(null,ctId);

    }

}
