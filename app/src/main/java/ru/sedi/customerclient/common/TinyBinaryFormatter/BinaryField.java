package ru.sedi.customerclient.common.TinyBinaryFormatter;

import java.nio.ByteBuffer;

public class BinaryField
{
	// ---------- Fields ----------

	public String Name;
	public Object Value;
	public BinaryValueType Type;

	// ---------- Constructors ----------

	public BinaryField(String name, Object value)
	{
		Name = name;
		Type = BinaryConverter.GetValueType(value);

		if (Type == BinaryValueType.Object)
			Value = new BinaryObject(value);
		else if (Type == BinaryValueType.Array)
			Value = new BinaryArray(value);
		else
			Value = value;
	}

	public BinaryField(byte[] bytes)
	{
		Deserialize(bytes);
	}

	// ---------- Methods ----------

	public byte[] GetBinary()
	{
		byte[] namebytes = BinaryConverter.ToBinary(Name);
		byte nameLenght = (byte) namebytes.length;
		byte typeValue = BinaryValueType.GetIndex(Type);

		byte[] valueBytes = null;

		if (Type == BinaryValueType.Object)
			valueBytes = ((BinaryObject) Value).GetBinary();
		else if (Type == BinaryValueType.Array)
			valueBytes = ((BinaryArray) Value).GetBinary();
		else
			valueBytes = BinaryConverter.GetFieldBinary(Value, Type);

		ByteBuffer writer = ByteBuffer.allocate(nameLenght + 1 + 1 + valueBytes.length);
		writer.put(nameLenght);
		writer.put(namebytes);
		writer.put(typeValue);
		writer.put(valueBytes);

		return writer.array();
	}

	private void Deserialize(byte[] bytes)
	{
		ByteBufferWrapper reader =new ByteBufferWrapper(bytes);
		byte nameLenght = reader.GetByte();
		byte[] nameBytes = reader.GetBytes(nameLenght);

		Name = BinaryConverter.ToString(nameBytes);

		Type = BinaryValueType.GetTypeByIndex(reader.GetByte());

		byte[] valueBytes = reader.GetBytes(bytes.length - reader.Position());

		if (Type == BinaryValueType.Object)
			Value = new BinaryObject(valueBytes);
		else if (Type == BinaryValueType.Array)
			Value = new BinaryArray(valueBytes);
		else
			Value = BinaryConverter.GetFieldFromBinary(valueBytes, Type);
	}

}
