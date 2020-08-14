package icmod.wvt.com.icmod.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.HorizonPack;
import icmod.wvt.com.icmod.others.MOD;
import icmod.wvt.com.icmod.others.SuperInstallSystem;

import static icmod.wvt.com.icmod.others.Algorithm.getFileLastName;

public class OpenActivity extends AppCompatActivity {

    List<MOD> modList = new ArrayList<>();
    Boolean dirSiMOD = false;
    List<File> notModDir = new ArrayList<>();
    String gamesNoMediaPath;
    String resPackWvTPath;
    SharedPreferences prefs;

    private static final int PERMISSION_REQUEST = 0xa00;
    // 声明一个数组，用来存储所有需要动态申请的权限
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE};
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();
    Context mContext;
    boolean mShowRequestPermission = true;//用户是否禁止权限

    @SuppressLint("StaticFieldLeak")
    class open_load extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                File testDir = new File(FinalValuable.MODTestDir);
                Algorithm.deleteFile(testDir);
                File downloadDir = new File(FinalValuable.DownLoadPath);
                Algorithm.deleteFile(downloadDir);
                testDir.mkdirs();
                File allMod = new File(FinalValuable.MODDir);
                File[] allModList = allMod.listFiles();
                if (allModList != null && allModList.length != 0) {
                    for (File file : allModList) {
                        if (file.isDirectory()) {
                            File modDel = new File(file.toString() + File.separator + "build.config");
                            if (!modDel.exists()) {
                                notModDir.add(file);
                            }
                        } else {
                            if (!getFileLastName(file).equals("js") || !getFileLastName(file).equals("json")) {
                                Algorithm.deleteFile(file);
                            }
                        }
                    }
                }
                FinalValuable.horizonPackList = Algorithm.getNativePackList(false);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!Algorithm.isNetworkAvailable(OpenActivity.this)) {
                File file111 = new File(FinalValuable.UserInfo);
                if (file111.exists()) {
                    Algorithm.deleteFile(file111);
                    Toast.makeText(OpenActivity.this, "您的登录信息已清除，请重新登陆", Toast.LENGTH_LONG).show();
                }
            }

            Intent intent = new Intent(OpenActivity.this, MainActivity.class);
            if (!dirSiMOD) {
                intent.putExtra("notFileList", (Serializable) notModDir);
            }
            intent.putExtra("hasNoMod", notModDir.size() == 0);
