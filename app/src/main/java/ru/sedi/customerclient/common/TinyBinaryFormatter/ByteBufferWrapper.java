package ru.sedi.customerclient.common.TinyBinaryFormatter;

import java.nio.ByteBuffer;

public class ByteBufferWrapper
{
	private ByteBuffer m_buffer;
	
	public ByteBufferWrapper(byte[] buffer)
	{
		m_buffer = ByteBuffer.wrap(buffer);
	}
	
	public byte GetByte()
	{
		return m_buffer.get();
	}
	
	public byte[] GetBytes(int countBytes)
	{
		byte[] bytes=new byte[countBytes];
		
		m_buffer.get(bytes);
		
		return bytes;
	}
	
	public short GetShort()
	{
		byte[] bytes=new byte[2];
		
		m_buffer.get(bytes);

		return BinaryConverter.ToShort(bytes);
	}
	
	
	public int GetInt()
	{
		byte[] bytes=new byte[4];
		
		m_buffer.get(bytes);
		
		return BinaryConverter.ToInt(bytes);
	}

	public int Position()
	{
		return m_buffer.position();
	}
}
