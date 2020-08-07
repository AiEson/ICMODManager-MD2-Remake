package icmod.wvt.com.icmod.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final FloatingActionButton loginButton = findViewById(R.id.login);
        final ProgressBar progressBar = findViewById(R.id.login_progressbar);
        final TextInputLayout accountTextInputLayout = findViewById(R.id.accountInputLayout);
        final TextInputLayout passwordTextInputLayout = findViewById(R.id.passwordInputLayout);
        final MaterialButton registerButton = findViewById(R.id.register);

        passwordTextInputLayout.setErrorEnabled(false);
        accountTextInputLayout.setErrorEnabled(false);

        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString();
            String passwd = passwordEditText.getText().toString();
            if (email.length() != 0) {
                Animation animationOut = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fab_out);
                loginButton.startAnimation(animationOut);
                loginButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    String ret2 = null;
                    String Resultms = null;
                    String lines;
                    StringBuilder response = new StringBuilder();
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL("https://adodoz.cn/app_login.php");
                        //连接服务器
                        connection = (HttpURLConnection) url.openConnection();
                        //上传服务器内容
                        connection.setRequestMethod("POST");
                        connection.setConnectTimeout(8000);
                        connection.setDoInput(true);//允许输入
                        connection.setDoOutput(true);//允许输出
                        connection.setUseCaches(false);
                        connection.setRequestProperty("Accept-Charset", "UTF-8");
                        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                        connection.connect();
                        DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                        outStream.writeBytes("username=" + email + "&password=" + passwd + "&type=" + "email");
                        outStream.flush();
                        outStream.close();
                        //读取响应
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        //读数据
                        while ((lines = reader.readLine()) != null) {
                            lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
                            response.append(lines);
                        }
                        ret2 = response.toString().trim();
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(ret2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    JSONObject finalJsonObject = jsonObject;
                    LoginActivity.this.runOnUiThread(() -> {
                        try {
                            if (finalJsonObject.isNull("code")) {
                                JSONObject userInfo = null;
                                try {
                                    userInfo = finalJsonObject.getJSONObject("user_info");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Toast.makeText(LoginActivity.this, "登录成功，欢迎您 " + userInfo.getString("user_name"), Toast.LENGTH_LONG).show();
                                    Algorithm.writeFile(FinalValuable.UserInfo, finalJsonObject.toString());
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                                LoginActivity.this.finish();
                            } else {
                                print("登录失败！请检查您的账户和密码再试", accountTextInputLayout, Snackbar.LENGTH_SHORT, true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            print("登录失败！服务器错误", accountTextInputLayout, Snackbar.LENGTH_SHORT, true);
                        }
                        Animation animationIn = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fab_in);
                        loginButton.startAnimation(animationIn);
                        loginButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    });
                }).start();
            } else {
                accountTextInputLayout.setError("请输入您的邮箱");
                if (passwd.length() == 0)
                {
                    passwordTextInputLayout.setError("请输入您的密码");
                }
            }

        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://adodoz.cn")
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    protected void print(String str, View view, int time, boolean up) {
        if (up)
            Snackbar.make(view, str, time).setAnchorView(view).show();
        else
            Snackbar.make(view, str, time).show();
    }

}