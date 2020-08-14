package icmod.wvt.com.icmod.ui.filechoose;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

import net.lingala.zip4j.ZipFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.CircleImageView;
import icmod.wvt.com.icmod.others.FileChoose;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.SuperInstallSystem;
import icmod.wvt.com.icmod.ui.MainActivity;

public class FileChooseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView subTextView;
    ListView listView;
    List<File> chooseFile = new ArrayList<>();
    Transition enterContainerTransform;
    //存储FAB是否显示
    boolean isShowFAB = false;
    //退出后数据是否刷新
    boolean isFlashData = false;

    SharedPreferences prefs;

    String nowFilePath;
    List<FileChoose> listFileChoose = new ArrayList<>();
    FloatingActionButton fab;
    JSONObject listViewPosition = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        findViewById(android.R.id.content).setTransitionName("shared_element_container");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        enterContainerTransform = new MaterialContainerTransform().setDuration(250L).addTarget(android.R.id.content);
        getWindow().setSharedElementEnterTransition(enterContainerTransform);
        getWindow().setSharedElementReturnTransition(new MaterialContainerTransform().setDuration(250L).addTarget(android.R.id.content));
        getWindow().setSharedElementExitTransition(new MaterialContainerTransform().setDuration(250L).addTarget(android.R.id.content));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filechoose_main);
        toolbar = findViewById(R.id.toolbar_dialog_fileChoose);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        toolbar.setSubtitle("test");
        listView = findViewById(R.id.dialog_fileChoose_listView);
        fab = findViewById(R.id.fabFileChooseActivity);

        new Thread(() -> {
            try {
                FinalValuable.fileConfig = new JSONArray(prefs.getString("signature", ""));
            } catch (Exception e) {
                try {
                    String defaultConfig = Algorithm.getString(getResources().openRawResource(R.raw.file_config_defalut));
                    FinalValuable.fileConfig = new JSONArray(defaultConfig);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("signature", defaultConfig);
                    editor.apply();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                FileChooseActivity.this.runOnUiThread(() -> Toast.makeText(FileChooseActivity.this, "配置文件格式错误，已恢复为默认配置", Toast.LENGTH_SHORT).show());
            }
        }).start();

        //获取成员变量
        Field f = null;
        try {
            Class classs = toolbar.getClass();

            f = classs.getDeclaredField("mSubtitleTextView");
            f.setAccessible(true);
            String fieldName = f.getName();
            Object fieldValueObj = null;
            try {
                fieldValueObj = f.get(toolbar);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if ((fieldValueObj == null)) {
                Log.e("TAG", "null aaaaa");
            } else {
                subTextView = (TextView) fieldValueObj;
                subTextView.setEllipsize(TextUtils.TruncateAt.START);
                subTextView.setTextAppearance(this, R.style.Toolbar_SubTitleText);
                subTextView.setText("正在获取文件列表...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        enterContainerTransform.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                new Thread(() -> {
                    nowFilePath = Environment.getExternalStorageDirectory().toString();
                    listFileChoose = Algorithm.orderByName(Environment.getExternalStorageDirectory().toString(), true);
                    FileChooseActivity.this.runOnUiThread(() -> {
                        FileAdapter adapter = new FileAdapter(FileChooseActivity.this, R.layout.file_item, listFileChoose, new File(nowFilePath), subTextView);
                        listView.setAdapter(adapter);
                    });
                }).start();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            FileChoose fileChoose = listFileChoose.get(position);
            String itemStr = fileChoose.getPath();
            File orderFile = new File(itemStr);
            Log.e("TAG", itemStr);
            if (orderFile.isDirectory()) {
                fabOut();
                if (chooseFile.size() != 0) {
                    Toast.makeText(FileChooseActivity.this, "您的选择已清空，请重新选择", Toast.LENGTH_SHORT).show();
                }
                chooseFile.clear();
                FileChooseActivity.FileAdapter adapter1;
                JSONObject thisPosition = new JSONObject();
                try {
                    thisPosition.put("position", listView.getFirstVisiblePosition());
                    thisPosition.put("y", listView.getChildAt(0).getTop());
                    listViewPosition.put(nowFilePath, thisPosition);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                listFileChoose = Algorithm.orderByName(orderFile.toString(), true);
                nowFilePath = orderFile.toString();
                adapter1 = new FileChooseActivity.FileAdapter(FileChooseActivity.this, R.layout.file_item, listFileChoose, new File(nowFilePath), subTextView);

                listView.setAdapter(adapter1);
                if (!listViewPosition.isNull(nowFilePath)) {
                    try {
                        JSONObject jsonObject = listViewPosition.getJSONObject(nowFilePath);
                        listView.setSelectionFromTop(jsonObject.getInt("position"), jsonObject.getInt("y"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (orderFile.isFile()) {
                    ProgressDialog progressDialog = new ProgressDialog(FileChooseActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("正在为您自动安装...");
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (new ZipFile(orderFile).isValidZipFile()) {
                                Thread tbTh = new Thread(() -> {
                                    while (true) {
                                        if (!progressDialog.isShowing()) break;
                                        try {
                                            Thread.sleep(20);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        FileChooseActivity.this.runOnUiThread(() -> progressDialog.setMessage("正在为您自动安装...\n" + FinalValuable.installState));
                                    }

                                });
                                tbTh.start();
                                progressDialog.setOnDismissListener(dialogInterface -> tbTh.interrupt());
                                new Thread(() -> {
                                    String statue = new SuperInstallSystem().intall(orderFile.toString());
                                    FileChooseActivity.this.runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        new MaterialAlertDialogBuilder(FileChooseActivity.this)
                                                .setTitle("安装信息")
                                                .setMessage(statue)
                                                .setPositiveButton("关闭", (dialogInterface, i) -> finishAfterTransition()).setOnDismissListener(dialogInterface -> finishAfterTransition()).create().show();

                                    });

                                }).start();
                            } else {
                                FileChooseActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(FileChooseActivity.this, "所选文件已损坏或不是MOD，请重新选择", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).start();

                }
            }
        });

        fab.setOnClickListener(view -> {
            final String[] mod = {"无"};
            final String[] map = {"无"};
            final String[] res = {"无"};
            final String[] pack = {"无"};
            final String[] none = {"无"};
            ProgressDialog progressDialog = new ProgressDialog(FileChooseActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.show();
            final int[] num = {0};
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (progressDialog.isShowing())
                            FileChooseActivity.this.runOnUiThread(() -> progressDialog.setMessage("正在自动安装，进度：" + (num[0] + 1) + "/" + chooseFile.size() + "\n" + FinalValuable.installState));
                        else break;
                    }

                }
            }).start();
            new Thread(() -> {
                StringBuffer sb = new StringBuffer();
                chooseFile = getItemWithoutRepeat(chooseFile);
                for (int i = 0; i < chooseFile.size(); i++) {
                    SuperInstallSystem sis = new SuperInstallSystem();
                    int finalI = i;
                    num[0] = i;
                    File orderFile = chooseFile.get(i);
                    Log.e("File", orderFile.toString());
                    String statue = sis.intall(orderFile.toString());
                    FinalValuable.clearSIS();
                    sb.append(orderFile.getName()).append("：\n\t\t").append(statue.replaceAll("\n", "\n\t\t")).append("\n");
                }
                this.runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(FileChooseActivity.this).setTitle("安装统计信息")
                            .setMessage(sb.toString())
                            .setPositiveButton("关闭", (dialog, which) -> finishAfterTransition())
                            .setOnDismissListener(dialog -> finishAfterTransition())
                            .create().show();
                });
            }).start();

        });

        toolbar.setOnLongClickListener(v -> {
            Algorithm.copyToClipboard(FileChooseActivity.this, (String) subTextView.getText());
            Toast.makeText(FileChooseActivity.this, "已复制当前路径到粘贴板", Toast.LENGTH_SHORT).show();
            return true;
        });

    }

    private List<File> getItemWithoutRepeat(List<File> list) {
        Set<File> fileSet = new HashSet<>();
        fileSet.addAll(list);
        List<File> retList = new ArrayList<>();
        retList.addAll(fileSet);
        return retList;
    }

    private void createFlashFile() {
        File flashFile = new File(FinalValuable.flashHomeData);
        if (!flashFile.exists()) {
            try {
                flashFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createHorizonFlashFile() {
        File flashFile = new File(FinalValuable.flashHorizonData);
        if (!flashFile.exists()) {
            try {
                flashFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        supportFinishAfterTransition();
    }

    private void fabIn() {
        Animation animationIn = AnimationUtils.loadAnimation(FileChooseActivity.this, R.anim.fab_in);
        fab.startAnimation(animationIn);
        fab.setVisibility(View.VISIBLE);
        isShowFAB = true;
    }

    private void fabOut() {
        Animation animationOut = AnimationUtils.loadAnimation(FileChooseActivity.this, R.anim.fab_out);
        fab.startAnimation(animationOut);
        fab.setVisibility(View.GONE);
        isShowFAB = false;
    }

    class FileAdapter extends ArrayAdapter<FileChoose> {
        List<FileChoose> fileList;
        private int resourceId;
        private TextView textView;
        File fileDir;

        FileAdapter(Context context, int resource, List<FileChoose> fileList, File fileDir, TextView textView2) {
            super(context, resource, fileList);
            this.fileList = fileList;
            this.resourceId = resource;
            this.fileDir = fileDir;
            textView2.setText(fileDir.toString());
        }

        public void setFileList(List<FileChoose> fileList) {
            this.fileList = fileList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FileChoose fileChoose = getItem(position);
            File file = new File(fileChoose.getPath());
            View view;


            FileChooseActivity.ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                viewHolder = new FileChooseActivity.ViewHolder();
                viewHolder.checkBox = view.findViewById(R.id.file_choose_checkbox);
                viewHolder.imageView = view.findViewById(R.id.file_choose_imageview);
                viewHolder.textView = view.findViewById(R.id.file_choose_textview);
                viewHolder.addTab = view.findViewById(R.id.file_choose_add_tab);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (FileChooseActivity.ViewHolder) view.getTag();
            }
            Bitmap bitmap;
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.addTab.setVisibility(View.VISIBLE);
            if (fileChoose.getType() == FinalValuable.FileChooseOther) {
                viewHolder.addTab.setVisibility(View.GONE);
                if (FinalValuable.lruCacheUtils.getPicFromMemory("open") != null)
                    bitmap = FinalValuable.lruCacheUtils.getPicFromMemory("open");
                else {
                    FinalValuable.lruCacheUtils.savePicToMemory("open", Algorithm.getBitmapFromRes(FileChooseActivity.this, R.drawable.baseline_open_in_new_black_24dp));
                    bitmap = Algorithm.getBitmapFromRes(FileChooseActivity.this, R.drawable.baseline_open_in_new_black_24dp);
                }
            } else {
                if (file.isFile()) {
                    viewHolder.checkBox.setVisibility(View.VISIBLE);
                    viewHolder.addTab.setVisibility(View.GONE);
                    if (FinalValuable.lruCacheUtils.getPicFromMemory("file") != null)
                        bitmap = FinalValuable.lruCacheUtils.getPicFromMemory("file");
                    else {
                        FinalValuable.lruCacheUtils.savePicToMemory("file", Algorithm.getBitmapFromRes(FileChooseActivity.this, R.drawable.round_description_black_36dp));
                        bitmap = Algorithm.getBitmapFromRes(FileChooseActivity.this, R.drawable.round_description_black_36dp);
                    }
                } else {
                    if (fileChoose.getName().equals("上级目录"))
                        viewHolder.addTab.setVisibility(View.GONE);
                    if (FinalValuable.lruCacheUtils.getPicFromMemory("folder") != null)
                        bitmap = FinalValuable.lruCacheUtils.getPicFromMemory("folder");
                    else {
                        FinalValuable.lruCacheUtils.savePicToMemory("folder", Algorithm.getBitmapFromRes(FileChooseActivity.this, R.drawable.round_folder_open_black_36dp));
                        bitmap = Algorithm.getBitmapFromRes(FileChooseActivity.this, R.drawable.round_folder_open_black_36dp);
                    }
                }
            }

            viewHolder.textView.setText(fileChoose.getName());
            viewHolder.imageView.setImageBitmap(bitmap);

            viewHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                fileChoose.setChecked(isChecked);
                if (fileChoose.getChecked()) {
                    chooseFile.add(new File(fileChoose.getPath()));
                } else {
                    for (int i = 0; i < chooseFile.size(); i++) {
                        if (chooseFile.get(i).getName().equals(new File(fileChoose.getName()).toString())) {
                            chooseFile.remove(i);
                            break;
                        }
                    }
                }

                if (chooseFile.size() != 0) {
                    if (!isShowFAB && chooseFile.size() != 0) {
                        fabIn();
                    }
                } else {
                    if (isShowFAB) {
                        fabOut();
                    }
                }
//                Toast.makeText(FileChooseActivity.this, "已选择" + position + " " + isChecked, Toast.LENGTH_SHORT).show();
            });
            viewHolder.checkBox.setChecked(fileChoose.getChecked());

            viewHolder.addTab.setOnClickListener(v -> new MaterialAlertDialogBuilder(FileChooseActivity.this)
                    .setTitle("添加快捷标签")
                    .setMessage("是否将此文件夹（" + fileChoose.getName() + "）添加进顶部快捷标签？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", (dialog, which) -> {
                        final EditText editText = new EditText(FileChooseActivity.this);
                        editText.setHint("留空默认为" + fileChoose.getName());
                        new MaterialAlertDialogBuilder(FileChooseActivity.this)
                                .setTitle("请输入标签名称")
                                .setView(editText)
                                .setCancelable(false)
                                .setPositiveButton("确定", (dialog1, which1) -> {
                                    new Thread(() -> {
                                        String str = editText.getText().toString();
                                        if (str.length() == 0) {
                                            str = fileChoose.getName();
                                        }
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("name", str);
                                            JSONArray jsonArrayName = new JSONArray();
                                            jsonArrayName.put("android");
                                            jsonObject.put("pack_name", jsonArrayName);
                                            JSONArray jsonArrayPath = new JSONArray();
                                            jsonArrayPath.put(fileChoose.getPath());
                                            jsonObject.put("path", jsonArrayPath);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        FinalValuable.fileConfig.put(jsonObject);
                                        String string = Algorithm.formatJson(FinalValuable.fileConfig.toString());
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("signature", string);
                                        editor.apply();
                                        FileChooseActivity.this.runOnUiThread(() -> Toast.makeText(FileChooseActivity.this, "配置已添加，重新进入以生效", Toast.LENGTH_SHORT).show());
                                    }).start();

                                })
                                .setNegativeButton("取消", null).create().show();
                    }).create().show());

            return view;
        }
    }

    static class ViewHolder {
        CircleImageView imageView;
        TextView textView;
        MaterialCheckBox checkBox;
        ImageView addTab;
    }
}