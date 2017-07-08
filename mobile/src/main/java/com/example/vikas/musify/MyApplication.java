package com.example.vikas.musify;

import android.app.Application;
import android.content.Intent;

import java.util.ArrayList;

public class MyApplication extends Application {

    private String someVariable;
    private ServiceCallbacks serviceCallbacks;


    public String getSomeVariable() {
        return someVariable;
    }

    public void setSomeVariable(String someVariable) {
        this.someVariable = someVariable;

        /*MainActivity test = new MainActivity();
        //test.test(someVariable);
        Intent myIntent = new Intent(MyApplication.this, Main2Activity.class);
        MyApplication.this.startActivity(myIntent);*/
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }


}