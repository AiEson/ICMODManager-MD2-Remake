package icmod.wvt.com.icmod.others;

public class MOD {
    private String name, imagePath, version, describe, author, modPath;
    private Boolean enabled, checked = false;
    private int type;
    MOD(String modPath, String name, String imagePath, String version, String describe, String author, boolean enabled, int type) {
        this.name = name;
        this.imagePath = imagePath;
        this.version = version;
        this.describe = describe;
        this.author = author;
        this.modPath = modPath;
        this.enabled = enabled;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public String getImagePath() {
        return imagePath;
    }
    public String getVersion() {
        return version;
    }
    public String getDescribe() {
        return describe;
    }
    public String getAuthor() {
        return author;
    }
    public String getModPath() {
        return modPath;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public int getType() {
        return type;
    }

    public Boolean isChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public boolean changeMOD() {
        boolean ret = false;
        if (Algorithm.changeMOD(this.modPath, !enabled)) {
            this.enabled = !enabled;
            ret = true;
        }
        return ret;
    }
}
