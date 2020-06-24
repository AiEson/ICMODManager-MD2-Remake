package icmod.wvt.com.icmod.others;

public class MAP {
    private String name, imagePath, mapPath;
    private int type;
    boolean checked = false;
    MAP(String name, String mapPath, String imagePath, int type)
    {
        this.name = name;
        this.mapPath = mapPath;
        this.imagePath = imagePath;
        this.type = type;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getMapPath() {
        return mapPath;
    }
    public String getImagePath() {
        return imagePath;
    }
    public String getName() {
        return name;
    }
    public int getType() {
        return type;
    }
}
