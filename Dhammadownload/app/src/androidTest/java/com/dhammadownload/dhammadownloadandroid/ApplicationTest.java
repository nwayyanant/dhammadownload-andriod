package com.dhammadownload.dhammadownloadandroid;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4; // Or androidx.test.runner.AndroidJUnit4 for older versions

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        android.content.Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.dhammadownload.dhammadownloadandroid", appContext.getPackageName());
    }
}

//old code
//import android.app.Application;
//import android.test.ApplicationTestCase;
//
///**
// * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
// */
//public class ApplicationTest extends ApplicationTestCase<Application> {
//    public ApplicationTest() {
//        super(Application.class);
//    }
//}