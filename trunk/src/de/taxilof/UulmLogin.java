/*
* ----------------------------------------------------------------------------
* "THE BEER-WARE LICENSE" (Revision 42):
* <simon.fuchs@uni-ulm.de> wrote this file. As long as you retain this notice you
* can do whatever you want with this stuff. If we meet some day, and you think
* this stuff is worth it, you can buy me a beer in return. Simon Fuchs
* ----------------------------------------------------------------------------
*/

package de.taxilof;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class UulmLogin extends Activity {
    private static final String PREFS_NAME = "UulmLoginPrefs";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // read preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String username = settings.getString("username", "");
        String password = settings.getString("password", "");
        // fill in textboxes
        EditText editUsername = (EditText) findViewById(R.id.editUsername);
        EditText editPassword = (EditText) findViewById(R.id.editPassword);
        editUsername.setText(username);
        editPassword.setText(password);   
    }
    
    private void savePrefs() {
    	// read textboxes
    	EditText editUsername = (EditText) findViewById(R.id.editUsername);
    	EditText editPassword = (EditText) findViewById(R.id.editPassword);
    	String username = editUsername.getText().toString();
    	String password = editPassword.getText().toString();
    	// save preferences
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("username", username);
    	editor.putString("password", password);
    	editor.commit();   	
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	savePrefs();
    }
    // used by the save&exit button
    public void exit(View view) {
    	this.finish();
    }
    // used by the login button
    public void login(View view) {
    	Log.d("uulmLogin","got Login Event,starting loginAgent");
    	savePrefs();
    	UulmLoginAgent loginAgent = new UulmLoginAgent (this.getBaseContext());
    	try {
    		loginAgent.login();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    }    
}