package com.jease.pineapple.record.photo;

import android.graphics.Bitmap;
import android.graphics.Point;

public class PhotoMarker {

    private PhotoMarker(Builder builder) {

    }


    public final class Builder {
        private String path;
        private Bitmap lookup;
        private Point resolution;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder filter(Bitmap lookup) {
            this.lookup = lookup;
            return this;
        }

        public Builder resolution(Point p) {
            resolution = p;
            return this;
        }

        public PhotoMarker build() {
            return new PhotoMarker(this);
        }
    }
}
