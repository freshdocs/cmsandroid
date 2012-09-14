/*
 * Copyright (C) 2011-2012 Wglxy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zia.freshdocs.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zia.freshdocs.R;

/**
 * This is the base class for activities in the dashboard application.
 * It implements methods that are useful to all top level activities.
 * That includes: (1) stub methods for all the activity lifecycle methods;
 * (2) onClick methods for clicks on home, search, feature 1, feature 2, etc.
 * (3) a method for displaying a message to the screen via the Toast class.
 *
 */

public abstract class DashboardActivity extends ListActivity {

/**
 * Variable usePrettyGoodSolution determines if we use the so-called "pretty good solution" for supporting
 * large and xlarge screens.
 */

public static final boolean usePrettyGoodSolution = false;


/**
 * onCreate - called when the activity is first created.
 *
 * Called when the activity is first created. 
 * This is where you should do all of your normal static set up: create views, bind data to lists, etc. 
 * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
 * 
 * Always followed by onStart().
 *
 */

protected void onCreate(Bundle savedInstanceState) 
{
	this.setTheme(R.style.Theme_HoloEverywhereLight);
    super.onCreate(savedInstanceState);
//    setContentView(R.layout.favorites);
}
    
/**
 * onDestroy
 * The final call you receive before your activity is destroyed. 
 * This can happen either because the activity is finishing (someone called finish() on it, 
 * or because the system is temporarily destroying this instance of the activity to save space. 
 * You can distinguish between these two scenarios with the isFinishing() method.
 *
 */

protected void onDestroy ()
{
   super.onDestroy ();
}

/**
 * onPause
 * Called when the system is about to start resuming a previous activity. 
 * This is typically used to commit unsaved changes to persistent data, stop animations 
 * and other things that may be consuming CPU, etc. 
 * Implementations of this method must be very quick because the next activity will not be resumed 
 * until this method returns.
 * Followed by either onResume() if the activity returns back to the front, 
 * or onStop() if it becomes invisible to the user.
 *
 */

protected void onPause ()
{
   super.onPause ();
}

/**
 * onRestart
 * Called after your activity has been stopped, prior to it being started again.
 * Always followed by onStart().
 *
 */

protected void onRestart ()
{
   super.onRestart ();
}

/**
 * onResume
 * Called when the activity will start interacting with the user. 
 * At this point your activity is at the top of the activity stack, with user input going to it.
 * Always followed by onPause().
 *
 */

protected void onResume ()
{
   super.onResume ();
}

/**
 * onStart
 * Called when the activity is becoming visible to the user.
 * Followed by onResume() if the activity comes to the foreground, or onStop() if it becomes hidden.
 *
 */

protected void onStart ()
{
   super.onStart ();
}

/**
 * onStop
 * Called when the activity is no longer visible to the user
 * because another activity has been resumed and is covering this one. 
 * This may happen either because a new activity is being started, an existing one 
 * is being brought in front of this one, or this one is being destroyed.
 *
 * Followed by either onRestart() if this activity is coming back to interact with the user, 
 * or onDestroy() if this activity is going away.
 */

protected void onStop ()
{
   super.onStop ();
}

/**
 */
// Click Methods

/**
 * Handle the click on the home button.
 * 
 * @param v View
 * @return void
 */

public void onClickHome (View v)
{
    goHome (this);
}

/**
 * Handle the click on the search button.
 * 
 * @param v View
 * @return void
 */

public void onClickSearch (View v)
{
	onSearchRequested();
}

/**
 * Handle the click on the About button.
 * 
 * @param v View
 * @return void
 */

public void onClickAbout (View v)
{
    startActivity (new Intent(getApplicationContext(), AboutActivity.class));
}

/**
 */
// More Methods

/**
 * Go back to the home activity.
 * 
 * @param context Context
 * @return void
 */

public void goHome(Context context) 
{
    final Intent intent = new Intent(context, HomeActivity.class);
    intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
    context.startActivity (intent);
}

/**
 * Set the content view for the activity.
 *
 * If we are using the "pretty good solution" for tablets, the following is done.
 * If the current configuration is large or xlarge, the layout is actually placed in another
 * container defined by large.xml. See the definitions in the layout-large, layout-large-land,
 * layout-xlarge, layout-xlarge-land folders.
 * 
 * @param layoutId int - the resource id of the layout to use for the activity
 * @return void
 */

@Override public void setContentView (int layoutId)
{
   if (!usePrettyGoodSolution) {
      super.setContentView (layoutId);
      return;
   }

   Configuration c = getResources ().getConfiguration ();
   int size = c.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
   boolean addFrame = (size == Configuration.SCREENLAYOUT_SIZE_LARGE);

   //if (isLarge) System.out.println ("Large screen");

   int finalLayoutId = addFrame ? R.layout.large : layoutId;
   super.setContentView (finalLayoutId);

   if (addFrame) {
      LinearLayout frameView = (LinearLayout) findViewById (R.id.frame);
      if (frameView != null) {

         // If the frameView is there, inflate the layout given as an argument.
         // Attach it as a child to the frameView.
         LayoutInflater li = ((Activity) this).getLayoutInflater();
         View childView = li.inflate (layoutId, null);
         if (childView != null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                                                  (ViewGroup.LayoutParams.FILL_PARENT,
                                                   ViewGroup.LayoutParams.FILL_PARENT,
                                                   1.0F);
            frameView.addView (childView, lp);
            //childView.setBackgroundResource (R.color.background1);
         }

      }
   }
} // end setContentView

/**
 * Use the activity label to set the text in the activity's title text view.
 * The argument gives the name of the view.
 *
 * <p> This method is needed because we have a custom title bar rather than the default Android title bar.
 * See the theme definitons in styles.xml.
 * 
 * @param textViewId int
 * @return void
 */

public void setTitleFromActivityLabel (int textViewId)
{
    TextView tv = (TextView) findViewById (textViewId);
    if (tv != null) tv.setText (getTitle ());
} // end setTitleText

/**
 * Show a string on the screen via Toast.
 * 
 * @param msg String
 * @return void
 */

public void toast (String msg)
{
    Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_SHORT).show ();
} // end toast

/**
 * Send a message to the debug log and display it using Toast.
 */

public void trace (String msg) 
{
    Log.d("Demo", msg);
    toast (msg);
}

} // end class
