package ru.mail.park.chat.helpers;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Михаил on 05.06.2016.
 */
public class UniqueArrayList <T> extends ArrayList<T>{
    @Override
    public boolean add(T object) {
        if (object != null) {
            for (int i = 0; i < size(); i++) {
                if (get(i).equals(object)) {
                    remove(i);
                    super.add(i, object);
                    return true;
                }
            }
            return super.add(object);
        }
        return false;
    }

    @Override
    public void add(int index, T object) {
        for (int i = 0; i < size(); i++) {
            if (get(i).equals(object)) {
                return;
            }
        }
        super.add(index, object);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean modified = false;
        for (T o : collection) {
            modified |= add(o);
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        return super.addAll(index, collection);
    }
}
