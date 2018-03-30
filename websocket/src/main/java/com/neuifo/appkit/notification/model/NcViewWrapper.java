package com.neuifo.appkit.notification.model;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neuifo on 2017/9/14.
 */

public class NcViewWrapper implements Parcelable {


    protected NcViewWrapper(){}

    @LayoutRes
    private int rootId;

    @IdRes
    private int dataId;

    @ColorInt
    private int color = android.R.color.white;

    @IdRes
    private int titleRes;

    @IdRes
    private int subTitleRes;

    @IdRes
    private int contentRes;

    @IdRes
    private int timeRes;

    @IdRes
    private int dissmissButtonRes;

    private String title;
    private String content;

    /**
     * 可能需要请求网络
     */
    private Bitmap icon;

    @DrawableRes
    private int subIcon;

    private List<EventPending> eventPendingList;

    private PendingIntent basePending;

    public int getRootId() {
        return rootId;
    }

    public void setRootId(int rootId) {
        this.rootId = rootId;
    }

    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public void setTitleRes(int titleRes) {
        this.titleRes = titleRes;
    }

    public int getSubTitleRes() {
        return subTitleRes;
    }

    public void setSubTitleRes(int subTitleRes) {
        this.subTitleRes = subTitleRes;
    }

    public int getContentRes() {
        return contentRes;
    }

    public void setContentRes(int contentRes) {
        this.contentRes = contentRes;
    }

    public int getTimeRes() {
        return timeRes;
    }

    public void setTimeRes(int timeRes) {
        this.timeRes = timeRes;
    }

    public int getDissmissButtonRes() {
        return dissmissButtonRes;
    }

    public void setDissmissButtonRes(int dissmissButtonRes) {
        this.dissmissButtonRes = dissmissButtonRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public int getSubIcon() {
        return subIcon;
    }

    public void setSubIcon(int subIcon) {
        this.subIcon = subIcon;
    }

    public List<EventPending> getEventPendingList() {
        return eventPendingList;
    }

    public void setEventPendingList(List<EventPending> eventPendingList) {
        this.eventPendingList = eventPendingList;
    }

    public PendingIntent getBasePending() {
        return basePending;
    }

    public void setBasePending(PendingIntent basePending) {
        this.basePending = basePending;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rootId);
        dest.writeInt(this.dataId);
        dest.writeInt(this.color);
        dest.writeInt(this.titleRes);
        dest.writeInt(this.subTitleRes);
        dest.writeInt(this.contentRes);
        dest.writeInt(this.timeRes);
        dest.writeInt(this.dissmissButtonRes);
        dest.writeString(this.title);
        dest.writeString(this.content);
        dest.writeParcelable(this.icon, flags);
        dest.writeInt(this.subIcon);
        dest.writeList(this.eventPendingList);
        dest.writeParcelable(this.basePending, flags);
    }

    protected NcViewWrapper(Parcel in) {
        this.rootId = in.readInt();
        this.dataId = in.readInt();
        this.color = in.readInt();
        this.titleRes = in.readInt();
        this.subTitleRes = in.readInt();
        this.contentRes = in.readInt();
        this.timeRes = in.readInt();
        this.dissmissButtonRes = in.readInt();
        this.title = in.readString();
        this.content = in.readString();
        this.icon = in.readParcelable(Bitmap.class.getClassLoader());
        this.subIcon = in.readInt();
        this.eventPendingList = new ArrayList<EventPending>();
        in.readList(this.eventPendingList, EventPending.class.getClassLoader());
        this.basePending = in.readParcelable(PendingIntent.class.getClassLoader());
    }

    public static final Creator<NcViewWrapper> CREATOR = new Creator<NcViewWrapper>() {
        @Override
        public NcViewWrapper createFromParcel(Parcel source) {
            return new NcViewWrapper(source);
        }

        @Override
        public NcViewWrapper[] newArray(int size) {
            return new NcViewWrapper[size];
        }
    };
}
