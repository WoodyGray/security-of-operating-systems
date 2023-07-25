public class Document extends Existence {
    private String index;
    private String material;
    public Document( String index, String secrecyLevel, String material) {
        super(secrecyLevel);
        this.material = material;
        this.index = index;
    }
    public void setMaterial(String material){
        this.material = material;
    }
    public void appendMaterial(String material){this.material = this.material + material;}
    public String getMaterial(){
        return material;
    }
}
