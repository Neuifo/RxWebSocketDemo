/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.neuifo.appkit.notification;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.neuifo.appkit.R;
import com.neuifo.appkit.notification.model.NcAction;
import com.neuifo.appkit.notification.model.NcEvent;
import com.neuifo.appkit.notification.model.NotificationHelper;
import com.neuifo.appkit.notification.theme.ThemeType;
import com.neuifo.appkit.notification.util.Mlog;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

@SuppressLint("NewApi")
public class NotificationListenerService extends android.service.notification.NotificationListenerService {
    private final static String logTag = "NotificationListener";
    public static final String ACTION_CUSTOM = "codes.simen.l50notifications.NotificationListenerService.ACTION_CUSTOM";

    //private VoiceOver voiceOver = null;
    private final IBinder mBinder = new LocalBinder();

    public NotificationListenerService() {
        Mlog.v(logTag, "Created listener");
    }

    public class LocalBinder extends Binder {
        NotificationListenerService getService() {
            return NotificationListenerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Mlog.e(logTag, "bind");
        if (intent.getAction().equals(ACTION_CUSTOM)) {
            super.onBind(intent);
            return mBinder;
        } else {
            doLoadSettings();
            return super.onBind(intent);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        try {
            /* if Show non-cancellable notifications is selected then need not check
            for Ongoing / Clearable as there will be ongoing notification by the background
             service which is trying to display.
            if Show non-cancellable notifications is not selected then existing logic
            prevails
             */

            if (!statusBarNotification.getPackageName().equals(getApplicationContext().getPackageName())) {
                return;
            }

            if ((statusBarNotification.isOngoing() || !statusBarNotification.isClearable()) && !PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext()).
                            getBoolean("show_non_cancelable", false)) return;

            if (NotificationListenerAccessibilityService.doLoadSettings) doLoadSettings();

            String statusBarNotificationKey = null;
            if (Build.VERSION.SDK_INT >= 20)
                statusBarNotificationKey = statusBarNotification.getKey();

            DecisionMaker decisionMaker = new DecisionMaker();

            decisionMaker.handleActionAdd(statusBarNotification.getNotification(), statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotificationKey, getApplicationContext(), "listener");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Mlog.e(logTag, "NPE");
        }
    }

    private void doLoadSettings() {
        NotificationListenerAccessibilityService.doLoadSettings = false;
        /*if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("music_on", false)) {
            if (voiceOver == null)
            voiceOver = new VoiceOver();
            voiceOver.enableVoiceOver(getApplicationContext());
        } else if (voiceOver != null)
            voiceOver.disableVoiceOver(getApplicationContext());*/

        if (isAccessibilityEnabled()) {
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dismiss_white);
            NcEvent event = new NotificationHelper.Builder()
                    .setTitle(getString(R.string.app_name))
                    .setThemeType(ThemeType.L5LIGHT)
                    .setNcAction(NcAction.STAY).setContent(getString(R.string.intro_warning_both_services))
                    //.setIcon(bitmap)
                    .setBasePending(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0))
                    .build();
            NotificationHelper.sendNotificaiton(getApplicationContext(), event);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        DecisionMaker decisionMaker = new DecisionMaker();
        decisionMaker.handleActionRemove(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), getApplicationContext());
    }

    @SuppressWarnings("deprecation")
    public void doRemove(String pkg, String tag, int id) {
        Mlog.e(logTag, pkg + tag + id);
        try {
            cancelNotification(pkg, tag, id);
        } catch (SecurityException e) {
            try {
                String report = e.getMessage();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                report = report.concat(writer.toString());
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("lastBug", report);
                editor.apply();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void doRemove(String key) {
        Mlog.e(logTag, key);
        try {
            cancelNotification(key);
        } catch (SecurityException e) {
            try {
                String report = e.getMessage();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                report = report.concat(writer.toString());
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("lastBug", report);
                editor.apply();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public boolean isNotificationValid(String pkg, String tag, int id) {
        final StatusBarNotification[] activeNotifications = getActiveNotifications();
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            final String statusBarNotificationTag = statusBarNotification.getTag();
            final String statusBarNotificationPackageName = statusBarNotification.getPackageName();
            final int statusBarNotificationId = statusBarNotification.getId();
            if (statusBarNotificationPackageName.equals(pkg) && statusBarNotificationId == id) {
                if (tag == null && statusBarNotificationTag == null) return true;
                if (tag != null && statusBarNotificationTag != null)
                    if (statusBarNotificationTag.equals(tag)) return true;
            }
        }
        return false;
    }

    /*
    TODO: Doesn't work, see VoiceOver.java
    public void pushMusicNotification (String pkg) {
        StatusBarNotification[] statusBarNotifications = getActiveNotifications();
        if (statusBarNotifications.length > 0) {
            for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                final String statusBarNotificationPackageName = statusBarNotification.getPackageName();
                Mlog.v(pkg, statusBarNotificationPackageName);
                //if (pkg.contains(statusBarNotificationPackageName) && !statusBarNotificationPackageName.equals("android")) {
                if (statusBarNotificationPackageName.equals("com.google.android.music")) {
                    DecisionMaker decisionMaker = new DecisionMaker();
                    decisionMaker.handleActionAdd(statusBarNotification.getNotification(),
                            statusBarNotificationPackageName,
                            statusBarNotification.getTag(),
                            statusBarNotification.getId(),
                            getApplicationContext(),
                            "music");
                }
            }
        }
    }*/

    boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Mlog.w(logTag, "Error finding accessibility setting: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Mlog.e(logTag, "Setting: " + accessibilityService);
                   /* if (accessibilityService.equalsIgnoreCase(WelcomeActivity.ACCESSIBILITY_SERVICE_NAME)){
                        return true;
                    }*/
                }
            }

        }
        return false;
    }
}
