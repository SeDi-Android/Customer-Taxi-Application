package ru.sedi.customerclient.common.GeoTools;


import ru.sedi.customerclient.common.LatLong;

public class GeoTools
{

    private static final double RADIAN_FACTOR = Math.PI / 180;

    /**
     * Converts knots to miles per hour
     *
     * @param aKnots
     * @return miles per hour
     */
    public static double knotsToMph(double aKnots)
    {
        return aKnots * 1.151;
    }

    /**
     * Converts knots to kilometers per hour
     *
     * @param aKnots
     * @return kilometers per hour
     */
    public static double knotsToKph(double aKnots)
    {
        return aKnots * 1.852;
    }

    /**
     * Converts meters to feet
     *
     * @param aMetres
     * @return feet
     */
    public static double metersToFeet(double aMetres)
    {
        return aMetres * 3.280839895013122;
    }

    /**
     * Converts meters to inches
     *
     * @param aMetres
     * @return inches
     */
    public static double metersToInches(double aMetres)
    {
        return aMetres * 39.700874015748;
    }

    /**
     * Converts meters to yards
     *
     * @param aMetres
     * @return yards
     */
    public static double metersToYards(double aMetres)
    {
        return aMetres * 1.0936132983377087;
    }

    /**
     * Converts meters to kilometers
     *
     * @param aMetres
     * @return kilometers
     */
    public static double metersToKm(double aMetres)
    {
        return aMetres * 0.001;
    }

    /**
     * Converts meters to centimeter
     *
     * @param aMetres
     * @return centimeter
     */
    public static double metersToCm(double aMetres)
    {
        return aMetres * 100;
    }

    /**
     * Converts meters to millimeters
     *
     * @param aMetres
     * @return millimeters
     */
    public static double metersToMm(double aMetres)
    {
        return aMetres * 1000;
    }

    /**
     * Converts meters to miles
     *
     * @param aMetres
     * @return miles
     */
    public static double metersToMiles(double aMetres)
    {
        return aMetres * 0.0006213711922373347;
    }

    /**
     * Converts kilometers to miles
     *
     * @param aKm
     * @return miles
     */
    public static double kmToMiles(double aKm)
    {
        return aKm * 0.6213711922373347;
    }

    /**
     * Converts nautical miles to meters
     *
     * @param aNm
     * @return meters
     */
    public static double nmToMeters(double aNm)
    {
        return aNm * 1852;
    }

    /**
     * Converts nautical miles to miles
     *
     * @param aNm
     * @return miles
     */
    public static double nmToMiles(double aNm)
    {
        return aNm * 1151;
    }

    public static double calculateDistance(LatLong point1, LatLong point2, Units unit)
    {
        return calculateDistance(point1.Latitude, point1.Longitude, point2.Latitude, point2.Longitude, unit);
    }

    /**
     * Calculate distance between a position and another position (in meters)
     * @return distance
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2, Units unit)
    {
        if ((lat1 == lat2) && (lon1 == lon2)) return 0;

        lat1 *= RADIAN_FACTOR;
        lat2 *= RADIAN_FACTOR;
        lon1 *= RADIAN_FACTOR;
        lon2 *= RADIAN_FACTOR;
        double distance = (60.0 * ((Math.acos((Math.sin(lat1) * Math.sin(lat2)) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)))) / RADIAN_FACTOR));

        switch (unit)
        {
            case Meters:
                return nmToMeters((double) distance);
            case Kilometers:
                return nmToMeters((double) distance) / 1000;
            case Knots:
                return ((double) distance) / 1000;
            case Miles:
                return nmToMiles((double) distance) / 1000;
            default:
                return 0;
        }
    }
}
