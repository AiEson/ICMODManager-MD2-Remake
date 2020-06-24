package icmod.wvt.com.icmod.others;

public class HorizonPack {
    private String installInfoUuid = "未知", installTime = "未知", installName = "未知", installInternalId = "未知"/*内部ID*/;
    private String game = "未知", gameVersion = "未知", pack = "未知", packVersion = "未知", developer = "未知",
            description = "未知", directories = "未知", keepDirectories = "未知", activity = "未知", environmentClass = "未知", folderName = "未知", fileSize = "请下拉刷新查看";
    private int packVersionCode = -1;

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public void setDirectories(String directories) {
        this.directories = directories;
    }

    public void setEnvironmentClass(String environmentClass) {
        this.environmentClass = environmentClass;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public void setInstallInfoUuid(String installInfoUuid) {
        this.installInfoUuid = installInfoUuid;
    }

    public void setInstallInternalId(String installInternalId) {
        this.installInternalId = installInternalId;
    }

    public void setInstallName(String installName) {
        this.installName = installName;
    }

    public void setInstallTime(String installTime) {
        this.installTime = installTime;
    }

    public void setKeepDirectories(String keepDirectories) {
        this.keepDirectories = keepDirectories;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public void setPackVersion(String packVersion) {
        this.packVersion = packVersion;
    }

    public void setPackVersionCode(int packVersionCode) {
        this.packVersionCode = packVersionCode;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public int getPackVersionCode() {
        return packVersionCode;
    }

    public String getActivity() {
        return activity;
    }

    public String getDescription() {
        return description;
    }

    public String getDeveloper() {
        return developer;
    }

    public String getDirectories() {
        return directories;
    }

    public String getEnvironmentClass() {
        return environmentClass;
    }

    public String getGame() {
        return game;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getInstallInfoUuid() {
        return installInfoUuid;
    }

    public String getInstallInternalId() {
        return installInternalId;
    }

    public String getInstallName() {
        return installName;
    }

    public String getInstallTime() {
        return installTime;
    }

    public String getKeepDirectories() {
        return keepDirectories;
    }

    public String getPack() {
        return pack;
    }

    public String getPackVersion() {
        return packVersion;
    }
}
