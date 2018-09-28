package ru.sedi.customerclient.common.TinyBinaryFormatter;


import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class TinyFormatter
{
	public static byte[] Serialize(Object obj)
	{
		BinaryObject customObject = new BinaryObject(obj);

		return customObject.GetBinary();
	}

	/*
	 * public static T Deserialize<T>(byte[] bytes) { BinaryObject customObject
	 * = new BinaryObject(bytes); return (T)CreateObject(typeof(T),
	 * customObject); } public static T Deserialize<T>(BinaryObject
	 * binaryObject) //where T : new() { return (T)CreateObject(typeof(T),
	 * binaryObject); } public static T Deserialize<T>(BinaryArray binaryArray)
	 * { return (T)CreateArray(typeof(T),binaryArray); }
	 */

	public static Object Deserialize(byte[] bytes, Class<?> type)
	{
        if(bytes.length <= 0) return null;
		BinaryObject customObject = new BinaryObject(bytes);
		return CreateObject(type, customObject);
	}

	public static Object Deserialize(BinaryObject binaryObject, Class<?> type)
	{
		return CreateObject(type, binaryObject);
	}

	public static Object Deserialize(BinaryArray binaryArray, Class<?> type)
	{
		return CreateArray(type, binaryArray);
	}

	private static Object CreateObject(Class<?> objectType, BinaryObject customObject)
	{
		try
		{
			Object instance = objectType.newInstance();

			Field[] properties = objectType.getDeclaredFields();

			for (Field property : properties)
			{
                property.setAccessible(true);
				BinaryField field = GetFieldByName(customObject, property.getName());
				if (field == null)
					continue;

				if (property.getType() == Object.class)
				{
					property.set(instance, field.Value);
					continue;
				}

				Object value = GetValue(field.Value, field.Type, property.getType());
				property.set(instance, value);
			}

			return instance;
		}
		catch (Exception e)
		{
            e.printStackTrace();
            return null;
		}
	}

	private static BinaryField GetFieldByName(BinaryObject binaryObject, String name)
	{
		for (BinaryField field : binaryObject.GetFields())
			if (field.Name.equals(name))
				return field;

		return null;
	}

	private static Object CreateArray(Class<?> arrayType, BinaryArray binaryArray)
	{
		if (binaryArray.GetType() == BinaryValueType.Null)
			return null;
		if (arrayType.isArray())
		{
			Class<?> elementType = arrayType.getComponentType();
			Object[] array = new Object[binaryArray.Count()];
			for (int i = 0; i < binaryArray.Count(); i++)
			{
				array[i] = GetValue(binaryArray.Array()[i], binaryArray.GetType(), elementType);
			}
			return ToArray(array, elementType);
		}

		return null;
	}

	public static Object ToArray(Object[] initial, Class<?> type)
	{
		Object array = Array.newInstance(type, initial.length);
		for (int i = 0; i < initial.length; i++)
			Array.set(array, i, initial[i]);
		return array;
	}

	private static Object GetValue(Object value, BinaryValueType valueType, Class<?> type)
	{
		if (valueType == BinaryValueType.Object)
			return CreateObject(type, (BinaryObject) value);

		if (valueType == BinaryValueType.Array)
			return CreateArray(type, (BinaryArray) value);

		if (valueType == BinaryValueType.Enum && type.isEnum())
			return GetEnumByName(type, value.toString());

		return value;
	}

	public static Object GetEnumByName(Class<?> type, String name)
	{
		for (Object en : type.getEnumConstants())
			if (en.toString().equals(name))
				return en;
		return null;
	}
}
