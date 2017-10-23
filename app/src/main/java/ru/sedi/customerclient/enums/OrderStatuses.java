package ru.sedi.customerclient.enums;

import android.text.TextUtils;

/**
 * Перечисление статусов заказа
 */
public enum OrderStatuses {
    cancelled, //Заказ отменен
    donefail, //Заказ провален
    donenoclient, //Заказ завершен (неявка клиента)
    donenodriver, //Заказ завершен (не найдена свободная машина)
    doneOk, //Заказ успешно завершен
    execute, //Заказчик в машине
    inway, //Такси на пути к заказчику
    nearcustomer, //Такси рядом с заказчиком
    search, //Заказ в поиске водителя
    trycancel, //Отмена заказа...
    waitexecute, //Водитель принял заказа "По завершению текущего"
    waittaxi, //Ожидание выезда
    needsdispatcherintervention, //Требуется вмешательство диспетчера
    ineditmode, //Заказ на редактировании
    searchinothergroups, //Поиск исполнителя в других группах
    executornotfound, //Не найден исполнитель для заказа
    searchonexchange, //Поиск исполнителя на бирже
    auction, //Торги
    searchonrbtaxiexchange, //Поиск исполнителя на РБТ
    executornotfoundonrbt, //Исполнитель на РБТ не найден
    driverwaitcustomer, //Водительт ожидает клиента
    waitconfirminwayfromdriver, //Ожидание вподтверждения выезда от водителя
    waitinwayconfirmfromdriver, //Ожидание вподтверждения выезда от водителя
    customernotifiedaboutdriverwait, //Заказчик оповещен о подачи машины
    waitcomplete, //Ожидание завершения заказа диспетчером
    unknown;

    public static OrderStatuses getShortStatus(String s) {
        if (TextUtils.isEmpty(s))
            return unknown;

        //Отмененные или завершенные заказы
        if (s.equalsIgnoreCase(cancelled.name()) ||
                s.equalsIgnoreCase(doneOk.name()) ||
                s.equalsIgnoreCase(trycancel.name())) {
            return cancelled;
        }

        //Поиск
        if (s.equalsIgnoreCase(search.name()) ||
                s.equalsIgnoreCase(searchinothergroups.name()) ||
                s.equalsIgnoreCase(searchonexchange.name()) ||
                s.equalsIgnoreCase(auction.name()) ||
                s.equalsIgnoreCase(searchonrbtaxiexchange.name())) {
            return search;
        }

        //Машина найдена
        if (s.equalsIgnoreCase(waitexecute.name()) ||
                s.equalsIgnoreCase(waittaxi.name()) ||
                s.equalsIgnoreCase(waitconfirminwayfromdriver.name()) ||
                s.equalsIgnoreCase(waitinwayconfirmfromdriver.name())) {
            return waittaxi;
        }

        //В пути
        if (s.equalsIgnoreCase(nearcustomer.name()) || s.equalsIgnoreCase(inway.name())) {
            return inway;
        }

        //Водитель ожидает клиента
        if (s.equalsIgnoreCase(driverwaitcustomer.name()) || s.equalsIgnoreCase(customernotifiedaboutdriverwait.name())) {
            return driverwaitcustomer;
        }

        //Водитель выполняет заказ
        if (s.equalsIgnoreCase(execute.name())) {
            return execute;
        }

        //Неизвестный статус
        else return unknown;
    }
}
