package ru.sedi.customerclient.NewDataSharing.Collections;


import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.NewDataSharing.NameId;
import ru.sedi.customerclient.NewDataSharing._Service;
import ru.sedi.customerclient.common.LINQ.IWhere;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class _ServiceCollection {
    QueryList<_Service> mServices = new QueryList<>();

    public _ServiceCollection() {
    }

    public QueryList<_Service> getAll() {
        if(!mServices.isEmpty()){
            java.util.Collections.sort(mServices, (o1, o2) ->
                    o1.getName().compareTo(o2.getName()));
        }
        return mServices;
    }

    public void set(QueryList<_Service> services) {
        mServices = new QueryList<>(services);
        reset(mServices);
    }

    public void set(_Service[] services) {
        mServices = new QueryList<>(services);
        reset(mServices);
    }

    public void setCheckedById(final String id, boolean checked) {
        _Service service = mServices.FirstOrDefault(new IWhere<_Service>() {
            @Override
            public boolean Condition(_Service item) {
                return item.getID().equals(id);
            }
        });
        if (service != null) {
            service.setChecked(checked);
        }
    }

    public _Service[] getAsArray() {
        return mServices.toArray(new _Service[mServices.size()]);
    }

    public String getCheckedNames() {
        String s = Const.EmptyStr;
        for (_Service service : getChecked()) {
            s += s.isEmpty() ? "" : ", ";
            s += service.getName();
        }
        return s;
    }

    public String getCheckedIds() {
        String s = Const.EmptyStr;
        for (_Service service : getChecked()) {
            s += s.isEmpty() ? "" : ",";
            s += service.getID();
        }
        return s;
    }

    public void reset(QueryList<_Service> list) {
        for (_Service service : list) {
            service.setChecked(false);
        }
    }

    public void reset() {
        reset(mServices);
    }

    public QueryList<_Service> getChecked() {
        return mServices.Where(new IWhere<_Service>() {
            @Override
            public boolean Condition(_Service item) {
                return item.isChecked();
            }
        });
    }

    public QueryList<NameId> getCheckedNameId() {
        QueryList<_Service> where = mServices.Where(item -> item.isChecked());
        QueryList<NameId> checkedNameIds = new QueryList<>();
        for (_Service service : where) {
            checkedNameIds.add(new NameId(service.getName(), service.getID()));
        }
        return checkedNameIds;
    }

    public boolean isEmpty() {
        return mServices.isEmpty();
    }

    public void setChecked(QueryList<NameId> checked) {
        for (final NameId nameId : checked) {
            setCheckedById(nameId.getID(), true);
        }
    }

}
