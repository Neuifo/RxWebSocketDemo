package com.neuifo.appkit.notification.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

/**
 * Created by neuifo on 2017/9/13.
 */

public class NotificationOptions implements Parcelable {

    /**
     * 通用的根布局，一般默认
     */
    @LayoutRes
    private int rootLayout;
    /**
     * 根布局中的载器,默认即可
     */
    @IdRes
    private int rootStub;

    private NofitycationGravity nofitycationGravity;

    /**
     * 在锁屏界面显示
     */
    private boolean showOnLock = false;
    /**
     * 仅在锁屏界面显示
     */
    private boolean showonLockOnly = false;

    /**
     * 如果不点击，会一只保持在锁屏界面
     */
    private boolean keepOnLockUntilClick = false;

    /**
     * 发送通知点亮屏幕
     */
    private boolean lightScreen = false;
    /**
     * 当手机放在口袋或者扣在桌子上时不点亮屏幕
     */
    private boolean pocketModle;

    /**
     * 显示时间(毫秒)
     */
    private int displayTime = 1500;

    /**
     * 字体放大倍数
     */
    private float textSize;

    /**
     * 简略模式，只显示一行
     */
    private boolean isCompact;

    /**
     * 不显示正在使用应用的消息弹窗
     */
    private boolean blockCurrentApp;

    /**
     * 隐藏通知上的消除按钮
     */
    private boolean hideDissButton;

    /**
     * 跳转的acvitivity是否悬浮
     */
    private boolean floatWindow;

    /**
     * 显示圆角icon
     */
    private boolean showRoundIcon;

    /**
     * 滑动消失
     */
    private boolean dismissOnSwip = true;

    /**
     * 非透明度（alpah值1-100反序）
     */
    private int alpha = 98;

    /**
     * 通知点击外部是否可取消
     */
    private boolean cancleAble = true;
    /**
     * 如果在锁屏界面点击通知，将会解锁界面，跳到指定itnent
     */
    private boolean dismissKeyGuard = true;

    /**
     * 暴力解锁，可能跳过锁屏页，可能无效
     */
    private boolean dismisskeyGuardForce = true;

    /**
     * 收到通知后保持屏幕常亮
     */
    private boolean keepScreenOn = false;

    public int getRootLayout() {
        return rootLayout;
    }

    public void setRootLayout(int rootLayout) {
        this.rootLayout = rootLayout;
    }

    public int getRootStub() {
        return rootStub;
    }

    public void setRootStub(int rootStub) {
        this.rootStub = rootStub;
    }

    public NofitycationGravity getNofitycationGravity() {
        return nofitycationGravity;
    }

    public void setNofitycationGravity(NofitycationGravity nofitycationGravity) {
        this.nofitycationGravity = nofitycationGravity;
    }

    public boolean isShowOnLock() {
        return showOnLock;
    }

    public void setShowOnLock(boolean showOnLock) {
        this.showOnLock = showOnLock;
    }

    public boolean isShowonLockOnly() {
        return showonLockOnly;
    }

    public void setShowonLockOnly(boolean showonLockOnly) {
        this.showonLockOnly = showonLockOnly;
    }

    public boolean isKeepOnLockUntilClick() {
        return keepOnLockUntilClick;
    }

    public void setKeepOnLockUntilClick(boolean keepOnLockUntilClick) {
        this.keepOnLockUntilClick = keepOnLockUntilClick;
    }

    public boolean isLightScreen() {
        return lightScreen;
    }

    public void setLightScreen(boolean lightScreen) {
        this.lightScreen = lightScreen;
    }

    public boolean isPocketModle() {
        return pocketModle;
    }

    public void setPocketModle(boolean pocketModle) {
        this.pocketModle = pocketModle;
    }

