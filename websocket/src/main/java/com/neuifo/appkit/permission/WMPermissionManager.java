/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package com.neuifo.appkit.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import com.neuifo.appkit.notification.model.NotificationHelper;
import com.neuifo.appkit.permission.rom.HuaweiUtils;
import com.neuifo.appkit.permission.rom.MeizuUtils;
import com.neuifo.appkit.permission.rom.MiuiUtils;
import com.neuifo.appkit.permission.rom.QikuUtils;
import com.neuifo.appkit.permission.rom.RomUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-10-17
 */

public class WMPermissionManager {

    private static final String TAG = "FloatWindowManager";

    private static Dialog dialog;

    private static Map<Activity, Boolean> activityBooleanMap = new HashMap<>();


    /*  public void openOverlaySettings() {
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        } catch (ActivityNotFoundException e) {
            openAppSettings();
        }
    }

    public void openAppSettings() {
        try {
            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
        }
    }*/

    public static boolean checkServicePerimssion(final Activity context) {

        if (!activityBooleanMap.containsKey(context)) {
            activityBooleanMap.put(context, true);
        }

        if ((Build.VERSION.SDK_INT >= 18 && NotificationHelper.isNotificationListenerEnabled(context)) || NotificationHelper.isAccessibilityEnabled(context)) {
            checkWindowManagerPermission(context);
        } else {
            showConfirmDialog(context, "请给予应用足够的权限", new OnConfirmResult() {
                @Override
                public void confirmResult(boolean confirm) {
                    if (confirm) {
                        toggleService(context);
                    } else {
                        context.finish();
                        activityBooleanMap.put(context, false);
                    }
                }
            });
        }

        return (Build.VERSION.SDK_INT >= 18 && NotificationHelper.isNotificationListenerEnabled(context)) || NotificationHelper.isAccessibilityEnabled(context);
    }


    private static void toggleService(Context context) {
        if (Build.VERSION.SDK_INT >= 18) {
            NotificationHelper.gotoNotifyservice(context);
        } else {
            NotificationHelper.gotoAccessibility(context);
        }
    }


    public static void checkWindowManagerPermission(Activity context) {
        try {
            if (checkPermission(context)) {

            } else {
                applyPermission(context);
            }
        } catch (Exception e) {
        }
    }


    public static boolean checkPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    private static boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private static boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }

    private static boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }

    private static boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }

    private static boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }

    public static void applyPermission(Activity context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            }
        }
        commonROMPermissionApply(context);
    }

    private static void ROM360PermissionApply(final Activity context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    QikuUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:360, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private static void huaweiROMPermissionApply(final Activity context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    HuaweiUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:huawei, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private static void meizuROMPermissionApply(final Activity context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MeizuUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:meizu, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private static void miuiROMPermissionApply(final Activity context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MiuiUtils.applyMiuiPermission(context);
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    /**
     * 通用 rom 权限申请
     */
    private static void commonROMPermissionApply(final Activity context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showConfirmDialog(context, new OnConfirmResult() {
                    @Override
                    public void confirmResult(boolean confirm) {
                        if (confirm) {
                            try {
                                Class clazz = Settings.class;
                                Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");

                                Intent intent = new Intent(field.get(null).toString());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                context.startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                        } else {
                            Log.d(TAG, "user manually refuse OVERLAY_PERMISSION");
                            //需要做统计效果
                        }
                    }
                });
            }
        }
    }

    private static void showConfirmDialog(Activity context, OnConfirmResult result) {
        showConfirmDialog(context, "您的手机没有授予悬浮窗权限，请开启后再试", result);
    }

    public static void showConfirmDialog(final Activity context, String message, final OnConfirmResult result) {

        if (!activityBooleanMap.containsKey(context)) {
            activityBooleanMap.put(context, true);
        }

        if (!activityBooleanMap.get(context)) {//已经销毁
            return;
        }

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle("").setMessage(message).setPositiveButton("现在去开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                result.confirmResult(true);
            }
        }).setNegativeButton("暂不开启", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                context.finish();
                activityBooleanMap.put(context, true);
                result.confirmResult(false);

            }
        }).setCancelable(false).create();
        dialog.show();
    }

    public interface OnConfirmResult {
        void confirmResult(boolean confirm);
    }

}
