package com.a.eye.skywalking.testframework.api;


import com.a.eye.skywalking.protocol.common.ISerializable;
import com.a.eye.skywalking.testframework.api.config.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ContextPoolOperator {
    public static List<ISerializable> acquireBufferData() {
        List<ISerializable> resultSpan = new ArrayList<ISerializable>();
        Object[] bufferGroupObjectArray = acquireBufferGroupObjectArrayByClassLoader();

        for (Object bufferGroup : bufferGroupObjectArray) {
            ISerializable[] spanList = acquireBufferData(bufferGroup);
            for (ISerializable span : spanList) {
                if (span != null) {
                    resultSpan.add(span);
                }
            }
        }

        return resultSpan;
    }

    public static void clearSpanData() {
        Object[] bufferGroupObjectArray = acquireBufferGroupObjectArrayByClassLoader();

        for (Object bufferGroup : bufferGroupObjectArray) {
            ISerializable[] spanList = acquireBufferData(bufferGroup);
            for (int i = 0; i < spanList.length; i++) {
                spanList[i] = null;
            }
        }
    }

    private static ISerializable[] acquireBufferData(Object bufferGroup) {
        try {
            Class bufferGroupClass = Thread.currentThread().getContextClassLoader().loadClass(Config.BUFFER_GROUP_CLASS_NAME);
            Field spanArrayField = bufferGroupClass.getDeclaredField(Config.SPAN_ARRAY_FIELD_NAME);
            spanArrayField.setAccessible(true);
            return (ISerializable[]) spanArrayField.get(bufferGroup);
        } catch (Exception e) {
            throw new RuntimeException("Failed to acquire span array", e);
        }
    }


    private static Object[] acquireBufferGroupObjectArrayByClassLoader() {
        try {
            Class bufferPoolClass = fetchBufferPoolClass();
            Field field = fetchBufferPoolObject(bufferPoolClass);
            return (Object[]) field.get(bufferPoolClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to acquire data group object array", e);
        }
    }

    private static Field fetchBufferPoolObject(Class bufferPoolClass) throws NoSuchFieldException {
        Field field = bufferPoolClass.getDeclaredField(Config.BUFFER_GROUP_FIELD_NAME);
        field.setAccessible(true);
        return field;
    }

    private static Class fetchBufferPoolClass() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.loadClass(Config.BUFFER_POOL_CLASS_NAME);
    }
}
