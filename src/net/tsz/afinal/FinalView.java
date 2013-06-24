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
package net.tsz.afinal;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import net.tsz.afinal.annotation.view.EventListener;
import net.tsz.afinal.annotation.view.Select;
import net.tsz.afinal.annotation.view.ViewInject;

import java.lang.reflect.Field;

public class FinalView {

    private FinalView() {
    }

    public static void init(Activity activity) {
        initActivity(activity);
    }

    public static void init(View view) {
        initView(view);
    }

    private static void initActivity(Activity activity) {
        Field[] fields = activity.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    int viewId = viewInject.id();
                    try {
                        field.setAccessible(true);
                        field.set(activity, activity.findViewById(viewId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String clickMethod = viewInject.click();
                    if (!TextUtils.isEmpty(clickMethod))
                        setViewClickListener(activity, field, clickMethod);

                    String longClickMethod = viewInject.longClick();
                    if (!TextUtils.isEmpty(longClickMethod))
                        setViewLongClickListener(activity, field, longClickMethod);

                    String itemClickMethod = viewInject.itemClick();
                    if (!TextUtils.isEmpty(itemClickMethod))
                        setItemClickListener(activity, field, itemClickMethod);

                    String itemLongClickMethod = viewInject.itemLongClick();
                    if (!TextUtils.isEmpty(itemLongClickMethod))
                        setItemLongClickListener(activity, field, itemLongClickMethod);

                    Select select = viewInject.select();
                    if (!TextUtils.isEmpty(select.selected()))
                        setViewSelectListener(activity, field, select.selected(), select.noSelected());

                }
            }
        }
    }

    private static void initView(View view) {
        Field[] fields = view.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                if (viewInject != null) {
                    int viewId = viewInject.id();
                    try {
                        field.setAccessible(true);
                        field.set(view, view.findViewById(viewId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String clickMethod = viewInject.click();
                    if (!TextUtils.isEmpty(clickMethod))
                        setViewClickListener(view, field, clickMethod);

                    String longClickMethod = viewInject.longClick();
                    if (!TextUtils.isEmpty(longClickMethod))
                        setViewLongClickListener(view, field, longClickMethod);

                    String itemClickMethod = viewInject.itemClick();
                    if (!TextUtils.isEmpty(itemClickMethod))
                        setItemClickListener(view, field, itemClickMethod);

                    String itemLongClickMethod = viewInject.itemLongClick();
                    if (!TextUtils.isEmpty(itemLongClickMethod))
                        setItemLongClickListener(view, field, itemLongClickMethod);

                    Select select = viewInject.select();
                    if (!TextUtils.isEmpty(select.selected()))
                        setViewSelectListener(view, field, select.selected(), select.noSelected());

                }
            }
        }
    }


    private static void setViewClickListener(Object target, Field field, String clickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((View) obj).setOnClickListener(new EventListener(target).click(clickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setViewLongClickListener(Object target, Field field, String clickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((View) obj).setOnLongClickListener(new EventListener(target).longClick(clickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setItemClickListener(Object target, Field field, String itemClickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemClickListener(new EventListener(target).itemClick(itemClickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setItemLongClickListener(Object target, Field field, String itemClickMethod) {
        try {
            Object obj = field.get(target);
            if (obj instanceof AbsListView) {
                ((AbsListView) obj).setOnItemLongClickListener(new EventListener(target).itemLongClick(itemClickMethod));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setViewSelectListener(Object target, Field field, String select, String noSelect) {
        try {
            Object obj = field.get(target);
            if (obj instanceof View) {
                ((AbsListView) obj).setOnItemSelectedListener(new EventListener(target).select(select).noSelect(noSelect));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
