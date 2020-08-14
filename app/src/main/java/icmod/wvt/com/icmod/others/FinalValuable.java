package icmod.wvt.com.icmod.others;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import icmod.wvt.com.icmod.ui.download.DownloadFragment;
import icmod.wvt.com.icmod.ui.home.HomeFragment;

public class FinalValuable {
    public static final int MOD = 1, MCMAP = 2,
        ICMAP = 3, TESTMAP = 23, ICRES = 4, HZPACK = 5, LoginError = 7, LoginSuccess = 8, LoginEmpty = 9, LoginNoUser = 10, LoginNoInternet = 11,
        OnlineGf = 13, OnlineHhz = 14, FileChoosePath = 15, FileChooseOther = 16, FragmentTypeHome = 17, FragmentTypeHorizon = 18;
    //自动安装要验证的List文件
    public static List<String> statueList = new ArrayList<>();
    public static String ICCNUrl = "https://adodoz.cn/";
    public static String ICHhzUrl = "https://dev.adodoz.cn/";
    public static String MODDir = null;
    public static String MCMAPDir = null;
    public static String ICMAPDir = null;
    public static String WvTWorkDir = null;
    public static String ResDir = null;
    public static String ICResDir = null;
    public static String MODTestDir = null;
    public static String MODDataPath = null;
    public static String DownLoadPath = null;
    public static String NetModDataGw =null;
    public static String NetModDataHhz =null;
    public static String UserInfo = null;
    public static String QQGroupJson = null;
    public static String PackSharePath = null;
    public static String HorizonPackPath = null;
    public static Context MainActivityContext = null;
    public static HomeFragment fragmentHome = null;
    public static DownloadFragment fragmentDownload = null;
    public static LruCacheUtils lruCacheUtils = null;
    public static String flashHomeData = null;
    public static String flashHorizonData = null;
    public static JSONArray jsonArrayGw = new JSONArray();
    public static JSONArray jsonArrayHhz = new JSONArray();
    public static String presentLoadingGw = "";
    public static String getPresentLoadingHhz = "";
    //加载是否完毕
    public static boolean loadingFinishGw = false;
    public static boolean loadingFinishHhz = false;
    //加载是否失败
    public static boolean loadingErrorGw = false;
    public static boolean loadingErrorHhz = false;
    //文件选择快速跳转自定义配置文件
    public static JSONArray fileConfig = null;
    //安装状态异步显示
    public static String installState = " ";
    //Hz包列表
    public static List<HorizonPack> horizonPackList = null;
    //当前选择管理的Horizon包的文件夹名字
    public static String chooseFolderName = null;
    //在线安装Hz包界面的AlertDialog
    public static AlertDialog alertDialog = null;
    //SuperInstallSystem类的一些变量
    public static List<String> globalList = new ArrayList<>();
    public static List<String> mods = new ArrayList<>();
    public static List<String> maps = new ArrayList<>();
    public static List<String> hzPacks = new ArrayList<>();
    public static List<String> resPacks = new ArrayList<>();
    public static void clearSIS() {
        globalList.clear();
        mods.clear();
        maps.clear();
        hzPacks.clear();
        resPacks.clear();
    }
}
