package ru.sedi.customerclient.common.TinyBinaryFormatter;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Vector;

public class BinaryObject
{
	// ---------- Fields ----------

	private Vector<BinaryField> m_fields = new Vector<BinaryField>();

	public BinaryField[] GetFields()
	{
		return (BinaryField[]) m_fields.toArray(new BinaryField[m_fields.size()]);
	}

	// ---------- Constructors ----------

	public BinaryObject(byte[] bytes)
	{
		Deserialize(bytes);
	}

	public BinaryObject(Object obj)
	{
		Field[] fields = obj.getClass().getDeclaredFields();

		for (Field field : fields)
		{
            IgnoreSerializationAttribute parametersInfo = field.getAnnotation(IgnoreSerializationAttribute.class);
            if(parametersInfo!=null)
                continue;

            field.setAccessible(true);
			Object value = null;
			try
			{
				value = field.get(obj);
			}
			catch (Exception e)
			{
                e.printStackTrace();
            }
			if (value == null)
				continue;

			BinaryField f = new BinaryField(field.getName(), value);
			m_fields.add(f);
		}
	}

	// ---------- Methods ----------

	public byte[] GetBinary()
	{
		Vector<byte[]> buffer = new Vector<byte[]>();
		buffer.add(BinaryConverter.ToBinary((short) m_fields.size()));

		for (BinaryField field : m_fields)
		{
			byte[] binary = field.GetBinary();
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

	private void Deserialize(byte[] bytes)
	{
		ByteBufferWrapper reader = new ByteBufferWrapper(bytes);
		short countFields = reader.GetShort();

		for (int i = 0; i < countFields; i++)
		{
			int lenghtField = reader.GetInt();
			byte[] fieldBytes = reader.GetBytes(lenghtField);

			m_fields.add(new BinaryField(fieldBytes));
		}
	}
}
