package ru.sedi.customerclient.common.LINQ;

/**
 * Created with IntelliJ IDEA.
 * User: Alexandr
 * Date: 25.09.12
 * Time: 17:38
 * To onSuccessCalculate this template use File | Settings | File Templates.
 */
public interface IOrderBy<T>
{
    Comparable OrderBy(T item);
}
