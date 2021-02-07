package com.rq.autoscrollbullet.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonUtils {

    private JsonUtils() {

    }

    private static Gson sGson = new GsonBuilder().create();

    public static Gson getGson() {
        return sGson;
    }

    public static String toJson(Object object) {

        return sGson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {

        return sGson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {

        return sGson.fromJson(json, typeOfT);
    }

}
