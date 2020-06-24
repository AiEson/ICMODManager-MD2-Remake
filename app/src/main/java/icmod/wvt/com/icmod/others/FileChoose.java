package icmod.wvt.com.icmod.others;

public class FileChoose {
    String path = "";
    String name = "";
    int type;
    boolean isChecked = false;

    public FileChoose(String path, String name, int type) {
        this.path = path;
        this.name = name;
        this.type = type;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean getChecked() {
        return isChecked;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
