/*
 * Copyright (C) 2011 Daniel Berndt - Codeus Ltd - DateSlider
 * 
 * This class contains all the scrolling logic of the slidable elements
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ricardogarfe.renfe.widgets.dateslider.labeler;

import java.util.Calendar;

import android.util.Log;

import com.ricardogarfe.renfe.widgets.dateslider.TimeObject;

/**
 * A Labeler that displays times in increments of {@value #MINUTEINTERVAL}
 * minutes.
 */
public class TimeLabeler extends Labeler {
    public static int MINUTEINTERVAL = 15;

    private final String mFormatString;

    public TimeLabeler(String formatString) {
        super(80, 60);
        mFormatString = formatString;
    }

    @Override
    public TimeObject add(long time, int val) {
        return timeObjectfromCalendar(Util.addMinutes(time, val
                * MINUTEINTERVAL));
    }

    /**
     * override this method to set the inital TimeObject to a multiple of
     * MINUTEINTERVAL
     */
    @Override
    public TimeObject getElem(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) / MINUTEINTERVAL
                * MINUTEINTERVAL);
        Log.v("GETELEM", "getelem: " + c.get(Calendar.MINUTE));
        return timeObjectfromCalendar(c);
    }

    @Override
    protected TimeObject timeObjectfromCalendar(Calendar c) {
        return Util.getTime(c, mFormatString, MINUTEINTERVAL);
    }
}