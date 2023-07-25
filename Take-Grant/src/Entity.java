import java.util.ArrayList;
import java.util.HashMap;

public class Entity {
    private String id;
    private HashMap<Entity, String> links;
    public Entity(String id){
        this.id = id;
        this.links = new HashMap<>();
    }
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    public HashMap<Entity, String> getLinks(){
        return links;
    }
    public void setLinks(HashMap<Entity, String> links){
        this.links = links;
    }
    public void addToLinks(Entity entity, String value){
        links.put(entity, value);
    }
    public String getLink(Entity entity){
        return links.get(entity);
    }
}
