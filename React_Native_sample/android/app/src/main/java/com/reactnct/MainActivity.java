package com.reactnct;

import android.os.Bundle;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.react.CleverTapModule;
import com.facebook.react.ReactActivity;


import java.util.List;


public class MainActivity<packages> extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "ReactnCT";
  }

  protected void onCreate(Bundle savedInstanceState) {
//    CleverTapAPI ct = CleverTapAPI.getDefaultInstance(getApplicationContext());


    super.onCreate(savedInstanceState);

//    CleverTapModule.setInitialUri(getIntent().getData());
  }



}
