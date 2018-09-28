package ru.sedi.customerclient.common.TinyBinaryFormatter;

import java.nio.ByteBuffer;
import java.util.Vector;

public class BinaryArray
{
	// ---------- Fields ----------

	private Vector<Object> m_array = new Vector<Object>();

	private BinaryValueType ArrayType;

	public BinaryValueType GetType()
	{
		return ArrayType;
	}

	public int Count()
	{
		return m_array.size();
	}

	public Object[] Array()
	{
		return m_array.toArray();
	}

	// ---------- Constructors ----------

	public BinaryArray(Object enumerable)
	{
		int lenght = java.lang.reflect.Array.getLength(enumerable);

		for (int i = 0; i < lenght; i++)
		{
			Object value = java.lang.reflect.Array.get(enumerable, i);
			ArrayType = BinaryConverter.GetValueType(value);
			Object v = ArrayType == BinaryValueType.Object ? new BinaryObject(value) : value;
			m_array.add(v);
		}

		if (m_array.size() == 0)
			ArrayType = BinaryValueType.Null;
	}

	public BinaryArray(byte[] binary)
	{
		Deserialize(binary);
	}

	// ---------- Methods ----------

	public byte[] GetBinary()
	{
		Vector<byte[]> buffer = new Vector<byte[]>();
		buffer.add(new byte[] { BinaryValueType.GetIndex(ArrayType) });
		buffer.add(BinaryConverter.ToBinary((short) m_array.size()));

		for (Object item : m_array)
		{
			byte[] binary = GetBinary(item);
			if(!BinaryValueType.IsPrimitive(ArrayType))
				buffer.add(BinaryConverter.ToBinary((int) binary.length));
			buffer.add(binary);
		}
		buffer.trimToSize();

		int size = 0;
		for (int i = 0; i < buffer.size(); i++)
		{
			size += buffer.get(i).length;
		}

		ByteBuffer writer = ByteBuffer.allocate(size);
		for (byte[] item : buffer)
		{
			writer.put(item);
		}

		return writer.array();
	}

	private byte[] GetBinary(Object item)
	{
		if (ArrayType == BinaryValueType.Object)
			return ((BinaryObject) item).GetBinary();

		return BinaryConverter.GetFieldBinary(item);
	}

	private void Deserialize(byte[] bytes)
	{
		ByteBufferWrapper reader = new ByteBufferWrapper(bytes);
		ArrayType = BinaryValueType.GetTypeByIndex(reader.GetByte());
		short countFields = reader.GetShort();

		for (int i = 0; i < countFields; i++)
		{
			int lenghtField = BinaryValueType.IsPrimitive(ArrayType) ? BinaryValueType.PrimitiveSize(ArrayType) : reader.GetInt();
			byte[] fieldBytes =reader.GetBytes(lenghtField);

			m_array.add(GetObject(fieldBytes));
		}
	}

	private Object GetObject(byte[] binary)
	{
		if (ArrayType == BinaryValueType.Object)
			return new BinaryObject(binary);

		return BinaryConverter.GetFieldFromBinary(binary, ArrayType);
	}
}
