package org.appland.settlers.rest.resource;

import java.util.HashMap;

class IdManager {

    private final HashMap<Object, Integer> objectToId;
    private int ids;
    private final HashMap<Integer, Object> idToObject;

    IdManager() {
        objectToId = new HashMap<>();
        idToObject = new HashMap<>();
        ids = 0;
    }

    int getId(Object o) {
        synchronized (objectToId) {
            if (!objectToId.containsKey(o)) {
                ids++;

                objectToId.put(o, ids);
                idToObject.put(ids, o);
            }
        }

        return objectToId.get(o);
    }

    Object getObject(int id) {
        return idToObject.get(id);
    }
}
