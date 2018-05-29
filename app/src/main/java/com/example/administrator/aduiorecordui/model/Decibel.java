package com.example.administrator.aduiorecordui.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ClassName: Decibel
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-05-15  13:45
 */
public class Decibel implements Parcelable {

    public float percent;

    public Decibel(float percent) {
        this.percent = percent;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.percent);
    }

    private Decibel(Parcel in) {
        this.percent = in.readFloat();
    }

    public static final Creator<Decibel> CREATOR = new Creator<Decibel>() {
        @Override
        public Decibel createFromParcel(Parcel source) {
            return new Decibel(source);
        }

        @Override
        public Decibel[] newArray(int size) {
            return new Decibel[size];
        }
    };
}
