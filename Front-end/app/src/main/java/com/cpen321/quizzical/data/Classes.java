package com.cpen321.quizzical.data;

import com.cpen321.quizzical.utils.OtherUtils;
import com.google.gson.Gson;

public class Classes {

    private int classCode;
    private String instructorUID;
    private String className;
    private CourseCategory category;

    public Classes(String UID, int classCode, String className, CourseCategory category) {
        this.instructorUID = UID;
        this.classCode = classCode;
        this.className = className;
        this.category = category;
    }

    public Classes(String jsonString) {
        if (OtherUtils.stringIsNullOrEmpty(jsonString)) {
            this.instructorUID = "";
            this.classCode = 0;
            this.className = "";
            this.category = CourseCategory.DontCare;
            return;
        }

        Gson gson = new Gson();
        Classes c = gson.fromJson(jsonString, Classes.class);
        this.instructorUID = c.instructorUID;
        this.classCode = c.classCode;
        this.className = c.className;
        this.category = c.category;
    }

    public int getClassCode() {
        return classCode;
    }

    public String getClassName() {
        return className;
    }

    public CourseCategory getCategory() {
        return category;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Classes)) {
            return false;
        }
        Classes o = (Classes) other;
        return this.getClassName().equals(o.getClassName()) && this.getClassCode() == o.getClassCode();
    }
}
