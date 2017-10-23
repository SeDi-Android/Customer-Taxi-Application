package ru.sedi.customerclient.classes.Customer;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.NewDataSharing._Owner;
import ru.sedi.customerclient.NewDataSharing._Phone;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.IFunc;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Prefs;

public class _LoginInfo {
    private int ID; // ID пользователя
    private int AccountID; // ID лицевого счета
    private String Login = Const.EmptyStr; // логин
    private String Name = Const.EmptyStr; // имя
    private String Nick = Const.EmptyStr; //Позывной
    private String SecondName = Const.EmptyStr; //Фамилия
    private String Patronymic = Const.EmptyStr; //Отчество
    private String Email = Const.EmptyStr; //Почта
    private String Birthday;
    private boolean Gender; //Пол (true – мужской, false - женский)
    private boolean Photo; //Загружено ли фото
    private boolean AnonymousOrder; //разрешены ли заказы без регистрации
    private boolean IsAnonymous; //это анонимный пользователь
    private boolean IsCustomer; //это заказчик
    private boolean IsEmployee; //это сотрудник
    private boolean AllowCustomerCost; //Разрешено ли задавать свою стоимость
    private boolean AllowAuction;  // разрешено ли регистрировать аукционы
    private boolean AllowCalc; //разрешен ли расчет стоимости
    private _Balance Balance = new _Balance(); // баланс
    private QueryList<_Phone> Phones = new QueryList<>();
    private QueryList<_Point> Addresses = new QueryList<>();
    private boolean Affiliate;  // подключена ли партнёрская программа
    private boolean AffilateOrders; // является ли пользователь ее участником
    private int Promocode;  // промокод партнерской программы
    private String Currency = "₱";
    private _Owner Owner = new _Owner();  // информация о группе
    private String UserKey;

    public _LoginInfo() {
    }

    public int getID() {
        return ID;
    }

    public int getAccountID() {
        return AccountID;
    }

    public String getName() {
        return Name;
    }

    public String getSecondName() {
        return SecondName;
    }

    public _Balance getBalance() {
        return Balance;
    }

    public void setBalance(_Balance balance) {
        Balance = balance;
    }

    public List<_Phone> getPhones() {
        return Phones;
    }

    public _Owner getOwner() {
        return Owner;
    }

    public String getUserKey() {
        return UserKey;
    }

    public void setUserKey(String userKey) {
        UserKey = userKey;
    }

    public String getPatronymic() {
        return Patronymic;
    }

    public void updateBalance(final Context context, final boolean b) {
        if (getID() <= 0)
            return;

        SweetAlertDialog pd = null;
        if (b && (context != null)) {
            pd = ProgressDialogHelper.show(context);
        }
        final SweetAlertDialog finalPd = pd;
        AsyncAction.run(new IFunc<_Balance>() {
            @Override
            public _Balance Func() throws Exception {
                return ServerManager.GetInstance().getCustomerBalance();
            }
        }, new IActionFeedback<_Balance>() {
            @Override
            public void onResponse(_Balance balance) {
                if (finalPd != null)
                    finalPd.dismiss();

                setBalance(balance);
                SediBus.getInstance().post(balance);
            }

            @Override
            public void onFailure(Exception e) {
                if (finalPd != null)
                    finalPd.dismiss();

                MessageBox.show(context, e.getMessage());
            }
        });
    }

    public DateTime getBirthday() {
        if (TextUtils.isEmpty(Birthday))
            return DateTime.Now();

        return DateTime.fromString(Birthday, DateTime.WEB_DATE);
    }

    public boolean getGender() {
        return Gender;
    }

    public String getQiwiPaymentPhone() {
        if (getID() <= 0)
            return Const.EmptyStr;

        String qiwiPhone = Prefs.getString(PrefsName.QIWI_PAYMENT_PHONE);
        if (TextUtils.isEmpty(qiwiPhone))
            Prefs.setValue(PrefsName.QIWI_PAYMENT_PHONE, Prefs.getString(PrefsName.REGISTER_USER_PHONE));
        qiwiPhone = Prefs.getString(PrefsName.QIWI_PAYMENT_PHONE);
        return qiwiPhone;
    }

    public String getCurrency(){
        return getBalance().getCurrency();
    }

    public QueryList<_Point> getAddresses() {
        return Addresses;
    }

    public boolean isEnabledPartnerProgram() {
        return Affiliate;
    }

    public boolean isAllowCalc() {
        return AllowCalc;
    }
}

