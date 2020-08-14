package icmod.wvt.com.icmod.ui;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.HorizonPack;
import icmod.wvt.com.icmod.others.LruCacheUtils;
import icmod.wvt.com.icmod.others.SuperInstallSystem;
import icmod.wvt.com.icmod.ui.download.DownloadFragment;
import icmod.wvt.com.icmod.ui.filechoose.FileChooseActivity;
import icmod.wvt.com.icmod.ui.forum.ForumFragment;
import icmod.wvt.com.icmod.ui.home.HomeFragment;
import icmod.wvt.com.icmod.ui.horizon.PackFragment;

import static icmod.wvt.com.icmod.others.Algorithm.createHorizonFlashFile;

public class MainActivity extends AppCompatActivity {
    //其他定义部分
    private AppBarConfiguration mAppBarConfiguration;
    //控件定义部分
    static FloatingActionButton fab;
    static ImageView sidebarImageView;
    static TextView sidebarUserName;
    CoordinatorLayout constraintLayout;
    DrawerLayout drawer;

    public LruCacheUtils lruCacheUtils;

    static MenuItem searchItem, hzItem;


    //数据定义部分
    static File userData;
    Fragment currentFragment = null;
    private long firstTime = 0;
    public static final String SHARE_APP_TAG = "SHARE_APP_TAG";
    SharedPreferences setting;
    int nowType = FinalValuable.FragmentTypeHome;

