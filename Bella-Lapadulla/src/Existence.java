public class Existence {
    private String secrecyLevel;

    public Existence(String secrecyLevel){
        this.secrecyLevel = secrecyLevel==null?"null":secrecyLevel;
    }
    public String getSecrecyLevel(){
        return secrecyLevel;
    }
    public Integer getIntSecrecyLevel(){
        return switch (secrecyLevel) {
            case "null" -> 0;
            case "secret" -> 1;
            case "superSecret" -> 2;
            case "specialImportance" -> 3;
            default -> -1;
        };
    }
    public void setSecrecyLevel(String secrecyLevel){
        this.secrecyLevel = secrecyLevel;
    }



}
