package com.cpen321.quizzical.utils;

public class ChoicePair {

    private boolean isPic;
    private String str;
    /*
     * if isPic is true, then str is the link to the image src.
     * if isPic is false, then str is the string needed for the choice.
     * */

    public ChoicePair(boolean isPic, String str) {
        this.isPic = isPic;
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public boolean isPic() {
        return isPic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoicePair that = (ChoicePair) o;
        return isPic() == that.isPic() &&
                getStr().equals(that.getStr());
    }

}
