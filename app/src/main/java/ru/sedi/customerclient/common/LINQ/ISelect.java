package ru.sedi.customerclient.common.LINQ;

/**
 * Created with IntelliJ IDEA.
 * User: Alexandr
 * Date: 25.09.12
 * Time: 10:19
 * To onSuccessCalculate this template use File | Settings | File Templates.
 */
public interface ISelect<Tin, Tout>
{
    Tout Select(Tin item);
}
