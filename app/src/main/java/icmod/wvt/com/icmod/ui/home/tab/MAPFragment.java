package icmod.wvt.com.icmod.ui.home.tab;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.MAP;
import icmod.wvt.com.icmod.others.MOD;
import icmod.wvt.com.icmod.ui.MainActivity;

import static icmod.wvt.com.icmod.ui.MainActivity.print;

public class MAPFragment extends Fragment {

    int type;
    ListView listView;
    MainActivity mainActivity;
    PullRefreshLayout pullRefreshLayout;
    LocalMAPAdapter adapter;
    boolean selectMode = false;

    public MAPFragment(int type) {
        this.type = type;
    }

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
            List<MAP> mapList = flashNativeMAP(false);
            adapter = new LocalMAPAdapter(getContext(), R.layout.map_item, mapList);
            mainActivity.runOnUiThread(() -> {
                listView.setAdapter(adapter);
                print("刷新成功，共" + mapList.size() + "个", Snackbar.LENGTH_SHORT);
                pullRefreshLayout.setRefreshing(false);
                pullRefreshLayout.clearAnimation();
            });
        }).start());

        List<MAP> mapList = flashNativeMAP(false);
        adapter = new LocalMAPAdapter(getContext(), R.layout.map_item, mapList);
        listView.setAdapter(adapter);
    }

    public List<MAP> flashNativeMAP(boolean showNumber) {
        List<MAP> ret = new ArrayList<MAP>();
        File mapP = new File(type == FinalValuable.ICMAP ? FinalValuable.ICMAPDir : FinalValuable.MCMAPDir);
        File[] mapsFIle = mapP.listFiles();
        if (mapsFIle != null)
            for (int i = 0; i < mapsFIle.length; i++) {
                if (mapsFIle[i].isDirectory()) {
                    MAP map = Algorithm.getNativeMAPClass(mapsFIle[i].toString(), type);
                    if (map != null) {
                        ret.add(map);
                        if (map.getImagePath() != null) {
                            Bitmap bitmap = Algorithm.getBitmap(map.getImagePath());
                            mainActivity.lruCacheUtils.savePicToMemory(map.getImagePath(), bitmap);
                        }
                    }
                }
            }
        if (showNumber)
            print("加载本地地图完毕，共" + ret.size() + "个", Snackbar.LENGTH_SHORT);
        return ret;
    }

    public void flashList(boolean isShow) {
        new Thread(() -> {
            List<MAP> mapList = flashNativeMAP(false);
            adapter = new LocalMAPAdapter(mainActivity, R.layout.map_item, mapList);
            mainActivity.runOnUiThread(() -> {
                listView.setAdapter(adapter);
                if (isShow)
                    print("已刷新", Snackbar.LENGTH_SHORT);
            });
        }).start();
    }


    class LocalMAPAdapter extends ArrayAdapter<MAP> {
        private List<MAP> mapList;
        private int resourceID;
        private List<Integer> selectNum = new ArrayList<>();


        LocalMAPAdapter(@NonNull Context context, int resource, List<MAP> objects) {
            super(context, resource, objects);
            this.resourceID = resource;
            this.mapList = objects;
        }

        public void setMapList(List<MAP> mapList) {
            this.mapList = mapList;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final MAP map = getItem(position);
            final View view;
            ViewHolderMAP viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(mainActivity).inflate(resourceID, parent, false);
                viewHolder = new ViewHolderMAP();
                viewHolder.cardView = view.findViewById(R.id.CardView);
                viewHolder.textView1 = view.findViewById(R.id.itemsettingTextView1);
                viewHolder.imageView = view.findViewById(R.id.itemsettingImageView1);
                viewHolder.button1 = view.findViewById(R.id.itemsettingButton1);
                viewHolder.button2 = view.findViewById(R.id.itemsettingButton2);
                viewHolder.button3 = view.findViewById(R.id.change);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolderMAP) view.getTag();
            }
            viewHolder.textView1.setText(getItem(position) == null ? "未知" : getItem(position).getName());
            if (map.getImagePath() != null) {
                Bitmap bitmap = mainActivity.lruCacheUtils.getPicFromMemory(map.getImagePath());
                if (bitmap != null)
                    viewHolder.imageView.setImageBitmap(bitmap);
                else
                    viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
            } else
                viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
            viewHolder.button1.setOnClickListener(v -> {
                String allInfo = "";
                if (map.getName() != null)
                    allInfo += "名称：" + map.getName() + "\n";
                else allInfo += "名称：未知\n";
                allInfo += "路径：" + map.getMapPath();
                AlertDialog alertDialog = new MaterialAlertDialogBuilder(mainActivity)
                        .setTitle("详细信息")
                        .setMessage(allInfo)
                        .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.button2.setOnClickListener(v -> {
                final File f = new File(map.getMapPath());
                AlertDialog alertDialog = new MaterialAlertDialogBuilder(mainActivity)
                        .setTitle("提示")
                        .setMessage("将要删除地图：" + map.getName() + "，是否继续（该操作不可撤销）？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Algorithm.deleteFile(f);
                                if (!f.exists()) {
                                    print("已删除地图：" + map.getName(), Snackbar.LENGTH_SHORT);
                                    mapList.remove(position);
                                    notifyDataSetChanged();
                                }
                            }
                        })
                        .create();
                alertDialog.show();
            });
            viewHolder.button3.setOnClickListener(v -> new MaterialAlertDialogBuilder(mainActivity)
                    .setTitle("提示")
                    .setMessage("是否移动此地图（" + getItem(position).getName() + "）至" + (type == FinalValuable.MCMAP ? "IC地图？" : "MC地图？"))
                    .setNegativeButton("取消", (dialog, which) -> {

                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final File mapFile = new File(getItem(position).getMapPath());
                            final File toFile = new File((type == FinalValuable.MCMAP ? FinalValuable.ICMAPDir : FinalValuable.MCMAPDir) + File.separator + mapFile.getName());
                            if (toFile.exists()) {
                                new MaterialAlertDialogBuilder(mainActivity)
                                        .setTitle("提示")
                                        .setMessage("目标路径已存在存档，是否覆盖？")
                                        .setNegativeButton("取消", (dialog, which) -> {

                                        })
                                        .setPositiveButton("确定", (dialogInterface1, i1) -> {
                                            ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                            progressDialog.setCancelable(false);
                                            progressDialog.setMessage("正在移动...");
                                            progressDialog.show();
                                            new Thread(() -> {
                                            Algorithm.deleteFile(toFile);
                                            if (Algorithm.copyFolder(mapFile.toString(), toFile.toString()))
                                                mainActivity.runOnUiThread(() -> {
                                                    progressDialog.dismiss();
                                                    print("移动成功", Snackbar.LENGTH_SHORT);
                                                });
                                            else mainActivity.runOnUiThread(() -> {
                                                progressDialog.dismiss();
                                                print("移动失败", Snackbar.LENGTH_SHORT);
                                            });
                                        }).start();})
                                        .create().show();
                            } else {
                                ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                progressDialog.setCancelable(false);
                                progressDialog.setMessage("正在移动...");
                                progressDialog.show();
                                new Thread(() -> {
                                    Algorithm.deleteFile(toFile);
                                    if (Algorithm.copyFolder(mapFile.toString(), toFile.toString()))
                                        mainActivity.runOnUiThread(() -> {
                                            progressDialog.dismiss();
                                            print("移动成功", Snackbar.LENGTH_SHORT);
                                        });
                                    else mainActivity.runOnUiThread(() -> {
                                        progressDialog.dismiss();
                                        print("移动失败", Snackbar.LENGTH_SHORT);
                                    });
                                }).start();
                            }
                        }
                    })
                    .create().show());

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

            viewHolder.cardView.setOnLongClickListener(v -> {
                if (!selectMode) {

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
                    MaterialButton buttonDelete = window.findViewById(R.id.mod_select_delete);
                    MaterialButton buttonShare = window.findViewById(R.id.mod_select_share);
                    LinearLayout linearLayoutEnable = window.findViewById(R.id.mod_select_layout_middle);

                    linearLayoutEnable.setVisibility(View.GONE);

                    buttonSelAll.setOnClickListener(v1 -> {
                        selectNum.clear();
                        for (int i = 0; i < mapList.size(); i++) {
                            Log.e("TAG", "aaaaaa");
                            getItem(i).setChecked(true);
                            selectNum.add(i);
                        }
                        notifyDataSetChanged();
                    });

                    buttonCalAll.setOnClickListener(v12 -> {
                        for (int i = 0; i < mapList.size(); i++) {
                            getItem(i).setChecked(false);
                        }
                        selectMode = false;
                        selectNum.clear();
                        notifyDataSetChanged();
                        dialogBuilder.dismiss();
                    });

                    buttonDelete.setOnClickListener(v13 -> {
                        String mods = "";
                        List<File> delFile = new ArrayList<>();
                        for (int i = 0; i < selectNum.size(); i++) {
                            int posi = selectNum.get(i);
                            mods += getItem(posi).getName() + "\n";
                        }
                        new MaterialAlertDialogBuilder(mainActivity).setTitle("提示")
                                .setMessage("将要删除以下地图：\n" + mods + "是否继续？")
                                .setPositiveButton("删除", (dialog, which) -> {
                                    ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                    progressDialog.setCancelable(false);
                                    progressDialog.setMessage("正在删除，请稍等...");
                                    progressDialog.show();
                                    new Thread(() -> {
                                        for (int i = 0; i < selectNum.size(); i++) {
                                            int posi2 = selectNum.get(i);
                                            File modFile = new File(getItem(posi2).getMapPath());
                                            delFile.add(modFile);
                                        }
                                        for (int i = 0; i < delFile.size(); i++) {
                                            Algorithm.deleteFile(delFile.get(i));
                                        }
                                        mainActivity.runOnUiThread(() -> {
                                            print("已删除所选地图", Snackbar.LENGTH_SHORT);
                                            dialogBuilder.dismiss();
                                            progressDialog.dismiss();
                                            flashList(false);
                                        });
                                    }).start();
                                })
                                .setNegativeButton("取消", null)
                                .create().show();
                    });

                    buttonShare.setOnClickListener(v14 -> {
                        String mods = "";
                        for (int i : selectNum) {
                            mods += getItem(i).getName() + "\n";
                        }
                        new MaterialAlertDialogBuilder(mainActivity).setTitle("提示")
                                .setMessage("将会打包并分享以下地图：\n" + mods + "是否继续？")
                                .setPositiveButton("确定", (dialog, which) -> {

                                    final EditText editText = new EditText(mainActivity);
                                    editText.setHint("留空默认为MAPs");
                                    new MaterialAlertDialogBuilder(mainActivity)
                                            .setTitle("请输入您要打包的名称")
                                            .setView(editText)
                                            .setCancelable(false)
                                            .setPositiveButton("确定", (dialog1, which1) -> {
                                                String str = editText.getText().toString();
                                                if (str.length() == 0) {
                                                    str = "MAPs";
                                                }
                                                ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                                                progressDialog.setCancelable(false);
                                                progressDialog.setMessage("正在打包，请稍等...");
                                                progressDialog.show();
                                                String finalStr = str;
                                                new Thread(() -> {
                                                    List<File> fileList = new ArrayList<>();
                                                    for (int i : selectNum) {
                                                        fileList.add(new File(getItem(i).getMapPath()));
                                                    }
                                                    String zipFile = FinalValuable.PackSharePath + File.separator + finalStr + ".zip";
                                                    boolean isDone = Algorithm.zipFile(fileList, zipFile);

                                                    mainActivity.runOnUiThread(() -> {
                                                        if (isDone) {
                                                            Toast.makeText(mainActivity, "压缩成功，正在前往分享", Toast.LENGTH_LONG).show();
                                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(zipFile)));
                                                            shareIntent.setType("application/zip");
                                                            mainActivity.startActivity(shareIntent);
                                                        } else {
                                                            Toast.makeText(mainActivity, "压缩失败", Toast.LENGTH_LONG).show();
                                                        }
                                                        progressDialog.dismiss();
                                                        dialogBuilder.dismiss();
                                                        clearSelect();
                                                        flashList(false);
                                                    });
                                                }).start();
                                            })
                                            .setNegativeButton("取消", null).create().show();
                                }).setNegativeButton("取消", null)
                                .create().show();
                    });
                }

                return true;
            });

            if (!getItem(position).isChecked()) {
                viewHolder.cardView.setChecked(false);
            } else {
                viewHolder.cardView.setChecked(true);
            }
            return view;
        }

        public void clearSelect() {
            selectMode = false;
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).setChecked(false);
            }
            selectNum.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Nullable
        @Override
        public MAP getItem(int position) {
            return mapList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    class ViewHolderMAP {
        MaterialCardView cardView;
        TextView textView1;
        ImageView imageView;
        MaterialButton button1, button2, button3;
    }

}


