package ru.sedi.customerclient.NewDataSharing;

public class SediGeocoderResponse {
    private SediGeocoderPoint[] Addresses;
    private boolean Success;

    public SediGeocoderPoint[] getAddresses() {
        return Addresses;
    }

    public boolean isSuccess() {
        return Success;
    }
}
