package com.neuifo.appkit.notification.model;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;

/**
 * Created by neuifo on 2017/9/14.
 */

public class EventPending implements Parcelable {

    @IdRes
    private int clickId;

    @IdRes
    private int clickParentId;


    private String title;

    @DrawableRes
    private int icon;

    private PendingIntent pendingIntent;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.clickId);
        dest.writeInt(this.clickParentId);
        dest.writeString(this.title);
        dest.writeInt(this.icon);
        dest.writeParcelable(this.pendingIntent, flags);
    }

    public EventPending() {
    }

    protected EventPending(Parcel in) {
        this.clickId = in.readInt();
        this.clickParentId = in.readInt();
        this.title = in.readString();
        this.icon = in.readInt();
        this.pendingIntent = in.readParcelable(PendingIntent.class.getClassLoader());
    }

    public static final Creator<EventPending> CREATOR = new Creator<EventPending>() {
        @Override
        public EventPending createFromParcel(Parcel source) {
            return new EventPending(source);
        }

        @Override
        public EventPending[] newArray(int size) {
            return new EventPending[size];
        }
    };


    public int getClickId() {
        return clickId;
    }

    public void setClickId(int clickId) {
        this.clickId = clickId;
    }

    public int getClickParentId() {
        return clickParentId;
    }

    public void setClickParentId(int clickParentId) {
        this.clickParentId = clickParentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    public static Creator<EventPending> getCREATOR() {
        return CREATOR;
    }
}
