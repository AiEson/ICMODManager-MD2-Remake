package icmod.wvt.com.icmod.ui.home.tab;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.MOD;
import icmod.wvt.com.icmod.ui.MainActivity;

import static icmod.wvt.com.icmod.ui.MainActivity.print;

public class MODFragment extends Fragment {

    ListView listView;
    MainActivity mainActivity;
    LocalMODAdapter modAdapter;
    PullRefreshLayout pullRefreshLayout;

    boolean selectMode = false;
    int choosed = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home_list, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = view.findViewById(R.id.home_fragment_listView);
        mainActivity = (MainActivity) getActivity();
        pullRefreshLayout = view.findViewById(R.id.pullRefresh);

        pullRefreshLayout.setOnRefreshListener(() -> {
            new Thread(() -> {
                int qy = 0;
                List<MOD> modList2 = flashNativeMOD(false);
                modAdapter.setModList(modList2);
                for (int i = 0; i < modList2.size(); i++)
                    if (modList2.get(i).getEnabled())
                        qy += 1;
                int finalQy = qy;
                mainActivity.runOnUiThread(() -> {
                    modAdapter.notifyDataSetChanged();
                    print("刷新成功，已启用的共" + finalQy + "个", Snackbar.LENGTH_SHORT);
                    pullRefreshLayout.setRefreshing(false);
                    pullRefreshLayout.clearAnimation();
                    modAdapter.clearSelect();
                });
            }).start();
        });

        List<MOD> modList = flashNativeMOD(false);
        modAdapter = new LocalMODAdapter(getContext(), R.layout.mod_item, modList);
        listView.setAdapter(modAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

//        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
//
//        });

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
    }


    public void flashList(boolean isShow) {
        new Thread(() -> {
            List<MOD> modList = flashNativeMOD(false);
            modAdapter.setModList(modList);
            mainActivity.runOnUiThread(() -> {
                modAdapter.notifyDataSetChanged();
                if (isShow)
                    print("已刷新", Snackbar.LENGTH_SHORT);
            });
        }).start();
    }

    public List<MOD> flashNativeMOD(boolean showNumber) {
        List<MOD> ret = new ArrayList<>();
        File[] modsFIle = new File(FinalValuable.MODDir).listFiles();
        int qy = 0;
        if (modsFIle != null)
            for (int i = 0; i < modsFIle.length; i++) {
                if (modsFIle[i].isDirectory()) {
                    MOD mod = Algorithm.getNativeMODClass(modsFIle[i].toString());
                    if (mod != null) {
                        ret.add(mod);
                        if (mod.getImagePath() != null) {
                            Bitmap bitmap = Algorithm.getBitmap(mod.getImagePath());
                            mainActivity.lruCacheUtils.savePicToMemory(mod.getImagePath(), bitmap);
                        }
                    }
                }
            }
        for (int i = 0; i < ret.size(); i++)
            if (ret.get(i).getEnabled())
                qy += 1;
        if (showNumber)
            print("加载本地MOD完毕，已启用的共" + qy + "个", Snackbar.LENGTH_SHORT);
        return ret;
    }


    class LocalMODAdapter extends ArrayAdapter<MOD> {
        private List<MOD> modList;
        private int resourceID;
        private List<Integer> selectNum = new ArrayList<>();

        public LocalMODAdapter(Context context, int resourcesID, List<MOD> objects) {
            super(context, resourcesID, objects);
            this.resourceID = resourcesID;
            this.modList = objects;
        }

        public void setModList(List<MOD> modList) {
            this.modList = modList;
        }

        public List<MOD> getModList() {
            return modList;
        }

        @Override
        public int getCount() {
            return modList.size();
        }

