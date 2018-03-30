package com.neuifo.appkit.notification.model;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.widget.Toast;
import com.neuifo.appkit.R;
import com.neuifo.appkit.notification.NotificationListenerAccessibilityService;
import com.neuifo.appkit.notification.OverlayServiceCommon;
import com.neuifo.appkit.notification.theme.ThemeType;
import java.util.List;

/**
 * Created by neuifo on 2017/9/14.
 */

public class NotificationHelper {


    public static void sendNotificaiton(Context activity, NcEvent ncEvent) {
        Intent intent = new Intent();
        intent.setClass(activity.getApplicationContext(), OverlayServiceCommon.class);
        intent.putExtra(OverlayServiceCommon.DATA, ncEvent);
        activity.startService(intent);
    }


    public static void sendNotificaiton(Context activity, NcEvent ncEvent, Class clazz) {
        Intent intent = new Intent();
        intent.setClass(activity.getApplicationContext(), clazz);
        intent.putExtra(OverlayServiceCommon.DATA, ncEvent);
        activity.startService(intent);
    }


    public static boolean isNotificationListenerEnabled(Context context) {
        try {
            //noinspection ConstantConditions
            ContentResolver contentResolver = context.getContentResolver();
            String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
            String packageName = context.getPackageName();

            // check to see if the enabledNotificationListeners String contains our package name
            return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
        } catch (NullPointerException e) {
            return false;
        }
    }

    //public static final String ACCESSIBILITY_SERVICE_NAME = "codes.simen.l50notifications/codes.simen.l50notifications.NotificationListenerAccessibilityService";

