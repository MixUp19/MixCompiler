package org.compiler.model.util;

import java.util.ArrayList;

public class TablaID {
    private TablaID padre;
    private final ArrayList<ID> tabla;

    public TablaID() {
        this.tabla = new ArrayList<>();
        this.padre = null;
    }
    public TablaID(TablaID padre) {
        this.tabla = new ArrayList<>();
        this.padre = padre;
    }

    public boolean agregarID(ID id) {
        if (revisarID(id.getId()) == null) {
            tabla.add(id);
            return true;
        } else {
            return false;
        }
    }

    public ID revisarID(String id){
        for (ID i : tabla) {
            if (i.getId().equals(id)) {
                return i;
            }
        }
        if (padre != null) {
            return padre.revisarID(id);
        }
        return null;
    }

    public TablaID getPadre() {
        return padre;
    }
}
