package ru.sedi.customerclient.common.LINQ;

import java.util.ArrayList;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Alexandr
 * Date: 20.09.12
 * Time: 9:36
 * To onSuccessCalculate this template use File | Settings | File Templates.
 */
public class QueryList<T extends Object> extends ArrayList<T> {
    private Object m_syncObject = new Object();

    @Override
    public boolean add(T object) {
        synchronized (m_syncObject) {
            return super.add(object);
        }
    }

    @Override
    public void clear() {
        synchronized (m_syncObject) {
            super.clear();
        }
    }

    @Override
    public T get(int index) {
        synchronized (m_syncObject) {
            return super.get(index);
        }
    }

    @Override
    public int size() {
        synchronized (m_syncObject) {
            return super.size();
        }
    }

    @Override
    public int indexOf(Object object) {
        synchronized (m_syncObject) {
            return super.indexOf(object);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (m_syncObject) {
            return super.isEmpty();
        }
    }

    @Override
    public T remove(int index) {
        synchronized (m_syncObject) {
            return super.remove(index);
        }
    }

    @Override
    public boolean remove(Object object) {
        synchronized (m_syncObject) {
            return super.remove(object);
        }
    }

    @Override
    public T set(int index, T object) {
        synchronized (m_syncObject) {
            return super.set(index, object);
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (m_syncObject) {
            return super.toArray();
        }
    }

    @Override
    public <T1> T1[] toArray(T1[] contents) {
        synchronized (m_syncObject) {
            return super.toArray(contents);
        }
    }

    @Override
    public Iterator<T> iterator() {
        synchronized (m_syncObject) {
            return super.iterator();
        }
    }

    @Override
    public void add(int index, T object) {
        synchronized (m_syncObject) {
            super.add(index, object);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        synchronized (m_syncObject) {
            return super.addAll(collection);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        synchronized (m_syncObject) {
            return super.addAll(index, collection);
        }
    }

    @Override
    public ListIterator<T> listIterator() {
        synchronized (m_syncObject) {
            return super.listIterator();
        }
    }

    @Override
    public ListIterator<T> listIterator(int location) {
        synchronized (m_syncObject) {
            return super.listIterator(location);
        }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        synchronized (m_syncObject) {
            return super.removeAll(collection);
        }
    }

    public QueryList() {

    }

    public QueryList(T[] array) {
        if (array != null)
            for (T item : array) {
                this.add(item);
            }
    }

    public QueryList(List<T> list) {
        if (list != null)
            for (T item : list) {
                this.add(item);
            }
    }

    public QueryList<T> Where(IWhere<T> where) {
        QueryList<T> list = new QueryList<T>();

        synchronized (m_syncObject) {
            for (T item : this) {
                if (where.Condition(item))
                    list.add(item);
            }
        }

        return list;
    }

    public T FirstOrDefault() {
        synchronized (m_syncObject) {
            if (this.size() > 0)
                return this.get(0);
        }
        return null;
    }

    public T FirstOrDefault(IWhere<T> where) {
        synchronized (m_syncObject) {
            QueryList<T> where1 = this.Where(where);
            if (where1.size() > 0)
                return where1.get(0);
        }
        return null;
    }

    public T LastOrDefault() {
        synchronized (m_syncObject) {
            if (this.size() > 0)
                return this.get(this.size() - 1);
        }
        return null;
    }

    public T LastOrDefault(IWhere<T> where) {
        synchronized (m_syncObject) {
            QueryList<T> where1 = this.Where(where);

            if (where1.size() > 0)
                return where1.get(where1.size() - 1);
        }
        return null;
    }

    public <Tout> QueryList<Tout> Select(ISelect<T, Tout> select) {
        QueryList<Tout> list = new QueryList<Tout>();
        synchronized (m_syncObject) {
            for (T item : this) {
                list.add(select.Select(item));
            }
        }
        return list;
    }

    public double Sum(IMath<T> math) {
        double result = 0;
        synchronized (m_syncObject) {
            for (T item : this) {
                result += math.GetValue(item);
            }
        }
        return result;
    }

    public double Max(IMath<T> math) {
        synchronized (m_syncObject) {
            if (this.size() == 0) return 0;

            double max = math.GetValue(this.get(0));
            for (T item : this) {
                max = Math.max(max, math.GetValue(item));
            }
            return max;
        }
    }

    public double Min(IMath<T> math) {
        synchronized (m_syncObject) {
            if (this.size() == 0) return 0;

            double max = math.GetValue(this.get(0));
            for (T item : this) {
                max = Math.min(max, math.GetValue(item));
            }
            return max;
        }
    }

    public QueryList<T> OrderBy(final IOrderBy<T> orderBy) {
        QueryList<T> list = new QueryList<T>();
        synchronized (m_syncObject) {
            list.addAll(this);

            Collections.sort(list, new Comparator<T>() {
                public int compare(T value1, T value2) {
                    return orderBy.OrderBy(value1).compareTo(orderBy.OrderBy(value2));
                }
            });
        }
        return list;
    }

    public QueryList<T> OrderByDescending(IOrderBy<T> orderBy) {
        synchronized (m_syncObject) {
            QueryList<T> list = this.OrderBy(orderBy);
            Collections.reverse(list);
            return list;
        }
    }

    public boolean Contains(IWhere<T> where) {
        synchronized (m_syncObject) {
            for (T item : this) {
                if (where.Condition(item))
                    return true;
            }
        }
        return false;
    }

    public void RemoveAll(IWhere<T> where) {
        synchronized (m_syncObject) {
            for (T item : this.Where(where)) {
                this.remove(item);
            }
        }
    }

    public QueryList<T> Distinct() {
        QueryList<T> list = new QueryList<T>();
        synchronized (m_syncObject) {
            for (final T item : this) {
                if (!list.Contains(new IWhere<T>() {
                    public boolean Condition(T item1) {
                        return item == item1;
                    }
                }))
                    list.add(item);
            }
        }
        return list;
    }

    public int Count(IWhere<T> where) {
        synchronized (m_syncObject) {
            return this.Where(where).size();
        }
    }

    public T tryGet(int position) {
        try {
            return get(position);
        } catch (Exception e) {
            return null;
        }
    }

    public QueryList<T> getTop(int count) {
        synchronized (m_syncObject) {
            if (size() <= count)
                return this;
            QueryList<T> list = new QueryList<>();
            for (int i = 0; i < count; i++) {
                list.add(get(i));
            }
            return list;
        }

    }

    public int getIndex(IWhere<T> where) {
        synchronized (m_syncObject) {
            for (T item : this) {
                if (where.Condition(item))
                    return this.indexOf(item);
            }
            return -1;
        }
    }
}
