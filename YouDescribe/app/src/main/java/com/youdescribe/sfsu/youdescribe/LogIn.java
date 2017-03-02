package com.youdescribe.sfsu.youdescribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by madhura on 2/15/2017.
 */

public class LogIn extends Activity {
    Button login;
    EditText userName, userPassword;
    int loginCounter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Intent videoListIntent = new Intent(this, VideoList.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        login = (Button) findViewById(R.id.login);
        userName = (EditText)findViewById(R.id.userName);
        userPassword = (EditText)findViewById(R.id.userPassword);

        login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(videoListIntent);
            }
        });
    }
}
