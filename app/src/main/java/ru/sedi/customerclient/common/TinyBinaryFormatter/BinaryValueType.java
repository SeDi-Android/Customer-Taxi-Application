package ru.sedi.customerclient.common.TinyBinaryFormatter;

public enum BinaryValueType
{
    Byte,
    Boolean,
    Short,
    Int,
    Long,
    Float,
    Double,
    Char,
    String,
    DateTime,
    Enum,
    Object,
    Array,
    Null;
    
	public static byte GetIndex(BinaryValueType type)
	{
		BinaryValueType[] types = BinaryValueType.values();
		for (int i = 0; i < types.length; i++)
			if (types[i] == type)
				return (byte) i;
		return 0;
	}
	
	public static BinaryValueType GetTypeByIndex(byte index)
	{
		BinaryValueType[] types = BinaryValueType.values();
		return types[index];
	}

	public static boolean IsPrimitive(BinaryValueType type)
	{
		return  type == BinaryValueType.Byte || 
				type == BinaryValueType.Boolean || 
				type == BinaryValueType.Short || 
				type == BinaryValueType.Int || 
				type == BinaryValueType.Long ||
				type == BinaryValueType.Float || 
				type == BinaryValueType.Double || 
				type == BinaryValueType.Char || 
				type == BinaryValueType.DateTime;
	}
	
	public static int PrimitiveSize(BinaryValueType type)
    {
		if(type == BinaryValueType.Byte)
          return 1;
		if(type == BinaryValueType.Boolean)
          return 1;
		if(type == BinaryValueType.Short)
          return 2;
		if(type == BinaryValueType.Int)
          return 4;
		if(type == BinaryValueType.Long)
          return 8;
		if(type == BinaryValueType.Float)
          return 4;
		if(type == BinaryValueType.Double)
          return 8;
		if(type == BinaryValueType.Char)
          return 2;
		if(type == BinaryValueType.DateTime)
          return 8;
     
		return 0;
    }
}