package ru.sedi.customerclient.common.TinyBinaryFormatter;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;

public class BinaryConverter
{
	// ---------- ToBinary ----------

	public static byte[] ToBinary(Boolean value)
	{
		return new byte[] { (byte) (value ? 1 : 0) };
	}

	public static byte[] ToBinary(char value)
	{
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putChar(value);
		return bb.array();
	}

	public static byte[] ToBinary(int value)
	{
		byte[] bytes=new byte[4];
		bytes[3] = (byte) (value >>> 24);
		bytes[2] = (byte) (value >>> 16);
		bytes[1] = (byte) (value >>> 8);
		bytes[0] = (byte) value;
		
		return bytes;
	}

	public static byte[] ToBinary(double value)
	{
		long l= Double.doubleToLongBits(value);
		return ToBinary(l);
	}

	public static byte[] ToBinary(float value)
	{
		int i = Float.floatToIntBits(value);
		return ToBinary(i);
	}

	public static byte[] ToBinary(long value)
	{
		byte[] bytes=new byte[8];
		bytes[7] = (byte) (value >>> 56);
		bytes[6] = (byte) (value >>> 48);
		bytes[5] = (byte) (value >>> 40);
		bytes[4] = (byte) (value >>> 32);
		
		bytes[3] = (byte) (value >>> 24);
		bytes[2] = (byte) (value >>> 16);
		bytes[1] = (byte) (value >>> 8);
		bytes[0] = (byte) value;
		
		return bytes;
	}

	public static byte[] ToBinary(short value)
	{
		byte[] bytes=new byte[2];
		bytes[1] = (byte) (value >>> 8);
		bytes[0] = (byte) value;
		
		return bytes;
	}

	public static byte[] ToBinary(String value)
	{
		try
		{
			return value.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
            e.printStackTrace();
        }
		return new byte[0];
	}

	public static byte[] ToBinary(Date value)
	{
		Date date = new Date(100, 0, 1, 0, 0, 0);
        double totalSeconds = value.getTime() - date.getTime();
        return ToBinary(totalSeconds / 1000);
	}

	public static byte[] ToBinary(Enum<?> value)
	{
		return ToBinary(value.toString());
	}

	public static byte[] GetFieldBinary(Object value)
	{
		return GetFieldBinary(value, GetValueType(value));
	}

	public static byte[] GetFieldBinary(Object value, BinaryValueType fieldType)
	{
		if (fieldType == BinaryValueType.Byte)
			return new byte[] { ((Byte) value) };
		if (fieldType == BinaryValueType.Boolean)
			return ToBinary((Boolean) value);
		if (fieldType == BinaryValueType.Short)
			return ToBinary((Short) value);
		if (fieldType == BinaryValueType.Int)
			return ToBinary((Integer) value);
		if (fieldType == BinaryValueType.Long)
			return ToBinary((Long) value);
		if (fieldType == BinaryValueType.Float)
			return ToBinary((Float) value);
		if (fieldType == BinaryValueType.Double)
			return ToBinary((Double) value);
		if (fieldType == BinaryValueType.Char)
			return ToBinary((Character) value);
		if (fieldType == BinaryValueType.String)
			return ToBinary((String) value);
		if (fieldType == BinaryValueType.DateTime)
			return ToBinary((Date) value);

		if (fieldType == BinaryValueType.Enum)
		{
			if (value.getClass() == String.class)
				return ToBinary((String) value);
			return ToBinary((Enum<?>) value);
		}

		return null;
	}

	// ---------- FromBinary ----------

	public static boolean ToBoolean(byte[] value)
	{
		return value[0] == 1 ? true : false;
	}

	public static char ToChar(byte[] value)
	{
		ByteBuffer bb = ByteBuffer.wrap(value);
		return bb.getChar();
	}

	public static int ToInt(byte[] value)
	{
		int result =  ((value[0] & 0xff) +
						(value[1] << 8   & 0xff00)+
						(value[2] << 16  & 0xff0000)+
						(value[3] << 24  & 0xff000000));
		return result;
	}

	public static double ToDouble(byte[] value)
	{
		long l=ToLong(value);
		return Double.longBitsToDouble(l);
	}

	public static float ToFloat(byte[] value)
	{
		int i = ToInt(value);
		return Float.intBitsToFloat(i);
	}

	public static long ToLong(byte[] value)
	{
		long result =  ((long)(value[0]) +
				((long)(value[1] & 255) << 8)+
				((long)(value[2] & 255)<< 16)+
				((long)(value[3] & 255)<< 24)
				+
				((long)(value[4] & 255)<< 32)+
				((long)(value[5] & 255)<< 40)+
				((long)(value[6] & 255)<< 48)+
				((long)(value[7] & 255)<< 56));
		return result;
	}

	public static short ToShort(byte[] value)
	{
		short result = (short) ((value[0] & 0xff) +
								(value[1] << 8   & 0xff00));
		return result;
	}

	public static String ToString(byte[] value)
	{
		try
		{
			return new String(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
           e.printStackTrace();
		}
		return "";
	}

	public static Date ToDateTime(byte[] value)
	{
		double totalSeconds = ToDouble(value);

		Date date = new Date(100, 0, 1, 0, 0, 0);
		return new Date((long) (date.getTime() + totalSeconds * 1000));
	}

	public static Object ToEnum(byte[] value, Class<? extends Enum> enumType)
	{
		String enumText = ToString(value);
		return Enum.valueOf(enumType, enumText);
	}

	public static BinaryValueType GetValueType(Object value)
	{
		Class<?> valueType = value.getClass();

		if (valueType == byte.class || valueType == Byte.class)
			return BinaryValueType.Byte;

		if (valueType == boolean.class || valueType == Boolean.class)
			return BinaryValueType.Boolean;

		if (valueType == short.class || valueType == Short.class)
			return BinaryValueType.Short;

		if (valueType == int.class || valueType == Integer.class)
			return BinaryValueType.Int;

		if (valueType == long.class || valueType == Long.class)
			return BinaryValueType.Long;

		if (valueType == float.class || valueType == Float.class)
			return BinaryValueType.Float;

		if (valueType == double.class || valueType == Double.class)
			return BinaryValueType.Double;

		if (valueType == char.class || valueType == Character.class)
			return BinaryValueType.Char;

		if (valueType == String.class)
			return BinaryValueType.String;

		if (valueType == Date.class)
			return BinaryValueType.DateTime;

		if (valueType.isEnum())
			return BinaryValueType.Enum;

		if (valueType.isArray())
			return BinaryValueType.Array;

		return BinaryValueType.Object;
	}

	public static Object GetFieldFromBinary(byte[] valueBytes, BinaryValueType fieldType)
	{
		if (fieldType == BinaryValueType.Byte)
			return valueBytes[0];
		if (fieldType == BinaryValueType.Boolean)
			return ToBoolean(valueBytes);
		if (fieldType == BinaryValueType.Short)
			return ToShort(valueBytes);
		if (fieldType == BinaryValueType.Int)
			return ToInt(valueBytes);
		if (fieldType == BinaryValueType.Long)
			return ToLong(valueBytes);
		if (fieldType == BinaryValueType.Float)
			return ToFloat(valueBytes);
		if (fieldType == BinaryValueType.Double)
			return ToDouble(valueBytes);
		if (fieldType == BinaryValueType.Char)
			return ToChar(valueBytes);
		if (fieldType == BinaryValueType.String)
			return ToString(valueBytes);
		if (fieldType == BinaryValueType.DateTime)
			return ToDateTime(valueBytes);
		if (fieldType == BinaryValueType.Enum)
			return ToString(valueBytes);
		return null;
	}

}
