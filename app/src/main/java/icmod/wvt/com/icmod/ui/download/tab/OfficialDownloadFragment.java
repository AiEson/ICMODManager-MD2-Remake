package icmod.wvt.com.icmod.ui.download.tab;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.MOD;
import icmod.wvt.com.icmod.others.OnlineMOD;
import icmod.wvt.com.icmod.others.SuperInstallSystem;
import icmod.wvt.com.icmod.ui.MainActivity;

import static icmod.wvt.com.icmod.ui.MainActivity.print;

public class OfficialDownloadFragment extends Fragment {
    public MainActivity mainActivity;
    ListView listView;
    List<OnlineMOD> onlineMODList = new ArrayList<>();
    List<OnlineMOD> onlineMODListSearch = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_download_list, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        listView = view.findViewById(R.id.download_fragment_listView);
        new Thread(() -> {
            List<MOD> modList = new ArrayList<>();
            for (int i = 0; i < FinalValuable.jsonArrayGw.length(); i++) {
                JSONObject jsonObject = null;
                Log.e("TAG", i + "");
                final int finalI = i;
                final JSONArray finalJsonArray = FinalValuable.jsonArrayGw;
                final int finalI1 = i;
                try {
                    jsonObject = FinalValuable.jsonArrayGw.getJSONObject(i);
                    String name = jsonObject.getString("title");
                    String description = jsonObject.getString("description");
                    OnlineMOD onlineMOD = new OnlineMOD(FinalValuable.ICCNUrl + "mods/" + jsonObject.getInt("id") + ".zip", FinalValuable.ICCNUrl + "mods/img/" + jsonObject.getString("icon"), name, description, FinalValuable.OnlineGf);
                    boolean isHave = false;
                    for (int j = 0; j < modList.size(); j++) {
                        if (modList.get(j).getName() != null && modList.get(j).getName().equals(name))
                            isHave = true;
                    }
                    if (!isHave)
                        onlineMODList.add(onlineMOD);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mainActivity.runOnUiThread(() -> {
                listView.setAdapter(new OnlineMODAdapter(mainActivity, R.layout.mod_item, onlineMODList));
                listView.setVisibility(View.VISIBLE);
            });
        }).start();
    }

    //获取符合条件的条目并加入List中,并显示出来
    public void showSearchList(String nr) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("正在搜索...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Integer> allowList = new ArrayList<>();
                for (int i = 0; i < onlineMODList.size(); i++) {
                    OnlineMOD onlineMOD = onlineMODList.get(i);
                    String allInfo = Algorithm.getStringNoBlank(onlineMOD.getName() + onlineMOD.getDescribe() + onlineMOD.getModUrl()).toLowerCase();
                    if (allInfo.contains(nr.toLowerCase())) {
                        allowList.add(i);
                    }
                }
                for (int i : allowList) {
                    onlineMODListSearch.add(onlineMODList.get(i));
                }
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        listView.setAdapter(new OnlineMODAdapter(mainActivity, R.layout.mod_item, onlineMODListSearch));
                        print("搜索完成，共" + onlineMODListSearch.size() + "个条目", Snackbar.LENGTH_SHORT);
                    }
                });
            }
        }).start();
    }

    class OnlineMODAdapter extends ArrayAdapter<OnlineMOD> {
        private List<OnlineMOD> modList;
        private int resourceID;

        public OnlineMODAdapter(Context context, int resourcesID, List<OnlineMOD> objects) {
            super(context, resourcesID, objects);
            this.resourceID = resourcesID;
            this.modList = objects;
        }

        @Override
        public int getCount() {
            return modList.size();
        }

        @Override
        public OnlineMOD getItem(int position) {
            return modList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final OnlineMOD mod = modList.get(position);
            final View view;
            final ViewHolderMOD viewHolder;
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
            viewHolder.aSwitch.setVisibility(View.GONE);
            viewHolder.button1.setText("安装");
            viewHolder.button2.setText("仅下载");
            viewHolder.textView1.setText(getItem(position).getName());
            viewHolder.textView2.setText(getItem(position).getDescribe());
            viewHolder.imageView.setTag(getItem(position).getImageUrl());
            viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
            if (mainActivity.lruCacheUtils.getPicFromMemory(getItem(position).getImageUrl()) != null) {
                if (getItem(position).getImageUrl() == viewHolder.imageView.getTag()) {
                    BitmapDrawable drawable = new BitmapDrawable(mainActivity.lruCacheUtils.getPicFromMemory(getItem(position).getImageUrl()));
                    drawable.getPaint().setFilterBitmap(false);
                    viewHolder.imageView.setImageDrawable(drawable);
                }
                else
                    viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
            } else {
                viewHolder.imageView.setImageBitmap(mainActivity.lruCacheUtils.getPicFromMemory("null"));
                new Thread(() -> {
                    final Bitmap bitmap = Algorithm.getImageBitmapFromUrl(getItem(position).getImageUrl());
                    // 如果本地还没缓存该图片，就缓存
                    if (mainActivity.lruCacheUtils.getPicFromMemory(getItem(position).getImageUrl()) == null) {
                        mainActivity.lruCacheUtils.savePicToMemory(getItem(position).getImageUrl(), bitmap);
                    }

                    mainActivity.runOnUiThread(() -> {
                        if (viewHolder.imageView != null && bitmap != null && viewHolder.imageView.getTag().toString() == getItem(position).getImageUrl()) {
                            BitmapDrawable drawable = new BitmapDrawable(bitmap);
                            drawable.getPaint().setFilterBitmap(false);
                            viewHolder.imageView.setImageDrawable(drawable);
                        }
                    });

                }).start();
            }

            if (mod.getName() != null)
                viewHolder.textView1.setText(mod.getName());
            viewHolder.button1.setOnClickListener(v -> {
                final ProgressDialog progressDialog = new ProgressDialog(mainActivity);
                progressDialog.setMessage("下载正在开始...请稍等");
                progressDialog.setCancelable(false);
                progressDialog.show();
                FileDownloader.getImpl().create(getItem(position).getModUrl()).setPath(FinalValuable.DownLoadPath + File.separator + getItem(position).getName() + ".icmod")
                        .setListener(new FileDownloadLargeFileListener() {
                            @Override
                            protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                            }

                            @Override
                            protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                                if (totalBytes == -1)
                                    progressDialog.setMessage("已下载：" + Algorithm.readableFileSize(soFarBytes));
                                else
                                    progressDialog.setMessage("已下载：" + Algorithm.readableFileSize(soFarBytes) + "  " + Algorithm.getPercent(soFarBytes, totalBytes));
                            }

                            @Override
                            protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {

                            }

                            @Override
                            protected void completed(BaseDownloadTask task) {
                                new Thread(() -> {
                                    if (!new SuperInstallSystem().intall(FinalValuable.DownLoadPath + File.separator + getItem(position).getName() + ".icmod").equals("")) {
                                        mainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                print("安装成功", Snackbar.LENGTH_SHORT);
                                                progressDialog.dismiss();
                                                try {
                                                    modList.remove(position);
                                                    notifyDataSetChanged();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        try {
                                            Algorithm.copyFile(FinalValuable.DownLoadPath + File.separator + getItem(position).getName() + ".icmod", Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + getItem(position).getName() + ".icmod");
                                            mainActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    print("安装失败，已保存" + getItem(position).getName() + ".icmod在" + "Download目录下，请尝试手动安装", Snackbar.LENGTH_LONG);
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }).start();

                            }

                            @Override
                            protected void error(BaseDownloadTask task, Throwable e) {
                                progressDialog.dismiss();
                                print("下载失败", Snackbar.LENGTH_SHORT);
                                e.printStackTrace();
                            }

                            @Override
                            protected void warn(BaseDownloadTask task) {

                            }
                        }).start();
            });
            viewHolder.button2.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getItem(position).getModUrl())
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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

}
