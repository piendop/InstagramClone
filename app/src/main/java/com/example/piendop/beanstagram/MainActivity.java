package com.example.piendop.beanstagram;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    /*********************************************************************/
    @Override
    public void onClick(View view) {
        //when click to change state
        if(view.getId()==R.id.textView2){
            TextView textView1 = findViewById(R.id.textView);
            Button button = findViewById(R.id.button);
            if(((TextView)view).getText().toString().equals("Login")){
                ((TextView)view).setText("Sign up");
                textView1.setText("Don't have an account?");
                button.setText("Login");
            }else{
                ((TextView)view).setText("Login");
                textView1.setText("Already have an account?");
                button.setText("Sign up");
            }
        }else if(view.getId() == R.id.backgroundRelativeLayout ||//when click somewhere on the screen
                view.getId() == R.id.logoImageView){
            InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**close virtual keyboard when enter**/
        closeVirtualKeyboard();
        /**close virtual keyboard when click somewhere*/
        closeVirtualKeyboardWhenClick();

        /**CHANGE SIGNUP TO LOGIN AND VICE VERSA*/
        changeState();

        /****IF USER ALREADY LOGGED IN THE APP SHOW USER LIST**/
        if(ParseUser.getCurrentUser()!=null){
            showUserList();
        }
        /************************************************************************************/

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    /*******************************************************************************/
    public void closeVirtualKeyboardWhenClick() {
        RelativeLayout relativeLayout = findViewById(R.id.backgroundRelativeLayout);
        ImageView imageView =findViewById(R.id.logoImageView);
        relativeLayout.setOnClickListener(this);
        imageView.setOnClickListener(this);
    }

    /*******************************************************************/
    public void closeVirtualKeyboard() {
        (findViewById(R.id.password)).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i== KeyEvent.KEYCODE_ENTER && keyEvent.getAction()==KeyEvent.ACTION_DOWN){
                    getUserInfo(findViewById(R.id.button));
                }
                return false;
            }
        });
    }

    public void changeState() {
        TextView textView = findViewById(R.id.textView2);
        //listen when click to text view
        textView.setOnClickListener(this);
    }


    /**********************************************************************************************/
    /************ACTION WHEN CLICK BUTTON**************************************/
    public void getUserInfo(View view)  {
        //get username and password from edit text
        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        String state = ((Button)view).getText().toString();
        /**CHECK VALID INFORMATION*/
        checkValidInformation(username,password,state);
    }

    /**CHECK VALID INFORMATION*/
    private void checkValidInformation(String username, String password,String state)  {

        //check that user enter username/password or not
        if(username.isEmpty()||password.isEmpty()){
            Toast.makeText(MainActivity.this,"Empty username or password!",Toast.LENGTH_SHORT).show();
            return;
        }

        //check that if its loggin or signup state
        if(state.equals("Login")){
            checkValidLogin(username,password);
        }else{
            checkValidSignup(username,password);
        }
    }



    /**CHECK USER LOGIN CORRECT INFORMATION*/
    private void checkValidLogin(String username, String password)  {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(user!=null){
                    Log.i("Login","Successful");
                    /******START NEW ACTIVITY WHEN CLICK ON BUTTON SUCCESSFULLY*******/
                    showUserList();

                }else{
                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**CHECK USER SIGNUP APPROPRIATE INFORMATION*/
    private void checkValidSignup(String username, String password)  {
        ParseUser parseUser = new ParseUser();

        parseUser.setUsername(username);
        parseUser.setPassword(password);

        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Log.i("Sign up","Successful");
                    showUserList();
                }else{
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showUserList() {
        /******START NEW ACTIVITY WHEN CLICK ON BUTTON SUCCESSFULLY*******/
        Intent intent = new Intent(this,UserListActivity.class);
        startActivity(intent);
    }

}
