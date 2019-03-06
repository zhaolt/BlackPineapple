package com.jease.pineapple.media.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable {

    protected String mId;

    protected String mFilePath;

    protected long mDate;

    public Media() {}

    protected Media(Parcel in) {
        mId = in.readString();
        mFilePath = in.readString();
        mDate = in.readLong();
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mFilePath);
        dest.writeLong(mDate);
    }
}
