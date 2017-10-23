package ru.sedi.customerclient.NewDataSharing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by RAM on 05.06.2017.
 */

public class YandexPointWrapper {

    public class Address {

        @SerializedName("country_code")
        @Expose
        private String countryCode;
        @SerializedName("formatted")
        @Expose
        private String formatted;
        @SerializedName("Components")
        @Expose
        private List<Component> components = null;

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getFormatted() {
            return formatted;
        }

        public void setFormatted(String formatted) {
            this.formatted = formatted;
        }

        public List<Component> getComponents() {
            return components;
        }

        public void setComponents(List<Component> components) {
            this.components = components;
        }

    }

    public class AddressDetails {

        @SerializedName("Country")
        @Expose
        private Country country;

        public Country getCountry() {
            return country;
        }

        public void setCountry(Country country) {
            this.country = country;
        }

    }

    public class AdministrativeArea {

        @SerializedName("AdministrativeAreaName")
        @Expose
        private String administrativeAreaName;
        @SerializedName("Locality")
        @Expose
        private Locality locality;
        @SerializedName("SubAdministrativeArea")
        @Expose
        private SubAdministrativeArea subAdministrativeArea;

        public String getAdministrativeAreaName() {
            return administrativeAreaName;
        }

        public void setAdministrativeAreaName(String administrativeAreaName) {
            this.administrativeAreaName = administrativeAreaName;
        }

        public Locality getLocality() {
            return locality;
        }

        public void setLocality(Locality locality) {
            this.locality = locality;
        }

        public SubAdministrativeArea getSubAdministrativeArea() {
            return subAdministrativeArea;
        }

        public void setSubAdministrativeArea(SubAdministrativeArea subAdministrativeArea) {
            this.subAdministrativeArea = subAdministrativeArea;
        }

    }

    public class BoundedBy {

        @SerializedName("Envelope")
        @Expose
        private Envelope envelope;

        public Envelope getEnvelope() {
            return envelope;
        }

        public void setEnvelope(Envelope envelope) {
            this.envelope = envelope;
        }

    }

    public class Component {