//            Toast.makeText(OpenActivity.this, dirSiMOD + "", 1).show();
            if (new File(FinalValuable.NetModDataGw).exists())
                Algorithm.deleteFile(new File(FinalValuable.NetModDataGw));
            if (new File(FinalValuable.NetModDataHhz).exists())
                Algorithm.deleteFile(new File(FinalValuable.NetModDataHhz));

            if (FinalValuable.horizonPackList == null)
                FinalValuable.horizonPackList = new ArrayList<>();
            if (FinalValuable.horizonPackList.size() == 0) {
                initFinalValuableIC();
                startActivity(intent);
                OpenActivity.this.finish();
            } else {
                //这个xml里只有ListView，直接拿过来用pwq
                View view = LayoutInflater.from(OpenActivity.this).inflate(R.layout.fragment_download_list, null, false);
                ListView listView = view.findViewById(R.id.download_fragment_listView);
                List<HorizonPack> list = new ArrayList<>();
                HorizonPack icPack = new HorizonPack();
                icPack.setInstallName("InnerCore管理");
                list.add(icPack);
                for (HorizonPack horizonPack : FinalValuable.horizonPackList) {
                    list.add(horizonPack);
                }
                PackChooseAdapter packChooseAdapter = new PackChooseAdapter(OpenActivity.this, R.layout.open_horizon_item, list);
                listView.setAdapter(packChooseAdapter);
                listView.setOnItemClickListener((parent, view1, position, id) -> {
                    if (position == 0) {
                        initFinalValuableIC();
                    } else {
                        FinalValuable.chooseFolderName = packChooseAdapter.getItem(position).getFolderName();
                        initFinalValuableHz(FinalValuable.HorizonPackPath + File.separator + FinalValuable.chooseFolderName + File.separator);
                    }
                    startActivity(intent);
                    OpenActivity.this.finish();
                });
                new MaterialAlertDialogBuilder(OpenActivity.this)
                        .setView(view)
                        .setTitle("请选择要管理的包")
                        .setCancelable(false)
                        .show();
            }
        }
    }

        //创建桌面快捷方式
        private void createShortCut() {
            //创建Intent对象
            Intent shortcutIntent = new Intent();

            //设置点击快捷方式，进入指定的Activity
            //注意：因为是从Lanucher中启动，所以这里用到了ComponentName
            //其中new ComponentName这里的第二个参数，是Activity的全路径名，也就是包名类名要写全。

            shortcutIntent.setComponent(new ComponentName(this.getPackageName(), "icmod.wvt.com.icmod.ui.filechoose.FileChooseActivity"));

            //给Intent添加 对应的flag
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent resultIntent = new Intent();
            // Intent.ShortcutIconResource.fromContext 这个就是设置快捷方式的图标

            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this,
                            R.mipmap.ic_launcher));
            //启动的Intent
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

            //这里可以设置快捷方式的名称
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "快速安装");

            //设置Action
            resultIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

            //发送广播、通知系统创建桌面快捷方式
            sendBroadcast(resultIntent);
        }


        private void intoMainActivity(int type, String rootPath) {

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
                title.setText(item.getInstallName());
                hint.setText(FinalValuable.HorizonPackPath + File.separator + item.getFolderName());
                if (position == 0) {
                    hint.setText("默认InnerCore路径");
                }
                return view;
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.open_loading);
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            initFinalValuableIC();
            mContext = this;
            resPackWvTPath = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT" + File.separator + ".nomedia";
            gamesNoMediaPath = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + ".nomedia";
            checkPermission();
            Log.e("TAG", OpenActivity.this.getExternalFilesDir(null).toString());
            createShortCut();
        }

        protected void initFinalValuableIC() {

            final String MODDir = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + "mods",
                    MCMAPDir = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + "minecraftWorlds",
                    ICMAPDir = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + "innercoreWorlds",
                    WvTWorkDir = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT",
                    MODTestDir = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT" + File.separator + "Test",
                    MODDataPath = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT" + File.separator + "AllModInfo.json",
                    DownLoadPath = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT" + File.separator + "Download",
                    ResDir = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT" + File.separator + "ResPack",
                    NetModDataGw = DownLoadPath + File.separator + "NetModDataGw.json",
                    NetModDataHhz = DownLoadPath + File.separator + "NetModDataHhz.json",
                    UserInfo = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT" + File.separator + "UserInfo.json",
                    QQGroupJson = OpenActivity.this.getExternalFilesDir(null) + File.separator + "WvT" + File.separator + "QQGroup.json",
                    ICResDir = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "com.mojang" + File.separator + "resource_packs" + File.separator + "innercore-resources",
                    PackSharePath = WvTWorkDir + File.separator + "share",
                    flashHomeData = WvTWorkDir + File.separator + "flash",
                    HorizonPackPath = Environment.getExternalStorageDirectory().toString() + File.separator + "games" + File.separator + "horizon" + File.separator + "packs",
                    flashHorizonData = WvTWorkDir + File.separator + "flash_hz";
            FinalValuable.MODDir = MODDir;
            FinalValuable.MCMAPDir = MCMAPDir;
            FinalValuable.ICMAPDir = ICMAPDir;
            FinalValuable.WvTWorkDir = WvTWorkDir;
            FinalValuable.MODTestDir = MODTestDir;
            FinalValuable.MODDataPath = MODDataPath;
            FinalValuable.DownLoadPath = DownLoadPath;
            FinalValuable.NetModDataGw = NetModDataGw;
            FinalValuable.NetModDataHhz = NetModDataHhz;
            FinalValuable.UserInfo = UserInfo;
            FinalValuable.ICResDir = ICResDir;
            FinalValuable.QQGroupJson = QQGroupJson;
            FinalValuable.ResDir = ResDir;
            FinalValuable.PackSharePath = PackSharePath;
            FinalValuable.flashHomeData = flashHomeData;
            FinalValuable.flashHorizonData = flashHorizonData;
            //初始化验证数据文件列表
            FinalValuable.statueList.add("build.config");//模组
            FinalValuable.statueList.add("level.dat");//地图
            FinalValuable.statueList.add("pack_manifest.json");//材质1
            FinalValuable.statueList.add("manifest.json");//材质2
            FinalValuable.HorizonPackPath = HorizonPackPath;
            //文件选择配置文件声明
            String fileConfigString = prefs.getString("signature", "");
            try {
                JSONArray jsonArray = new JSONArray(fileConfigString);
                FinalValuable.fileConfig = new JSONArray(fileConfigString);
            } catch (JSONException e) {
                fileConfigString = Algorithm.getString(getResources().openRawResource(R.raw.file_config_defalut));
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("signature", fileConfigString);
                editor.apply();
                try {
                    FinalValuable.fileConfig = new JSONArray(fileConfigString);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }

        }

        public static void initFinalValuableHz(String rootPath) {

            final String MODDir = rootPath + "innercore" + File.separator + "mods",
                    ICMAPDir = rootPath + "worlds",
                    ICResDir = rootPath + "resourcepacks" + File.separator + "ICMODManagerAutoResPack",
                    ResDir = rootPath + "MODManagerResPacks";

            FinalValuable.MODDir = MODDir;
            FinalValuable.ResDir = ResDir;
            FinalValuable.ICMAPDir = ICMAPDir;
            FinalValuable.ICResDir = ICResDir;
        }

        private void createNoMedia() throws IOException {
            File noMediaWvT = new File(resPackWvTPath);
            File noMediaGame = new File(gamesNoMediaPath);
            if (!noMediaGame.exists())
                noMediaGame.createNewFile();
            if (!noMediaWvT.exists())
                noMediaWvT.createNewFile();
        }

        private void checkPermission() {
            mPermissionList.clear();
            /**
             * 判断哪些权限未授予
             * 以便必要的时候重新申请
             */
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permission);
                }
            }
            /**
             * 判断存储委授予权限的集合是否为空
             */
            if (!mPermissionList.isEmpty()) {
                // 后续操作...
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST);
            } else {//未授予的权限为空，表示都授予了
                // 后续操作...
                open_load ol = new open_load();
                ol.execute();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode) {
                case PERMISSION_REQUEST:
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //判断是否勾选禁止后不再询问
                            boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(OpenActivity.this, permissions[i]);
                            if (showRequestPermission) {
                                // 后续操作...
                                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST);
                            } else {
                                // 后续操作...
                            }
                        }
                    }
                    // 授权结束后的后续操作...
                    try {
                        createNoMedia();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    new File(FinalValuable.MODDir).mkdirs();
                    new File(FinalValuable.ICMAPDir).mkdirs();
                    new File(FinalValuable.ICResDir).mkdirs();
                    new File(FinalValuable.MCMAPDir).mkdirs();
                    Intent intent = new Intent(OpenActivity.this, MainActivity.class);
                    if (!dirSiMOD) {
                        intent.putExtra("notFileList", (Serializable) notModDir);
                    }
                    intent.putExtra("hasNoMod", notModDir.size() == 0);
                    startActivity(intent);
                    OpenActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }
