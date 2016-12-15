/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.app.job.JobScheduler;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStatsManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.RestrictionsManager;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutManager;
import android.hardware.ConsumerIrManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.media.midi.MidiManager;
import android.media.projection.MediaProjectionManager;
import android.media.session.MediaSessionManager;
import android.media.tv.TvInputManager;
import android.net.ConnectivityManager;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.HardwarePropertiesManager;
import android.os.PowerManager;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.InputMethodManager;
import android.view.textservice.TextServicesManager;

/**
 * Convenience methods to get system services from {@link Context}
 */
public final class ContextUtils {
    private ContextUtils() {
    }

    /**
     * Obtain a {@link PowerManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link PowerManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link PowerManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static PowerManager getPowerManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.POWER_SERVICE));
    }

    /**
     * Obtain a {@link WindowManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link WindowManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link WindowManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static WindowManager getWindowManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.WINDOW_SERVICE));
    }

    /**
     * Obtain a {@link LayoutInflater} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link LayoutInflater} associated with specified {@link Context}
     * @throws InvalidContextException if {@link LayoutInflater} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static LayoutInflater getLayoutInflater(@NonNull Context context) {
        return validate(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    /**
     * Obtain a {@link AccountManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link AccountManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link AccountManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static AccountManager getAccountManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.ACCOUNT_SERVICE));
    }

    /**
     * Obtain a {@link ActivityManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link ActivityManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link ActivityManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static ActivityManager getActivityManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.ACTIVITY_SERVICE));
    }

    /**
     * Obtain a {@link AlarmManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link AlarmManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link AlarmManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static AlarmManager getAlarmManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.ALARM_SERVICE));
    }

    /**
     * Obtain a {@link NotificationManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link NotificationManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link NotificationManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static NotificationManager getNotificationManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.NOTIFICATION_SERVICE));
    }

    /**
     * Obtain a {@link AccessibilityManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link AccessibilityManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link AccessibilityManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static AccessibilityManager getAccessibilityManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.ACCESSIBILITY_SERVICE));
    }

    /**
     * Obtain a {@link CaptioningManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link CaptioningManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link CaptioningManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static CaptioningManager getCaptioningManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.CAPTIONING_SERVICE));
    }

    /**
     * Obtain a {@link KeyguardManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link KeyguardManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link KeyguardManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static KeyguardManager getKeyguardManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.KEYGUARD_SERVICE));
    }

    /**
     * Obtain a {@link LocationManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link LocationManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link LocationManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static LocationManager getLocationManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.LOCATION_SERVICE));
    }

    /**
     * Obtain a {@link SearchManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link SearchManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link SearchManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static SearchManager getSearchManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.SEARCH_SERVICE));
    }

    /**
     * Obtain a {@link SensorManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link SensorManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link SensorManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static SensorManager getSensorManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.SENSOR_SERVICE));
    }

    /**
     * Obtain a {@link StorageManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link StorageManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link StorageManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static StorageManager getStorageManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.STORAGE_SERVICE));
    }

    /**
     * Obtain a {@link WallpaperManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link WallpaperManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link WallpaperManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static WallpaperManager getWallpaperManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.WALLPAPER_SERVICE));
    }

    /**
     * Obtain a {@link Vibrator} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link Vibrator} associated with specified {@link Context}
     * @throws InvalidContextException if {@link Vibrator} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static Vibrator getVibrator(@NonNull Context context) {
        return validate(context.getSystemService(Context.VIBRATOR_SERVICE));
    }

    /**
     * Obtain a {@link ConnectivityManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link ConnectivityManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link ConnectivityManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static ConnectivityManager getConnectivityManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    /**
     * Obtain a {@link NetworkStatsManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link NetworkStatsManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link NetworkStatsManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.M)
    public static NetworkStatsManager getNetworkStatsManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.NETWORK_STATS_SERVICE));
    }

    /**
     * Obtain a {@link WifiManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link WifiManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link WifiManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static WifiManager getWifiManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.WIFI_SERVICE));
    }

    /**
     * Obtain a {@link WifiP2pManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link WifiP2pManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link WifiP2pManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static WifiP2pManager getWifiP2pManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.WIFI_P2P_SERVICE));
    }

    /**
     * Obtain a {@link NsdManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link NsdManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link NsdManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static NsdManager getNsdManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.NSD_SERVICE));
    }

    /**
     * Obtain a {@link AudioManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link AudioManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link AudioManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static AudioManager getAudioManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.AUDIO_SERVICE));
    }

    /**
     * Obtain a {@link FingerprintManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link FingerprintManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link FingerprintManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.M)
    public static FingerprintManager getFingerprintManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.FINGERPRINT_SERVICE));
    }

    /**
     * Obtain a {@link MediaRouter} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link MediaRouter} associated with specified {@link Context}
     * @throws InvalidContextException if {@link MediaRouter} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static MediaRouter getMediaRouter(@NonNull Context context) {
        return validate(context.getSystemService(Context.MEDIA_ROUTER_SERVICE));
    }

    /**
     * Obtain a {@link MediaSessionManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link MediaSessionManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link MediaSessionManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static MediaSessionManager getMediaSessionManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.MEDIA_SESSION_SERVICE));
    }

    /**
     * Obtain a {@link TelephonyManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link TelephonyManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link TelephonyManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static TelephonyManager getTelephonyManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.TELEPHONY_SERVICE));
    }

    /**
     * Obtain a {@link SubscriptionManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link SubscriptionManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link SubscriptionManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static SubscriptionManager getSubscriptionManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE));
    }

    /**
     * Obtain a {@link TelecomManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link TelecomManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link TelecomManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static TelecomManager getTelecomManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.TELECOM_SERVICE));
    }

    /**
     * Obtain a {@link CarrierConfigManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link CarrierConfigManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link CarrierConfigManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.M)
    public static CarrierConfigManager getCarrierConfigManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.CARRIER_CONFIG_SERVICE));
    }

    /**
     * Obtain a {@link ClipboardManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link ClipboardManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link ClipboardManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static ClipboardManager getClipboardManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.CLIPBOARD_SERVICE));
    }

    /**
     * Obtain a {@link InputMethodManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link InputMethodManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link InputMethodManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static InputMethodManager getInputMethodManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.INPUT_METHOD_SERVICE));
    }

    /**
     * Obtain a {@link TextServicesManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link TextServicesManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link TextServicesManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static TextServicesManager getTextServicesManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE));
    }

    /**
     * Obtain a {@link AppWidgetManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link AppWidgetManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link AppWidgetManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static AppWidgetManager getAppWidgetManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.APPWIDGET_SERVICE));
    }

    /**
     * Obtain a {@link DropBoxManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link DropBoxManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link DropBoxManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static DropBoxManager getDropBoxManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.DROPBOX_SERVICE));
    }

    /**
     * Obtain a {@link DevicePolicyManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link DevicePolicyManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link DevicePolicyManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static DevicePolicyManager getDevicePolicyManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.DEVICE_POLICY_SERVICE));
    }

    /**
     * Obtain a {@link UiModeManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link UiModeManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link UiModeManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static UiModeManager getUiModeManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.UI_MODE_SERVICE));
    }

    /**
     * Obtain a {@link DownloadManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link DownloadManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link DownloadManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static DownloadManager getDownloadManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.DOWNLOAD_SERVICE));
    }

    /**
     * Obtain a {@link BatteryManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link BatteryManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link BatteryManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static BatteryManager getBatteryManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.BATTERY_SERVICE));
    }

    /**
     * Obtain a {@link NfcManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link NfcManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link NfcManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static NfcManager getNfcManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.NFC_SERVICE));
    }

    /**
     * Obtain a {@link BluetoothManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link BluetoothManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link BluetoothManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static BluetoothManager getBluetoothManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.BLUETOOTH_SERVICE));
    }

    /**
     * Obtain a {@link UsbManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link UsbManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link UsbManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static UsbManager getUsbManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.USB_SERVICE));
    }

    /**
     * Obtain a {@link InputManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link InputManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link InputManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static InputManager getInputManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.INPUT_SERVICE));
    }

    /**
     * Obtain a {@link DisplayManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link DisplayManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link DisplayManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static DisplayManager getDisplayManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.DISPLAY_SERVICE));
    }

    /**
     * Obtain a {@link UserManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link UserManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link UserManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static UserManager getUserManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.USER_SERVICE));
    }

    /**
     * Obtain a {@link LauncherApps} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link LauncherApps} associated with specified {@link Context}
     * @throws InvalidContextException if {@link LauncherApps} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static LauncherApps getLauncherApps(@NonNull Context context) {
        return validate(context.getSystemService(Context.LAUNCHER_APPS_SERVICE));
    }

    /**
     * Obtain a {@link RestrictionsManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link RestrictionsManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link RestrictionsManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static RestrictionsManager getRestrictionsManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.RESTRICTIONS_SERVICE));
    }

    /**
     * Obtain a {@link AppOpsManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link AppOpsManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link AppOpsManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static AppOpsManager getAppOpsManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.APP_OPS_SERVICE));
    }

    /**
     * Obtain a {@link CameraManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link CameraManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link CameraManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static CameraManager getCameraManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.CAMERA_SERVICE));
    }

    /**
     * Obtain a {@link PrintManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link PrintManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link PrintManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static PrintManager getPrintManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.PRINT_SERVICE));
    }

    /**
     * Obtain a {@link ConsumerIrManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link ConsumerIrManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link ConsumerIrManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    public static ConsumerIrManager getConsumerIrManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.CONSUMER_IR_SERVICE));
    }

    /**
     * Obtain a {@link TvInputManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link TvInputManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link TvInputManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static TvInputManager getTvInputManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.TV_INPUT_SERVICE));
    }

    /**
     * Obtain a {@link UsageStatsManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link UsageStatsManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link UsageStatsManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static UsageStatsManager getUsageStatsManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.USAGE_STATS_SERVICE));
    }

    /**
     * Obtain a {@link JobScheduler} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link JobScheduler} associated with specified {@link Context}
     * @throws InvalidContextException if {@link JobScheduler} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static JobScheduler getJobScheduler(@NonNull Context context) {
        return validate(context.getSystemService(Context.JOB_SCHEDULER_SERVICE));
    }

    /**
     * Obtain a {@link MediaProjectionManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link MediaProjectionManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link MediaProjectionManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static MediaProjectionManager getMediaProjectionManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.MEDIA_PROJECTION_SERVICE));
    }

    /**
     * Obtain a {@link MidiManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link MidiManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link MidiManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.M)
    public static MidiManager getMidiManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.MIDI_SERVICE));
    }

    /**
     * Obtain a {@link HardwarePropertiesManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link HardwarePropertiesManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link HardwarePropertiesManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public static HardwarePropertiesManager getHardwarePropertiesManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE));
    }

    /**
     * Obtain a {@link ShortcutManager} instance associated with specified {@link Context}
     *
     * @param context Context
     * @return {@link ShortcutManager} associated with specified {@link Context}
     * @throws InvalidContextException if {@link HardwarePropertiesManager} can't be obtained
     *                                 from specified {@link Context}
     */
    @NonNull
    @SuppressWarnings("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    public static ShortcutManager getShortcutManager(@NonNull Context context) {
        return validate(context.getSystemService(Context.SHORTCUT_SERVICE));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private static <T> T validate(@Nullable Object service) {
        if (service == null) {
            throw new InvalidContextException(
                    "Specified context is not valid or service is not available.");
        }
        return (T) service;
    }
}
