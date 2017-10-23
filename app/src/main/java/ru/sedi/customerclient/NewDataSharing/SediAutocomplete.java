package ru.sedi.customerclient.NewDataSharing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by RAM on 02.06.2017.
 */

public class SediAutocomplete {

    public class Address {
        @SerializedName("n")
        @Expose
        private Integer n;
        @SerializedName("v")
        @Expose
        private String v;
        @SerializedName("c")
        @Expose
        private String c;
        @SerializedName("g")
        @Expose
        private G g;
        @SerializedName("t")
        @Expose
        private String t;

        public Integer getN() {
            return n;
        }

        public void setN(Integer n) {
            this.n = n;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public G getG() {
            return g;
        }

        public void setG(G g) {
            this.g = g;
        }

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }

    }

    public class G {

        @SerializedName("lat")
        @Expose
        private Double lat;
        @SerializedName("lon")
        @Expose
        private Double lon;

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLon() {
            return lon;
        }

        public void setLon(Double lon) {
            this.lon = lon;
        }

    }
}
