package ru.sedi.customerclient.common.LINQ;

/**
 * Created with IntelliJ IDEA.
 * User: Alexandr
 * Date: 20.09.12
 * Time: 9:37
 * To onSuccessCalculate this template use File | Settings | File Templates.
 */
public interface IWhere<T>
{
    boolean Condition(T item);
}
