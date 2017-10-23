package ru.sedi.customerclient.common.GeoTools;

/**
 * Координата на карте
 * 
 * @author NDN
 * 
 */
public class GeoPoint
{

	private final static double MIN_LATITUDE = -90;
	private final static double MIN_LONGITUDE = -180;
	private final static double MAX_LATITUDE = 90;
	private final static double MAX_LONGITUDE = 180;
	private final static double EPSILON = 1e-5;

	// Широта
	private double mLat;

	// Долгота
	private double mLon;

	public double getLatitude()
	{
		return this.mLat;
	}

	public void setLatitude(double aLat)
	{
		this.mLat = aLat;
	}

	public double getLongitude()
	{
		return this.mLon;
	}

	public void setLongitude(double aLon)
	{
		this.mLon = aLon;
	}

	public GeoPoint()
	{
		this.mLat = MAX_LATITUDE;
		this.mLon = MAX_LONGITUDE;
	}

	public GeoPoint(double aLat, double aLon)
	{
		this.mLat = aLat;
		this.mLon = aLon;
	}

	public GeoPoint(String aLat, String aLon)
	{
		if (aLat != null && aLat.length() > 0 && aLon != null && aLon.length() > 0)
		{
			aLat = aLat.replace(',', '.');
			aLon = aLon.replace(',', '.');
			this.mLat = Double.parseDouble(aLat);
			this.mLon = Double.parseDouble(aLon);
		}
		else
		{
			this.mLat = 200;
			this.mLon = 200;
		}

	}

	public GeoPoint(GeoPoint aPoint)
	{
		this.mLat = aPoint.getLatitude();
		this.mLon = aPoint.getLongitude();
	}

	/**
	 * Возвращает объект с заранее неверными координатами.
	 * 
	 * @return
	 */
	public static GeoPoint getInvalid()
	{
		return new GeoPoint(200, 200);
	}

	/**
	 * Верность координат
	 */
	public boolean isValid()
	{
		return Math.abs(this.mLat) < MAX_LATITUDE && Math.abs(this.mLon) < MAX_LONGITUDE && !equals(new GeoPoint(0, 0)) && !equals(new GeoPoint(-1, -1));
	}

	@Override
	public boolean equals(Object aObj)
	{
		if (aObj == null) return false;
		if (this == aObj) return true;
		if (aObj.getClass() != GeoPoint.class) return false;
		return equals((GeoPoint) aObj);
	}

	/**
	 * 
	 * @param aObj
	 * @return
	 */
	public boolean equals(GeoPoint aObj)
	{
		if (aObj == null) return false;
		if (aObj == this) return true;
		return Math.abs(aObj.getLatitude() - this.mLat) < EPSILON && Math.abs(aObj.getLongitude() - this.mLon) < EPSILON;
	}
}
