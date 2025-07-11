package com.dhammadownload.dhammadownloadandroid.common;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Chaw on 4/5/2017.
 */

public class TextUtils {
    public static Spannable makeTextPartBold(String mainText, String textToBold){
        final Pattern p = Pattern.compile(textToBold);
        final Matcher matcher = p.matcher(mainText);

        final SpannableStringBuilder spannable = new SpannableStringBuilder(mainText);
        final StyleSpan span =new StyleSpan(android.graphics.Typeface.BOLD);
        //final StyleSpan span=new android.text.style.StyleSpan(android.graphics.Typeface.BOLD);
        while (matcher.find()) {
            spannable.setSpan(
                    span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return  spannable;
    }
}
