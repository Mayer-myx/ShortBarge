package com.deepsoft.shortbarge.driver.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GsonConvertUtil {

    /**
     * 转换和赋值
     *
     * @param sourceObj     原数据对象
     * @param methodName    需要赋值原数据对象的方法名
     * @param filedObjValue 需要处理的字段对象值
     * @param filedMapClass 处理的字段对象映射Class
     */
    public static void performTransformWithEvaluation(Object sourceObj, String methodName, Object filedObjValue,
                                                      Class filedMapClass) {
        List<Object> listObjects = performTransform(filedObjValue, filedMapClass);
        Class<?> clazz = sourceObj.getClass();
        Method method = null;
        try {
            method = clazz.getMethod(methodName, Object.class);
            method.invoke(sourceObj, listObjects);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 统一转换成List输出，对于基本类型，取出集合中的值即可
     *
     * @param filedObjValue 需要处理的字段对象值
     * @param filedMapClass 处理的字段对象映射Class
     * @return
     */
    public static <T> List<T> performTransform(Object filedObjValue, Class filedMapClass) {
        List<T> beanList = new ArrayList<T>();
        Gson gson = new Gson();
        String jsonStr = gson.toJson(filedObjValue);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonStr);
        if (element.isJsonObject()) {
            // 把JsonElement对象转换成JsonObject
            T t = (T) gson.fromJson(element, filedMapClass);
            beanList.add(t);
        } else if (element.isJsonArray()) {
            //下面会导致T为LinkedTreeMap，说明Gson解析时不支持泛型
//            Type type = new TypeToken<List<T>>() {}.getType();
//            // 把JsonElement对象转换成JsonArray
//            List<T> list = gson.fromJson(element, type);

            List<T> list = jsonToList(element,filedMapClass);
            beanList.addAll(list);
        } else if (element.isJsonPrimitive()) {
            T t = (T) gson.fromJson(element, filedMapClass);
            beanList.add(t);
        } else {
            // element.isJsonNull()
            return null;
        }
        return beanList;
    }

    /**
     * 通过json字符串转List
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToList(String json, Class clazz) {
        Type type = new ParameterizedTypeImpl(clazz);
        List<T> list = new Gson().fromJson(json, type);
        return list;
    }

    /**
     * 通过element转List
     * @param element
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToList(JsonElement element, Class clazz) {
        Type type = new ParameterizedTypeImpl(clazz);
        List<T> list = new Gson().fromJson(element, type);
        return list;
    }

    /**
     * 自定义ParameterizedType
     */
    private static class ParameterizedTypeImpl implements ParameterizedType {
        Class clazz;

        public ParameterizedTypeImpl(Class clz) {
            clazz = clz;
        }

        /**
         * 返回实际类型组成的数据,比如Map<String,Long> map的类型是：java.lang.String、java.lang.Long
         * @return
         */
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] { clazz };
        }

        /**
         * 返回原生类型，比如Map<String,Long> map的原生类型为java.util.Map
         * @return
         */
        @Override
        public Type getRawType() {
            return List.class;
        }

        /**
         * 返回 Type 对象，表示此类型是其成员之一的类型,比如Map.Entry<Long,Short> map的OwnerType为java.util.Map
         * @return
         */
        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
