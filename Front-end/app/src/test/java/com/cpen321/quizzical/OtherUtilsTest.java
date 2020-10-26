package com.cpen321.quizzical;

import android.graphics.Bitmap;

import com.cpen321.quizzical.utils.OtherUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class OtherUtilsTest {
    @Test
    public void checkUserNameValid() {
        assertTrue(OtherUtils.checkUserName("yuntaowu"));
    }

    @Test
    public void checkUserNameValid1() {
        //user names containing some numbers and underscores are valid
        assertTrue(OtherUtils.checkUserName("yuntaowu_2000"));
    }

    @Test
    public void checkUserNameInvalid() {
        //user name less than 3 characters are invalid
        assertFalse(OtherUtils.checkUserName("yu"));
    }

    @Test
    public void checkUserNameInvalid1() {
        //user name longer than 15 characters are invalid
        assertFalse(OtherUtils.checkUserName("aaaaaaaaaaaaaaaa"));
    }

    @Test
    public void checkUserNameInvalid2() {
        //user name containing spaces are invalid
        assertFalse(OtherUtils.checkUserName("avsxaa a"));
    }

    @Test
    public void checkUserNameInvalid3() {
        //user name containing some value other than a-z, A-Z, 0-9, -_ are invalid
        assertFalse(OtherUtils.checkUserName("!@$"));
    }

    @Test
    public void checkEmailValid() {
        //valid email
        assertTrue(OtherUtils.checkEmail("yuntaowu2000@alumni.ubc.ca"));
    }

    @Test
    public void checkEmailValid1() {
        //valid email
        assertTrue(OtherUtils.checkEmail("some@sd44.ca"));
    }

    @Test
    public void checkEmailInvalid() {
        //email that does not have an @ sign is invalid
        assertFalse(OtherUtils.checkEmail("asv"));
    }

    @Test
    public void checkClassNameValid() {
        //class names containing _ are valid
        assertTrue(OtherUtils.checkUserName("math_world"));
    }

    @Test
    public void checkClassNameValid1() {
        //class names containing some numbers and - are valid
        assertTrue(OtherUtils.checkUserName("quizzy-class1"));
    }

    @Test
    public void checkClassNameInvalid() {
        //class name less than 3 characters are invalid
        assertFalse(OtherUtils.checkUserName("aa"));
    }

    @Test
    public void checkString() {
        assertTrue(OtherUtils.stringIsNullOrEmpty(""));
    }

    @Test
    public void checkString1() {
        assertTrue(OtherUtils.stringIsNullOrEmpty(null));
    }

    @Test
    public void checkString2() {
        assertFalse(OtherUtils.stringIsNullOrEmpty("false"));
    }

//    @Test
//    public void checkReadFromUrl() {
//        //nothing should be read from this url
//        String result = OtherUtils.readFromURL("http://193.122.108.23:8080/s");
//        assertEquals("", result);
//    }

    @Test
    public void checkReadFromUrl1() {
        //nothing should be read from this url
        String result = OtherUtils.readFromURL("http://193.122.108.23:8080/Time");
        String expected_contains = "Time on trails-game.com is ";
        assertTrue(result.contains(expected_contains));
    }


    //following tests need mocking to work, comment out for now
//    @Test
//    public void imageTest() {
//        Bitmap bitmap = OtherUtils.getBitmapFromUrl("https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png");
//        String bitmapString = OtherUtils.encodeImage(bitmap);
//        Bitmap bitmap2 = OtherUtils.decodeImage(bitmapString);
//        assertEquals(bitmap, bitmap2);
//    }
//
//    @Test
//    public void imageScalingTest() {
//        Bitmap bitmap = OtherUtils.getBitmapFromUrl("https://raw.githubusercontent.com/yuntaowu2000/testUploadModels/master/006.png");
//        Bitmap bitmap2 = OtherUtils.scaleImage(bitmap);
//        assertEquals(400, bitmap2.getHeight());
//        assertEquals(400, bitmap2.getWidth());
//    }

}