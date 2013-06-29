package net.tsz.afinal.db.sqlite;

import java.util.LinkedList;

public class SqlInfo {

    private String sql;
    private LinkedList<Object> bindingArgs;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public LinkedList<Object> getBindingArgs() {
        return bindingArgs;
    }

    public void setBindingArgs(LinkedList<Object> bindingArgs) {
        this.bindingArgs = bindingArgs;
    }

    public Object[] getBindingArgsAsArray() {
        if (bindingArgs != null)
            return bindingArgs.toArray();
        return null;
    }

    public String[] getBindingArgsAsStringArray() {
        if (bindingArgs != null) {
            String[] strings = new String[bindingArgs.size()];
            for (int i = 0; i < bindingArgs.size(); i++) {
                strings[i] = bindingArgs.get(i).toString();
            }
            return strings;
        }
        return null;
    }

    public void addValue(Object obj) {
        if (bindingArgs == null)
            bindingArgs = new LinkedList<Object>();

        bindingArgs.add(obj);
    }

}
