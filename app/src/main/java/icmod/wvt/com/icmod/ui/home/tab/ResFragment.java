package icmod.wvt.com.icmod.ui.home.tab;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.ResPack;
import icmod.wvt.com.icmod.ui.MainActivity;

import static icmod.wvt.com.icmod.ui.MainActivity.print;

public class ResFragment extends Fragment {


    ListView listView;
    MainActivity mainActivity;
    LocalResAdapter localResAdapter;
    PullRefreshLayout pullRefreshLayout;
    //用户选择否时的状态阈值
    boolean isCopy = true;
    
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

        pullRefreshLayout.setOnRefreshListener(() -> new Thread(() -> {
            List<ResPack> list2 = flashNativeRes(false);
            localResAdapter.setResPackList(list2);

            mainActivity.runOnUiThread(() -> {

                localResAdapter.notifyDataSetChanged();
                print("刷新成功，共" + list2.size() + "个", Snackbar.LENGTH_SHORT);
                pullRefreshLayout.setRefreshing(false);
                pullRefreshLayout.clearAnimation();
            });
        }).start());

        List<ResPack> list = flashNativeRes(false);
        localResAdapter = new LocalResAdapter(getContext(), R.layout.mod_item, list);
        listView.setAdapter(localResAdapter);
    }

    class LocalResAdapter extends ArrayAdapter<ResPack> {
        private List<ResPack> resPackList;
        private int resourceID;

        public LocalResAdapter(Context context, int resourcesID, List<ResPack> objects) {
            super(context, resourcesID, objects);
            this.resourceID = resourcesID;
            this.resPackList = objects;
        }

        public void setResPackList(List<ResPack> resPackList) {
            this.resPackList = resPackList;
        }

        public List<ResPack> getModList() {
            return resPackList;
        }

        @Override
        public int getCount() {
            return resPackList.size();
        }

        @Override
        public ResPack getItem(int position) {
            return resPackList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ResPack mod = resPackList.get(position);
            final View view;
            ViewHolderMOD viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(mainActivity).inflate(resourceID, parent, false);
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
            if (mod.getPackId() != null)
                viewHolder.textView2.setText(mod.getPackId());
            else viewHolder.textView2.setText("未知");
            if (mod.getPackVersion() != null)
                viewHolder.textView2.setText(viewHolder.textView2.getText() + "-" + mod.getPackVersion());
            else viewHolder.textView2.setText(viewHolder.textView2.getText() + "-未知");
            if (mod.getImagePath() != null) {
                Bitmap bitmap = mainActivity.lruCacheUtils.getPicFromMemory(mod.getImagePath());
                if (bitmap != null)
                    viewHolder.imageView.setImageBitmap(bitmap);
                else
                    viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
            } else viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));


            viewHolder.button1.setOnClickListener(v -> {
                String allInfo = "";
                if (mod.getName() != null)
                    allInfo += "名称：" + mod.getName() + "\n";
                else allInfo += "名称：未知\n";
                if (mod.getPackId() != null)
                    allInfo += "资源包ID：" + mod.getPackId() + "\n";
                else allInfo += "资源包ID：未知\n";
                if (mod.getPackVersion() != null)
                    allInfo += "资源包版本：" + mod.getPackVersion() + "\n";
                else allInfo += "资源包版本：未知\n";
                if (mod.getDescribe() != null)
                    allInfo += "资源包描述：" + mod.getDescribe() + "\n";
                else allInfo += "资源包描述：未知" + "\n";
                if (mod.getUuid() != null)
                    allInfo += "资源包UUID：" + mod.getUuid() + "\n";
                else allInfo += "资源包UUID：未知" + "\n";
                if (mod.getModuleType() != null)
                    allInfo += "模块类型：" + mod.getModuleType() + "\n";
                else allInfo += "模块类型：未知" + "\n";
                if (mod.getModuleVersion() != null)
                    allInfo += "模块版本：" + mod.getModuleVersion() + "\n";
                else allInfo += "模块版本：未知" + "\n";
                if (mod.getModuleDes() != null)
                    allInfo += "模块描述：" + mod.getModuleDes() + "\n";
                else allInfo += "模块描述：未知" + "\n";
                if (mod.getModuleUuid() != null)
                    allInfo += "模块UUID：" + mod.getModuleUuid();
                else allInfo += "模块UUID：未知";

                final String finalAllInfo = allInfo;
                AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
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
                        .create();
                alertDialog.show();
            });
            viewHolder.button2.setOnClickListener(v -> {
                final File f = new File(mod.getResPath());
                AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
                        .setTitle("提示")
                        .setMessage("将要删除资源包：" + mod.getName() + "，是否继续（该操作不可撤销）？")
                        .setNegativeButton("取消", (dialog, which) -> {

                        })
                        .setPositiveButton("确定", (dialog, which) -> {
                            if (getItem(position).getEnabled()) {
                                AlertDialog alertDialog2 = new AlertDialog.Builder(mainActivity)
                                        .setMessage("警告")
                                        .setMessage("将会关闭所有已启用资源包，是否继续？")
                                        .setPositiveButton("是", (dialogInterface, i) -> {
                                            ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                            progressDialog.setCancelable(false);
                                            progressDialog.show();
                                            new Thread(() -> {
                                                File icResDir = new File(FinalValuable.ICResDir);
                                                for (int j = 0; j < resPackList.size(); j++) {
                                                    File file = new File(resPackList.get(j).getResPath() + File.separator + "enabled.txt");
                                                    if (file.exists())
                                                        Algorithm.deleteFile(file);
                                                }
                                                if (icResDir.exists())
                                                    Algorithm.deleteFile(icResDir);
                                                Algorithm.deleteFile(f);
                                                if (!f.exists()) {
                                                    print("已删除资源包：" + mod.getName(), Snackbar.LENGTH_SHORT);
                                                }
                                                mainActivity.runOnUiThread(() -> {
                                                    localResAdapter = new LocalResAdapter(mainActivity, R.layout.mod_item, flashNativeRes(false));
                                                    listView.setAdapter(localResAdapter);
                                                    print("已禁用所有材质包，请重新手动启用", Snackbar.LENGTH_SHORT);
                                                    progressDialog.dismiss();
                                                });
                                            }).start();


                                        })
                                        .setNegativeButton("否", null).create();
                                alertDialog2.show();
                            } else {
                                Algorithm.deleteFile(f);
                                if (!f.exists()) {
                                    print("已删除资源包：" + mod.getName(), Snackbar.LENGTH_SHORT);
                                    resPackList.remove(position);
                                    notifyDataSetChanged();
                                }
                            }

                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.aSwitch.setOnCheckedChangeListener(null);
            viewHolder.aSwitch.setChecked(mod.getEnabled());
            viewHolder.aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                //防止初始化的时候出发监听
                if (!buttonView.isPressed()) {
                    return;
                }
                if (isChecked) {
                    ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("请稍等...");
                    progressDialog.show();
                    new Thread(() -> {
                        File[] filelist = new File(getItem(position).getResPath()).listFiles();
                        Algorithm.copyFolder(getItem(position).getResPath(), FinalValuable.ICResDir);
                        getItem(position).setEnabled(true);
                        try {
                            new File(getItem(position).getResPath() + File.separator + "enabled.txt").createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mainActivity.runOnUiThread(() -> {
                            print("已启用材质包：" + getItem(position).getName(), Snackbar.LENGTH_SHORT);
                            progressDialog.dismiss();
                        });
                    }).start();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
                            .setMessage("警告")
                            .setMessage("将会关闭所有已启用资源包，是否继续？")
                            .setPositiveButton("是", (dialogInterface, i) -> {
                                ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                new Thread(() -> {
                                    File icResDir = new File(FinalValuable.ICResDir);
                                    for (int j = 0; j < resPackList.size(); j++) {
                                        File file = new File(resPackList.get(j).getResPath() + File.separator + "enabled.txt");
                                        if (file.exists())
                                            Algorithm.deleteFile(file);
                                    }
                                    if (icResDir.exists())
                                        Algorithm.deleteFile(icResDir);
                                    mainActivity.runOnUiThread(() -> {
                                        localResAdapter = new LocalResAdapter(mainActivity, R.layout.mod_item, flashNativeRes(false));
                                        listView.setAdapter(localResAdapter);
                                        print("已禁用所有材质包，请重新手动启用", Snackbar.LENGTH_SHORT);
                                        progressDialog.dismiss();
                                    });
                                }).start();


                            })
                            .setNegativeButton("否", (dialogInterface, i) -> {
                                viewHolder.aSwitch.setChecked(true);
                            }).create();
                    alertDialog.show();
                }
            });
            return view;
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

    public List<ResPack> flashNativeRes(boolean showNumber) {
        List<ResPack> ret = new ArrayList<>();
        File[] mapsFIle = new File(FinalValuable.ResDir).listFiles();
        if (mapsFIle != null)
            for (int i = 0; i < mapsFIle.length; i++) {
                if (mapsFIle[i].isDirectory()) {
                    ResPack res = Algorithm.getNativeResClass(mapsFIle[i].toString());
                    if (res != null) {
                        ret.add(res);
                        if (res.getImagePath() != null) {
                            Bitmap bitmap = Algorithm.getBitmap(res.getImagePath());
                            mainActivity.lruCacheUtils.savePicToMemory(res.getImagePath(), bitmap);
                        }
                    }
                }
            }
        if (showNumber)
            print("加载本地资源包完毕，共" + ret.size() + "个", Snackbar.LENGTH_SHORT);
        return ret;
    }
}
