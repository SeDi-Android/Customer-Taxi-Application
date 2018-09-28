package ru.sedi.customerclient.NewDataSharing;

public class CostCalculationResult {
    private double RouteDistance;
    private double RouteDuration;
    private _Tariff Tariff;
    private String ApiKey;
    private boolean Success;

    public double getRouteDistance() {
        return RouteDistance;
    }

    public double getRouteDuration() {
        return RouteDuration;
    }

    public _Tariff getTariff() {
        return Tariff;
    }

    public String getApiKey() {
        return ApiKey;
    }

    public boolean isSuccess() {
        return Success;
    }
}