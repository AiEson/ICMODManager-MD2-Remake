package icmod.wvt.com.icmod.others;


import android.util.Log;

import net.lingala.zip4j.ZipFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static icmod.wvt.com.icmod.others.Algorithm.copyFile;
import static icmod.wvt.com.icmod.others.Algorithm.copyFolder;
import static icmod.wvt.com.icmod.others.Algorithm.formatJson;
import static icmod.wvt.com.icmod.others.Algorithm.getFileNameNoEx;
import static icmod.wvt.com.icmod.others.Algorithm.getNewICFolderName;
import static icmod.wvt.com.icmod.others.Algorithm.getUUID32;
import static icmod.wvt.com.icmod.others.Algorithm.readFile;
import static icmod.wvt.com.icmod.others.Algorithm.renameFile;
import static icmod.wvt.com.icmod.others.Algorithm.writeFile;

//超级安装系统
public class SuperInstallSystem {
    String[] typeName = {"None", "模组", "地图", "材质", "Horizon包"};
    String[] fileName = {" ", "build.config", "level.dat", "manifest.json||pack_manifest.json", "manifest.json"};
    Integer[] typeInt = { -1,   123  ,    124 , 125,     126 };


    public String intall(String filePath) {
        Algorithm.deleteFile(new File(FinalValuable.MODTestDir));
        new File(FinalValuable.MODTestDir).mkdir();
        String folderPath = FinalValuable.MODTestDir + File.separator + getFileNameNoEx(new File(filePath).getName());
        Algorithm.unZip(filePath, folderPath);
        //然后从测试文件夹开始遍历
        runInstallFolder(new File(FinalValuable.MODTestDir));
        StringBuilder ret = new StringBuilder();
        if (FinalValuable.mods.size() != 0) {
            ret.append("MOD：\n");
            for (int i = 0; i < FinalValuable.mods.size(); i++) {
                ret.append(FinalValuable.mods.get(i)).append("\n");
            }
            ret.append("\n");
        }
        if (FinalValuable.maps.size() != 0) {
            ret.append("IC地图：\n");
            for (int i = 0; i < FinalValuable.maps.size(); i++) {
                ret.append(FinalValuable.maps.get(i)).append("\n");
            }
            ret.append("\n");
        }
        if (FinalValuable.resPacks.size() != 0) {
            ret.append("材质包：\n");
            for (int i = 0; i < FinalValuable.resPacks.size(); i++) {
                ret.append(FinalValuable.resPacks.get(i)).append("\n");
            }
            ret.append("\n");
        }
        if (FinalValuable.hzPacks.size() != 0) {
            ret.append("Horizon分包：\n");
            for (int i = 0; i < FinalValuable.hzPacks.size(); i++) {
                ret.append(FinalValuable.hzPacks.get(i)).append("\n");
            }
            ret.append("\n");
        }
        Algorithm.deleteFile(new File(FinalValuable.MODTestDir));
        new File(FinalValuable.MODTestDir).mkdir();
        return ret.toString();
    }

