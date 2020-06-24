package icmod.wvt.com.icmod.others;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Algorithm {


    //字节转换
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static void writeFile(String fp, String nr) throws IOException {
        FinalValuable.installState = "正在写入文件信息：" + new File(fp).getName();
        File wj = new File(fp);
        wj.getParentFile().mkdirs();
        if (!wj.exists())
            wj.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(wj));
        writer.write(nr);
        writer.close();
    }

    //毫秒时间戳转时间
    public static String getTimeFromTimestamp(long timestamp) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timestamp);
        return simpleDateFormat.format(date);
    }

    public static String getPercent(long y, long z) {
        String baifenbi = "";// 接受百分比的值
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
// NumberFormat nf = NumberFormat.getPercentInstance();注释掉的也是一种方法
// nf.setMinimumFractionDigits( 2 ); 保留到小数点后几位
        DecimalFormat df1 = new DecimalFormat("##.00%");
// ##.00%
// 百分比格式，后面不足2位的用0补齐
// baifenbi=nf.format(fen);
        baifenbi = df1.format(fen);
        System.out.println(baifenbi);
        return baifenbi;
    }

    public static int getVersionCode(Context context)//获取版本号(内部识别号)
    {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static String getEncoding(String path) throws Exception {
        String encoding = "GBK";
        ZipFile zipFile = new ZipFile(path);
        zipFile.setCharset(Charset.forName(encoding));
        List<FileHeader> list = zipFile.getFileHeaders();
        for (int i = 0; i < list.size(); i++) {
            FileHeader fileHeader = list.get(i);
            String fileName = fileHeader.getFileName();
            if (isMessyCode(fileName)) {
                encoding = "UTF-8";
                break;
            }
        }
        return encoding;
    }

    private static boolean isMessyCode(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
            // 从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
            if ((int) c == 0xfffd) {
                // 存在乱码
                return true;
            }
        }
        return false;
    }
    //数字转化字符串
    public static String leftPad(int num, final int maxLen, char filledChar) {
        StringBuffer sb = new StringBuffer();
        String str = String.valueOf(num);
        for(int i = str.length(); i < maxLen; i++){
            sb.append(filledChar);
        }
        return sb.append(str).toString();
    }

    public static String Get(String urll, String cs, boolean hasCs) throws IOException {
        URL url = new URL(urll + (hasCs ? ("?" + cs) : ""));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }


    public static String Post(String string, String get, Context context) {
        String html = "";
        try {
            String urldizhi = get; //请求地址
            URL url = new URL(urldizhi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(50000);//超时时间
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
//      conn.setRequestProperty("User-Agent", Other.getUserAgent(context));
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(string);
            out.flush();
            out.close();

            InputStream inputStream = conn.getInputStream();
            byte[] data = StreamTool.read(inputStream);
            html = new String(data, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println("-----" + e);
            String string2 = "{\"success\":-1}";

            return string2;
        }
        return html;
    }

    //得到32位的uuid
    public static String getUUID32() {
        return UUID.randomUUID().toString().toLowerCase();
    }


    public static void createFlashFile() {
        File flashFile = new File(FinalValuable.flashHomeData);
        if (!flashFile.exists()) {
            try {
                flashFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void createHorizonFlashFile() {
        File flashFile = new File(FinalValuable.flashHorizonData);
        if (!flashFile.exists()) {
            try {
                flashFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getImageBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        URL imgUrl = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        return networkinfo != null && networkinfo.isAvailable();
    }

    public static boolean emailFormat(String acc) {
        if (acc == null || acc.length() < 5) {
            // #如果帐号小于5位，则肯定不可能为邮箱帐号eg: x@x.x
            return false;
        }
        if (!acc.contains("@")) {// 判断是否含有@符号
            return false;// 没有@则肯定不是邮箱
        }
        String[] sAcc = acc.split("@");
        if (sAcc.length != 2) {// # 数组长度不为2则包含2个以上的@符号，不为邮箱帐号
            return false;
        }
        if (sAcc[0].length() <= 0) {// #@前段为邮箱用户名，自定义的话至少长度为1，其他暂不验证
            return false;
        }
        if (sAcc[1].length() < 3 || !sAcc[1].contains(".")) {
            // # @后面为域名，位数小于3位则不为有效的域名信息
            // #如果后端不包含.则肯定不是邮箱的域名信息
            return false;
        } else {
            if (sAcc[1].substring(sAcc[1].length() - 1).equals(".")) {
                // # 最后一位不能为.结束
                return false;
            }
            String[] sDomain = sAcc[1].split("\\.");
            // #将域名拆分 tm-sp.com 或者 .com.cn.xxx
            for (String s : sDomain) {
                if (s.length() <= 0) {
                    System.err.println(s);
                    return false;
                }
            }

        }
        return true;
    }

    public static List<HorizonPack> getNativePackList(boolean showSize) {
        List<HorizonPack> horizonPacks = new ArrayList<>();

        File horizonPackPath = new File(FinalValuable.HorizonPackPath);
        File[] folderFiles = horizonPackPath.listFiles();
        for (File folder : folderFiles) {
            if (folder.isDirectory()) {
                if (new File(folder.toString() + File.separator + ".installation_complete").exists()) {
                    HorizonPack horizonPack = new HorizonPack();
                    horizonPack.setFolderName(folder.getName());
                    if (showSize)
                        horizonPack.setFileSize(FileSizeUtil.getAutoFileOrFilesSize(folder.toString()));
                    File installInfo = new File(folder.toString() + File.separator + ".installation_info");
                    File manifestFile = new File(folder.toString() + File.separator + "manifest.json");
                    try {
                        JSONObject installInfoJson = new JSONObject(Algorithm.readFile(installInfo));
                        if (!installInfoJson.isNull("uuid"))
                            horizonPack.setInstallInfoUuid(installInfoJson.getString("uuid"));
                        if (!installInfoJson.isNull("timestamp"))
                            horizonPack.setInstallTime(Algorithm.getTimeFromTimestamp(Long.parseLong(installInfoJson.getString("timestamp"))));
                        if (!installInfoJson.isNull("customName"))
                            horizonPack.setInstallName(installInfoJson.getString("customName"));
                        if (!installInfoJson.isNull("internalId"))
                            horizonPack.setInstallInternalId(installInfoJson.getString("internalId"));
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONObject manifestJson = new JSONObject(Algorithm.readFile(manifestFile));
                        if (!manifestJson.isNull("game"))
                            horizonPack.setGame(manifestJson.getString("game"));
                        if (!manifestJson.isNull("gameVersion"))
                            horizonPack.setGameVersion(manifestJson.getString("gameVersion"));
                        if (!manifestJson.isNull("pack"))
                            horizonPack.setPack(manifestJson.getString("pack"));
                        if (!manifestJson.isNull("packVersion"))
                            horizonPack.setPackVersion(manifestJson.getString("packVersion"));
                        if (!manifestJson.isNull("packVersionCode"))
                            horizonPack.setPackVersionCode(manifestJson.getInt("packVersionCode"));
                        if (!manifestJson.isNull("developer"))
                            horizonPack.setDeveloper(manifestJson.getString("developer"));
                        JSONObject descriptionJson = null;
                        if (!manifestJson.isNull("description"))
                            descriptionJson = manifestJson.getJSONObject("description");
                        if (descriptionJson != null) {
                            if (!descriptionJson.isNull("zh")) {
                                horizonPack.setDescription(descriptionJson.getString("zh"));
                            } else if (!descriptionJson.isNull("en")) {
                                horizonPack.setDescription(descriptionJson.getString("en"));
                            } else if (!descriptionJson.isNull("ru")) {
                                horizonPack.setDescription(descriptionJson.getString("ru"));
                            } else {
                                horizonPack.setDescription("未正确匹配语言，原文：" + descriptionJson.toString());
                            }
                        }

                        JSONArray directoriesJson = null;
                        String directoriesString = "";
                        if (!manifestJson.isNull("directories"))
                            directoriesJson = manifestJson.getJSONArray("directories");
                        if (directoriesJson != null) {
                            for (int i = 0; i < directoriesJson.length(); i++) {
                                directoriesString += directoriesJson.getString(i) + ", ";
                            }
                            horizonPack.setDirectories(directoriesString.substring(0, directoriesString.length() - 3));
                        }

                        JSONArray keepDirectoriesJson = null;
                        String keepDirectoriesString = "";
                        if (!manifestJson.isNull("keepDirectories"))
                            keepDirectoriesJson = manifestJson.getJSONArray("keepDirectories");
                        if (keepDirectoriesJson != null) {
                            for (int i = 0; i < keepDirectoriesJson.length(); i++) {
                                keepDirectoriesString += keepDirectoriesJson.getString(i) + ", ";
                            }
                            horizonPack.setKeepDirectories(keepDirectoriesString.substring(0, keepDirectoriesString.length() - 3));
                        }

                        if (!manifestJson.isNull("activity"))
                            horizonPack.setActivity(manifestJson.getString("activity"));
                        if (!manifestJson.isNull("environmentClass"))
                            horizonPack.setEnvironmentClass(manifestJson.getString("environmentClass"));
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    horizonPacks.add(horizonPack);
                }
            }
        }

        return horizonPacks;
    }


    public static MAP getNativeMAPClass(String path, int type) {
        MAP ret = null;
        File mappath = new File(path);
        if (mappath.isDirectory()) {
            String retName = null, retImage = null;
            File image = new File(path + File.separator + "world_icon.jpeg");
            File nameFile = new File(path + File.separator + "levelname.txt");
            if (image.exists())
                retImage = image.toString();
            if (nameFile.exists()) {
                try {
                    retName = readFile(nameFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ret = new MAP(retName, path, retImage, type);
        }
        return ret;
    }

    public static ResPack getNativeResClass(String path) {
        ResPack ret = null;
        File mappath = new File(path);
        if (mappath.isDirectory()) {
            boolean enabled = false;
            String name = null, imagePath = null, describe = null, uuid = null, packId = null, resPath = path, packVersion = null, moduleDes = null, moduleVersion = null, moduleUuid = null, moduleType = null;
            JSONObject info = null;
            File image = new File(path + File.separator + "pack_icon.png");
            File infoFile = new File(path + File.separator + "pack_manifest.json");
            if (!infoFile.exists())
                infoFile = new File(path + File.separator + "manifest.json");
            if (image.exists())
                imagePath = image.toString();
            enabled = new File(path + File.separator + "enabled.txt").exists();
            if (infoFile.exists()) {
                try {
                    info = new JSONObject(readFile(infoFile));
                    JSONArray modules = null;
                    JSONObject modulesObj = null;
                    JSONObject header = info.getJSONObject("header");
                    if (!header.isNull("pack_id"))
                        packId = header.getString("pack_id");
                    if (!header.isNull("name"))
                        name = header.getString("name");

                    if (!header.isNull("uuid"))
                        uuid = header.getString("uuid");

                    if (!header.isNull("packs_version"))
                        packVersion = header.getString("packs_version");

                    if (!header.isNull("description"))
                        describe = header.getString("description");

                    if (!header.isNull("modules"))
                        modules = header.getJSONArray("modules");
                    if (modules != null && modules.length() != 0) {
                        modulesObj = modules.getJSONObject(0);
                    }
                    if (modulesObj != null && !modulesObj.isNull("description"))
                        moduleDes = modulesObj.getString("description");

                    if (modulesObj != null && !modulesObj.isNull("version"))
                        moduleVersion = modulesObj.getString("version");
                    if (modulesObj != null && !modulesObj.isNull("uuid"))
                        moduleUuid = modulesObj.getString("uuid");
                    if (modulesObj != null && !modulesObj.isNull("type"))
                        moduleType = modulesObj.getString("type");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ret = new ResPack(name, imagePath, describe, uuid, packId, resPath, packVersion, moduleDes, moduleVersion, moduleUuid,
                    moduleType, enabled);
        }
        return ret;
    }

    //通过文件路径获取MOD类
    public static MOD getNativeMODClass(String path) {
        MOD ret = null;
        File modpath = new File(path);
        if (modpath.isDirectory()) {
            String retPath = path, retImage = null, retName = null, retVerison = null, retDescribe = null,
                    retAuthor = null;
            Boolean retEnabled = false;
            File image = new File(path + File.separator + "mod_icon.png");
            File modinfo = new File(path + File.separator + "mod.info");
            File config = new File(path + File.separator + "config.json");
            if (image.exists())
                retImage = image.toString();
            if (modinfo.exists()) {
                JSONObject modInfoJson = null;
                try {
                    modInfoJson = new JSONObject(readFile(modinfo));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (modInfoJson != null) {
                        if (!modInfoJson.isNull("name"))
                            retName = modInfoJson.getString("name");
                        else retName = modpath.getName();
                        if (!modInfoJson.isNull("author"))
                            retAuthor = modInfoJson.getString("author");
                        else retAuthor = "未知";
                        if (!modInfoJson.isNull("version"))
                            retVerison = modInfoJson.getString("version");
                        else retVerison = "未知";
                        if (!modInfoJson.isNull("description"))
                            retDescribe = modInfoJson.getString("description");
                        else retDescribe = "未知";
                    } else {
                        retName = modpath.getName();
                        retAuthor = "未知";
                        retVerison = "未知";
                        retDescribe = "未知";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            if (config.exists()) {
                try {
                    JSONObject configJson = new JSONObject(readFile(config));
                    if (!configJson.isNull("enabled"))
                        retEnabled = configJson.getBoolean("enabled");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ret = new MOD(retPath, retName, retImage, retVerison, retDescribe, retAuthor, retEnabled, FinalValuable.MOD);
        }
        return ret;
    }

    //改变MOD的启动状态
    public static boolean changeMOD(String path, boolean zt) {
        boolean ret = false;
        File config = new File(path + File.separator + "config.json");
        if (config.exists()) {
            try {
                JSONObject nr = new JSONObject(getStringNoBlank(readFile(config)));
                nr.put("enabled", zt);
                writeFile(config.toString(), nr.toString());
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                config.createNewFile();
                JSONObject nr = new JSONObject("{}");
                nr.put("enabled", zt);
                writeFile(config.toString(), nr.toString());
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    //压缩文件算法，需要多线程执行
    public static boolean zipFile(List<File> list, String pathWithName) {
        boolean ret = false;

        ZipFile zipFile = new ZipFile(pathWithName);
        File file = zipFile.getFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(false);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        try {
            for (File file1 : list) {
                if (file1.isDirectory()) {
                    zipFile.addFolder(file1, zipParameters);
                } else {
                    zipFile.addFile(file1, zipParameters);
                }
            }

            ret = true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public static ArrayList<FileChoose> orderByName(String filePath, boolean retFileChoose) {
        if (retFileChoose) {
            ArrayList<FileChoose> FileNameList = new ArrayList<>();
            File file = new File(filePath);
            File[] files = file.listFiles();
            List fileList = Arrays.asList(files);
            Collections.sort(fileList, (Comparator<File>) (o1, o2) -> {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            });
            if (!filePath.equals(Environment.getExternalStorageDirectory().toString())) {
                FileNameList.add(new FileChoose(new File(filePath).getParent(), "上级目录", FinalValuable.FileChoosePath));
            } else {
                for (int i = 0; i < FinalValuable.fileConfig.length(); i++) {
                    try {
                        JSONObject singlePack = FinalValuable.fileConfig.getJSONObject(i);
                        String packTab = singlePack.getString("name");
                        JSONArray packNameArray = singlePack.getJSONArray("pack_name");
                        JSONArray packPathArray = singlePack.getJSONArray("path");
                        boolean isHaveApp = false, isHavePath = false;
                        //验证APP是否存在
                        for (int j = 0; j < packNameArray.length(); j++) {
                            String packName = packNameArray.getString(j);
                            //如果存在APP
                            if (isAvailable(FinalValuable.MainActivityContext, packName)) {
                                isHaveApp = true;
                                break;
                            }
                        }
                        if (isHaveApp) {
                            for (int j = 0; j < packPathArray.length(); j++) {
                                String pathStr = packPathArray.getString(j);
                                String pathReplace = pathStr.replace("%SDCARD%", Environment.getExternalStorageDirectory().toString());
                                if (new File(pathReplace).exists()) {
                                    FileNameList.add(new FileChoose(pathReplace, packTab, FinalValuable.FileChooseOther));
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (File file1 : files) {
                FileNameList.add(new FileChoose(file1.toString(), file1.getName(), FinalValuable.FileChoosePath));
            }
            return FileNameList;
        }
        return null;
    }


    public static String formatJson(String strJson) {
        // 计数tab的个数
        int tabNum = 1;
        StringBuffer jsonFormat = new StringBuffer();
        int length = strJson.length();

        char last = 0;
        for (int i = 0; i < length; i++) {
            char c = strJson.charAt(i);
            if (c == '{') {
                tabNum++;
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else if (c == '}') {
                tabNum--;
                jsonFormat.append("\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
                jsonFormat.append(c);
            } else if (c == ',') {
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else if (c == ':') {
                jsonFormat.append(c + " ");
            } else if (c == '[') {
                tabNum++;
                char next = strJson.charAt(i + 1);
                if (next == ']') {
                    jsonFormat.append(c);
                } else {
                    jsonFormat.append(c + "\n");
                    jsonFormat.append(getSpaceOrTab(tabNum));
                }
            } else if (c == ']') {
                tabNum--;
                if (last == '[') {
                    jsonFormat.append(c);
                } else {
                    jsonFormat.append("\n" + getSpaceOrTab(tabNum) + c);
                }
            } else {
                jsonFormat.append(c);
            }
            last = c;
        }
        return jsonFormat.toString();
    }

    private static String getSpaceOrTab(int tabNum) {
        StringBuffer sbTab = new StringBuffer();
        for (int i = 0; i < tabNum; i++) {
            sbTab.append('\t');
        }
        return sbTab.toString();
    }

    public static boolean openApp(String packageName, Context context) {
        boolean ret = false;
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAvailable(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<>();

        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        // 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    public static Bitmap getBitmapFromRes(Context context, int res) {
        Resources r = context.getResources();
        InputStream is = r.openRawResource(res);
        BitmapDrawable bmpDraw = new BitmapDrawable(is);
        Bitmap bmp = bmpDraw.getBitmap();
        return bmp;
    }

    public static String readFile(File f) throws IOException {
        FinalValuable.installState = "正在读取文件信息：" + f.getName();
        FileReader fre = new FileReader(f);
        BufferedReader bre = new BufferedReader(fre);
        String str = "";
        StringBuilder Strret = new StringBuilder();
        while ((str = bre.readLine()) != null) {
            Strret.append(str);
        }
        bre.close();
        fre.close();
        return Strret.toString();
    }

    public static void deleteFile(File file) {
        FinalValuable.installState = "正在释放：" + file.getName();
        // 判断传递进来的是文件还是文件夹,如果是文件,直接删除,如果是文件夹,则判断文件夹里面有没有东西
        if (file.isDirectory()) {
            // 如果是目录,就删除目录下所有的文件和文件夹
            File[] files = file.listFiles();
            // 遍历目录下的文件和文件夹
            for (File f : files) {
                // 如果是文件,就删除
                if (f.isFile()) {
//                    System.out.println("已经被删除的文件:" + f);
                    // 删除文件
                    f.delete();
                } else if (file.isDirectory()) {
                    // 如果是文件夹,就递归调用文件夹的方法
                    deleteFile(f);
                }
            }
            // 删除文件夹自己,如果它低下是空的,就会被删除
//            System.out.println("已经被删除的文件夹:" + file);
            file.delete();
            return;// 文件夹被删除后,直接用return语句结束当次递归调用
        }

        // 如果是文件,就直接删除自己
//        System.out.println("已经被删除的文件:" + file);
        file.delete();

    }

    //复制到粘贴板
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("ICMOD管理器", text));
    }

    //验证某目录和子目录是否存在指定文件
    protected static int isExFile(String path, String filename) {
        int ret = 0;
        File MODPath = new File(path + File.separator + filename);

        if (MODPath.exists()) {
            ret = 3;
        } else if (new File(path + File.separator + Objects.requireNonNull(new File(path).list())[0] + File.separator + filename).exists()) {
            ret = 2;
        }
        return ret;
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     * */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static void copyFile(String oldPath$Name, String newPath$Name) {
        FinalValuable.installState = "正在复制：" + new File(oldPath$Name).getName();
        try {
            File oldFile = new File(oldPath$Name);
            File newFile = new File(newPath$Name);
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //安装MOD，需要在多线程运行
    public static boolean installMOD(String file) {
        boolean ret = false;
        try {
            Log.e("TAG", file);
            int modStatus = isExFile(FinalValuable.MODTestDir, "build.config");
            Log.e("TAG", modStatus + "");
            if (modStatus == 1) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), new File(FinalValuable.MODDir).toString());
                ret = true;
            } else if (modStatus == 2) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), new File(FinalValuable.MODDir).toString());
                Log.e("TAG", "copyFolder(" + new File(FinalValuable.MODTestDir).toString() + ", " + new File(FinalValuable.MODDir));
                ret = true;
            } else if (modStatus == 3) {
                String fileName = getFileNameNoEx(new File(file).getName());
                File modTest = new File(FinalValuable.MODTestDir);
                File mbPath = new File(FinalValuable.MODDir + File.separator + fileName);
                mbPath.mkdirs();
                String[] list = modTest.list();
                for (int i = 0; i < list.length; i++) {
                    File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                    if (file1.isDirectory()) {
                        copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    } else {
                        copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    }
                }
                ret = true;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean installMAP(String file, String toFile) {
        boolean ret = false;
        try {
            int modStatus = isExFile(FinalValuable.MODTestDir, "level.dat");
            if (modStatus == 1) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), toFile);
                ret = true;
            } else if (modStatus == 2) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), toFile);
                ret = true;
            } else if (modStatus == 3) {
                String fileName = getFileNameNoEx(new File(file).getName());
                File modTest = new File(FinalValuable.MODTestDir);
                File mbPath = new File(toFile + File.separator + fileName);
                mbPath.mkdirs();
                String[] list = modTest.list();
                for (int i = 0; i < list.length; i++) {
                    File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                    Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                    if (file1.isDirectory()) {
                        copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    } else {
                        copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    }
                }
                ret = true;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean installRes(String file, String toFile) {
        boolean ret = false;
        try {
            int modStatus = isExFile(FinalValuable.MODTestDir, "pack_manifest.json");
            if (modStatus == 0)
                modStatus = isExFile(FinalValuable.MODTestDir, "manifest.json");
            if (modStatus == 2) {
                copyFolder(new File(FinalValuable.MODTestDir).toString(), toFile);
                ret = true;
            } else if (modStatus == 3) {
                String fileName = getFileNameNoEx(new File(file).getName());
                File modTest = new File(FinalValuable.MODTestDir);
                File mbPath = new File(toFile + File.separator + fileName);
                mbPath.mkdirs();
                String[] list = modTest.list();
                for (int i = 0; i < list.length; i++) {
                    File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                    Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                    if (file1.isDirectory()) {
                        copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    } else {
                        copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                    }
                }
                ret = true;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static int autoInstall(String file) {
        int ret = 0;
        try {
            unZip(file, FinalValuable.MODTestDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Log.e("TAG", file);
            for (int i = 0; i < FinalValuable.statueList.size(); i++) {
                int modStatus = isExFile(FinalValuable.MODTestDir, FinalValuable.statueList.get(i));
                if (modStatus == 1 || modStatus == 2 || modStatus == 3) {
                    if (FinalValuable.statueList.get(i).equals("build.config")) {
                        if (installMOD(file)) {
                            ret = FinalValuable.MOD;
                        }

                    } else if (FinalValuable.statueList.get(i).equals("level.dat")) {
                        if (installMAP(file, FinalValuable.ICMAPDir)) {
                            ret = FinalValuable.ICMAP;
                        }
                    } else if (FinalValuable.statueList.get(i).equals("pack_manifest.json")) {
                        if (installRes(file, FinalValuable.ResDir)) {
                            ret = FinalValuable.ICRES;
                        }
                    } else if (FinalValuable.statueList.get(i).equals("manifest.json")) {
                        //两种情况，材质或者Hz分包
                        if (installPack(file, FinalValuable.HorizonPackPath)) {
                            FinalValuable.horizonPackList = Algorithm.getNativePackList(false);
                            ret = FinalValuable.HZPACK;
                        } else if (installRes(file, FinalValuable.ResDir)) {
                            ret = FinalValuable.ICRES;
                        }
                    }
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        File testDir = new File(FinalValuable.MODTestDir);
        deleteFile(testDir);
        testDir.mkdirs();

        return ret;
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    public static void renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return;
        }

        if (TextUtils.isEmpty(newPath)) {
            return;
        }

        File file = new File(oldPath);
        file.renameTo(new File(newPath));
    }

    private static boolean installPack(String file, String horizonPackPath) {
        boolean ret = false;
        try {
            int modStatus = isExFile(FinalValuable.MODTestDir, "manifest.json");
            if (modStatus == 2) {
                File manifestFile = new File(new File(FinalValuable.MODTestDir).listFiles()[0].toString() + File.separator + "manifest.json");
                File installInfoFile = new File(new File(FinalValuable.MODTestDir).listFiles()[0].toString() + File.separator + ".installation_info");
                File installStatus = new File(new File(FinalValuable.MODTestDir).listFiles()[0].toString() + File.separator + ".installation_complete");
                String manifestStr = readFile(manifestFile);
                JSONObject jsonObject = new JSONObject(manifestStr);
                if (!jsonObject.isNull("pack")) {
                    if (installStatus.exists()) {

                        String infoStr = readFile(installInfoFile);

                        JSONObject infoObject = new JSONObject(infoStr);

                        infoObject.put("timestamp", new Date().getTime() + "");
                        infoObject.put("customName", manifestFile.getParentFile().getName().replace("_", " "));
                        infoObject.put("internalId", getUUID32());
                        writeFile(installInfoFile.toString(), formatJson(infoObject.toString()));
                        renameFile(new File(FinalValuable.MODTestDir).listFiles()[0].toString(), FinalValuable.MODTestDir + File.separator + getNewICFolderName(jsonObject.getString("pack")));
                        copyFolder(new File(FinalValuable.MODTestDir).toString(), horizonPackPath);
                        ret = true;

                    } else {
                        //DO SOMETHING
                        File nowDir = new File(new File(FinalValuable.MODTestDir).listFiles()[0].toString());
                        File packZip = new File(nowDir.toString() + File.separator + "pack.zip");
                        File picZip = new File(nowDir.toString() + File.separator + "graphics.zip");
                        File changeLog = new File(nowDir.toString() + File.separator + "changelog-zh.html");
                        File installStart = new File(nowDir.toString() + File.separator + ".installation_started");
                        File installComplete = installStatus;
                        if (!installStart.exists()) installStart.createNewFile();
                        JSONObject infoObject = new JSONObject();
                        infoObject.put("uuid", getUUID32());
                        infoObject.put("timestamp", new Date().getTime() + "");
                        infoObject.put("customName", getFileNameNoEx(new File(file).getName()).replace("_", " "));
                        infoObject.put("internalId", getUUID32());
                        writeFile(installInfoFile.toString(), formatJson(infoObject.toString()));
                        if (!changeLog.exists())
                            changeLog = new File(nowDir.toString() + File.separator + "changelog.html");
                        JSONObject jsonObjectManifest = new JSONObject(readFile(manifestFile));
                        JSONArray folderArray = null, keepFolderArray = null;
                        if (!jsonObjectManifest.isNull("directories"))
                            folderArray = jsonObjectManifest.getJSONArray("directories");
                        if (!jsonObjectManifest.isNull("keepDirectories"))
                            keepFolderArray = jsonObjectManifest.getJSONArray("keepDirectories");
                        if (folderArray != null) {
                            for (int i = 0; i < folderArray.length(); i++) {
                                File folder = new File(nowDir.toString() + File.separator + folderArray.getString(i));
                                folder.mkdirs();
                            }
                        }
                        if (keepFolderArray != null) {
                            for (int i = 0; i < keepFolderArray.length(); i++) {
                                File folder = new File(nowDir.toString() + File.separator + keepFolderArray.getString(i));
                                folder.mkdirs();
                            }
                        }
                        if (packZip.exists()) {
                            if (picZip.exists()) {
                                renameFile(picZip.toString(), nowDir.toString() + File.separator + ".cached_graphics");
                            }
                            unZip(packZip.toString(), nowDir.toString());
                            deleteFile(packZip);
                        }
                        installComplete.createNewFile();
                        renameFile(nowDir.toString(), FinalValuable.MODTestDir + File.separator + getNewICFolderName(jsonObject.getString("pack")));
                        copyFolder(new File(FinalValuable.MODTestDir).toString(), horizonPackPath);
                        ret = true;
                    }
                } else return false;
            } else if (modStatus == 3) {
                File manifestFile = new File(FinalValuable.MODTestDir + File.separator + "manifest.json");
                File installInfoFile = new File(FinalValuable.MODTestDir + File.separator + ".installation_info");
                File installStatus = new File(FinalValuable.MODTestDir + File.separator + ".installation_complete");
                String manifestStr = readFile(manifestFile);
                JSONObject jsonObject = new JSONObject(manifestStr);
                if (!jsonObject.isNull("pack")) {
                    if (installStatus.exists()) {

                        String infoStr = readFile(installInfoFile);

                        JSONObject infoObject = new JSONObject(infoStr);

                        infoObject.put("timestamp", new Date().getTime() + "");
                        infoObject.put("customName", getFileNameNoEx(new File(file).getName()).replace("_", " "));
                        infoObject.put("internalId", getUUID32());
                        writeFile(installInfoFile.toString(), infoObject.toString());
                        File modTest = new File(FinalValuable.MODTestDir);
                        File mbPath = new File(horizonPackPath + File.separator + getNewICFolderName(jsonObject.getString("pack")));
                        mbPath.mkdirs();
                        String[] list = modTest.list();
                        for (int i = 0; i < list.length; i++) {
                            File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                            Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                            if (file1.isDirectory()) {
                                copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                            } else {
                                copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                            }
                        }
                        ret = true;

                    } else {
                        //DO SOMETHING
                        File nowDir = new File(FinalValuable.MODTestDir);
                        File packZip = new File(nowDir.toString() + File.separator + "pack.zip");
                        File picZip = new File(nowDir.toString() + File.separator + "graphics.zip");
                        File changeLog = new File(nowDir.toString() + File.separator + "changelog-zh.html");
                        File installStart = new File(nowDir.toString() + File.separator + ".installation_started");
                        File installComplete = installStatus;
                        if (!installStart.exists()) installStart.createNewFile();
                        JSONObject infoObject = new JSONObject();
                        infoObject.put("uuid", getUUID32());
                        infoObject.put("timestamp", new Date().getTime() + "");
                        infoObject.put("customName", getFileNameNoEx(new File(file).getName()).replace("_", " "));
                        infoObject.put("internalId", getUUID32());
                        writeFile(installInfoFile.toString(), formatJson(infoObject.toString()));
                        if (!changeLog.exists())
                            changeLog = new File(nowDir.toString() + File.separator + "changelog.html");
                        JSONObject jsonObjectManifest = new JSONObject(readFile(manifestFile));
                        JSONArray folderArray = null, keepFolderArray = null;
                        if (!jsonObjectManifest.isNull("directories"))
                            folderArray = jsonObjectManifest.getJSONArray("directories");
                        if (!jsonObjectManifest.isNull("keepDirectories"))
                            keepFolderArray = jsonObjectManifest.getJSONArray("keepDirectories");
                        if (folderArray != null) {
                            for (int i = 0; i < folderArray.length(); i++) {
                                File folder = new File(nowDir.toString() + File.separator + folderArray.getString(i));
                                folder.mkdirs();
                            }
                        }
                        if (keepFolderArray != null) {
                            for (int i = 0; i < keepFolderArray.length(); i++) {
                                File folder = new File(nowDir.toString() + File.separator + keepFolderArray.getString(i));
                                folder.mkdirs();
                            }
                        }
                        if (packZip.exists()) {
                            if (picZip.exists()) {
                                renameFile(picZip.toString(), nowDir.toString() + File.separator + ".cached_graphics");
                            }
                            unZip(packZip.toString(), nowDir.toString());
                            deleteFile(packZip);
                        } else {

                        }
                        installComplete.createNewFile();
                        File mbPath = new File(horizonPackPath + File.separator + getNewICFolderName(jsonObjectManifest.getString("pack")));
                        mbPath.mkdirs();
                        String[] list = nowDir.list();
                        for (int i = 0; i < list.length; i++) {
                            File file1 = new File(FinalValuable.MODTestDir + File.separator + list[i]);
                            Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                            if (file1.isDirectory()) {
                                copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                            } else {
                                copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                            }
                        }
                        ret = true;
                    }
                } else return false;
            } else {
                ret = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getNewICFolderName(String pack) {
        String packFolderFirst = pack.replace(" ", "_");
        File packFolderFirstFile = new File(FinalValuable.HorizonPackPath, packFolderFirst);
        if (packFolderFirstFile.exists()) {
            for (int i = 1; true; i++) {
                File packFolderSecFile = new File(packFolderFirstFile.toString() + "(_" + i + ")");
                if (!packFolderSecFile.exists()) return packFolderSecFile.getName();
            }
        } else {
            return packFolderFirst;
        }
    }

    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                FinalValuable.installState = "正在复制：" + new File(file).getName();
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getStringNoBlank(String str) {
        if (str != null && !"".equals(str)) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            String strNoBlank = m.replaceAll("");
            return strNoBlank;
        } else {
            return str;
        }
    }

    public static String getFileLastName(File file) {
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix;
    }

    public static void unZip(String zipfile, String dest) {
        FinalValuable.installState = "正在解压文件：" + new File(zipfile).getName();
        Log.e("TAG", "正在解压文件：" + new File(zipfile).getName() + "到：" + dest);
        try {
            ZipUtils.UnZipFolder(zipfile, dest);
        } catch (Exception e) {
            e.printStackTrace();
            try{
                File testDir = new File(FinalValuable.MODTestDir);
                deleteFile(testDir);
                testDir.mkdirs();
                ZipFile zipFile = new ZipFile(zipfile);
                zipFile.setCharset(Charset.forName(getEncoding(zipfile)));
                zipFile.extractAll(dest);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public static Bitmap getBitmap(String path) {
        FileInputStream fis;
        Bitmap ret = null;
        try {
            fis = new FileInputStream(path);
            ret = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
