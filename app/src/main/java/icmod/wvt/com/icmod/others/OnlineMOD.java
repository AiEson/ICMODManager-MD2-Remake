package icmod.wvt.com.icmod.others;


public class OnlineMOD {
    private String name, describe, modUrl, imageUrl;
    private int type;
    public OnlineMOD(String modUrl,String imageUrl, String name, String describe, int type) {
        this.name = name;
        this.describe = describe;
        this.type = type;
        this.modUrl = modUrl;
        this.imageUrl = imageUrl;
    }

    public String getModUrl() {
        return modUrl;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getName() {
        return name;
    }
    public String getDescribe() {
        return describe;
    }
    public int getType() {
        return type;
    }
}
