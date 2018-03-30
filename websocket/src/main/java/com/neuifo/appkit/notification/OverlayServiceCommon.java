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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.neuifo.appkit.notification.admin.AdminReceiver;
import com.neuifo.appkit.notification.model.EventPending;
import com.neuifo.appkit.notification.model.NcAction;
import com.neuifo.appkit.notification.model.NcEvent;
import com.neuifo.appkit.notification.model.NofitycationGravity;
import com.neuifo.appkit.notification.model.NotificationOptions;
import com.neuifo.appkit.notification.theme.CustomTheme;
import com.neuifo.appkit.notification.theme.HoloDark;
import com.neuifo.appkit.notification.theme.HoloLight;
import com.neuifo.appkit.notification.theme.L5Black;
import com.neuifo.appkit.notification.theme.L5Dark;
import com.neuifo.appkit.notification.theme.L5Light;
import com.neuifo.appkit.notification.theme.Random;
import com.neuifo.appkit.notification.theme.ThemeClass;
import com.neuifo.appkit.notification.theme.Ubuntu;
import com.neuifo.appkit.notification.util.Mlog;
import com.neuifo.appkit.notification.util.ObjectSerializer;
import com.neuifo.appkit.notification.util.SwipeDismissTouchListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class OverlayServiceCommon extends Service implements SensorEventListener {
    final static String logTag = "OverlayServiceCommon";

    public static final String DATA = "OverlayService_DATA";

    public static final int MAX_DISPLAY_TIME = 60000;
    public static final int MAX_REMINDER_TIME = 1200000;
    public static final int MIN_REMINDER_TIME = 6000;
    private static final int MAX_LINES = 12;
    private static final int SENSOR_DELAY_MILLIS = 10000;
    private static final int MIN_LINES = 2;
    public static final int FLAG_FLOATING_WINDOW = 0x00002000;
    private static final ArrayList<String> LOCKSCREEN_APPS = new ArrayList<>(Arrays.asList(new String[]{"com.achep.acdisplay", "com.silverfinger.lockscreen", "com.slidelock", "com.coverscreen.cover", "com.jiubang.goscreenlock", "com.greatbytes.activenotifications", "com.nemis.memlock", "com.teslacoilsw.widgetlocker", "com.jedga.peek", "com.jedga.peek.free", "com.jedga.peek.pro", "com.hi.locker", "com.vlocker.locker", "com.microsoft.next", "com.cmcm.locker"}));

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private LinearLayout layout;
    private static boolean isViewAdded = false;
    private ThemeClass themeClass = new ThemeClass();

    private PendingIntent pendingIntent;
    private int displayTime = 15000;
    private String currentPackage = "";
    private boolean isCompact = false;
    private boolean isActionButtons = false;
    private Date notificationTime = null;

    private SensorManager sensorManager = null;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    private DevicePolicyManager policyManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wLock;
    private boolean isProximityClose = true;
    private boolean isLocked;

    String packageName = "";
    String tag = "";
    String key = "";
    private String prevPackageName = "0";

    NotificationOptions notificationOptions;

    int id = 0;
    private NofitycationGravity nofitycationGravity;
    private NcEvent ncEvent;

    private enum FINISHSTATUS {

        /**
         * 保持0
         */
        KEEP,

        DELETE,

        /**
         * 打开2
         */
        OPEN
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Mlog.e(logTag, "Create");

            getCurrentPackage();
            isLocked = isLocked();

            /*if (!getResources().getBoolean(R.bool.is_tablet)) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                final ViewGroup.LayoutParams stubLayoutParams = stub.getLayoutParams();
                if (metrics.widthPixels <= metrics.heightPixels)
                    stubLayoutParams.width = metrics.widthPixels;
                else
                    //noinspection SuspiciousNameCombination
                    stubLayoutParams.width = metrics.heightPixels;
                stub.setLayoutParams(stubLayoutParams);
            }*/

        } catch (VerifyError ve) {
            Mlog.w(logTag, ve.getMessage());
        }
    }

    private void getCurrentPackage() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= 21) {
                ActivityManager.RunningAppProcessInfo processInfo = getTopAppLollipop(am);
                if (processInfo != null) currentPackage = processInfo.processName;
            } else {
                currentPackage = am.getRunningTasks(1).get(0).topActivity.getPackageName();
            }
        } catch (SecurityException | IndexOutOfBoundsException | NullPointerException e) {
            reportError(e, "Please allow Heads-up to get running tasks", getApplicationContext());
        }
    }

    @TargetApi(21)
    private ActivityManager.RunningAppProcessInfo getTopAppLollipop(ActivityManager am) {
        List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
        if (tasks != null && tasks.size() > 0) {
            Field processStateField = null;
            try {
                processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            } catch (NoSuchFieldException ignored) {
                Mlog.e(logTag, ignored.getLocalizedMessage());
            }
            final int PROCESS_STATE_TOP = 2;

            for (ActivityManager.RunningAppProcessInfo task : tasks) {
                if (task.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && task.importanceReasonCode == 0) {
                    if (processStateField == null) return task;
                    try {
                        if (processStateField.getInt(task) == PROCESS_STATE_TOP) return task;
                    } catch (IllegalAccessException e) {
                        Mlog.e(logTag, "IAE: " + e.getMessage());
                        return task;
                    }
                }
            }
        }
        return null;
    }

    private void displayWindow() {
        if (notificationOptions.isShowOnLock()) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            if (notificationOptions.isShowonLockOnly()) {
                if (stopIfNotLocked()) return;
            }
            initPowerManager();

            if (notificationOptions.isLightScreen() && !powerManager.isScreenOn()) {
                if (notificationOptions.isPocketModle()) {
                    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    if (sensor == null) {
                        try {
                            sensor = sensorManager.getSensorList(Sensor.TYPE_PROXIMITY).get(0);
                        } catch (Exception sensorListException) {
                            if (Mlog.isLogging) sensorListException.printStackTrace();
                        }
                    }
                    if (sensor != null) {
                        addViewToWindowManager();
                        sensorEventListener = this;
                        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
                        handler.postDelayed(sensorChecker, SENSOR_DELAY_MILLIS);
                    } else {
                        //reportError(null, getString(R.string.no_proximity_sensor_error), getApplicationContext());
                        //preferences.edit().putBoolean("use_proximity", false).apply();
                        notificationOptions.setPocketModle(false);//没有传感器，
                        createWLock();
                        screenOn();
                        addViewToWindowManager();
                    }
                } else {
                    createWLock();
                    screenOn();
                    addViewToWindowManager();
                }
            } else {
                addViewToWindowManager();
            }
        } else {
            if (isLocked()) {
                stopSelf();
            } else {
                addViewToWindowManager();
            }
        }
    }

    private boolean stopIfNotLocked() {
        if (isLocked) return false;
        Mlog.e(logTag, "not locked");
        stopSelf();
        return true;
    }

    private boolean isLocked() {
        /*if (preferences.getBoolean("off_as_locked", false)) {
            initPowerManager();
            if (!powerManager.isScreenOn()) {
                isLocked = true;
                return isLocked;
            }
        }*/

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        final boolean isKeyguardLocked;
        if (Build.VERSION.SDK_INT >= 16) {
            isKeyguardLocked = keyguardManager.isKeyguardLocked();
        } else {
            isKeyguardLocked = keyguardManager.inKeyguardRestrictedInputMode();
        }

        Mlog.v(logTag, isKeyguardLocked + " " + LOCKSCREEN_APPS.contains(currentPackage));
        isLocked = isKeyguardLocked || (currentPackage != null && LOCKSCREEN_APPS.contains(currentPackage));
        return isLocked;
    }

    private void addViewToWindowManager() {
        if (!isViewAdded) {
            windowManager.addView(layout, layoutParams);
            Mlog.e(logTag, "onMessage Show notify!");
            layout.requestFocus();
        }
        isViewAdded = true;
    }

    private final Runnable sensorChecker = new Runnable() {
        @Override
        public void run() {
            Mlog.e(logTag + "SensorChecker", String.valueOf(isProximityClose));

            if (sensorManager != null) {
                sensorManager.unregisterListener(sensorEventListener, sensor);
                sensorManager = null;
            }
        }
    };

    private void initView() {
        /**
         * initlayout
         */
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);

        layout = new LinearLayout(this);
        inflater.inflate(ncEvent.getNotificationOptions().getRootLayout(), layout);
        layout.setVisibility(View.GONE);

        ViewStub stub = layout.findViewById(ncEvent.getNotificationOptions().getRootStub());

        displayTime = notificationOptions.getDisplayTime();

        switch (ncEvent.getThemeType()) {
            case L5LIGHT:
                themeClass = new L5Light(stub);
                break;
            case L5DARK:
                themeClass = new L5Dark(stub);
                break;
            case L5BLACK:
                themeClass = new L5Black(stub);
                break;
            case HOLOLIGHT:
                themeClass = new HoloLight(stub);
                break;
            case HOLODARK:
                themeClass = new HoloDark(stub);
                break;
            case RANDOM:
                themeClass = new Random(stub);
                break;
            case UBUNTU:
                themeClass = new Ubuntu(stub);
                break;
            case COUSTOM:
                themeClass = new CustomTheme(ncEvent.getNcViewWrapper(), stub);
                break;
        }
        stub.inflate();
        themeClass.init(layout);

        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PRIORITY_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);

        nofitycationGravity = notificationOptions.getNofitycationGravity();
        switch (nofitycationGravity) {
            case TOP:
                layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case BOTTOM:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case BELOW_NAVIGATIONBAR:
                layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case CENTER:
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
                break;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Mlog.e(logTag, "Start");

        /**
         * init
         */
        ncEvent = intent.getParcelableExtra(DATA);

        if (ncEvent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }


        if (isViewAdded) {//消除上次
            windowManager.removeViewImmediate(layout);
            themeClass.destroy(layout);
            isViewAdded = false;
        }

        if (intent.getExtras() != null) key = intent.getExtras().getString("key");

        notificationOptions = ncEvent.getNotificationOptions();

        try {
            if (ncEvent.getNcAction() == NcAction.REMOVE) {
                try {
                    if (packageName.equals(ncEvent.getPackageName()) && tag.equals(ncEvent.getTag()) && id == ncEvent.getId()) {
                        Mlog.e(logTag, "remove");
                        doFinish(FINISHSTATUS.KEEP);
                    }
                } catch (Exception e) {
                    reportError(e, "remove failed", getApplicationContext());
                    stopSelf();
                }
                if (TextUtils.isEmpty(packageName)) stopSelf();
                return START_NOT_STICKY;
            }

            initView();
            displayWindow();
            PackageManager pm = getPackageManager();
            Resources appRes = null;

            handler.removeCallbacks(closeTimer);
            //handler.removeCallbacks(delayStop);

            String content = ncEvent.getNcViewWrapper().getContent();
            String title = ncEvent.getNcViewWrapper().getTitle();
            packageName = ncEvent.getPackageName();
            //key = extras.getString("key");
            tag = ncEvent.getTag();
            id = ncEvent.getId();

            if (notificationTime != null) {
                themeClass.hideTime(layout, ncEvent.getNcViewWrapper().getTimeRes());
                notificationTime.setTime(System.currentTimeMillis());
            } else {
                notificationTime = new Date();
            }

            Log.e("notify", "packageName----" + packageName + "-----currentPackage-----" + currentPackage);
            if (notificationOptions.isBlockCurrentApp() || (!isLocked && packageName.equals(currentPackage) && !packageName.equals(getApplicationContext().getPackageName()))) {
                Mlog.e(logTag, "Current package match - stopping");
                stopSelf();
                return START_NOT_STICKY;
            }
           /* Set<String> blockedApps = (Set<String>) ObjectSerializer.deserialize(preferences.getString("noshowlist", ""));
            if (blockedApps != null && blockedApps.size() > 0) {
                final boolean isBlacklistInverted = preferences.getBoolean("noshowlist_inverted", false);
                boolean contains = blockedApps.contains(currentPackage);
                Mlog.v(logTag, blockedApps.toString());
                Mlog.v(logTag + "NoShow", String.format("%s %s", String.valueOf(isBlacklistInverted), contains));
                if ((!isBlacklistInverted && contains) || (isBlacklistInverted && !contains)) {
                    Mlog.e(logTag + "NoShow", "Package match - stopping");
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }*/

            try {
                if (packageName.equals("codes.simen.voiceover")) {//关联播放器
                    appRes = getResources();
                } else {
                    appRes = pm.getResourcesForApplication(packageName);
                }
            } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                reportError(e, "", getApplicationContext());
            }

            isCompact = notificationOptions.isCompact();

            View dismissButton = themeClass.getDismissButton(layout, ncEvent.getNcViewWrapper().getDissmissButtonRes());
            if (notificationOptions.isHideDissButton()) themeClass.hideDismissButton(dismissButton);

            try {
                pendingIntent = ncEvent.getNcViewWrapper().getBasePending();
            } catch (NullPointerException npe) {
                reportError(npe, "", getApplicationContext());
            }

            ImageView imageView = themeClass.getIconView(layout);
            if (imageView != null) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPopupClick(v, notificationOptions.isFloatWindow());
                    }
                });
            }

            try {
                if (Build.VERSION.SDK_INT >= 11) {

                    final int color = ncEvent.getNcViewWrapper().getColor();

                    Drawable drawable = null;

                    Bitmap notifyHead = ncEvent.getNcViewWrapper().getIcon();

                    if (appRes != null && ncEvent.getNcViewWrapper().getSubIcon() != 0) {
                        drawable = appRes.getDrawable(ncEvent.getNcViewWrapper().getSubIcon());
                    } else {
                        try {
                            drawable = pm.getApplicationIcon(packageName);
                        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                            reportError(e, "", getApplicationContext());
                        }
                    }

                    /**
                     *这里暂时不处理
                     */
                    int subIcon = ncEvent.getNcViewWrapper().getSubIcon();

                    ImageView smallIconView = themeClass.getSmallIconView(layout);

                    if (notifyHead == null || notifyHead.isRecycled()) {
                        if (drawable != null) {
                            notifyHead = drawableToBitmap(drawable);
                        }
                        if (smallIconView != null) {
                            themeClass.setSmallIcon(smallIconView, null, color);
                        }
                    } else if (smallIconView != null) {
                        themeClass.setSmallIcon(smallIconView, drawable, color);
                    }

                    if (notifyHead != null && !notifyHead.isRecycled()) {
                        final int shortestSide;
                        final int width = notifyHead.getWidth();
                        final int height = notifyHead.getHeight();
                        if (width > height) {
                            shortestSide = height;
                        } else {
                            shortestSide = width;
                        }

                        notifyHead = ThumbnailUtils.extractThumbnail(notifyHead, shortestSide, shortestSide, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        themeClass.setIcon(imageView, notifyHead, notificationOptions.isShowRoundIcon(), color);
                    }
                }
            } catch (Exception e) {
                reportError(e, "Icon", getApplicationContext());
            }

            if (title.equals("")) {
                try {
                    title = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0));
                } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                    reportError(e, "EmptyTitle", getApplicationContext());
                }
            }

            TextView titleTextView = layout.findViewById(ncEvent.getNcViewWrapper().getTitleRes());
            TextView textView = layout.findViewById(ncEvent.getNcViewWrapper().getContentRes());

            titleTextView.setText(title);

            final boolean privacy_on_lockscreen = isLocked;

            if (privacy_on_lockscreen) {//锁屏时显示的文本
                //textView.setText(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));
                textView.setText(content);
            } else {
                textView.setText(content);
            }

            if (isCompact) {
                textView.setMaxLines(MIN_LINES);
            } else {
                textView.setMaxLines(MAX_LINES);
            }

            final Resources resources = getResources();
            //titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, notificationOptions.textSize * resources.getDimension(R.dimen.text_size_notification_title));
            //textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, notificationOptions.textSize * resources.getDimension(R.dimen.text_size_notification_text));

            if (Build.VERSION.SDK_INT >= 16 && !privacy_on_lockscreen) {
                try {
                    if (ncEvent.getNcViewWrapper().getEventPendingList() != null) {
                        for (final EventPending eventPending : ncEvent.getNcViewWrapper().getEventPendingList()) {
                            ViewGroup actionButtons = themeClass.getActionButtons(layout, eventPending.getClickId());
                            if (eventPending.getIcon() != 0) {
                                if (actionButtons != null) {
                                    themeClass.showActionButtons(layout, eventPending.getClickParentId());
                                    actionButtons.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            triggerIntent(eventPending.getPendingIntent(), false);
                                        }
                                    });
                                }
                            } else {
                                themeClass.addActionButton(actionButtons, eventPending.getTitle(), getApplicationContext().getResources().getDrawable(eventPending.getIcon()), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            if (themeClass.getRootView(layout, ncEvent.getNcViewWrapper().getDataId()).getTranslationX() != 0) {
                                                return; // Stop if we're currently swiping. Bug 0000034
                                            }
                                            triggerIntent(eventPending.getPendingIntent(), false);
                                        } catch (NullPointerException e) {
                                            reportError(e, "", getApplicationContext());
                                        }
                                    }
                                }, notificationOptions.getTextSize());
                            }
                        }
                    }

                    /*ViewGroup actionButtons = themeClass.getActionButtons(layout);
                    //themeClass.removeActionButtons(actionButtons);
                    int i = extras.getInt("actionCount");
                    isActionButtons = i > 0;
                    if (isActionButtons) {
                        themeClass.showActionButtons(layout, i);
                        while (i > 0) {
                            String actionTitle = extras.getString("action" + i + "title");
                            final PendingIntent actionIntent = (PendingIntent) extras.get("action" + i + "intent");

                            int actionIcon = extras.getInt("action" + i + "icon");
                            Drawable icon = null;
                            if (appRes != null) {
                                try {
                                    icon = appRes.getDrawable(actionIcon);
                                } catch (Resources.NotFoundException ignored) {
                                }
                            }

                            themeClass.addActionButton(actionButtons, actionTitle, icon, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        if (themeClass.getRootView(layout).getTranslationX() != 0)
                                            return; // Stop if we're currently swiping. Bug 0000034
                                        Mlog.e(logTag, "sendPendingAction");
                                        triggerIntent(actionIntent, false);
                                    } catch (NullPointerException e) {
                                        reportError(e, "", getApplicationContext());
                                    }
                                }
                            }, notificationOptions.textSize);

                            i--;
                        }

                        if (isCompact) {
                            themeClass.hideActionButtons(layout);
                        }
                    } else {
                        themeClass.hideActionButtons(layout);
                    }*/
                } catch (Exception rte) {
                    reportError(rte, "ThemeActionIcon", getApplicationContext());
                }
            } else {
                //themeClass.hideActionButtons(layout);
            }

            dismissButton.setOnLongClickListener(blockTouchListener);

            if (Build.VERSION.SDK_INT >= 12) {
                ViewGroup self = themeClass.getRootView(layout, ncEvent.getNcViewWrapper().getDataId());

                // Init swipe listener
                final SwipeDismissTouchListener dismissTouchListener = new SwipeDismissTouchListener(self, nofitycationGravity == NofitycationGravity.TOP || nofitycationGravity == NofitycationGravity.BELOW_NAVIGATIONBAR, new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss() {
                        return true;
                    }

                    @Override
                    public boolean canExpand() {
                        return isCompact;
                    }

                    @Override
                    public void onDismiss(View view, Object token, int direction) {
                        //Mlog.v(logTag, "DIR" + direction);
                        switch (direction) {
                            case SwipeDismissTouchListener.DIRECTION_LEFT:
                            case SwipeDismissTouchListener.DIRECTION_RIGHT:
                                if (notificationOptions.isDismissOnSwip()) {
                                    doFinish(FINISHSTATUS.DELETE);
                                } else {
                                    doFinish(FINISHSTATUS.KEEP);
                                }
                                break;
                            case SwipeDismissTouchListener.DIRECTION_UP:
                                doFinish(FINISHSTATUS.KEEP);
                                break;
                            case SwipeDismissTouchListener.DIRECTION_DOWN:
                                expand();
                                return;
                            default:
                                Mlog.e(logTag, "Unknown direction: " + direction);
                                break;
                        }
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void outside() {
                        if (notificationOptions.isCancleAble() || (isLocked && !isLocked())) {
                            if (isLocked) pokeScreenTimer();
                            doFinish(FINISHSTATUS.KEEP);
                        }
                    }
                });
                self.setClipChildren(false);
                self.setClipToPadding(false);
                final ArrayList<View> allChildren = getAllChildren(layout);
                if (allChildren.size() > 0) {
                    for (View v : allChildren) {
                        v.setOnTouchListener(dismissTouchListener);
                    }
                }

                // Animate in
                if (!prevPackageName.equals(packageName)) {
                    AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (displayTime < MAX_DISPLAY_TIME || ncEvent.getNcAction() != NcAction.STAY) {
                                handler.postDelayed(closeTimer, displayTime);
                                System.gc();
                            }
                        }
                    };
                    self.setTranslationX(0);
                    switch (nofitycationGravity) {
                        case TOP:
                        case BELOW_NAVIGATIONBAR:
                            self.setTranslationY(-300);
                            break;
                        case BOTTOM:
                            self.setTranslationY(300);
                            break;
                        case CENTER:
                            self.setTranslationX(-self.getWidth());
                            break;
                    }
                    self.setAlpha(0.0f);
                    Float opacity = (float) notificationOptions.getAlpha();
                    if (opacity == null) opacity = 98f;
                    try {
                        self.animate().setDuration(700).alpha(opacity / 100).translationY(0).translationX(0).setListener(listener);
                    } catch (NullPointerException npe) {
                        reportError(npe, "", getApplicationContext());
                        if (displayTime < MAX_DISPLAY_TIME || ncEvent.getNcAction() != NcAction.STAY) {
                            handler.postDelayed(closeTimer, displayTime);
                        }
                    }
                    prevPackageName = packageName;
                } else {
                    if (displayTime < MAX_DISPLAY_TIME || ncEvent.getNcAction() != NcAction.STAY) {
                        handler.postDelayed(closeTimer, displayTime);
                    }
                }
            } else {
                textView.setMaxLines(MAX_LINES);
                if (displayTime < MAX_DISPLAY_TIME || ncEvent.getNcAction() != NcAction.STAY) {
                    handler.postDelayed(closeTimer, displayTime);
                }
            }

            layout.setVisibility(View.VISIBLE);
        } catch (Exception catchAllException) {
            reportError(catchAllException, "", getApplicationContext());
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private ArrayList<View> getAllChildren(View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<>();

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    private final Handler handler = new Handler();
    private final Runnable closeTimer = new Runnable() {
        @Override
        public void run() {
            if (notificationOptions.isKeepOnLockUntilClick()) {
                if (isLocked()) {
                    if (!notificationOptions.isKeepScreenOn()) screenOff();
                    themeClass.showTime(layout, ncEvent.getNcViewWrapper().getTimeRes(), notificationTime);
                    themeClass.hideDismissButton(themeClass.getDismissButton(layout, ncEvent.getNcViewWrapper().getDissmissButtonRes()));
                    return;
                }

                Mlog.v(logTag, "not locked - removing notification");
            }
            final ViewGroup rootView = themeClass.getRootView(layout, ncEvent.getNcViewWrapper().getDataId());
            if (rootView.getTranslationX() != 0 || rootView.getTranslationY() != 0) {
                handler.postDelayed(closeTimer, displayTime);
                return; // Stop if we're currently swiping.
            }
            if (displayTime == MAX_DISPLAY_TIME) return;

            doFinish(FINISHSTATUS.KEEP);
        }
    };

    @SuppressWarnings("UnusedDeclaration")
    public void doStop(View v) {
        doFinish(FINISHSTATUS.DELETE);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void doHide(View v) {
        doFinish(FINISHSTATUS.KEEP);
    }

    @SuppressWarnings("unused")
    public void onPopupClick(View v) {
        onPopupClick(v, false);
    }

    public void onPopupClick(View v, boolean isFloating) {
        final ViewGroup rootView = themeClass.getRootView(layout, ncEvent.getNcViewWrapper().getDataId());
        if (rootView.getTranslationX() != 0 || rootView.getTranslationY() != 0) {
            return; // Stop if we're currently swiping. Bug 0000034 (in the old bug tracker)
        }

        if (Build.VERSION.SDK_INT >= 12 || !expand()) triggerIntent(pendingIntent, isFloating);
    }

    /*
     * Expand the heads-up. Returns true if the heads-up was expanded, false if it was expanded before calling this method.
     */
    private boolean expand() {
        if (!isCompact) {
            return false;
        } else {
            TextView subtitle = (TextView) layout.findViewById(ncEvent.getNcViewWrapper().getSubTitleRes());
            if (subtitle != null) {
                if ((subtitle.getLineCount() <= MIN_LINES && subtitle.length() < 80) && !isActionButtons) {
                    return false;
                }
                isCompact = false;
                subtitle.setMaxLines(MAX_LINES);
                if (isActionButtons) themeClass.showActionButtons(layout, -1);
                if (displayTime < MAX_DISPLAY_TIME) {
                    handler.removeCallbacks(closeTimer);
                    handler.postDelayed(closeTimer, displayTime);
                }
            }

            return true;
        }
    }

    private void triggerIntent(PendingIntent mPendingIntent, boolean isFloating) {
        if (isLocked() && notificationOptions.isDismissKeyGuard()) {
            pokeScreenTimer();
            if (notificationOptions.isDismisskeyGuardForce()) {
                dismissKeyguard();
                openIntent(mPendingIntent, isFloating);
            } else {
                //startActivity(new Intent(getApplicationContext(), UnlockActivity.class).putExtra("action", mPendingIntent).putExtra("floating", isFloating).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            doFinish(FINISHSTATUS.OPEN);
        } else {
            openIntent(mPendingIntent, isFloating);
        }
    }

    @SuppressLint("WrongConstant")
    private void openIntent(PendingIntent mPendingIntent, boolean isFloating) {
        try {
            Mlog.e(logTag, "sendPending");

            Intent intent = new Intent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isFloating) intent.addFlags(FLAG_FLOATING_WINDOW);
            mPendingIntent.send(getApplicationContext(), 0, intent);
            doFinish(FINISHSTATUS.OPEN);
        } catch (PendingIntent.CanceledException e) {
            //reportError(e, "App has canceled action", getApplicationContext());
            //Toast.makeText(getApplicationContext(), getString(R.string.pendingintent_cancel_exception), Toast.LENGTH_SHORT).show();
            doFinish(FINISHSTATUS.KEEP);
        } catch (NullPointerException e) {
            //reportError(e, "No action defined", getApplicationContext());
            //Toast.makeText(getApplicationContext(), getString(R.string.pendingintent_null_exception), Toast.LENGTH_SHORT).show();
            doFinish(FINISHSTATUS.KEEP);
        }
    }

    private void dismissKeyguard() {
        if (Build.VERSION.SDK_INT >= 16) {
            if (!notificationOptions.isDismissKeyGuard()) return;

            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (keyguardManager.isKeyguardLocked()) {
                Mlog.e(logTag, "attempt exit");
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), KeyguardRelock.class);
                intent.setAction(Intent.ACTION_SCREEN_ON);
                startService(intent);
            }
        }
    }

    private final View.OnLongClickListener blockTouchListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
           /* try {
                Set<String> blacklist = (Set<String>) ObjectSerializer.deserialize(preferences.getString("blacklist", ""));
                if (blacklist == null) blacklist = new HashSet<>();

                final boolean isBlacklistInverted = preferences.getBoolean("blacklist_inverted", false);
                Mlog.v(logTag, isBlacklistInverted);

                if (isBlacklistInverted) {
                    if (blacklist.contains(packageName) && blacklist.remove(packageName))
                        Toast.makeText(getApplicationContext(), getText(R.string.blocked_confirmation), Toast.LENGTH_SHORT).show();
                } else if (blacklist.add(packageName))
                    Toast.makeText(getApplicationContext(), getText(R.string.blocked_confirmation), Toast.LENGTH_SHORT).show();

                Mlog.v(logTag, blacklist);
                final String serialized = ObjectSerializer.serialize((Serializable) blacklist);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("blacklist", serialized);
                editor.apply();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }*/
            doFinish(FINISHSTATUS.DELETE);
            return true;
        }
    };

    private void doFinish(final FINISHSTATUS doDismiss) {
        handler.removeCallbacks(closeTimer);

        // Integrate with Voiceify
        if (doDismiss == FINISHSTATUS.DELETE || doDismiss == FINISHSTATUS.OPEN) {
            isViewAdded = false;
            PackageManager packageManager = getPackageManager();
            Intent intent = new Intent("codes.simen.notificationspeaker.STOP_READING");
            intent.putExtra("packageName", packageName);
            intent.putExtra("tag", tag);
            intent.putExtra("id", id);
            intent.setPackage("codes.simen.notificationspeaker");
            try {
                ResolveInfo resolveInfo = packageManager.resolveService(intent, 0);
                if (resolveInfo.serviceInfo != null) {
                    Mlog.e(logTag, "Voiceify found and resolved");
                    startService(intent);
                }
            } catch (NullPointerException ignored) {

            } // Don't panic! We'll survive without Voiceify
        }

        if (Build.VERSION.SDK_INT >= 12) {
            try {
                View self = layout.getChildAt(0);
                ViewPropertyAnimator animator = self.animate().setDuration(300).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        layout.setVisibility(View.GONE);
                        if (doDismiss == FINISHSTATUS.DELETE) {
                            doDismiss(true);
                        } else if (doDismiss == FINISHSTATUS.OPEN) {
                            doDismiss(false);
                        } else {
                            if (wLock != null && wLock.isHeld()) wLock.release();
                            stopSelf();
                        }
                    }
                });
                if (doDismiss == FINISHSTATUS.DELETE) {
                    animator.translationX(-400);
                } else if (doDismiss == FINISHSTATUS.KEEP) {
                    switch (nofitycationGravity) {
                        case TOP:
                        case BELOW_NAVIGATIONBAR:
                            animator.translationY(-300);
                            break;
                        case BOTTOM:
                            animator.translationY(300);
                            break;
                        case CENTER:
                            break;
                    }
                }
            } catch (Exception e) {
                reportError(e, "", getApplicationContext());
                e.printStackTrace();
                layout.setVisibility(View.GONE);
                if (doDismiss == FINISHSTATUS.DELETE) {
                    doDismiss(true);
                } else if (doDismiss == FINISHSTATUS.OPEN) {
                    doDismiss(false);
                } else {
                    if (wLock != null && wLock.isHeld()) wLock.release();
                    stopSelf();
                }
            }
        } else {
            layout.setVisibility(View.GONE);
            if (doDismiss == FINISHSTATUS.DELETE) {
                doDismiss(true);
            } else if (doDismiss == FINISHSTATUS.OPEN) {
                doDismiss(false);
            } else {
                if (wLock != null && wLock.isHeld()) wLock.release();
                stopSelf();
            }
        }
        prevPackageName = "0";
    }

    void doDismiss(boolean stopNow) {
        if (stopNow) {
            if (wLock != null && wLock.isHeld()) wLock.release();
            stopSelf();
        } else {
            layout.setVisibility(View.GONE);
            /*Mlog.v(logTag, "delayStop");
            isDelaying = true;
            handler.postDelayed(delayStop, 10000);*/
            stopSelf();
        }
    }

    /*private final Runnable delayStop = new Runnable() {
        @Override
        public void run() {
            if (wLock != null && wLock.isHeld())
                wLock.release();
            stopSelf();
            isDelaying = false;
        }
    };*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        Mlog.e(logTag, "Destroy");

        if (isViewAdded) windowManager.removeViewImmediate(layout);
        if (sensorManager != null) sensorManager.unregisterListener(this, sensor);
        if (wLock != null && wLock.isHeld()) wLock.release();

        themeClass.destroy(layout);
        isViewAdded = false;
        System.gc();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Mlog.v(logTag + "Sensor", String.valueOf(event.values[0]));
        isProximityClose = (event.values[0] != sensor.getMaximumRange());

        if (isProximityClose) {
            screenOff();
        } else {
            screenOn();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Mlog.v(logTag + "SensorAccuracy", String.valueOf(accuracy));
    }

    void createWLock() {
        initPowerManager();

        wLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "heads-up");
    }

    private void initPowerManager() {
        if (powerManager == null) powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    }

    void screenOn() {
        if (wLock == null) {
            createWLock();
        }
        if (!wLock.isHeld()) {
            Mlog.v(logTag, "wLock not held");
            if (notificationOptions.isKeepScreenOn()) {
                Mlog.v(logTag, "wLock forever");
                wLock.acquire();
            } else {
                Mlog.v(logTag, "wLock for " + displayTime);
                wLock.acquire(displayTime);
            }
        }
    }

    void screenOff() {
        if (wLock != null && wLock.isHeld()) wLock.release();

        initPowerManager();
        if (powerManager.isScreenOn()) {
            if (policyManager == null) {
                policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            }
            if (policyManager.isAdminActive(AdminReceiver.getComponentName(getApplicationContext()))) {
                Mlog.v(logTag, "ADMIN_ACTIVE");
                policyManager.lockNow();
            } else {
                Mlog.v(logTag, "ADMIN_NOT_ACTIVE");
            }
        }
    }

    /**
     * Acquire a WakeLock which ensures the screen is on and then pokes the user activity timer.
     */
    void pokeScreenTimer() {
        initPowerManager();
        powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "pokeScreenTimer").acquire(1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static void reportError(Exception e, String msg, Context c) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            if (e != null) {
                e.printStackTrace();
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                msg = msg.concat(writer.toString());
                editor.putString("lastException", ObjectSerializer.serialize(e));
            }
            editor.putString("lastBug", msg);
            editor.apply();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
