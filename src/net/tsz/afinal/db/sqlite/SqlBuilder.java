/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tsz.afinal.db.sqlite;

import android.text.TextUtils;
import net.tsz.afinal.db.table.*;
import net.tsz.afinal.exception.DbException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlBuilder {

    /**
     * 获取插入的sql语句
     *
     * @param entity
     * @return
     */
    public static SqlInfo buildInsertSql(Object entity) {

        List<KeyValue> keyValueList = getSaveKeyValueListByEntity(entity);

        StringBuffer strSQL = new StringBuffer();
        SqlInfo sqlInfo = null;
        if (keyValueList != null && keyValueList.size() > 0) {

            sqlInfo = new SqlInfo();

            strSQL.append("INSERT INTO ");
            strSQL.append(TableInfo.get(entity.getClass()).getTableName());
            strSQL.append(" (");
            for (KeyValue kv : keyValueList) {
                strSQL.append(kv.getKey()).append(",");
                sqlInfo.addValue(kv.getValue());
            }
            strSQL.deleteCharAt(strSQL.length() - 1);
            strSQL.append(") VALUES ( ");

            int length = keyValueList.size();
            for (int i = 0; i < length; i++) {
                strSQL.append("?,");
            }
            strSQL.deleteCharAt(strSQL.length() - 1);
            strSQL.append(")");

            sqlInfo.setSql(strSQL.toString());
        }

        return sqlInfo;
    }

    public static List<KeyValue> getSaveKeyValueListByEntity(Object entity) {

        List<KeyValue> keyValueList = new ArrayList<KeyValue>();

        TableInfo table = TableInfo.get(entity.getClass());
        Object idValue = table.getId().getValue(entity);

        if (!(idValue instanceof Integer)) { //用了非自增长,添加id , 采用自增长就不需要添加id了
            if (idValue instanceof String && idValue != null) {
                KeyValue kv = new KeyValue(table.getId().getColumn(), idValue);
                keyValueList.add(kv);
            }
        }

        //添加属性
        Collection<Property> properties = table.propertyMap.values();
        for (Property property : properties) {
            KeyValue kv = property2KeyValue(property, entity);
            if (kv != null)
                keyValueList.add(kv);
        }

        //添加外键（多对一）
        Collection<ManyToOne> manyToOnes = table.manyToOneMap.values();
        for (ManyToOne many : manyToOnes) {
            KeyValue kv = manyToOne2KeyValue(many, entity);
            if (kv != null) keyValueList.add(kv);
        }

        return keyValueList;
    }


    private static String getDeleteSqlByTableName(String tableName) {
        return "DELETE FROM " + tableName;
    }


    public static SqlInfo buildDeleteSql(Object entity) {
        TableInfo table = TableInfo.get(entity.getClass());

        Id id = table.getId();
        Object idValue = id.getValue(entity);

        if (idValue == null) {
            throw new DbException("getDeleteSQL:" + entity.getClass() + " id value is null");
        }
        StringBuffer strSQL = new StringBuffer(getDeleteSqlByTableName(table.getTableName()));
        strSQL.append(" WHERE ").append(id.getColumn()).append("=?");

        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSql(strSQL.toString());
        sqlInfo.addValue(idValue);

        return sqlInfo;
    }


    public static SqlInfo buildDeleteSql(Class<?> clazz, Object idValue) {
        TableInfo table = TableInfo.get(clazz);
        Id id = table.getId();

        if (null == idValue) {
            throw new DbException("getDeleteSQL:idValue is null");
        }

        StringBuffer strSQL = new StringBuffer(getDeleteSqlByTableName(table.getTableName()));
        strSQL.append(" WHERE ").append(id.getColumn()).append("=?");

        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSql(strSQL.toString());
        sqlInfo.addValue(idValue);

        return sqlInfo;
    }

    /**
     * 根据条件删除数据 ，条件为空的时候将会删除所有的数据
     *
     * @param clazz
     * @param strWhere
     * @return
     */
    public static String buildDeleteSql(Class<?> clazz, String strWhere) {
        TableInfo table = TableInfo.get(clazz);
        StringBuffer strSQL = new StringBuffer(getDeleteSqlByTableName(table.getTableName()));

        if (!TextUtils.isEmpty(strWhere)) {
            strSQL.append(" WHERE ");
            strSQL.append(strWhere);
        }

        return strSQL.toString();
    }


    ////////////////////////////select sql start///////////////////////////////////////


    private static String getSelectSqlByTableName(String tableName) {
        return new StringBuffer("SELECT * FROM ").append(tableName).toString();
    }


    public static String getSelectSQL(Class<?> clazz, Object idValue) {
        TableInfo table = TableInfo.get(clazz);

        StringBuffer strSQL = new StringBuffer(getSelectSqlByTableName(table.getTableName()));
        strSQL.append(" WHERE ");
        strSQL.append(getPropertyStrSql(table.getId().getColumn(), idValue));

        return strSQL.toString();
    }

    public static SqlInfo getSelectSqlAsSqlInfo(Class<?> clazz, Object idValue) {
        TableInfo table = TableInfo.get(clazz);

        StringBuffer strSQL = new StringBuffer(getSelectSqlByTableName(table.getTableName()));
        strSQL.append(" WHERE ").append(table.getId().getColumn()).append("=?");

        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSql(strSQL.toString());
        sqlInfo.addValue(idValue);

        return sqlInfo;
    }


    public static String getSelectSQL(Class<?> clazz) {
        return getSelectSqlByTableName(TableInfo.get(clazz).getTableName());
    }

    public static String getSelectSQLByWhere(Class<?> clazz, String strWhere) {
        TableInfo table = TableInfo.get(clazz);

        StringBuffer strSQL = new StringBuffer(getSelectSqlByTableName(table.getTableName()));

        if (!TextUtils.isEmpty(strWhere)) {
            strSQL.append(" WHERE ").append(strWhere);
        }

        return strSQL.toString();
    }

    //////////////////////////////update sql start/////////////////////////////////////////////

    public static SqlInfo getUpdateSqlAsSqlInfo(Object entity) {

        TableInfo table = TableInfo.get(entity.getClass());
        Object idValue = table.getId().getValue(entity);

        if (null == idValue) {//主键值不能为null，否则不能更新
            throw new DbException("this entity[" + entity.getClass() + "]'s id value is null");
        }

        List<KeyValue> keyValueList = new ArrayList<KeyValue>();
        //添加属性
        Collection<Property> properties = table.propertyMap.values();
        for (Property property : properties) {
            KeyValue kv = property2KeyValue(property, entity);
            if (kv != null)
                keyValueList.add(kv);
        }

        //添加外键（多对一）
        Collection<ManyToOne> manyToOnes = table.manyToOneMap.values();
        for (ManyToOne many : manyToOnes) {
            KeyValue kv = manyToOne2KeyValue(many, entity);
            if (kv != null) keyValueList.add(kv);
        }

        if (keyValueList == null || keyValueList.size() == 0) return null;

        SqlInfo sqlInfo = new SqlInfo();
        StringBuffer strSQL = new StringBuffer("UPDATE ");
        strSQL.append(table.getTableName());
        strSQL.append(" SET ");
        for (KeyValue kv : keyValueList) {
            strSQL.append(kv.getKey()).append("=?,");
            sqlInfo.addValue(kv.getValue());
        }
        strSQL.deleteCharAt(strSQL.length() - 1);
        strSQL.append(" WHERE ").append(table.getId().getColumn()).append("=?");
        sqlInfo.addValue(idValue);
        sqlInfo.setSql(strSQL.toString());
        return sqlInfo;
    }


    public static SqlInfo getUpdateSqlAsSqlInfo(Object entity, String strWhere) {

        TableInfo table = TableInfo.get(entity.getClass());

        List<KeyValue> keyValueList = new ArrayList<KeyValue>();

        //添加属性
        Collection<Property> properties = table.propertyMap.values();
        for (Property property : properties) {
            KeyValue kv = property2KeyValue(property, entity);
            if (kv != null) keyValueList.add(kv);
        }

        //添加外键（多对一）
        Collection<ManyToOne> manyToOnes = table.manyToOneMap.values();
        for (ManyToOne many : manyToOnes) {
            KeyValue kv = manyToOne2KeyValue(many, entity);
            if (kv != null) keyValueList.add(kv);
        }

        if (keyValueList == null || keyValueList.size() == 0) {
            throw new DbException("this entity[" + entity.getClass() + "] has no property");
        }

        SqlInfo sqlInfo = new SqlInfo();
        StringBuffer strSQL = new StringBuffer("UPDATE ");
        strSQL.append(table.getTableName());
        strSQL.append(" SET ");
        for (KeyValue kv : keyValueList) {
            strSQL.append(kv.getKey()).append("=?,");
            sqlInfo.addValue(kv.getValue());
        }
        strSQL.deleteCharAt(strSQL.length() - 1);
        if (!TextUtils.isEmpty(strWhere)) {
            strSQL.append(" WHERE ").append(strWhere);
        }
        sqlInfo.setSql(strSQL.toString());
        return sqlInfo;
    }


    public static String getCreateTableSQL(Class<?> clazz) {
        TableInfo table = TableInfo.get(clazz);

        Id id = table.getId();
        StringBuffer strSQL = new StringBuffer();
        strSQL.append("CREATE TABLE IF NOT EXISTS ");
        strSQL.append(table.getTableName());
        strSQL.append(" ( ");

        Class<?> primaryClazz = id.getDataType();
        if (primaryClazz == int.class || primaryClazz == Integer.class)
            strSQL.append("\"").append(id.getColumn()).append("\"    ").append("INTEGER PRIMARY KEY AUTOINCREMENT,");
        else
            strSQL.append("\"").append(id.getColumn()).append("\"    ").append("TEXT PRIMARY KEY,");

        Collection<Property> properties = table.propertyMap.values();
        for (Property property : properties) {
            strSQL.append("\"").append(property.getColumn());
            strSQL.append("\",");
        }

        Collection<ManyToOne> manyToOnes = table.manyToOneMap.values();
        for (ManyToOne manyToOne : manyToOnes) {
            strSQL.append("\"").append(manyToOne.getColumn()).append("\",");
        }
        strSQL.deleteCharAt(strSQL.length() - 1);
        strSQL.append(" )");
        return strSQL.toString();
    }


    /**
     * @param key
     * @param value
     * @return eg1: name='afinal'  eg2: id=100
     */
    private static String getPropertyStrSql(String key, Object value) {
        StringBuffer sbSQL = new StringBuffer(key).append("=");
        if (value instanceof String || value instanceof java.util.Date || value instanceof java.sql.Date) {
            sbSQL.append("'").append(value).append("'");
        } else {
            sbSQL.append(value);
        }
        return sbSQL.toString();
    }


    private static KeyValue property2KeyValue(Property property, Object entity) {
        KeyValue kv = null;
        String key = property.getColumn();
        Object value = property.getValue(entity);
        if (value != null) {
            kv = new KeyValue(key, value);
        } else {
            if (property.getDefaultValue() != null && property.getDefaultValue().trim().length() != 0)
                kv = new KeyValue(key, property.getDefaultValue());
        }
        return kv;
    }


    private static KeyValue manyToOne2KeyValue(ManyToOne many, Object entity) {
        KeyValue kv = null;
        String key = many.getColumn();
        Object oneKey = many.getValue(entity);
        if (oneKey != null) {
            Object value = TableInfo.get(oneKey.getClass()).getId().getValue(oneKey);
            if (key != null && value != null) {
                kv = new KeyValue(key, value);
            }
        }

        return kv;
    }

}
