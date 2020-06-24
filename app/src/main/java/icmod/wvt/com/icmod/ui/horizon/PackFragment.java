package icmod.wvt.com.icmod.ui.horizon;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.AnimatedExpandableListView;
import icmod.wvt.com.icmod.others.FileSizeUtil;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.HorizonPack;
import icmod.wvt.com.icmod.ui.MainActivity;
import icmod.wvt.com.icmod.ui.OpenActivity;
import icmod.wvt.com.icmod.ui.filechoose.FileChooseActivity;

import static icmod.wvt.com.icmod.others.Algorithm.getUUID32;
import static icmod.wvt.com.icmod.ui.MainActivity.print;

public class PackFragment extends Fragment {

    MainActivity mainActivity;
    private AnimatedExpandableListView listView;
    private PullRefreshLayout pullRefreshLayout;
    private MyAdapter adapter;
    List<HorizonPack> horizonPackList;
    List<GroupItem> items = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_horizon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        listView = view.findViewById(R.id.expand_view);
        pullRefreshLayout = view.findViewById(R.id.horizon_pullRefresh);

        new Thread(this::showList).start();

        pullRefreshLayout.setOnRefreshListener(() -> new Thread(() -> {
            FinalValuable.horizonPackList = Algorithm.getNativePackList(true);
            items.clear();
            showList();
            mainActivity.runOnUiThread(() -> {
                print("刷新成功，共" + items.size() + "个", Snackbar.LENGTH_SHORT);
                pullRefreshLayout.setRefreshing(false);
                pullRefreshLayout.clearAnimation();
            });

        }).start());
    }

    private void showList() {
        horizonPackList = FinalValuable.horizonPackList;
        for (HorizonPack horizonPack : horizonPackList) {
            GroupItem item = new GroupItem();
            item.title = horizonPack.getInstallName();
            item.hint = horizonPack.getPack() + " - " + horizonPack.getPackVersion();
            ChildItem childItem = new ChildItem();
            childItem.title = "文件信息：\n\t\t文件路径：" + FinalValuable.HorizonPackPath + File.separator + horizonPack.getFolderName() + "\n\t\t文件体积：" + horizonPack.getFileSize() + "\n\n安装信息：\n" + "\t\tUUID：" + horizonPack.getInstallInfoUuid() + "\n\t\t安装时间：" + horizonPack.getInstallTime()
                    + "\n\t\t自定义名称：" + horizonPack.getInstallName() + "\n\t\t内部ID：" + horizonPack.getInstallInternalId()
                    + "\n\n清单文件信息：\n" + "\t\t游戏：" + horizonPack.getGame() + "\n\t\t游戏版本：" + horizonPack.getGameVersion()
                    + "\n\t\t包：" + horizonPack.getPack() + "\n\t\t包版本：" + horizonPack.getPackVersion() + "\n\t\t包版本号：" + horizonPack.getPackVersionCode()
                    + "\n\t\t开发者：" + horizonPack.getDeveloper() + "\n\t\t描述：" + horizonPack.getDescription() + "\n\t\t目录：" + horizonPack.getDirectories()
                    + "\n\t\t保留目录：" + horizonPack.getKeepDirectories() + "\n\t\tActivity：" + horizonPack.getActivity() + "\n\t\t环境类名：" + horizonPack.getEnvironmentClass();
            item.items.add(childItem);
            items.add(item);
            adapter = new MyAdapter(mainActivity);
            adapter.setData(items);
        }
        mainActivity.runOnUiThread(() -> {
            if (items.size() != 0) {
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.GONE);
                print("点击右上角图标可在线安装", Snackbar.LENGTH_LONG);
            }
            listView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                }
                return true;
            });
        });
    }

    private static class GroupItem {
        String title;
        String hint;
        List<ChildItem> items = new ArrayList<>();
    }

    private static class ChildItem {
        String title;
    }

    private static class ChildHolder {
        TextView title;
    }

    private static class GroupHolder {
        TextView title;
        TextView hint;
        MaterialCardView cardView;
        ImageView expandImageView, menuImageView;
    }

    /**
     * Adapter for our list of {@link GroupItem}s.
     */
    private class MyAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {
        private LayoutInflater inflater;

        private List<GroupItem> items;

        public MyAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<GroupItem> items) {
            this.items = items;
        }

        @Override
        public ChildItem getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder holder;
            ChildItem item = getChild(groupPosition, childPosition);
            if (convertView == null) {
                holder = new ChildHolder();
                convertView = inflater.inflate(R.layout.expand_chidren_item, parent, false);
                holder.title = convertView.findViewById(R.id.horizon_expand_text);
                convertView.setTag(holder);
            } else {
                holder = (ChildHolder) convertView.getTag();
            }

            holder.title.setText(item.title);
            holder.title.setOnLongClickListener(v -> {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) mainActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", item.title);
                cm.setPrimaryClip(mClipData);
                print("已复制到粘贴板", Snackbar.LENGTH_SHORT);
                return true;
            });
            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return items.get(groupPosition).items.size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder holder;
            GroupItem item = getGroup(groupPosition);
            if (convertView == null) {
                holder = new GroupHolder();
                convertView = inflater.inflate(R.layout.horizon_item, parent, false);
                holder.title = convertView.findViewById(R.id.horizon_itemsettingTextView1);
                holder.hint = convertView.findViewById(R.id.horizon_itemsettingTextView2);
                holder.cardView = convertView.findViewById(R.id.horizon_cardView);
                holder.menuImageView = convertView.findViewById(R.id.horizon_itemsettingImageView1);
                holder.expandImageView = convertView.findViewById(R.id.horizon_itemsettingImageView2);
                convertView.setTag(holder);
            } else {
                holder = (GroupHolder) convertView.getTag();
            }

            holder.menuImageView.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(mainActivity, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.pack_manager, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item1 -> {
                    switch (item1.getItemId()) {
                        case R.id.pack_delete:
                            new MaterialAlertDialogBuilder(mainActivity)
                                    .setTitle("提示")
                                    .setMessage("是否删除Horizon包：" + item.title)
                                    .setPositiveButton("确定", (dialog, which) -> {
                                        ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                        progressDialog.setCancelable(false);
                                        progressDialog.setMessage("正在删除");
                                        progressDialog.show();
                                        new Thread(() -> {
                                            String deleteName = FinalValuable.horizonPackList.get(groupPosition).getFolderName();
                                            Algorithm.deleteFile(new File(FinalValuable.HorizonPackPath + File.separator + deleteName));
                                            FinalValuable.horizonPackList = Algorithm.getNativePackList(false);
                                            items.clear();
                                            showList();
                                            mainActivity.runOnUiThread(() -> {
                                                progressDialog.dismiss();
                                                print("已删除", Snackbar.LENGTH_SHORT);
                                                if (FinalValuable.chooseFolderName != null)
                                                    if (FinalValuable.chooseFolderName.equals(deleteName)) {
                                                        startActivity(new Intent(mainActivity, OpenActivity.class));
                                                        mainActivity.finish();
                                                        Toast.makeText(mainActivity, "已删除当前选择的包，正在重启软件...", Toast.LENGTH_SHORT).show();
                                                    }

                                            });
                                        }).start();
                                    })
                                    .setNegativeButton("取消", null)
                                    .show();

                            break;
                        case R.id.pack_clone:
                            final EditText editText = new EditText(mainActivity);
                            editText.setHint("留空默认为" + item.title + " clone");
                            new MaterialAlertDialogBuilder(mainActivity)
                                    .setTitle("请输入克隆后的名称")
                                    .setView(editText)
                                    .setCancelable(false)
                                    .setPositiveButton("确定", (dialog1, which1) -> {
                                        String str = editText.getText().toString();
                                        if (str.length() == 0) {
                                            str = item.title + " clone";
                                        }
                                        ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                        progressDialog.setCancelable(false);
                                        progressDialog.setMessage("正在克隆，请稍等...");
                                        progressDialog.show();
                                        String finalStr = str;
                                        String finalStr1 = str;
                                        new Thread(() -> {
                                            final File[] orderFile = {new File(FinalValuable.HorizonPackPath + File.separator + finalStr.replace(" ", "_"))};
                                            if (orderFile[0].exists()) {
                                                mainActivity.runOnUiThread(() -> {
                                                    orderFile[0] = new File(orderFile[0].toString() + "_bak");
                                                    Toast.makeText(mainActivity, "文件夹已存在，正在克隆到：" + orderFile[0].getName(), Toast.LENGTH_SHORT).show();
                                                });
                                            }
                                            Algorithm.copyFolder(FinalValuable.HorizonPackPath + File.separator + FinalValuable.horizonPackList.get(groupPosition).getFolderName()
                                                    , orderFile[0].toString());
                                            File installFile = new File(orderFile[0].toString() + File.separator + ".installation_info");
                                            try {
                                                JSONObject jsonObject = new JSONObject(Algorithm.readFile(installFile));
                                                jsonObject.put("timestamp", new Date().getTime() + "");
                                                jsonObject.put("customName", finalStr1);
                                                jsonObject.put("internalId", getUUID32());
                                                Algorithm.writeFile(installFile.toString(), Algorithm.formatJson(jsonObject.toString()));
                                            } catch (JSONException | IOException e) {
                                                e.printStackTrace();
                                            }
                                            FinalValuable.horizonPackList = Algorithm.getNativePackList(false);
                                            items.clear();
                                            showList();
                                            mainActivity.runOnUiThread(() -> {
                                                progressDialog.dismiss();
                                                print("克隆完毕", Snackbar.LENGTH_SHORT);
                                            });
                                        }).start();
                                        new Thread(() -> {
                                            while (true) {
                                                if (!progressDialog.isShowing()) break;
                                                try {
                                                    Thread.sleep(20);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                mainActivity.runOnUiThread(() -> progressDialog.setMessage("正在执行克隆...\n" + FinalValuable.installState));
                                            }

                                        }).start();
                                    })
                                    .setNegativeButton("取消", null).create().show();
                            break;
                        case R.id.pack_rename:
                            File installFile = new File(FinalValuable.HorizonPackPath + File.separator + FinalValuable.horizonPackList.get(groupPosition).getFolderName() + File.separator + ".installation_info");
                            final EditText editText2 = new EditText(mainActivity);
                            editText2.setHint("留空默认为" + FinalValuable.horizonPackList.get(groupPosition).getInstallName() + " Rename");
                            new MaterialAlertDialogBuilder(mainActivity)
                                    .setTitle("请输入名称")
                                    .setView(editText2)
                                    .setCancelable(false)
                                    .setPositiveButton("确定", (dialog1, which1) -> {
                                        String str = editText2.getText().toString();
                                        if (str.length() == 0) {
                                            str = item.title + " Rename";
                                        }
                                        JSONObject jsonObject = null;
                                        try {
                                            jsonObject = new JSONObject(Algorithm.readFile(installFile));
                                            jsonObject.put("customName", str);
                                            Algorithm.writeFile(installFile.toString(), Algorithm.formatJson(jsonObject.toString()));
                                            holder.title.setText(str);
                                            item.title = str;
                                            print("重命名完成", Snackbar.LENGTH_SHORT);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create().show();
                            break;
                        default:
                            break;
                    }
                    return false;
                });

                popupMenu.show();
            });

            holder.expandImageView.setOnClickListener(v -> {
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroupWithAnimation(groupPosition);
                } else {
                    listView.expandGroupWithAnimation(groupPosition);
                }
            });
            holder.title.setText(item.title);
            holder.hint.setText(item.hint);
            holder.cardView.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(mainActivity)
                        .setMessage("要切换为管理" + item.title + "吗？")
                        .setTitle("提示")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Algorithm.createFlashFile();
                                OpenActivity.initFinalValuableHz(FinalValuable.HorizonPackPath + File.separator + FinalValuable.horizonPackList.get(groupPosition).getFolderName() + File.separator);
                                print("已切换", Snackbar.LENGTH_SHORT);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create().show();
            });
            if (isExpanded) {
                holder.expandImageView.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
            } else {
                holder.expandImageView.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return true;
        }

    }
}