    public static boolean isAccessibilityEnabled(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            //Mlog.d(logTag, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            //Mlog.d(logTag, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            //Mlog.d(logTag, "Setting: " + settingValue);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    // Mlog.d(logTag, "Setting: " + accessibilityService);

                    if (accessibilityService.equalsIgnoreCase(/*ACCESSIBILITY_SERVICE_NAME*/context.getPackageName() + "/" + NotificationListenerAccessibilityService.class.getName())) {
                        //Mlog.d(logTag, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    } else if (Build.VERSION.SDK_INT < 18 && "com.pushbullet.android/com.pushbullet.android.notifications.mirroring.CompatNotificationMirroringService".equals(accessibilityService)) {
                        // For easier translation in case of other troublesome services
                        Toast.makeText(context, String.format(context.getString(R.string.accessibility_service_blocked), "PushBullet Notification Mirroring"), Toast.LENGTH_LONG).show();
                    }
                }
            }

        } else {
            //Mlog.d(logTag, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    public static void gotoNotifyservice(Context context) {
        try {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            try {
                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                context.startActivity(intent);
                Toast.makeText(context, context.getText(R.string.notification_listener_not_found_detour), Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException anfe2) {
                Toast.makeText(context, anfe2.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void gotoAccessibility(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
            Toast.makeText(context, context.getText(R.string.accessibility_toast), Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException anfe) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
                Toast.makeText(context, context.getText(R.string.accessibility_not_found_detour), Toast.LENGTH_LONG).show();
            } catch (ActivityNotFoundException anfe2) {
                Toast.makeText(context, anfe2.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class Builder {

        private NcEvent ncEvent;
        private NcViewWrapper ncViewWrapper;
        private NotificationOptions notificationOptions;


        public Builder() {
            ncEvent = new NcEvent();
            ncViewWrapper = new NcViewWrapper();
            notificationOptions = new NotificationOptions();
        }

        /**
         * 设置当前提请依赖的程序报名
         */
        public Builder setPackageName(String packAgeName) {
            ncEvent.setPackageName(packAgeName);
            return this;
        }

        public Builder setNcAction(NcAction ncAction) {
            ncEvent.setNcAction(ncAction);
            return this;
        }

        public Builder setNcViewWarrper(NcViewWrapper ncViewWarrper) {
            ncEvent.setNcViewWrapper(ncViewWarrper);
            return this;
        }

        public Builder setThemeType(ThemeType themeType) {
            ncEvent.setThemeType(themeType);
            return this;
        }

        public Builder setId(int id) {
            ncEvent.setId(id);
            return this;
        }

        public Builder setTag(String tag) {
            ncEvent.setTag(tag);
            return this;
        }

        public Builder setColor(@ColorInt int color) {
            ncViewWrapper.setColor(color);
            return this;
        }

        public Builder setRootId(int rootId) {
            this.ncViewWrapper.setRootId(rootId);
            return this;
        }

        public Builder setDataId(int dataId) {
            this.ncViewWrapper.setDataId(dataId);
            return this;
        }

        public Builder setTitleRes(int titleRes) {
            this.ncViewWrapper.setTitleRes(titleRes);
            return this;
        }

        public Builder setSubTitleRes(int subTitleRes) {
            this.ncViewWrapper.setSubTitleRes(subTitleRes);
            return this;
        }

        public Builder setContentRes(int contentRes) {
            this.ncViewWrapper.setContentRes(contentRes);
            return this;
        }

        public Builder setTimeRes(int timeRes) {
            this.ncViewWrapper.setTimeRes(timeRes);
            return this;
        }


        public Builder setDissmissButtonRes(int dissmissButtonRes) {
            this.ncViewWrapper.setDissmissButtonRes(dissmissButtonRes);
            return this;
        }

        public Builder setTitle(String title) {
            ncViewWrapper.setTitle(title);
            return this;
        }

        public Builder setContent(String content) {
            ncViewWrapper.setContent(content);
            return this;
        }

        /**
         * 设置通知的icon，由于可能需要加载，故为Bitmap
         */
        public Builder setIcon(Bitmap bitmap) {
            ncViewWrapper.setIcon(bitmap);
            return this;
        }

        public Builder setSubIcon(int subIcon) {
            ncViewWrapper.setSubIcon(subIcon);
            return this;
        }

        public Builder setEventPendingList(List<EventPending> eventPendingList) {
            ncViewWrapper.setEventPendingList(eventPendingList);
            return this;
        }

        public Builder setBasePending(PendingIntent basePending) {
            ncViewWrapper.setBasePending(basePending);
            return this;
        }

        public Builder setRootLayout(int rootLayout) {
            notificationOptions.setRootLayout(rootLayout);
            return this;
        }


        public Builder setRootStub(int rootStub) {
            notificationOptions.setRootStub(rootStub);
            return this;
        }


        public Builder setNofitycationGravity(NofitycationGravity nofitycationGravity) {
            notificationOptions.setNofitycationGravity(nofitycationGravity);
            return this;
        }


        /**
         * 在锁屏界面显示
         */
        public Builder setShowOnLock(boolean showOnLock) {
            notificationOptions.setShowOnLock(showOnLock);
            return this;
        }

        /**
         * 仅在锁屏界面显示
         */
        public Builder setShowonLockOnly(boolean showonLockOnly) {
            notificationOptions.setShowonLockOnly(showonLockOnly);
            return this;
        }

        /**
         * 如果不点击，会一只保持在锁屏界面
         */
        public Builder setKeepOnLockUntilClick(boolean keepOnLockUntilClick) {
            notificationOptions.setKeepOnLockUntilClick(keepOnLockUntilClick);
            return this;
        }

        /**
         * 发送通知点亮屏幕
         */
        public Builder setLightScreen(boolean lightScreen) {
            notificationOptions.setLightScreen(lightScreen);
            return this;
        }

        /**
         * 当手机放在口袋或者扣在桌子上时不点亮屏幕
         * 依赖传感器，可能无效
         */
        public Builder setPocketModle(boolean pocketModle) {
            notificationOptions.setPocketModle(pocketModle);
            return this;
        }

        /**
         * 显示时间，默认1500毫秒
         */
        public Builder setDisplayTime(int displayTime) {
            notificationOptions.setDisplayTime(displayTime);
            return this;
        }


        public Builder setTextSize(float textSize) {
            notificationOptions.setTextSize(textSize);
            return this;
        }


        /**
         * 简略模式，只显示一行
         */
        public Builder setCompact(boolean compact) {
            notificationOptions.setCompact(compact);
            return this;
        }

        /**
         * 不显示正在使用应用的消息弹窗,如果为true，必须调用{@link #setPackageName}
         */
        public Builder setBlockCurrentApp(boolean blockCurrentApp) {
            notificationOptions.setBlockCurrentApp(blockCurrentApp);
            return this;
        }

        /**
         * 隐藏通知上的消除按钮
         */
        public Builder setHideDissButton(boolean hideDissButton) {
            notificationOptions.setHideDissButton(hideDissButton);
            return this;
        }

        /**
         * 跳转的acvitivity是否悬浮
         */
        public Builder setFloatWindow(boolean floatWindow) {
            notificationOptions.setFloatWindow(floatWindow);
            return this;
        }

        /**
         * 显示圆角icon
         */
        public Builder setShowRoundIcon(boolean showRoundIcon) {
            notificationOptions.setShowRoundIcon(showRoundIcon);
            return this;
        }

        /**
         * 滑动消失
         */
        public Builder setDismissOnSwip(boolean dismissOnSwip) {
            notificationOptions.setDismissOnSwip(dismissOnSwip);
            return this;
        }

        /**
         * 非透明度（alpah值1-100反序）
         */
        public Builder setAlpha(int alpha) {
            notificationOptions.setAlpha(alpha);
            return this;
        }

        /**
         * 通知点击外部是否可取消
         */
        public Builder setCancleAble(boolean cancleAble) {
            notificationOptions.setCancleAble(cancleAble);
            return this;
        }

        /**
         * 如果在锁屏界面点击通知，将会解锁界面，跳到指定itnent
         */
        public Builder setDismissKeyGuard(boolean dismissKeyGuard) {
            notificationOptions.setDismissKeyGuard(dismissKeyGuard);
            return this;
        }

        /**
         * 暴力解锁，可能跳过锁屏页，可能无效
         */
        public Builder setDismisskeyGuardForce(boolean dismisskeyGuardForce) {
            notificationOptions.setDismisskeyGuardForce(dismisskeyGuardForce);
            return this;
        }

        /**
         * 收到通知后保持屏幕常亮
         */
        public Builder setKeepScreenOn(boolean keepScreenOn) {
            notificationOptions.setKeepScreenOn(keepScreenOn);
            return this;
        }

        public NcEvent build() {

            ncEvent.setNotificationOptions(notificationOptions);

            if (ncEvent.getNotificationOptions() == null) {
                throw new IllegalArgumentException("You must provide a NotificationOptions");
            }

            if (ncEvent.getNcAction() == null) {
                ncEvent.setNcAction(NcAction.STAY);
            }

            if (ncViewWrapper == null) {
                throw new IllegalArgumentException("You must provide a NcViewWrapper");
            }

            if (ncViewWrapper.getContentRes() == 0) {
                ncViewWrapper.setContentRes(R.id.im_notification_subtitle);
            }

            if (ncViewWrapper.getTitleRes() == 0) {
                ncViewWrapper.setTitleRes(R.id.im_notification_title);
            }

            if (ncViewWrapper.getDataId() == 0) {
                ncViewWrapper.setDataId(R.id.im_linearLayout);
            }

            ncEvent.setNcViewWrapper(ncViewWrapper);


            if (notificationOptions.getNofitycationGravity() == null) {
                notificationOptions.setNofitycationGravity(NofitycationGravity.TOP);
            }

            if (notificationOptions.getRootLayout() == 0) {
                notificationOptions.setRootLayout(R.layout.im_activity_read);
            }

            if (notificationOptions.getRootStub() == 0) {
                notificationOptions.setRootStub(R.id.im_viewStub);
            }


            if (notificationOptions.isBlockCurrentApp() && TextUtils.isEmpty(ncEvent.getPackageName())) {
                throw new IllegalArgumentException("If you want to block your app when the notification arrived,please set your app packagename");
            }


            if (ncEvent.getThemeType() == null) {
                ncEvent.setThemeType(ThemeType.HOLOLIGHT);
            }

            return ncEvent;
        }
    }
}
