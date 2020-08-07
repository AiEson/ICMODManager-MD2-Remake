package icmod.wvt.com.icmod.ui.download;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import icmod.wvt.com.icmod.R;
import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;
import icmod.wvt.com.icmod.others.MOD;
import icmod.wvt.com.icmod.others.OnlineMOD;
import icmod.wvt.com.icmod.ui.LoginActivity;
import icmod.wvt.com.icmod.ui.MainActivity;
import icmod.wvt.com.icmod.ui.download.tab.ChineseDownloadFragment;
import icmod.wvt.com.icmod.ui.download.tab.OfficialDownloadFragment;
import icmod.wvt.com.icmod.ui.home.HomeFragment;

public class DownloadFragment extends Fragment {
    //控件定义部分
    TabLayout tabLayout;
    List<Fragment> fragments = new ArrayList<>();
    ViewPager viewPager;
    RelativeLayout rootLayout;
    //其他定义
    MainActivity mainActivity;

    //数据定义
    List<String> titles = new ArrayList<>();

    //Fragment定义
    OfficialDownloadFragment officialDownloadFragment = null;
    ChineseDownloadFragment chineseDownloadFragment = null;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.home_tablayout_download);
        viewPager = view.findViewById(R.id.home_viewPager_download);
        rootLayout = view.findViewById(R.id.download_layout);
        mainActivity = (MainActivity) getActivity();

        MainActivity.getFab().setVisibility(View.GONE);

        if (new File(FinalValuable.UserInfo).exists()) {
            ProgressDialog progressDialog = new ProgressDialog(mainActivity);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("正在加载数据...");
            progressDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //如果数据加载未完成
                        if (!FinalValuable.loadingFinishGw) {
                            //判断加载是否出错
                            if (FinalValuable.loadingErrorGw) {
                                //重启加载
                                FinalValuable.loadingErrorGw = false;
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
                            }
                        }

                        //如果数据加载未完成
                        if (!FinalValuable.loadingFinishHhz) {
                            //判断加载是否出错
                            if (FinalValuable.loadingErrorHhz) {
                                //重启加载
                                new Thread(() -> {
                                    try {
                                        String ret2 = null;
                                        ret2 = Algorithm.Post("", "https://dev.adodoz.cn/api/mod/list", mainActivity);
                                        try {
                                            FinalValuable.jsonArrayHhz = new JSONObject(ret2).getJSONArray("data");
                                            Algorithm.writeFile(FinalValuable.NetModDataHhz, ret2);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        FinalValuable.loadingErrorHhz = false;
                                        FinalValuable.loadingFinishHhz = true;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        FinalValuable.loadingErrorHhz = true;
                                        FinalValuable.loadingFinishHhz = false;
                                    }
                                }).start();
                            }
                        }

                        //异常分析完毕，开始循环排查加载状况
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //使用Set记录失效服务器，防止重复
                                Set<String> errorServer = new HashSet<>();
                                String message;
                                while(true) {
                                    message = "";
                                    if (!FinalValuable.loadingFinishGw) {
                                        message += "官网源" + FinalValuable.presentLoadingGw + "\n";
                                    } else {
                                        message += "官网源：加载完毕\n";
                                    }

                                    if (!FinalValuable.loadingFinishHhz) {
                                        message += "汉化组源：加载完毕";
                                    } else {
                                        message += "汉化组源：加载中...";
                                    }

                                    String finalMessage = message;
                                    mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.setMessage(finalMessage);
                                        }
                                    });

                                    //如果有服务器失效，则剔除失效服务器
                                    if (FinalValuable.loadingErrorHhz) {
                                        //设置加载完成以便跳出循环
                                        FinalValuable.loadingFinishHhz = true;
                                        errorServer.add("汉化组源");
                                    }
                                    if (FinalValuable.loadingErrorGw) {
                                        //设置加载完成以便跳出循环
                                        FinalValuable.loadingFinishGw = true;
                                        errorServer.add("官网源");
                                    }

                                    if (true){
                                        mainActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String errorServerString = "";
                                                progressDialog.dismiss();
                                                for (String servers : errorServer)
                                                    errorServerString += servers;
                                                if (errorServer.size() != 0)
                                                    Snackbar.make(rootLayout, "已失效服务器：" + errorServerString, Snackbar.LENGTH_SHORT).show();
                                                else Snackbar.make(rootLayout, "数据加载完成！", Snackbar.LENGTH_SHORT).show();

                                                if (!FinalValuable.loadingErrorGw) {
                                                    officialDownloadFragment = new OfficialDownloadFragment();
                                                    fragments.add(officialDownloadFragment);
                                                    titles.add("官网源");
                                                }
                                                if (!FinalValuable.loadingErrorHhz) {
                                                    chineseDownloadFragment = new ChineseDownloadFragment();
                                                    fragments.add(chineseDownloadFragment);
                                                    titles.add("汉化组源");
                                                }

                                                viewPager.setAdapter(new DownloadFragment.FragmentPagerAdapterCompat(getChildFragmentManager()) {
                                                    @NonNull
                                                    @Override
                                                    public Fragment getItem(int position) {
                                                        return fragments.get(position);
                                                    }

                                                    @Override
                                                    public int getCount() {
                                                        return fragments.size();
                                                    }

                                                    @Override
                                                    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                                                        super.destroyItem(container, position, object);
                                                    }

                                                    @Nullable
                                                    @Override
                                                    public CharSequence getPageTitle(int position) {
                                                        return titles.get(position);
                                                    }
                                                });
                                                viewPager.setOffscreenPageLimit(3);
                                                tabLayout.setupWithViewPager(viewPager);
                                                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                                    @Override
                                                    public void onTabSelected(TabLayout.Tab tab) {
                                                        if (((String) tab.getText()).equals("汉化组源"))
                                                            MainActivity.setDownloadType(FinalValuable.OnlineHhz);
                                                        if (((String) tab.getText()).equals("官网源"))
                                                            MainActivity.setDownloadType(FinalValuable.OnlineGf);
                                                    }

                                                    @Override
                                                    public void onTabUnselected(TabLayout.Tab tab) {

                                                    }

                                                    @Override
                                                    public void onTabReselected(TabLayout.Tab tab) {

                                                    }
                                                });

                                            }
                                        });
                                        break;
                                    }
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mainActivity, "数据载入失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                }
            }).start();
        } else {
            new MaterialAlertDialogBuilder(mainActivity)
                    .setTitle("提示")
                    .setMessage("若要使用在线下载，请先登录")
                    .setNegativeButton("暂不登录", null)
                    .setPositiveButton("去登录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mainActivity.startActivity(new Intent(mainActivity, LoginActivity.class));
                        }
                    }).create()
                    .show();

        }


    }

    public void flashList(String nr, int type) {
        switch (type) {
            case FinalValuable.OnlineGf:
                officialDownloadFragment.showSearchList(nr);
                break;
            case FinalValuable.OnlineHhz:
                chineseDownloadFragment.showSearchList(nr);
                break;

        }
    }

    public abstract static class FragmentPagerAdapterCompat extends FragmentPagerAdapter {

        private SparseArray<Fragment> fragments;

        public FragmentPagerAdapterCompat(FragmentManager fm) {
            super(fm);
            fragments = new SparseArray<>(getCount());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragments.put(position, fragment);
            return fragment;
        }



        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragments.remove(position);
        }



        public Fragment getFragment(int position) {
            return fragments.get(position);
        }

    }
}