        @Override
        public MOD getItem(int position) {
            return modList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"SetTextI18n", "UnsafeExperimentalUsageError"})
        @NonNull
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final MOD mod = modList.get(position);
            final View view;
            ViewHolderMOD viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceID, parent, false);
                viewHolder = new ViewHolderMOD();
                viewHolder.cardView = view.findViewById(R.id.CardView);
                viewHolder.textView1 = view.findViewById(R.id.itemsettingTextView1);
                viewHolder.textView2 = view.findViewById(R.id.itemsettingTextView2);
                viewHolder.imageView = view.findViewById(R.id.itemsettingImageView1);
                viewHolder.button1 = view.findViewById(R.id.itemsettingButton1);
                viewHolder.button2 = view.findViewById(R.id.itemsettingButton2);
                viewHolder.aSwitch = view.findViewById(R.id.switch1);
                viewHolder.needInflate = false;
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolderMOD) view.getTag();
            }
            if (mod.getName() != null)
                viewHolder.textView1.setText(mod.getName());
            else viewHolder.textView1.setText("未知");
            if (mod.getAuthor() != null)
                viewHolder.textView2.setText(mod.getAuthor());
            else viewHolder.textView2.setText("未知");
            if (mod.getVersion() != null)
                viewHolder.textView2.setText(viewHolder.textView2.getText() + "-" + mod.getVersion());
            else viewHolder.textView2.setText(viewHolder.textView2.getText() + "-未知");
            if (mod.getImagePath() != null) {
                Bitmap bitmap = mainActivity.getLruCacheUtils().getPicFromMemory(mod.getImagePath());
                if (bitmap != null) {
                    BitmapDrawable drawable = new BitmapDrawable(bitmap);
                    drawable.getPaint().setFilterBitmap(false);
                    viewHolder.imageView.setImageDrawable(drawable);
                }
                else
                    viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
            } else viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
            
            viewHolder.button1.setOnClickListener(v -> {
                String allInfo = "";
                if (mod.getName() != null)
                    allInfo += "名称：" + mod.getName() + "\n";
                else allInfo += "名称：未知\n";
                if (mod.getAuthor() != null)
                    allInfo += "作者：" + mod.getAuthor() + "\n";
                else allInfo += "作者：未知\n";
                if (mod.getVersion() != null)
                    allInfo += "版本：" + mod.getVersion() + "\n";
                else allInfo += "版本：未知\n";
                allInfo += "路径：" + mod.getModPath() + "\n";
                if (mod.getDescribe() != null)
                    allInfo += "描述：" + mod.getDescribe();
                else allInfo += "描述：未知";

                final String finalAllInfo = allInfo;
                new MaterialAlertDialogBuilder(mainActivity)
                        .setTitle("详细信息")
                        .setMessage(allInfo)
                        .setNegativeButton("关闭", (dialog, which) -> {

                        })
                        .setPositiveButton("复制信息", (dialogInterface, i) -> {
                            //获取剪贴板管理器：
                            ClipboardManager cm = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData mClipData = ClipData.newPlainText("Label", finalAllInfo);
                            cm.setPrimaryClip(mClipData);
                            print("已复制到粘贴板", Snackbar.LENGTH_SHORT);
                        })
                        .create().show();
            });
            viewHolder.button2.setOnClickListener(v -> {
                final File f = new File(mod.getModPath());
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("提示")
                        .setMessage("将要删除MOD：" + mod.getName() + "，是否继续（该操作不可撤销）？")
                        .setNegativeButton("取消", (dialog, which) -> {

                        })
                        .setPositiveButton("确定", (dialog, which) -> {
                            Algorithm.deleteFile(f);
                            if (!f.exists()) {
                                print("已删除MOD：" + mod.getName(), Snackbar.LENGTH_SHORT);
                                modList.remove(position);
                                notifyDataSetChanged();
                            }
                        })
                        .create().show();
            });
            viewHolder.aSwitch.setOnCheckedChangeListener(null);
            viewHolder.aSwitch.setChecked(mod.getEnabled());
            viewHolder.aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (mod.changeMOD()) {
                        print("已启用该MOD", Snackbar.LENGTH_SHORT);
                    }
                } else {
                    if (mod.changeMOD()) {
                        print("已禁用该MOD", Snackbar.LENGTH_SHORT);
                    }
                }
            });

            viewHolder.cardView.setOnLongClickListener(v -> {
                if (!selectMode){
                    print("再次长按以弹出操作框", Snackbar.LENGTH_SHORT);
                    selectMode = true;
                    viewHolder.cardView.setChecked(true);
                } else {
                    AlertDialog dialogBuilder = new MaterialAlertDialogBuilder(mainActivity).setView(R.layout.mod_select_control)
                            .create();
                    dialogBuilder.show();
                    Window window = dialogBuilder.getWindow();
                    MaterialButton buttonSelAll = window.findViewById(R.id.mod_select_all);
                    MaterialButton buttonCalAll = window.findViewById(R.id.mod_select_cancel);
                    MaterialButton buttonEnable = window.findViewById(R.id.mod_select_enable);
                    MaterialButton buttonDisable = window.findViewById(R.id.mod_select_disable);
                    MaterialButton buttonDelete = window.findViewById(R.id.mod_select_delete);
                    MaterialButton buttonShare = window.findViewById(R.id.mod_select_share);
                    buttonSelAll.setOnClickListener(v1 -> {
                        selectNum.clear();
                        for (int i = 0; i < modList.size(); i++) {
                            modList.get(i).setChecked(true);
                            selectNum.add(i);
                        }
                        notifyDataSetChanged();
                    });

                    buttonCalAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (int i = 0; i < modList.size(); i++) {
                                modList.get(i).setChecked(false);
                            }
                            selectMode = false;
                            selectNum.clear();
                            notifyDataSetChanged();
                            dialogBuilder.dismiss();
                        }
                    });

                    buttonEnable.setOnClickListener(v12 -> {
                        for (int i = 0; i < selectNum.size(); i++) {
                            int posi = selectNum.get(i);
                            if (!modList.get(posi).getEnabled()) {
                                modList.get(posi).changeMOD();
                            }
                        }
                        notifyDataSetChanged();
                        print("已启用所选MOD", Snackbar.LENGTH_SHORT);
                    });

                    buttonDisable.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (int i = 0; i < selectNum.size(); i++) {
                                int posi = selectNum.get(i);
                                if (modList.get(posi).getEnabled()) {
                                    modList.get(posi).changeMOD();
                                }
                            }
                            notifyDataSetChanged();
                            print("已禁用所选MOD", Snackbar.LENGTH_SHORT);
                        }
                    });

                    buttonDelete.setOnClickListener(v13 -> {
                        String mods = "";
                        List<File> delFile = new ArrayList<>();
                        for (int i = 0; i < selectNum.size(); i++) {
                            int posi = selectNum.get(i);
                            mods += modList.get(posi).getName() + "\n";
                        }
                        new MaterialAlertDialogBuilder(mainActivity).setTitle("提示")
                                .setMessage("将要删除以下MOD：\n" + mods + "是否继续？")
                                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                        progressDialog.setCancelable(false);
                                        progressDialog.setMessage("正在删除，请稍等...");
                                        progressDialog.show();
                                        new Thread(() -> {
                                            for (int i = 0; i < selectNum.size(); i++) {
                                                int posi2 = selectNum.get(i);
                                                File modFile = new File(modList.get(posi2).getModPath());
                                                delFile.add(modFile);
                                            }
                                            for (int i = 0; i < delFile.size(); i++) {
                                                Algorithm.deleteFile(delFile.get(i));
                                            }
                                            mainActivity.runOnUiThread(() -> {
                                                print("已删除所选MOD", Snackbar.LENGTH_SHORT);
                                                dialogBuilder.dismiss();
                                                progressDialog.dismiss();
                                                flashList(false);
                                                selectMode = false;
                                                selectNum.clear();
                                            });
                                        }).start();
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .create().show();
                    });

                    buttonShare.setOnClickListener(v14 -> {
                        String mods = "";
                        for (int i : selectNum) {
                            mods += modList.get(i).getName() + "\n";
                        }
                        new MaterialAlertDialogBuilder(mainActivity).setTitle("提示")
                        .setMessage("将会打包并分享以下MOD：\n" + mods + "是否继续？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final EditText editText = new EditText(mainActivity);
                                editText.setHint("留空默认为MODs");
                                new MaterialAlertDialogBuilder(mainActivity)
                                .setTitle("请输入您要打包的名称")
                                .setView(editText)
                                .setCancelable(false)
                                .setPositiveButton("确定", (dialog1, which1) -> {
                                    String str = editText.getText().toString();
                                    if (str.length() == 0) {
                                        str = "MODs";
                                    }
                                    ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                    progressDialog.setCancelable(false);
                                    progressDialog.setMessage("正在打包，请稍等...");
                                    progressDialog.show();
                                    String finalStr = str;
                                    new Thread(() -> {
                                        List<File> fileList = new ArrayList<>();
                                        for (int i : selectNum) {
                                            fileList.add(new File(modList.get(i).getModPath()));
                                        }
                                        try {
                                            Algorithm.deleteFile(new File(FinalValuable.PackSharePath));
                                            new File(FinalValuable.PackSharePath).mkdirs();
                                        } catch (Exception e){}

                                        String zipFile = FinalValuable.PackSharePath + File.separator + finalStr + ".zip";
                                        boolean isDone = Algorithm.zipFile(fileList, zipFile);

                                        mainActivity.runOnUiThread(() -> {
                                            if (isDone)
                                            {
                                                Toast.makeText(mainActivity, "压缩成功，正在前往分享", Toast.LENGTH_LONG).show();
                                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(zipFile)));
                                                shareIntent.setType("application/zip");
                                                mainActivity.startActivity(shareIntent);
                                            }
                                             else {
                                                Toast.makeText(mainActivity, "压缩失败", Toast.LENGTH_LONG).show();
                                            }
                                             progressDialog.dismiss();
                                             dialogBuilder.dismiss();
                                             clearSelect();
                                             flashList(false);
                                        });
                                    }).start();
                                })
                                .setNegativeButton("取消",null).create().show();
                            }
                        }).setNegativeButton("取消", null)
                                .create().show();
                    });
                }

                return true;
            });

            viewHolder.cardView.setOnClickListener(v -> {
                if (selectMode) {
                    viewHolder.cardView.setChecked(!getItem(position).isChecked());
                }
            });

            viewHolder.cardView.setOnCheckedChangeListener((card, isChecked) -> {
                if (isChecked) {
                    card.setBackgroundResource(R.drawable.shape_cardview);
                    if (!getItem(position).isChecked()) {
                        getItem(position).setChecked(isChecked);
                        selectNum.add(position);
                    }
                } else {
                    card.setBackgroundResource(R.drawable.shape_cardview_white);
                    getItem(position).setChecked(isChecked);
                    for (int i = 0; i < selectNum.size(); i++) {
                        if (selectNum.get(i) == position)
                            selectNum.remove(i);
                    }
                }
                if (selectNum.size() == 0)
                    selectMode = false;
            });
            Log.e("TAG", choosed + "");
            if (!getItem(position).isChecked()) {
                viewHolder.cardView.setChecked(false);
            } else {
                viewHolder.cardView.setChecked(true);
            }
            return view;
        }

        public void clearSelect() {
            selectMode = false;
            for (int i = 0; i < modList.size(); i++) {
                modList.get(i).setChecked(false);
            }
            selectNum.clear();
            notifyDataSetChanged();
        }
    }

    static class ViewHolderMOD {
        MaterialCardView cardView;
        TextView textView1, textView2;
        ImageView imageView;
        MaterialButton button1, button2;
        SwitchMaterial aSwitch;
        boolean needInflate;
    }
}
