package icmod.wvt.com.icmod.others;

public class ResPack {
    private String name, imagePath, describe, uuid, packId, resPath, packVersion, moduleDes, moduleVersion, moduleUuid, moduleType;
    private Boolean enabled;
    ResPack(String name, String imagePath, String describe, String uuid, String packId, String resPath, String packVersion,
            String moduleDes, String moduleVersion, String moduleUuid, String moduleType, boolean enabled)
    {
        this.name = name;
        this.imagePath = imagePath;
        this.describe = describe;
        this.uuid = uuid;
        this.packId = packId;
        this.resPath = resPath;
        this.packVersion = packVersion;
        this.moduleDes = moduleDes;
        this.moduleVersion = moduleVersion;
        this.moduleUuid = moduleUuid;
        this.moduleType = moduleType;
        this.enabled = enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public String getModuleUuid() {
        return moduleUuid;
    }

    public String getModuleType() {
        return moduleType;
    }

    public String getModuleDes() {
        return moduleDes;
    }

    public Boolean getEnabled() {
        return enabled;
    }


    public String getPackVersion() {
        return packVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public String getResPath() {
        return resPath;
    }

    public String getPackId() {
        return packId;
    }

    public String getDescribe() {
        return describe;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }
}