        @SerializedName("kind")
        @Expose
        private String kind;
        @SerializedName("name")
        @Expose
        private String name;

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public class Country {

        @SerializedName("AddressLine")
        @Expose
        private String addressLine;
        @SerializedName("CountryNameCode")
        @Expose
        private String countryNameCode;
        @SerializedName("CountryName")
        @Expose
        private String countryName;
        @SerializedName("AdministrativeArea")
        @Expose
        private AdministrativeArea administrativeArea;

        public String getAddressLine() {
            return addressLine;
        }

        public void setAddressLine(String addressLine) {
            this.addressLine = addressLine;
        }

        public String getCountryNameCode() {
            return countryNameCode;
        }

        public void setCountryNameCode(String countryNameCode) {
            this.countryNameCode = countryNameCode;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public AdministrativeArea getAdministrativeArea() {
            return administrativeArea;
        }

        public void setAdministrativeArea(AdministrativeArea administrativeArea) {
            this.administrativeArea = administrativeArea;
        }

    }

    public class Envelope {

        @SerializedName("lowerCorner")
        @Expose
        private String lowerCorner;
        @SerializedName("upperCorner")
        @Expose
        private String upperCorner;

        public String getLowerCorner() {
            return lowerCorner;
        }

        public void setLowerCorner(String lowerCorner) {
            this.lowerCorner = lowerCorner;
        }

        public String getUpperCorner() {
            return upperCorner;
        }

        public void setUpperCorner(String upperCorner) {
            this.upperCorner = upperCorner;
        }

    }

    public class FeatureMember {

        @SerializedName("GeoObject")
        @Expose
        private GeoObject geoObject;

        public GeoObject getGeoObject() {
            return geoObject;
        }

        public void setGeoObject(GeoObject geoObject) {
            this.geoObject = geoObject;
        }

    }

    public class GeoObject {

        @SerializedName("metaDataProperty")
        @Expose
        private MetaDataProperty_ metaDataProperty;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("boundedBy")
        @Expose
        private BoundedBy boundedBy;
        @SerializedName("Point")
        @Expose
        private Point point;

        public MetaDataProperty_ getMetaDataProperty() {
            return metaDataProperty;
        }

        public void setMetaDataProperty(MetaDataProperty_ metaDataProperty) {
            this.metaDataProperty = metaDataProperty;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BoundedBy getBoundedBy() {
            return boundedBy;
        }

        public void setBoundedBy(BoundedBy boundedBy) {
            this.boundedBy = boundedBy;
        }

        public Point getPoint() {
            return point;
        }

        public void setPoint(Point point) {
            this.point = point;
        }

    }

    public class GeoObjectCollection {

        @SerializedName("metaDataProperty")
        @Expose
        private MetaDataProperty metaDataProperty;
        @SerializedName("featureMember")
        @Expose
        private List<FeatureMember> featureMember = null;

        public MetaDataProperty getMetaDataProperty() {
            return metaDataProperty;
        }

        public void setMetaDataProperty(MetaDataProperty metaDataProperty) {
            this.metaDataProperty = metaDataProperty;
        }

        public List<FeatureMember> getFeatureMember() {
            return featureMember;
        }

        public void setFeatureMember(List<FeatureMember> featureMember) {
            this.featureMember = featureMember;
        }

    }

    public class GeocoderMetaData {

        @SerializedName("kind")
        @Expose
        private String kind;
        @SerializedName("text")
        @Expose
        private String text;
        @SerializedName("precision")
        @Expose
        private String precision;
        @SerializedName("Address")
        @Expose
        private Address address;
        @SerializedName("AddressDetails")
        @Expose
        private AddressDetails addressDetails;
        @SerializedName("formerName")
        @Expose
        private String formerName;

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getPrecision() {
            return precision;
        }

        public void setPrecision(String precision) {
            this.precision = precision;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public AddressDetails getAddressDetails() {
            return addressDetails;
        }

        public void setAddressDetails(AddressDetails addressDetails) {
            this.addressDetails = addressDetails;
        }

        public String getFormerName() {
            return formerName;
        }

        public void setFormerName(String formerName) {
            this.formerName = formerName;
        }

    }

    public class GeocoderResponseMetaData {

        @SerializedName("request")
        @Expose
        private String request;
        @SerializedName("found")
        @Expose
        private String found;
        @SerializedName("results")
        @Expose
        private String results;

        public String getRequest() {
            return request;
        }

        public void setRequest(String request) {
            this.request = request;
        }

        public String getFound() {
            return found;
        }

        public void setFound(String found) {
            this.found = found;
        }

        public String getResults() {
            return results;
        }

        public void setResults(String results) {
            this.results = results;
        }

    }

    public class Locality {

        @SerializedName("LocalityName")
        @Expose
        private String localityName;
        @SerializedName("Thoroughfare")
        @Expose
        private Thoroughfare thoroughfare;

        public String getLocalityName() {
            return localityName;
        }

        public void setLocalityName(String localityName) {
            this.localityName = localityName;
        }

        public Thoroughfare getThoroughfare() {
            return thoroughfare;
        }

        public void setThoroughfare(Thoroughfare thoroughfare) {
            this.thoroughfare = thoroughfare;
        }

    }

    public class Locality_ {

        @SerializedName("LocalityName")
        @Expose
        private String localityName;
        @SerializedName("Thoroughfare")
        @Expose
        private Thoroughfare_ thoroughfare;

        public String getLocalityName() {
            return localityName;
        }

        public void setLocalityName(String localityName) {
            this.localityName = localityName;
        }

        public Thoroughfare_ getThoroughfare() {
            return thoroughfare;
        }

        public void setThoroughfare(Thoroughfare_ thoroughfare) {
            this.thoroughfare = thoroughfare;
        }

    }

    public class MetaDataProperty {

        @SerializedName("GeocoderResponseMetaData")
        @Expose
        private GeocoderResponseMetaData geocoderResponseMetaData;

        public GeocoderResponseMetaData getGeocoderResponseMetaData() {
            return geocoderResponseMetaData;
        }

        public void setGeocoderResponseMetaData(GeocoderResponseMetaData geocoderResponseMetaData) {
            this.geocoderResponseMetaData = geocoderResponseMetaData;
        }

    }


    public class MetaDataProperty_ {

        @SerializedName("GeocoderMetaData")
        @Expose
        private GeocoderMetaData geocoderMetaData;

        public GeocoderMetaData getGeocoderMetaData() {
            return geocoderMetaData;
        }

        public void setGeocoderMetaData(GeocoderMetaData geocoderMetaData) {
            this.geocoderMetaData = geocoderMetaData;
        }

    }

    public class Point {

        @SerializedName("pos")
        @Expose
        private String pos;

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

    }

    public class Premise {

        @SerializedName("PremiseName")
        @Expose
        private String premiseName;

        public String getPremiseName() {
            return premiseName;
        }

        public void setPremiseName(String premiseName) {
            this.premiseName = premiseName;
        }

    }

    public class Response {

        @SerializedName("GeoObjectCollection")
        @Expose
        private GeoObjectCollection geoObjectCollection;

        public GeoObjectCollection getGeoObjectCollection() {
            return geoObjectCollection;
        }

        public void setGeoObjectCollection(GeoObjectCollection geoObjectCollection) {
            this.geoObjectCollection = geoObjectCollection;
        }

    }

    public class SubAdministrativeArea {

        @SerializedName("SubAdministrativeAreaName")
        @Expose
        private String subAdministrativeAreaName;
        @SerializedName("Locality")
        @Expose
        private Locality_ locality;

        public String getSubAdministrativeAreaName() {
            return subAdministrativeAreaName;
        }

        public void setSubAdministrativeAreaName(String subAdministrativeAreaName) {
            this.subAdministrativeAreaName = subAdministrativeAreaName;
        }

        public Locality_ getLocality() {
            return locality;
        }

        public void setLocality(Locality_ locality) {
            this.locality = locality;
        }

    }

    public class Thoroughfare {

        @SerializedName("ThoroughfareName")
        @Expose
        private String thoroughfareName;
        @SerializedName("Premise")
        @Expose
        private Premise premise;

        public String getThoroughfareName() {
            return thoroughfareName;
        }

        public void setThoroughfareName(String thoroughfareName) {
            this.thoroughfareName = thoroughfareName;
        }

        public Premise getPremise() {
            return premise;
        }

        public void setPremise(Premise premise) {
            this.premise = premise;
        }

    }

    public class Thoroughfare_ {

        @SerializedName("ThoroughfareName")
        @Expose
        private String thoroughfareName;

        public String getThoroughfareName() {
            return thoroughfareName;
        }

        public void setThoroughfareName(String thoroughfareName) {
            this.thoroughfareName = thoroughfareName;
        }

    }


    public class YandexPoint {

        @SerializedName("response")
        @Expose
        private Response response;

        public Response getResponse() {
            return response;
        }

        public void setResponse(Response response) {
            this.response = response;
        }

    }
}
