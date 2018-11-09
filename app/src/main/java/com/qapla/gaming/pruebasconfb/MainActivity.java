package com.qapla.gaming.pruebasconfb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (!isLoggedIn) {
            final LoginButton loginButton = findViewById(R.id.login_button);
            loginButton.setReadPermissions(Arrays.asList("email", "user_likes"));
            loginButton.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // App code
                }

                @Override
                public void onCancel() {
                    // App code
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                }
            });
        }else {
            final Activity activity = this;
            final Context context = this;
            new GraphRequest(
                accessToken,
                "/"+accessToken.getUserId()+"/permissions",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        int length = 0;
                        try {
                            length = response.getJSONObject().getJSONArray("data").length();
                        } catch (JSONException e) { e.printStackTrace(); }
                        boolean permExistsAndGranted = false;
                        for (int i = 0; i < length; i++){
                            String status = "";
                            String perm = "";
                            try {
                                status = response.getJSONObject().getJSONArray("data").getJSONObject(i).getString("status");
                            } catch (JSONException e) { e.printStackTrace(); }
                            try {
                                perm = response.getJSONObject().getJSONArray("data").getJSONObject(i).getString("permission");
                            } catch (JSONException e) { e.printStackTrace(); }
                            if (perm.equals("user_likes") && status.equals("granted")){
                                permExistsAndGranted = true;
                            }
                        }
                        if (!permExistsAndGranted){
                            Toast.makeText(context, "Concede los siguientes permisos en facebook para poder redimir este logro", Toast.LENGTH_LONG).show();
                            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("user_likes"));
                        }else {
                            String pageLink = "664390713617517";
                            String graphPath = "/" + accessToken.getUserId() + "/likes/"+pageLink;
                            new GraphRequest(
                                accessToken,
                                graphPath,
                                null,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response) {
                                        Log.d("Pages", response.getJSONObject().toString());
                                        if (response.getRawResponse().contains("[]")) {
                                            Toast.makeText(context, "No pudimos verificar el like", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(context, "Gracias, tus qaploins seran abonados en breve", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            ).executeAsync();
                        }
                    }
                }
            ).executeAsync();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}