package com.jease.pineapple.record.filter;

import android.os.Parcel;
import android.os.Parcelable;

public class Filter implements Parcelable {

    private String mLutPath;

    private int mCoverResId;

    private transient boolean isSelected;

    public Filter() {

    }

    protected Filter(Parcel in) {
        mLutPath = in.readString();
        mCoverResId = in.readInt();
    }

    public static final Creator<Filter> CREATOR = new Creator<Filter>() {
        @Override
        public Filter createFromParcel(Parcel in) {
            return new Filter(in);
        }

        @Override
        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

    public String getLutPath() {
        return mLutPath;
    }

    public void setLutPath(String lutPath) {
        mLutPath = lutPath;
    }

    public int getCoverResId() {
        return mCoverResId;
    }

    public void setCoverResId(int coverResId) {
        mCoverResId = coverResId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLutPath);
        dest.writeInt(mCoverResId);
    }

    @Override
    public String toString() {
        return "Filter{" +
                "mLutPath='" + mLutPath + '\'' +
                ", mCoverResId=" + mCoverResId +
                ", isSelected=" + isSelected +
                '}';
    }
}
