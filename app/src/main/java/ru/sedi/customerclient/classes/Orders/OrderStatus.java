package ru.sedi.customerclient.classes.Orders;

/**
 * User: Stalker
 * Date: 11.04.2014
 * Time: 13:24
 * To onSuccessCalculate this template use File | Settings | File Templates.
 */
public class OrderStatus
{
  String mId;
  String mName;

  public OrderStatus(String id, String mName)
  {
    this.mId = id;
    this.mName = mName;
  }

  public OrderStatus()
  {
  }

  public String GetId()
  {
    return mId;
  }

  public void SetId(String id)
  {
    this.mId = id;
  }

  public String GetName()
  {
    return mName;
  }

  public void SetName(String name)
  {
    this.mName = name;
  }
}
