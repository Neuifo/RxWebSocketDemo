package com.neuifo.appkit.notification.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.neuifo.appkit.notification.theme.ThemeType;

/**
 * Created by neuifo on 2017/9/14.
 */

public class NcEvent implements Parcelable {

    protected NcEvent() {
    }

    private NcAction ncAction;
    private NcViewWrapper ncViewWrapper;

    private ThemeType themeType;

    private String packageName = "";
    private int id;
    private String tag = "";

    private NotificationOptions notificationOptions;


    public NcAction getNcAction() {
        return ncAction;
    }

    public void setNcAction(NcAction ncAction) {
        this.ncAction = ncAction;
    }

    public NcViewWrapper getNcViewWrapper() {
        return ncViewWrapper;
    }

    public void setNcViewWrapper(NcViewWrapper ncViewWrapper) {
        this.ncViewWrapper = ncViewWrapper;
    }

    public ThemeType getThemeType() {
        return themeType;
    }

    public void setThemeType(ThemeType themeType) {
        this.themeType = themeType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public NotificationOptions getNotificationOptions() {
        return notificationOptions;
    }

    public void setNotificationOptions(NotificationOptions notificationOptions) {
        this.notificationOptions = notificationOptions;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ncAction == null ? -1 : this.ncAction.ordinal());
        dest.writeParcelable(this.ncViewWrapper, flags);
        dest.writeInt(this.themeType == null ? -1 : this.themeType.ordinal());
        dest.writeString(this.packageName);
        dest.writeInt(this.id);
        dest.writeString(this.tag);
        dest.writeParcelable(this.notificationOptions, flags);
    }

    protected NcEvent(Parcel in) {
        int tmpNcAction = in.readInt();
        this.ncAction = tmpNcAction == -1 ? null : NcAction.values()[tmpNcAction];
        this.ncViewWrapper = in.readParcelable(NcViewWrapper.class.getClassLoader());
        int tmpThemeType = in.readInt();
        this.themeType = tmpThemeType == -1 ? null : ThemeType.values()[tmpThemeType];
        this.packageName = in.readString();
        this.id = in.readInt();
        this.tag = in.readString();
        this.notificationOptions = in.readParcelable(NotificationOptions.class.getClassLoader());
    }

    public static final Creator<NcEvent> CREATOR = new Creator<NcEvent>() {
        @Override
        public NcEvent createFromParcel(Parcel source) {
            return new NcEvent(source);
        }

        @Override
        public NcEvent[] newArray(int size) {
            return new NcEvent[size];
        }
    };
}
