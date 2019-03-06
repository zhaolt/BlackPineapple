package com.jease.pineapple.media.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Image extends Media implements Parcelable {


    private double mLongitude;

    private double mLatitude;

    private int mWidth;

    private int mHeight;

    public Image() {}

    protected Image(Parcel in) {
        super(in);
        mLongitude = in.readDouble();
        mLatitude = in.readDouble();
        mWidth = in.readInt();
        mHeight = in.readInt();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(mLongitude);
        dest.writeDouble(mLatitude);
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }
}