    public int getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(int displayTime) {
        this.displayTime = displayTime;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public boolean isCompact() {
        return isCompact;
    }

    public void setCompact(boolean compact) {
        isCompact = compact;
    }

    public boolean isBlockCurrentApp() {
        return blockCurrentApp;
    }

    public void setBlockCurrentApp(boolean blockCurrentApp) {
        this.blockCurrentApp = blockCurrentApp;
    }

    public boolean isHideDissButton() {
        return hideDissButton;
    }

    public void setHideDissButton(boolean hideDissButton) {
        this.hideDissButton = hideDissButton;
    }

    public boolean isFloatWindow() {
        return floatWindow;
    }

    public void setFloatWindow(boolean floatWindow) {
        this.floatWindow = floatWindow;
    }

    public boolean isShowRoundIcon() {
        return showRoundIcon;
    }

    public void setShowRoundIcon(boolean showRoundIcon) {
        this.showRoundIcon = showRoundIcon;
    }

    public boolean isDismissOnSwip() {
        return dismissOnSwip;
    }

    public void setDismissOnSwip(boolean dismissOnSwip) {
        this.dismissOnSwip = dismissOnSwip;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public boolean isCancleAble() {
        return cancleAble;
    }

    public void setCancleAble(boolean cancleAble) {
        this.cancleAble = cancleAble;
    }

    public boolean isDismissKeyGuard() {
        return dismissKeyGuard;
    }

    public void setDismissKeyGuard(boolean dismissKeyGuard) {
        this.dismissKeyGuard = dismissKeyGuard;
    }

    public boolean isDismisskeyGuardForce() {
        return dismisskeyGuardForce;
    }

    public void setDismisskeyGuardForce(boolean dismisskeyGuardForce) {
        this.dismisskeyGuardForce = dismisskeyGuardForce;
    }

    public boolean isKeepScreenOn() {
        return keepScreenOn;
    }

    public void setKeepScreenOn(boolean keepScreenOn) {
        this.keepScreenOn = keepScreenOn;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rootLayout);
        dest.writeInt(this.rootStub);
        dest.writeInt(this.nofitycationGravity == null ? -1 : this.nofitycationGravity.ordinal());
        dest.writeByte(this.showOnLock ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showonLockOnly ? (byte) 1 : (byte) 0);
        dest.writeByte(this.keepOnLockUntilClick ? (byte) 1 : (byte) 0);
        dest.writeByte(this.lightScreen ? (byte) 1 : (byte) 0);
        dest.writeByte(this.pocketModle ? (byte) 1 : (byte) 0);
        dest.writeInt(this.displayTime);
        dest.writeFloat(this.textSize);
        dest.writeByte(this.isCompact ? (byte) 1 : (byte) 0);
        dest.writeByte(this.blockCurrentApp ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hideDissButton ? (byte) 1 : (byte) 0);
        dest.writeByte(this.floatWindow ? (byte) 1 : (byte) 0);
        dest.writeByte(this.showRoundIcon ? (byte) 1 : (byte) 0);
        dest.writeByte(this.dismissOnSwip ? (byte) 1 : (byte) 0);
        dest.writeInt(this.alpha);
        dest.writeByte(this.cancleAble ? (byte) 1 : (byte) 0);
        dest.writeByte(this.dismissKeyGuard ? (byte) 1 : (byte) 0);
        dest.writeByte(this.dismisskeyGuardForce ? (byte) 1 : (byte) 0);
        dest.writeByte(this.keepScreenOn ? (byte) 1 : (byte) 0);
    }

    public NotificationOptions() {
    }

    protected NotificationOptions(Parcel in) {
        this.rootLayout = in.readInt();
        this.rootStub = in.readInt();
        int tmpNofitycationGravity = in.readInt();
        this.nofitycationGravity = tmpNofitycationGravity == -1 ? null
            : NofitycationGravity.values()[tmpNofitycationGravity];
        this.showOnLock = in.readByte() != 0;
        this.showonLockOnly = in.readByte() != 0;
        this.keepOnLockUntilClick = in.readByte() != 0;
        this.lightScreen = in.readByte() != 0;
        this.pocketModle = in.readByte() != 0;
        this.displayTime = in.readInt();
        this.textSize = in.readFloat();
        this.isCompact = in.readByte() != 0;
        this.blockCurrentApp = in.readByte() != 0;
        this.hideDissButton = in.readByte() != 0;
        this.floatWindow = in.readByte() != 0;
        this.showRoundIcon = in.readByte() != 0;
        this.dismissOnSwip = in.readByte() != 0;
        this.alpha = in.readInt();
        this.cancleAble = in.readByte() != 0;
        this.dismissKeyGuard = in.readByte() != 0;
        this.dismisskeyGuardForce = in.readByte() != 0;
        this.keepScreenOn = in.readByte() != 0;
    }

    public static final Creator<NotificationOptions> CREATOR = new Creator<NotificationOptions>() {
        @Override public NotificationOptions createFromParcel(Parcel source) {
            return new NotificationOptions(source);
        }

        @Override public NotificationOptions[] newArray(int size) {
            return new NotificationOptions[size];
        }
    };
}
