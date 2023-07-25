public class Subject extends Existence {
    private String login;
    private String tempSecrecyLevel;
    public Subject( String login, String secrecyLevel, String tempSecrecyLevel) {
        super(secrecyLevel);
        this.tempSecrecyLevel = tempSecrecyLevel ==null?"null":tempSecrecyLevel;
        this.login = login;
    }
    public String getTempSecrecyLevel(){return tempSecrecyLevel;}
    public String getLogin(){return login;}
    public Integer getIntTempSecrecyLevel(){
        return switch (tempSecrecyLevel) {
            case "null" -> 0;
            case "secret" -> 1;
            case "superSecret" -> 2;
            case "specialImportance" -> 3;
            default -> -1;
        };
    }
    public void setTempSecrecyLevel(String tempSecrecyLevel){this.tempSecrecyLevel = tempSecrecyLevel;}
}
