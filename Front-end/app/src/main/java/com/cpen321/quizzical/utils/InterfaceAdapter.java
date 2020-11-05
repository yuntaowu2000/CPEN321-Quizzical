package com.cpen321.quizzical.utils;

import com.cpen321.quizzical.data.questions.QuestionsMC;
import com.cpen321.quizzical.data.questions.QuestionsText;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class InterfaceAdapter implements JsonSerializer, JsonDeserializer {


    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String questionType = jsonObject.get("questionType").getAsString();
        Class mClass = getClassByType(questionType);
        return context.deserialize(jsonObject, mClass);
    }

    private Class getClassByType(String questionType) {
        if ("MC".equals(questionType)) {
            return QuestionsMC.class;
        }
        return QuestionsText.class;
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

}
