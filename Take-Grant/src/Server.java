

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {
    private static final int PORT = 8000;
    private static boolean runUsl = true;

    private static List<String> rules = Arrays.asList("t", "g", "a");
    private static List<ClientThread> clients = new ArrayList<>();
    private static HashMap<String, Document> documents;
    private static HashMap<String, Subject> subjects;
    private static ArrayList<Entity> accessGraph;


    public static class ClientThread extends Thread{
        private BufferedReader in;
        private BufferedWriter out;
        private Socket connection;
        private String login;
        private static void graphBackUp() throws IOException {
            BufferedWriter writer = new BufferedWriter(new FileWriter("accessGraph.csv"));
            StringJoiner line = new StringJoiner(";");
            for (String d: documents.keySet()
                 ) {
                line.add(d);
            }
            writer.write(line + "\n");
            writer.flush();
            line = new StringJoiner(";");
            for (String s: subjects.keySet()
                 ) {
                line.add(s);
            }
            writer.write(line + "\n");
            writer.flush();
            for (Entity e: accessGraph
                 ) {
                if (e != null) {
                    line = new StringJoiner(";");
                    line.add(e.getId());
                    for (Entity link : e.getLinks().keySet()
                    ) {
                        line.add(link.getId() + ":" + e.getLink(link));
                    }
                    writer.write(line + "\n");
                    writer.flush();
                }
            }
        }

        private void commandCreate(String mess) throws IOException {
            mess = mess.substring(0, mess.length()-1);
            String[] arguments = mess.split(",");
            if (arguments.length == 4){
                if (rules.contains(arguments[0])){
                    if (!documents.containsKey(arguments[2]) && !subjects.containsKey(arguments[2])) {
                        if (documents.containsKey(arguments[1])){
                            if (arguments[3].equals("s")) {
                                Subject subject = new Subject(arguments[2]);
                                subjects.put(arguments[2], subject);
                                documents.get(arguments[1]).addToLinks(subject, arguments[0]);
                                out.write("creating such subject complete\n");
                            }else if (arguments[3].equals("d")){
                                Document document = new Document(arguments[2]);
                                documents.put(arguments[2], document);
                                documents.get(arguments[1]).addToLinks(new Document(arguments[2]), arguments[0]);
                                out.write("creating such document complete\n");
                            }else {
                                out.write("wrong type of entity\n");
                            }
                        }else if (subjects.containsKey(arguments[1])) {
                            if (arguments[3].equals("s")) {
                                Subject subject = new Subject(arguments[2]);
                                subjects.put(arguments[2], subject);
                                subjects.get(arguments[1]).addToLinks(subject, arguments[0]);
                                out.write("creating such subject complete\n");
                            }else if (arguments[3].equals("d")){
                                Document document = new Document(arguments[2]);
                                documents.put(arguments[2], document);
                                subjects.get(arguments[1]).addToLinks(new Document(arguments[2]), arguments[0]);
                                out.write("creating such document complete\n");
                            }else {
                                out.write("wrong type of entity\n");
                            }
                        }else {
                            out.write("such entity does not exist\n");
                        }
                    } else {
                        out.write("such entity exist\n");
                    }
                }else {
                    out.write("such rule does not exist\n");
                }
            }else {
                out.write("wrong syntax of command\n");
            }
            graphBackUp();
        }
        private void commandDelete(String mess) throws IOException {
            mess = mess.substring(0, mess.length()-1);
            String[] arguments = mess.split(",");
            if (arguments.length == 2){
                if (documents.containsKey(arguments[0])){
                    if (documents.containsKey(arguments[1])){
                        Entity d = documents.get(arguments[1]);
                        if (documents.get(arguments[0]).getLink(d).equals("a")){
                            removeEntity(arguments[1], d);
                            out.write("remove such document complete\n");
                        }else {
                            out.write("have no privileges on this move\n");
                        }
                    }else if (subjects.containsKey(arguments[1])){
                        Entity s = subjects.get(arguments[1]);
                        if (documents.get(arguments[0]).getLink(s).equals("a")){
                            removeEntity(arguments[1], s);
                            out.write("remove such subject complete\n");
                        }else {
                            out.write("have no privileges on this move\n");
                        }
                    }else {
                        out.write("no such entity for remove\n");
                    }
                }else if (subjects.containsKey(arguments[0])){
                    if (documents.containsKey(arguments[1])){
                        Entity d = documents.get(arguments[1]);
                        if (subjects.get(arguments[0]).getLink(d).equals("a")){
                            removeEntity(arguments[1], d);
                            out.write("remove such document complete\n");
                        }else {
                            out.write("have no privileges on this move\n");
                        }
                    }else if (subjects.containsKey(arguments[1])){
                        Entity s = subjects.get(arguments[1]);
                        if (subjects.get(arguments[0]).getLink(s).equals("a")){
                            removeEntity(arguments[1], s);
                            out.write("remove such subject complete\n");
                        }else {
                            out.write("have no privileges on this move\n");
                        }
                    }else {
                        out.write("have no such entity for remove\n");
                    }
                }else {
                    out.write("have no such entity\n");
                }
            }else {
                out.write("wrong syntax of command\n");
            }
            graphBackUp();

        }
        private void commandTake(String mess) throws IOException {
            mess = mess.substring(0, mess.length()-1);
            String[] arguments = mess.split(",");
            if (rules.contains(arguments[0])){
                if (documents.containsKey(arguments[1]) || subjects.containsKey(arguments[1])){
                    if (documents.containsKey(arguments[2])||subjects.containsKey(arguments[2])){
                        if (documents.containsKey(arguments[3])||subjects.containsKey(arguments[3])){
                            if (findPath(arguments[1],arguments[3],arguments[2], "t", arguments[0])){
                                out.write("yes, this entity can do it\n");
                            }else {
                                out.write("no, this entity cant do it\n");
                            }
                        }else {
                            out.write("fourth argument is wrong\n");
                        }
                    }else {
                        out.write("third argument is wrong\n");
                    }
                }else {
                    out.write("second argument is wrong\n");
                }
            }else {
                out.write("first argument is wrong\n");
            }
            graphBackUp();
        }
        private void commandGrand(String mess) throws IOException {
            mess = mess.substring(0, mess.length()-1);
            String[] arguments = mess.split(",");
            if (rules.contains(arguments[0])){
                if (documents.containsKey(arguments[1]) || subjects.containsKey(arguments[1])){
                    Entity fromWhom = documents.containsKey(arguments[1])?documents.get(arguments[1]):subjects.get(arguments[1]);
                    if (documents.containsKey(arguments[2]) || subjects.containsKey(arguments[2])){
                        Entity toWhom = documents.containsKey(arguments[2])?documents.get(arguments[2]):subjects.get(arguments[2]);
                        if (documents.containsKey(arguments[3]) || subjects.containsKey(arguments[3])){
                            Entity onWhat = documents.containsKey(arguments[3])?documents.get(arguments[3]):subjects.get(arguments[3]);
                            //out.write(findVer(fromWhom, onWhat, arguments[0]) +" ");
                            if ((fromWhom.getLink(toWhom).equals("g") || fromWhom.getLink(toWhom).equals("a"))
                                && findVer(fromWhom, onWhat, arguments[0])){
                                out.write("yes, this entity can do it\n");
                            }else {
                                out.write("no, this entity cant do it\n");
                            }
                        }else {
                            out.write("fourth argument is wrong\n");
                        }
                    }else {
                        out.write("third argument is wrong\n");
                    }
                }else {
                    out.write("second argument is wrong\n");
                }
            }else {
                out.write("first argument is wrong\n");
            }
            graphBackUp();
        }
        private void commandProcessing(String message) throws IOException {
            String[] components = message.split("\\(");
            //out.write(components[0]+" ");
            if (components.length ==2) {
                switch (components[0]) {
                    case "/cr":
                        commandCreate(components[1]);
                        break;
                    case "/tk":
                        commandTake(components[1]);
                        break;
                    case "/gr":
                        commandGrand(components[1]);
                        break;
                    case "/rm":
                        commandDelete(components[1]);
                        break;
                    default:
                        out.write("no such command1\n");
                        //out.flush();
                }
            }else {
                out.write("no such command\n");
                //out.flush();
            }
        }
        public ClientThread(Socket connection) throws IOException {
            this.connection = connection;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            start();
        }

        @Override
        public void run() {
            try {

                out.write("please write u login to authorized:\n");
                out.flush();
                String message = in.readLine();
                if (!message.equals("/cancel chat")) {
                    login = message;


                    while (!message.equals("/cancel chat")) {
                        out.write("please write u command:\n");
                        out.flush();
                        message = in.readLine();
                        //System.out.println(message);
                        if (!message.equals("/cancel chat")) {
                            commandProcessing(message);
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("error1");
                throw new RuntimeException(e);
            }finally {
                try {
                    clients.remove(this);
                    connection.close();
                    in.close();
                    out.close();

                }catch (Exception e){

                }
            }
        }
    }
    private static void removeEntity(String id, Entity entity){
        if (documents.containsKey(id)){
            documents.remove(id);
        }else {
            subjects.remove(id);
        }
        for (Entity e: accessGraph
             ) {
            e.getLinks().remove(entity);
        }
        accessGraph.remove(entity);
    }
    private static void unpackingGraph(String file) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader("accessGraph.csv"));
        String[] line = fileReader.readLine().split(";");
        documents = new HashMap<>();
        for (String doc: line
             ) {
            documents.put(doc, new Document(doc));
        }
        line = fileReader.readLine().split(";");
        subjects = new HashMap<>();
        for (String sub: line
             ) {
            subjects.put(sub, new Subject(sub));
        }
        accessGraph = new ArrayList<>();
        Entity entity;
        String[] ver;
        while (fileReader.ready()){
            line = fileReader.readLine().split(";");
            accessGraph.add(documents.containsKey(line[0])?documents.get(line[0]):subjects.get(line[0]));

            for (int i = 1; i < line.length; i++) {
                ver = line[i].split(":");
                if (documents.containsKey(ver[0])){
                    entity = documents.get(ver[0]);
                }else {
                    entity = subjects.get(ver[0]);
                }
                accessGraph.get(accessGraph.size()-1).addToLinks(entity, ver[1]);
            }
        }
    }
    private static boolean findPath(String first, String last, String penultimate, String fRule, String sRule){
        boolean runUsl = true;
        Entity firstVer = documents.containsKey(first)?documents.get(first):subjects.get(first);
        Entity penultimateVer = documents.containsKey(penultimate)?documents.get(penultimate):subjects.get(penultimate);
        Entity lastVer = documents.containsKey(last)?documents.get(last):subjects.get(last);
        if (penultimateVer.getLink(lastVer) != null && penultimateVer.getLink(lastVer).equals(sRule)) {
            if (firstVer.getLinks().size() == 0) {
                return false;
            } else {
                Entity ver = firstVer;
                while (ver.getLinks().size() == 1){
                    if (ver.getLinks().entrySet().iterator().next().getValue().equals(fRule)){
                        if (ver.getLinks().entrySet().iterator().next().getKey() == penultimateVer) {
                            return true;
                        }else {
                            ver = ver.getLinks().entrySet().iterator().next().getKey();
                        }
                    }else {
                        return false;
                    }
                }
                return findVer(ver, penultimateVer, fRule);
            }
        }else {
            return false;
        }
    }
    private static boolean findVer(Entity nVer,Entity eVer, String rule){
        for (Entity ver: nVer.getLinks().keySet()
             ) {
            if (ver.getId().equals(eVer.getId())){
                if (nVer.getLink(ver).equals(rule) || nVer.getLink(ver).equals("a")){
                    return true;
                }else {
                    return false;
                }
            }else {
                if (ver.getLinks().size() != 0){
                    if(findVer(ver, eVer, rule)){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static void main(String[] args) throws IOException {
        unpackingGraph("accessGraph.csv");

        //Breaker breaker = new Breaker(System.in);
        ServerSocket server = new ServerSocket(PORT);
        clients = new ArrayList<>();
        System.out.println("Server running...");
        while (runUsl){
            try{
                Socket clientConn = server.accept();
                clients.add(new ClientThread(clientConn));
                System.out.println("new client connect");
            }catch (Exception e){

            }

        }
    }

}