    private void runInstallFolder(File folderName) {
        int type = getFolderType(folderName.toString());
        if (type == -1) {
            File[] files = folderName.listFiles();
            //既然不是可安装的文件夹，先解压了里面的ZIP再说
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (isZipFile(file.toString())) {
                    Algorithm.unZip(file.toString(), folderName.toString() + File.separator + getFileNameNoEx(file.getName()));
                }
                if (file.getName().equals(".staticids")) {
                    Algorithm.copyFile(fileName.toString(), FinalValuable.MODDir);
                }
            }
            //更新列表
            files = folderName.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    runInstallFolder(file);
                }
            }
        } else {
            installFolder(folderName.toString(), type);
        }
    }

    private boolean isZipFile(String filePath) {
        ZipFile zipFile = new ZipFile(filePath);
        return zipFile.isValidZipFile();
    }

    private void installFolder(String folderPath, int type) {
        String folderName = new File(folderPath).getName();
        switch (type) {
            case -1:
                System.out.println("-1你装你马呢？？？");
                break;
            case 123:
                copyFolder(folderPath, FinalValuable.MODDir + File.separator + folderName);
                FinalValuable.mods.add(folderName);
                break;
            case 124:
                copyFolder(folderPath, FinalValuable.ICMAPDir + File.separator + folderName);
                FinalValuable.maps.add(folderName);
                break;
            case 125:
                copyFolder(folderPath, FinalValuable.ResDir + File.separator + folderName);
                FinalValuable.resPacks.add(folderName);
                break;
            case 126:
            try {
                File manifestFile = new File(folderPath + File.separator + "manifest.json");
                File installInfoFile = new File(folderPath + File.separator + ".installation_info");
                File installStatus = new File(folderPath + File.separator + ".installation_complete");
                String manifestStr = readFile(manifestFile);
                JSONObject jsonObject = new JSONObject(manifestStr);
                if (!jsonObject.isNull("pack")) {
                    if (installStatus.exists()) {

                        String infoStr = readFile(installInfoFile);

                        JSONObject infoObject = new JSONObject(infoStr);

                        infoObject.put("timestamp", new Date().getTime() + "");
                        infoObject.put("customName", getFileNameNoEx(folderName.replace("_", " ")));
                        infoObject.put("internalId", getUUID32());
                        writeFile(installInfoFile.toString(), infoObject.toString());
                        File modTest = new File(folderPath);
                        File mbPath = new File(FinalValuable.HorizonPackPath + File.separator + getNewICFolderName(jsonObject.getString("pack")));
                        mbPath.mkdirs();
                            File file1 = new File(folderPath);
                            Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                            if (file1.isDirectory()) {
                                copyFolder(file1.toString(), mbPath.toString() );
                            } else {
                                copyFile(file1.toString(), mbPath.toString() );
                            }

                    } else {
                        //DO SOMETHING
                        File nowDir = new File(folderPath);
                        File packZip = new File(nowDir.toString() + File.separator + "pack.zip");
                        File picZip = new File(nowDir.toString() + File.separator + "graphics.zip");
                        File changeLog = new File(nowDir.toString() + File.separator + "changelog-zh.html");
                        File installStart = new File(nowDir.toString() + File.separator + ".installation_started");
                        File installComplete = installStatus;
                        if (!installStart.exists()) installStart.createNewFile();
                        JSONObject infoObject = new JSONObject();
                        infoObject.put("uuid", getUUID32());
                        infoObject.put("timestamp", new Date().getTime() + "");
                        infoObject.put("customName", getFileNameNoEx(new File(folderName).getName()).replace("_", " "));
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
                            Algorithm.unZip(packZip.toString(), nowDir.toString());
                            Algorithm.deleteFile(packZip);
                        } else {

                        }
                        installComplete.createNewFile();
                        File mbPath = new File(FinalValuable.HorizonPackPath + File.separator + getNewICFolderName(jsonObjectManifest.getString("pack")));
                        mbPath.mkdirs();
                        String[] list = nowDir.list();
                        for (int i = 0; i < list.length; i++) {
                            File file1 = new File(folderPath + File.separator + list[i]);
                            Log.e("TAG", file1.toString() + "  目标：   " + mbPath.toString());
                            if (file1.isDirectory()) {
                                copyFolder(file1.toString(), mbPath.toString() + File.separator + list[i]);
                            } else {
                                copyFile(file1.toString(), mbPath.toString() + File.separator + list[i]);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            FinalValuable.hzPacks.add(folderName);
            FinalValuable.horizonPackList = Algorithm.getNativePackList(false);
            break;
        }
    }

    public Integer getFolderType(String folderPath) {
        int ret = -1;
        loop1: for (int i = 1; i < typeInt.length; i++){
            String fileNames = this.fileName[i];
            String[] fileNameArr = fileNames.split("\\|\\|");
            if (fileNameArr != null) {
                //多文件判断
                for (int j = 0; j < fileNameArr.length; j++) {
                    String realFileName = fileNameArr[j];
                    File file = new File(folderPath + File.separator + realFileName);
                    if (file.exists()) {
                        if (realFileName.equals("manifest.json")) {
                            //判断是Hz包还是材质
                            ret =  getManifestFileType(file);
                            break loop1;
                        }
                        ret =  typeInt[i];
                    }
                }
            } else {
                //单文件判断
                File file = new File(folderPath + File.separator + fileNames);
                if (file.exists()) {
                    if (fileNames.equals("manifest.json")) {
                        //判断是Hz包还是材质
                        ret =  getManifestFileType(file);
                        break;
                    }
                    ret =  typeInt[i];
                }
            }
        }
        return ret;
    }

    private Integer getManifestFileType(File maniFile) {
        try {
            int ret = -1;
            String fileNr = readFile(maniFile);
            JSONObject fileJson = new JSONObject(fileNr);
            if (!fileJson.isNull("modules")) {
                JSONArray modules = fileJson.getJSONArray("modules");
                for (int i = 0; i < modules.length(); i++) {
                    JSONObject modulesObj = modules.getJSONObject(i);
                    if (modulesObj.getString("type").equals("resources")) {
                        ret = 125;
                    }
                }
            } else if (!fileJson.isNull("pack") && !fileJson.isNull("gameVersion")) {
                ret = 126;
            }
            return ret;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    //设置安装状态显示
    private void changeState(String str) {
        FinalValuable.installState = str;
    }

}
