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
package net.tsz.afinal.db.table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author michael Young (www.YangFuhai.com)
 * @version 1.0
 * @title 属性
 * @description 【非主键】的【基本数据类型】 都是属性
 * @created 2012-10-10
 */
public class Property {

    private String fieldName;
    private String column;
    private String defaultValue;
    private Class<?> dataType;
    private Field field;

    private Method getMethod;
    private Method setMethod;

    public void setValue(Object receiver, Object value) {
        if (setMethod != null && value != null) {
            try {
                if (dataType == String.class) {
                    setMethod.invoke(receiver, value.toString());
                } else if (dataType == int.class || dataType == Integer.class) {
                    setMethod.invoke(receiver, value == null ? (Integer) null : Integer.parseInt(value.toString()));
                } else if (dataType == float.class || dataType == Float.class) {
                    setMethod.invoke(receiver, value == null ? (Float) null : Float.parseFloat(value.toString()));
                } else if (dataType == double.class || dataType == Double.class) {
                    setMethod.invoke(receiver, value == null ? (Double) null : Double.parseDouble(value.toString()));
                } else if (dataType == long.class || dataType == Long.class) {
                    setMethod.invoke(receiver, value == null ? (Long) null : Long.parseLong(value.toString()));
                } else if (dataType == Date.class || dataType == java.sql.Date.class) {
                    setMethod.invoke(receiver, value == null ? (Date) null : stringToDateTime(value.toString()));
                } else if (dataType == boolean.class || dataType == Boolean.class) {
                    setMethod.invoke(receiver, value == null ? (Boolean) null : "1".equals(value.toString()));
                } else {
                    setMethod.invoke(receiver, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                field.setAccessible(true);
                field.set(receiver, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取某个实体执行某个方法的结果
     *
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Object obj) {
        if (obj != null && getMethod != null) {
            try {
                return (T) getMethod.invoke(obj);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Date stringToDateTime(String strDate) {
        if (strDate != null) {
            try {
                return sdf.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public void setDataType(Class<?> dataType) {
        this.dataType = dataType;
    }

    public Method getGetMethod() {
        return getMethod;
    }

    public void setGetMethod(Method getMethod) {
        this.getMethod = getMethod;
    }

    public Method getSetMethod() {
        return setMethod;
    }

    public void setSetMethod(Method setMethod) {
        this.setMethod = setMethod;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }


}