    //变量定义部分
    static boolean haveUserData;
    static int downloadType = FinalValuable.OnlineGf;//定义搜索框使用的类型阈值，判断是处于哪一下载页面
    static int globalTab = 0;
    static boolean tabHasSwitch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("本地管理");
        constraintLayout = findViewById(R.id.main_layout);
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            FinalValuable.lruCacheUtils = this.lruCacheUtils;
            Intent intent = new Intent(this, FileChooseActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, view, "shared_element_container");
            startActivity(intent, options.toBundle());
        });
        FragmentManager fragmentManager = getSupportFragmentManager();
        FinalValuable.MainActivityContext = MainActivity.this;
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        FileDownloader.setup(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //为了保证动画结束之后再显示，使用动画监听
        //先初始化全局TAB为Home
        globalTab = R.id.nav_home;
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                //关闭，说明动画执行完毕

                if (tabHasSwitch) {
                    //如果TAB执行切换
                    tabHasSwitch = false;
                    switch (globalTab) {
                        case R.id.nav_home:
                            nowType = FinalValuable.FragmentTypeHome;
                            toolbar.setVisibility(View.VISIBLE);
                            new Thread(() -> {
                                FinalValuable.fragmentHome = new HomeFragment();
                                MainActivity.this.runOnUiThread(() -> {
                                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, FinalValuable.fragmentHome).commit();
                                });
                            }).start();
                            setSearchItemVisible(false);
                            setHzDownloadItemVisible(false);
                            break;

                        case R.id.hz_pack:
                            fab.setVisibility(View.VISIBLE);
                            nowType = FinalValuable.FragmentTypeHorizon;
                            toolbar.setVisibility(View.VISIBLE);
                            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, new PackFragment()).commit();
                            setSearchItemVisible(false);
                            setHzDownloadItemVisible(true);
                            break;

                        case R.id.nav_download:
                            toolbar.setVisibility(View.VISIBLE);
                            FinalValuable.fragmentDownload = new DownloadFragment();
                            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, FinalValuable.fragmentDownload).commit();
                            setSearchItemVisible(true);
                            setHzDownloadItemVisible(false);
                            break;

                        case R.id.nav_forum:
                            fab.setVisibility(View.GONE);
                            new Thread(() -> {
                                MainActivity.this.runOnUiThread(() -> {
                                    toolbar.setVisibility(View.GONE);
                                    fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, new ForumFragment()).commit();
                                });
                            }).start();
                            break;
                    }
                }

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {


            switch (item.getItemId()) {
                case R.id.nav_home:
                case R.id.nav_download:
                case R.id.nav_forum:
                case R.id.hz_pack:
                    globalTab = item.getItemId();
                    tabHasSwitch = true;
                    drawer.closeDrawer(Gravity.LEFT);
                    break;


                case R.id.settings:
                    startActivityOfClass(SettingsActivity.class);
                    break;
                case R.id.openic:
                    if (!Algorithm.openApp("com.zheka.horizon", MainActivity.this))
                    if (!Algorithm.openApp("com.zhekasmirnov.innercore", MainActivity.this))
                        print("进入失败", Snackbar.LENGTH_SHORT);
                    break;

                case R.id.join:
                    if (Algorithm.isNetworkAvailable(MainActivity.this)) {
                        String urlStr = "https://adodoz.cn/QQGroup.json";
                        getqqgroup_json();
                    } else {
                        print("请连接您的网络再试", Snackbar.LENGTH_LONG);
                    }
                    break;
                case R.id.donate:
                    showDonateDialog();
                    break;
            }
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        sidebarImageView = headerView.findViewById(R.id.head_imageView);
        sidebarUserName = headerView.findViewById(R.id.head_textview1);
        lruCacheUtils = new LruCacheUtils(this);
        lruCacheUtils.savePicToMemory("null", Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.no_logo));
        lruCacheUtils.savePicToMemory("file", Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.round_description_black_36dp));
        lruCacheUtils.savePicToMemory("folder", Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.round_folder_open_black_36dp));
        initListener();
        flashUser();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        if (!Algorithm.isAvailable(MainActivity.this, "com.mojang.minecraftpe")) {
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("警告")
                    .setMessage("检测到您未安装Minecraft国际版，不能正常进入InnerCore，是否跳转去下载伪装软件？（安装Minecraft原版需要卸载伪装）")
                    .setNegativeButton("不用了", null)
                    .setPositiveButton("去下载", (dialogInterface, i) -> {
                        Intent intent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://adodoz.cn/MinecraftCamouflage.apk")
                        );
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    })
                    .create().show();
        }
        setting = getSharedPreferences(SHARE_APP_TAG, 0);
        boolean user_first = setting.getBoolean("FIRST", true);

        if (user_first) {
            //第一次启动
            setting.edit().putBoolean("FIRST", false).apply();
            drawer.openDrawer(GravityCompat.START);
        }

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
            @Override
            public void onDrawerOpened(@NonNull View drawerView) { }
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                boolean user_first = setting.getBoolean("FIRST_CloseDrawer", true);

                if (user_first) {
                    setting.edit().putBoolean("FIRST_CloseDrawer", false).apply();
                    print("长按条目可进入多选模式", Snackbar.LENGTH_LONG);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        //更新检查
        if (Algorithm.isNetworkAvailable(MainActivity.this))
            new Thread(() -> {
                try {
                    String ret2 = null;
                    String Resultms = null;
                    String lines;
                    StringBuilder response = new StringBuilder();
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL("https://adodoz.cn/ICMODManagerAPI.php");
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
                        outStream.writeBytes("order=getupdate");
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
                    JSONObject jsonObject = new JSONObject(ret2);
                    if (jsonObject != null) {
                        if (jsonObject.getInt("versioncode") != Algorithm.getVersionCode(MainActivity.this)) {
                            MainActivity.this.runOnUiThread(() -> {
                                new MaterialAlertDialogBuilder(MainActivity.this)
                                        .setTitle("更新提醒")
                                        .setMessage("发现新版本，请立即更新")
                                        .setNegativeButton("取消", (dialogInterface, i) -> {

                                        })
                                        .setPositiveButton("去更新", (dialogInterface, i) -> {
                                            Intent intent = new Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://www.coolapk.com/game/icmod.wvt.com.icmod")
                                            );
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }).create().show();
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        //异步加载MOD数据，从MainActivity启动开始
        //加载官网数据，状态放入FinalValuable同步
        FileDownloader.getImpl().create("https://adodoz.cn/mods/allmodinfo.json").setPath(FinalValuable.NetModDataGw)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FinalValuable.presentLoadingGw = Algorithm.getPercent(soFarBytes, totalBytes);
                            }
                        }).start();
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        new Thread(() -> {
                            File file = new File(FinalValuable.NetModDataGw);
                            if (file.exists()) {
                                try {
                                    FinalValuable.jsonArrayGw = new JSONArray(Algorithm.readFile(file));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            FinalValuable.loadingFinishGw = true;
                            FinalValuable.loadingErrorGw = false;
                        }).start();
                        Log.e("TAG", String.valueOf(FinalValuable.jsonArrayGw));
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        FinalValuable.loadingFinishGw = false;
                        FinalValuable.loadingErrorGw = true;
                        e.printStackTrace();
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();

        //同步汉化组数据
        new Thread(() -> {
            try {
                String ret2 = null;
                ret2 = Algorithm.Post("", "https://dev.adodoz.cn/api/mod/list", MainActivity.this);
                Log.e("TAG", ret2);
                try {
                    FinalValuable.jsonArrayHhz = new JSONObject(ret2).getJSONArray("data");
                    Algorithm.writeFile(FinalValuable.NetModDataHhz, ret2);
                } catch (Exception e) {
                    e.printStackTrace();
                    FinalValuable.loadingErrorHhz = false;
                    FinalValuable.loadingFinishHhz = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                FinalValuable.loadingErrorHhz = true;
                FinalValuable.loadingFinishHhz = false;
            }
        }).start();

    }



    public void fabIn() {
        Animation animationIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_in);
        fab.startAnimation(animationIn);
        fab.setVisibility(View.VISIBLE);
    }

    public void fabOut() {
        Animation animationOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_out);
        fab.startAnimation(animationOut);
        fab.setVisibility(View.GONE);
    }

    public static void setSearchItemVisible(boolean b) {
        searchItem.setVisible(b);
    }

    public static void setHzDownloadItemVisible(boolean b) {
        hzItem.setVisible(b);
    }

    public static FloatingActionButton getFab() {
        return fab;
    }

    private void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (!targetFragment.isAdded()) {
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.nav_host_fragment, targetFragment);
            transaction.commit();
        } else {
            transaction
                    .hide(currentFragment)
                    .show(targetFragment)
                    .commit();
        }
        currentFragment = targetFragment;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                print("再按一次退出程序", Snackbar.LENGTH_SHORT);
                firstTime = secondTime;
            } else {
                finish();
            }
        }
    }

    public LruCacheUtils getLruCacheUtils() {
        return lruCacheUtils;
    }

    public void initListener() {
        sidebarImageView.setOnClickListener(v -> {
            if (!haveUserData)
                startActivityOfClass(LoginActivity.class);
            else {
                try {
                    final File userData = new File(FinalValuable.UserInfo);
                    JSONObject json = new JSONObject(Algorithm.readFile(userData));
                    JSONObject userInfo = json.getJSONObject("user_info");
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("用户信息")
                            .setMessage("ID：" + userInfo.getInt("user_id") + "\n名称：" + userInfo.getString("user_name"))
                            .setNegativeButton("返回", (dialog, which) -> {

                            })
                            .setPositiveButton("注销登录", (dialog, which) -> {
                                sidebarImageView.setImageBitmap(Algorithm.getBitmapFromRes(MainActivity.this, R.drawable.baseline_account_circle_black_24dp));
                                sidebarUserName.setText("点击头像以登录");
                                Algorithm.deleteFile(userData);
                                haveUserData = false;
                            })
                            .create().show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void showDonateDialog() {
        List<String> nameList = new ArrayList<>();
        nameList.add("支付宝捐赠");
        nameList.add("微信捐赠");

        AlertDialog alertDialogWindow = null;
        final MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        ListView listView = new ListView(MainActivity.this);
        listView.setPadding(10, 10, 10, 5);
        QQAdapter qqAdapter = new QQAdapter(MainActivity.this, R.layout.qqgroup, nameList);
        listView.setAdapter(qqAdapter);
        listView.setDivider(new ColorDrawable(Color.alpha(0)));
        listView.setDividerHeight(0);
        alertDialogWindow = alertDialog.setView(layout)
                .setTitle("请选择您的捐赠方式")
                .setNegativeButton("捐赠列表", (dialog, which) -> {
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("正在获取捐赠信息...");
                    progressDialog.show();
                    if (Algorithm.isNetworkAvailable(MainActivity.this))
                        new Thread(() -> {
                            try {
                                String ret2 = null;
                                String Resultms = null;
                                String lines;
                                StringBuilder response = new StringBuilder();
                                HttpURLConnection connection = null;
                                try {
                                    URL url = new URL("https://adodoz.cn/ICMODManagerAPI.php");
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
                                    outStream.writeBytes("order=donate");
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
                                JSONArray jsonArray = new JSONArray(ret2);
                                List<String> donateList = new ArrayList<>();
                                if (jsonArray != null) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        if (i == 0) {
                                            donateList.add(jsonObject.getString("name"));
                                        } else {
                                            donateList.add(jsonObject.getString("name") + " : " + jsonObject.getString("money"));
                                        }
                                    }
                                    MainActivity.this.runOnUiThread(() -> {

                                        LinearLayout layout2 = new LinearLayout(MainActivity.this);
                                        layout2.setLayoutParams(params);
                                        layout2.setGravity(Gravity.CENTER);
                                        layout2.setOrientation(LinearLayout.VERTICAL);
                                        ListView listView2 = new ListView(MainActivity.this);
                                        listView2.setPadding(10, 10, 10, 5);
                                        QQAdapter qqAdapter2 = new QQAdapter(MainActivity.this, R.layout.qqgroup, donateList);
                                        listView2.setAdapter(qqAdapter2);
                                        listView2.setDivider(new ColorDrawable(Color.alpha(0)));
                                        listView2.setDividerHeight(0);
                                        layout2.addView(listView2);
                                        AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this)
                                                .setView(layout2)
                                                .setTitle("感谢下列小伙伴的捐赠")
                                                .setPositiveButton("关闭", (dialogInterface, i) -> {

                                                }).create();
                                        alertDialog2.show();
                                        progressDialog.dismiss();
                                    });
                                } else {
                                    MainActivity.this.runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "获取捐赠信息失败", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                })
                .setPositiveButton("为什么？", (dialogInterface, i) -> {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("关于捐赠")
                            .setMessage("本软件开发者为在校高中学生，无收入来承受高昂的服务器费用\n如果您手头富足且愿意支持ICCN（InnerCore China）的发展，请助我们一臂之力\n毕竟用爱发电不是长久之计");
                    dialog.show();
                })
                .show();
        final AlertDialog finalAlertDialogWindow = alertDialogWindow;
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i) {
                case 0:
                    donateAlipay();
                    finalAlertDialogWindow.dismiss();
                    break;
                case 1:
                    donateWeixin();
                    finalAlertDialogWindow.dismiss();
                    break;
            }

        });
        layout.addView(listView);

    }

    /**
     * 支付宝支付
     **/
    private void donateAlipay() {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(this);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(this, "fkx18184vir1w8crfl6vsa9");
        }
    }

    /**
     * 需要提前准备好 微信收款码 照片，可通过微信客户端生成
     */
    private void donateWeixin() {
        Toast.makeText(MainActivity.this, "付款码图片已保存，请手动打开本地相册扫码", Toast.LENGTH_LONG).show();
        InputStream weixinQrIs = getResources().openRawResource(R.raw.wxfkm);
        String qrPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" + File.separator + "Donate" + File.separator +
                "Donate.png";
        WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs));
        WeiXinDonate.donateViaWeiXin(this, qrPath);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File flashFile = new File(FinalValuable.flashHomeData);
        File flashHzFile = new File(FinalValuable.flashHorizonData);
        if (flashFile.exists() && nowType == FinalValuable.FragmentTypeHome) {
            ProgressDialog progressDialog1 = new ProgressDialog(MainActivity.this);
            progressDialog1.setCancelable(false);
            progressDialog1.setMessage("正在载入数据，请稍等...");
            progressDialog1.show();
            new Thread(() -> {
                FinalValuable.fragmentHome = new HomeFragment();
                Algorithm.deleteFile(flashFile);
                MainActivity.this.runOnUiThread(() -> {
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, FinalValuable.fragmentHome).commit();
                    progressDialog1.dismiss();
                });
            }).start();
        } else if (flashHzFile.exists() && nowType == FinalValuable.FragmentTypeHorizon) {
            ProgressDialog progressDialog1 = new ProgressDialog(MainActivity.this);
            progressDialog1.setCancelable(false);
            progressDialog1.setMessage("正在载入数据，请稍等...");
            progressDialog1.show();
            new Thread(() -> {
                PackFragment packFragment = new PackFragment();
                Algorithm.deleteFile(flashHzFile);
                MainActivity.this.runOnUiThread(() -> {
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, packFragment).commit();
                    progressDialog1.dismiss();
                });
            }).start();
        }

    }

    public static void print(String string, int time) {
        Snackbar.make(fab, string, time).setAnchorView(fab).show();
    }

    public static void setDownloadType(int downloadType) {
        MainActivity.downloadType = downloadType;
    }

    public static int getDownloadType() {
        return downloadType;
    }

    protected void startActivityOfClass(Class mClass) {
        Intent intent = new Intent(MainActivity.this, mClass);
        startActivity(intent);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    static class load_avatar extends AsyncTask<Void, Void, Bitmap> {
        String url;
        String name;

        load_avatar(String url, String name) {
            this.url = url;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sidebarUserName.setText("正在登录，请稍候...");
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            return Algorithm.getImageBitmapFromUrl(url);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap == null) {
                print("登录失败，请重新登录", Snackbar.LENGTH_LONG);
                Algorithm.deleteFile(userData);
            } else {
                sidebarUserName.setText("欢迎您，" + name);
                sidebarImageView.setImageBitmap(bitmap);
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search) {
            return true;
        } else if (id == R.id.download_hzpack) {
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("正在获取包信息...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            List<HorizonPack> list = new ArrayList<>();
            new Thread(() -> {
                String aaa = "";
                JSONObject root;
                JSONArray packArray = null;
                JSONObject manifestJSON = null;
                try {
                    aaa = Algorithm.Get("https://cdn.jsdelivr.net/gh/WvTStudio/horizon-cloud-config@master/packs.json", "", false);
                    root = new JSONObject(aaa);
                    packArray = root.getJSONArray("packs");
                    for (int i = 0; i < packArray.length(); i++) {
                        JSONObject packObject = packArray.getJSONObject(i);
                        String manifestFileUrl = packObject.getString("manifest");
                        manifestJSON = new JSONObject(Algorithm.Get(manifestFileUrl, "", false));
                        HorizonPack horizonPack = new HorizonPack();
                        horizonPack.setPack(manifestJSON.getString("pack"));
                        horizonPack.setPackVersion(manifestJSON.getString("packVersion"));
                        list.add(horizonPack);
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                JSONArray finalPackArray = packArray;
                JSONObject finalManifestJSON = manifestJSON;
                MainActivity.this.runOnUiThread(() -> {
                    //这个xml里只有ListView，直接拿过来用pwq
                    View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.fragment_download_list, null, false);
                    ListView listView = view.findViewById(R.id.download_fragment_listView);
                    PackChooseAdapter packChooseAdapter = new PackChooseAdapter(MainActivity.this, R.layout.open_horizon_item, list);
                    listView.setAdapter(packChooseAdapter);
                    listView.setOnItemClickListener((parent, view1, position, id1) -> {
                        final EditText editText2 = new EditText(MainActivity.this);
                        editText2.setHint("留空默认为" + list.get(position).getPack() + " ManagerInstall");
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("请输入名称")
                                .setView(editText2)
                                .setCancelable(false)
                                .setPositiveButton("确定", (dialog1, which1) -> {
                                    FinalValuable.alertDialog.dismiss();
                                    String str = editText2.getText().toString();
                                    if (str.length() == 0) {
                                        str = list.get(position).getPack() + " ManagerInstall";
                                    }
                                    String folderName = str.replace(" ", "_");
                                    try {
                                        ProgressDialog progressDialog1 = new ProgressDialog(MainActivity.this);
                                        progressDialog1.setMessage("正在开始下载...");
                                        progressDialog1.setCancelable(false);
                                        progressDialog1.show();
                                        JSONObject pack = finalPackArray.getJSONObject(position);
                                        JSONObject packList = pack.getJSONObject("package");
                                        FileDownloader.getImpl().create(pack.getString("graphics")).setPath(FinalValuable.DownLoadPath + File.separator + folderName + File.separator + "graphics.zip")
                                                .setListener(new FileDownloadListener() {
                                                    @Override
                                                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                                                    }

                                                    @Override
                                                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                                        if (totalBytes == -1)
                                                            progressDialog1.setMessage("已下载：" + Algorithm.readableFileSize(soFarBytes));
                                                        else
                                                            progressDialog1.setMessage("已下载：" + Algorithm.readableFileSize(soFarBytes) + "  " + Algorithm.getPercent(soFarBytes, totalBytes));
                                                    }

                                                    @Override
                                                    protected void completed(BaseDownloadTask task) {
                                                        new Thread(() -> {
                                                            try {
                                                                Algorithm.writeFile(FinalValuable.DownLoadPath + File.separator + folderName + File.separator + "manifest.json", Algorithm.formatJson(finalManifestJSON.toString()));
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }).start();
                                                        int max = 0;
                                                        for (int i = 1; !packList.isNull("part" + Algorithm.leftPad(i, 4, '0')); i++) {
                                                            max = i;
                                                        }
                                                        JSONObject jsonObject = new JSONObject();
                                                        for (int jj = 0; jj <= max; jj++) {
                                                            try {
                                                                jsonObject.put("" + jj, "");
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        final int[] completeNum = {0};
                                                        for (int i = 1; !packList.isNull("part" + Algorithm.leftPad(i, 4, '0')); i++) {
                                                            try {
                                                                int finalI = i;
                                                                int finalMax = max;
                                                                FileDownloader.getImpl().create(packList.getString("part" + Algorithm.leftPad(i, 4, '0'))).setPath(FinalValuable.DownLoadPath + File.separator + folderName + File.separator + "part" + Algorithm.leftPad(i, 4, '0'))
                                                                        .setListener(new FileDownloadLargeFileListener() {
                                                                            @Override
                                                                            protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                                                                            }

                                                                            @Override
                                                                            protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                                                                                if (totalBytes == -1) {
                                                                                    try {
                                                                                        jsonObject.put(finalI + "", "包" + finalI + "已下载：" + Algorithm.readableFileSize(soFarBytes));
                                                                                    } catch (JSONException e) {
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                } else {
                                                                                    try {
                                                                                        jsonObject.put(finalI + "", "包" + finalI + "已下载：" + Algorithm.readableFileSize(soFarBytes) + "  " + Algorithm.getPercent(soFarBytes, totalBytes));
                                                                                    } catch (JSONException e) {
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                }
                                                                                StringBuilder s = new StringBuilder("下载状态：\n");
                                                                                for (int i1 = 1; i1 <= finalMax; i1++) {
                                                                                    try {
                                                                                        s.append(jsonObject.getString(i1 + "")).append("\n");
                                                                                    } catch (JSONException e) {
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                }
                                                                                progressDialog1.setMessage(s.toString());
                                                                            }

                                                                            @Override
                                                                            protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                                                                            }

                                                                            @Override
                                                                            protected void completed(BaseDownloadTask task) {
                                                                                try {
                                                                                    jsonObject.put(finalI + "", "包" + finalI + "下载完成");
                                                                                } catch (JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                                completeNum[0] += 1;
                                                                                if (completeNum[0] == finalMax) {
                                                                                    progressDialog1.setMessage("下载完成，正在合并文件...");
                                                                                    new Thread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            // 将碎片文件存入到集合中
                                                                                            List<FileInputStream> al = new ArrayList<FileInputStream>();
                                                                                            for (int iii = 1; iii <= finalMax; iii++) {
                                                                                                try {
                                                                                                    al.add(new FileInputStream(new File(FinalValuable.DownLoadPath + File.separator + folderName + File.separator + "part" + Algorithm.leftPad(iii, 4, '0'))));

                                                                                                } catch (Exception e) {
                                                                                                    // 异常
                                                                                                    e.printStackTrace();
                                                                                                }
                                                                                            }
                                                                                            try {
                                                                                                // 构建文件流集合
                                                                                                Enumeration<FileInputStream> en = Collections.enumeration(al);
                                                                                                // 将多个流合成序列流
                                                                                                SequenceInputStream sis = new SequenceInputStream(en);
                                                                                                FileOutputStream fos = new FileOutputStream(new File(FinalValuable.DownLoadPath + File.separator + folderName + File.separator + "pack.zip"));
                                                                                                byte[] b = new byte[1024];
                                                                                                int len = 0;
                                                                                                while ((len = sis.read(b)) != -1) {
                                                                                                    fos.write(b, 0, len);
                                                                                                }
                                                                                                fos.close();
                                                                                                sis.close();
                                                                                            } catch (Exception e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                            MainActivity.this.runOnUiThread(() -> {
                                                                                                progressDialog1.setMessage("合并完毕，正在删除无关文件...");
                                                                                                new Thread(() -> {
                                                                                                    for (int iii = 1; iii <= finalMax; iii++) {
                                                                                                        Algorithm.deleteFile(new File(FinalValuable.DownLoadPath + File.separator + folderName + File.separator + "part" + Algorithm.leftPad(iii, 4, '0')));
                                                                                                    }
                                                                                                    MainActivity.this.runOnUiThread(() -> {
                                                                                                        progressDialog1.setMessage("删除完毕，正在打包为安装文件...");
                                                                                                        new Thread(() -> {
                                                                                                            File[] files = new File(FinalValuable.DownLoadPath + File.separator + folderName).listFiles();
                                                                                                            List<File> fileList = new ArrayList<>();
                                                                                                            for (File file : files) {
                                                                                                                fileList.add(file);
                                                                                                            }
                                                                                                            Algorithm.zipFile(fileList, FinalValuable.DownLoadPath + File.separator + folderName + ".zip");
                                                                                                            MainActivity.this.runOnUiThread(() -> {
                                                                                                                progressDialog1.setMessage("压缩完成，正在为您安装...");
                                                                                                                new Thread(() -> {
                                                                                                                    while (true) {
                                                                                                                        if (!progressDialog1.isShowing()) break;
                                                                                                                        try {
                                                                                                                            Thread.sleep(20);
                                                                                                                        } catch (InterruptedException e) {
                                                                                                                            e.printStackTrace();
                                                                                                                        }
                                                                                                                        MainActivity.this.runOnUiThread(() -> progressDialog1.setMessage("正在为您自动安装...\n" + FinalValuable.installState));
                                                                                                                    }
                                                                                                                }).start();
                                                                                                                new Thread(() -> {
                                                                                                                    String statue = new SuperInstallSystem().intall(FinalValuable.DownLoadPath + File.separator + folderName + ".zip");
                                                                                                                            createHorizonFlashFile();
                                                                                                                            MainActivity.this.runOnUiThread(() -> {
                                                                                                                                progressDialog1.dismiss();
                                                                                                                                print("安装完成", Snackbar.LENGTH_SHORT);
                                                                                                                                createHorizonFlashFile();
                                                                                                                                onResume();
                                                                                                                            });
                                                                                                                }).start();
                                                                                                            });
                                                                                                        }).start();
                                                                                                    });
                                                                                                }).start();
                                                                                            });
                                                                                        }
                                                                                    }).start();
                                                                                }
                                                                            }

                                                                            @Override
                                                                            protected void error(BaseDownloadTask task, Throwable e) {

                                                                            }

                                                                            @Override
                                                                            protected void warn(BaseDownloadTask task) {

                                                                            }
                                                                        }).start();
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                                                    }

                                                    @Override
                                                    protected void error(BaseDownloadTask task, Throwable e) {

                                                    }

                                                    @Override
                                                    protected void warn(BaseDownloadTask task) {

                                                    }
                                                }).start();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }).setNegativeButton("取消", null)
                        .create().show();

                    });
                    FinalValuable.alertDialog = new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("选择一个要安装的包")
                            .setView(view)
                            .setNegativeButton("取消", null)
                            .create();
                    FinalValuable.alertDialog.show();
                    progressDialog.dismiss();
                });
            }).start();
        }

        return super.onOptionsItemSelected(item);
    }

    class PackChooseAdapter extends ArrayAdapter<HorizonPack> {

        private int resourceId;

        PackChooseAdapter(Context context, int textViewResourceId, List<HorizonPack> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;

        }

        @NotNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HorizonPack item = getItem(position); //获取当前项的Fruit实例
            @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            TextView title = view.findViewById(R.id.horizon_itemsettingTextView1);
            TextView hint = view.findViewById(R.id.horizon_itemsettingTextView2);
            MaterialCardView cardView = view.findViewById(R.id.horizon_cardView);
            ImageView menuImageView = view.findViewById(R.id.horizon_itemsettingImageView1);
            ImageView expandImageView = view.findViewById(R.id.horizon_itemsettingImageView2);
            menuImageView.setVisibility(View.GONE);
            expandImageView.setVisibility(View.GONE);
            title.setText(item.getPack());
            hint.setText(item.getPackVersion());
            return view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        boolean user_first = setting.getBoolean("FIRST_Search", true);

        if (user_first) {
            //第一次启动
            setting.edit().putBoolean("FIRST_Search", false).apply();
            searchView.onActionViewExpanded();
        }

        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //关闭，但不确定另外一个Fragment状态，直接回到初始状态
                drawer.closeDrawer(Gravity.LEFT);
                FinalValuable.fragmentDownload = new DownloadFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, FinalValuable.fragmentDownload).commit();
                setSearchItemVisible(true);
                //把数据源切回官网源
                downloadType = FinalValuable.OnlineGf;
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && query.length() > 0) {
                    FinalValuable.fragmentDownload.flashList(query, downloadType);
                    searchView.setIconified(true);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.download_hzpack).setVisible(false);
        searchItem = menu.findItem(R.id.search);
        hzItem = menu.findItem(R.id.download_hzpack);
        return true;
    }

    public static void flashUser() {
        try {
            userData = new File(FinalValuable.UserInfo);
            if (userData.exists()) {
                try {
                    haveUserData = true;
                    JSONObject json = new JSONObject(Algorithm.readFile(userData));
                    JSONObject userInfo = json.getJSONObject("user_info");
                    load_avatar loadAvatar = new load_avatar(userInfo.getString("user_avatar"), userInfo.getString("user_name"));
                    loadAvatar.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    print("出现未知错误，请注销后再次登录", Snackbar.LENGTH_LONG);
                }
            } else {
                haveUserData = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FinalValuable.MainActivityContext = MainActivity.this;
        if (!haveUserData)
            flashUser();
    }


    protected void getqqgroup_json() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("获取群聊信息中...");
        progressDialog.show();
        FileDownloader.getImpl().create("https://adodoz.cn/QQGroup.json").setPath(FinalValuable.QQGroupJson)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        List<String> nameList = new ArrayList<>();
                        final List<String> urlList = new ArrayList<>();
                        try {
                            String nr = Algorithm.readFile(new File(FinalValuable.QQGroupJson));
                            JSONArray jsonArray = new JSONArray(nr);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                nameList.add(jsonObject.getString("name"));
                                urlList.add(jsonObject.getString("url"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AlertDialog alertDialogWindow = null;
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        LinearLayout layout = new LinearLayout(MainActivity.this);
                        layout.setLayoutParams(params);
                        layout.setGravity(Gravity.CENTER);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        ListView listView = new ListView(MainActivity.this);
                        listView.setPadding(10, 10, 10, 5);
                        QQAdapter qqAdapter = new QQAdapter(MainActivity.this, R.layout.qqgroup, nameList);
                        listView.setAdapter(qqAdapter);
                        listView.setDivider(new ColorDrawable(Color.alpha(0)));
                        listView.setDividerHeight(0);
                        listView.setOnItemClickListener((adapterView, view, i, l) -> {
                            String url = urlList.get(i);
                            joinQQGroup(url);
                        });
                        layout.addView(listView);
                        alertDialogWindow = alertDialog.setView(layout)
                                .setTitle("请选择您想加入的群聊")
                                .setNegativeButton("取消", (dialog, which) -> {
                                })
                                .show();
                        progressDialog.dismiss();
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        print("获取失败", Snackbar.LENGTH_LONG);
                        progressDialog.dismiss();
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    public boolean joinQQGroup(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            print("未安装手机QQ或版本不支持", Snackbar.LENGTH_LONG);
            return false;
        }
    }

    class QQAdapter extends ArrayAdapter<String> {

        private int resourceId;

        QQAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;

        }


        @NotNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String fruit = getItem(position); //获取当前项的Fruit实例
            @SuppressLint("ViewHolder") View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            TextView fruitName = view.findViewById(R.id.qqtext);
            fruitName.setText(fruit);
            return view;
        }
    }

}