package ru.sedi.customerclient.fragments;


import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.PickerDate;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.interfaces.IAction;

public class PreorderTimeFragment extends Fragment {
    private final int LAYOUT = R.layout.fragment_pre_order;

    @BindView(R.id.datePicker) NumberPicker date_picker;
    @BindView(R.id.timePicker) NumberPicker time_picker;
    @BindView(R.id.minutePicker) NumberPicker minute_picker;

    private final String[] MINUTES = {"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"};

    private DateTime mDate;
    private _Order mOrder;
    private IAction mDateTimeChangeAction;

    public static PreorderTimeFragment getInstance() {
        PreorderTimeFragment fragment = new PreorderTimeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);
        ButterKnife.bind(this, view);
        mOrder = _OrderRegistrator.me().getOrder();
        mDate = mOrder.getDateTime();
        if (!mOrder.isValidPreOrderTime() || mOrder.isRush()) {
            mDate = getValidTime();
            mOrder.setDateTime(mDate);
        }
        init();
        return view;
    }

    private void init() {
        initDatePicker();
        initTimePicker();

        int dividerColor = ContextCompat.getColor(getContext(), R.color.primaryColor);
        setDividerColor(date_picker, dividerColor);
        setDividerColor(time_picker, dividerColor);
        setDividerColor(minute_picker, dividerColor);
    }

    private void initTimePicker() {
        time_picker.setMinValue(0);
        time_picker.setMaxValue(23);
        time_picker.setValue(mDate.getHour());
        time_picker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            mDate.setHour(newVal);
            if (isValidTime())
                mOrder.setDateTime(mDate);
            notifySubscribers();
        });

        minute_picker.setMinValue(0);
        minute_picker.setMaxValue(MINUTES.length - 1);
        minute_picker.setDisplayedValues(MINUTES);
        minute_picker.setValue(mDate.getMinute() / 5);
        minute_picker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            mDate.setMinute(Integer.parseInt(MINUTES[newVal]));
            if (isValidTime())
                mOrder.setDateTime(mDate);
            notifySubscribers();
        });
    }

    private void notifySubscribers() {
        if (mDateTimeChangeAction != null)
            mDateTimeChangeAction.action();
    }

    private void initDatePicker() {
        DateTime endDate = DateTime.Now();
        endDate.addMonth(2);

        QueryList<String> s = new QueryList<>();
        final QueryList<PickerDate> pickerDates = new QueryList<>();
        for (DateTime startDate = DateTime.Now(); startDate.getDate().before(endDate.getDate()); startDate.addDay(1)) {
            String string = startDate.toString("EE, dd MMMM");
            s.add(string);
            pickerDates.add(new PickerDate(new DateTime(startDate.getDate()), string));
        }

        int index = pickerDates.getIndex(item -> item.getDateTime().getYear() == mDate.getYear()
                && item.getDateTime().getMonth() == mDate.getMonth()
                && item.getDateTime().getDay() == mDate.getDay());

        date_picker.setMinValue(0);
        date_picker.setMaxValue(s.size() - 1);
        date_picker.setDisplayedValues(s.toArray(new String[s.size()]));
        date_picker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            PickerDate pickerDate = pickerDates.tryGet(newVal);
            if (pickerDate != null) {
                mDate.setYear(pickerDate.getDateTime().getYear());
                mDate.setMonth(pickerDate.getDateTime().getMonth() - 1);
                mDate.setDay(pickerDate.getDateTime().getDay());
                if (isValidTime()) {
                    mOrder.setDateTime(mDate);
                    LogUtil.log(LogUtil.INFO, "Дата изменена на: %s", mDate.toString("dd.MM.yyyy HH:mm"));
                }
                notifySubscribers();
            }
        });

        if (index >= 0)
            date_picker.setValue(index);
    }


    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException
                        | Resources.NotFoundException
                        | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public DateTime getValidTime() {
        DateTime date = DateTime.Now();
        int currentMinute = date.getMinute();
        date.setMinute(currentMinute - (currentMinute % 5));
        date.addMinute(20);
        return date;
    }

    public boolean isValidTime() {
        boolean b = mDate.getTime() - DateTime.Now().getTime() > DateTime.MINUTE * 15;
        if (!b) {
            ToastHelper.showShortToast(getString(R.string.incorrect_order_time_message));
            mDate = getValidTime();
            initDatePicker();
            initTimePicker();
        }
        return b;
    }

    public void subscribeOnChange(IAction action) {
        mDateTimeChangeAction = action;
    }
}
