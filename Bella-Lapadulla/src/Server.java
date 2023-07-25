

import jdk.dynalink.linker.LinkerServices;
import org.ini4j.Ini;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server {
    private static final int PORT = 8000;
    private static final boolean runUsl = true;
    private static List<ClientThread> clients = new ArrayList<>();
    private static HashMap<String, Subject> subjects =new HashMap<>();
    private static HashMap<String, Document> objects = new HashMap<>();
    private static HashMap<Subject, HashMap<Document, String>> accessMatrix = new HashMap<>();


    public static class ClientThread extends Thread{
        private BufferedReader in;
        private BufferedWriter out;
        private Socket connection;
        private String login;

        public Integer getIntSecrecyLevel(String secrecyLevel){
            return switch (secrecyLevel) {
                case "null" -> 0;
                case "секретно" -> 1;
                case "супер секретно" -> 2;
                case "особой важности" -> 3;
                default -> -1;
            };
        }
        private void commandCreate(String fileName, Document object) throws IOException {
            if (objects.containsKey(fileName)){
                out.write("such a file exist, try another name\n");
                //out.flush();
            }else {
                if (object.getIntSecrecyLevel() <= subjects.get(login).getIntSecrecyLevel()){
                    objects.put(fileName, object);
                    for (Subject s:accessMatrix.keySet()
                         ) {
                        accessMatrix.get(s).put(object, "null");
                    }
                    out.write("file creation completed\n");
                    objectsBackUp(objects);
                    matrixBackUp(accessMatrix);
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
                //out.flush();
            }
        }
        private void commandDelete(String fileName) throws IOException {
            if (!objects.containsKey(fileName)){
                out.write("such file does not exist\n");
                //out.flush();
            }else {
                if (subjects.get(login).getIntSecrecyLevel() >= objects.get(fileName).getIntSecrecyLevel() + 1
                            || subjects.get(login).getIntSecrecyLevel() == 3){
                    for (Subject s:accessMatrix.keySet()
                         ) {
                        accessMatrix.get(s).remove(objects.get(fileName));
                    }
                    objects.remove(fileName);
                    objectsBackUp(objects);
                    matrixBackUp(accessMatrix);
                    out.write("file deletion completed\n");
                    //out.flush();
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
        }
        private void commandWrite(String fileName, String material) throws IOException {
            if (!objects.containsKey(fileName)){
                out.write("such file does not exist\n");
                //out.flush();
            }else {
                if (subjects.get(login).getIntTempSecrecyLevel() <= objects.get(fileName).getIntSecrecyLevel()){
                    if (subjects.get(login).getIntTempSecrecyLevel() == objects.get(fileName).getIntSecrecyLevel()){
                        objects.get(fileName).setMaterial(material);
                    }else {
                        objects.get(fileName).appendMaterial(material);
                    }
                    accessMatrix.get(subjects.get(login)).put(objects.get(fileName), "w");
                    subjects.get(login).setTempSecrecyLevel(objects.get(fileName).getSecrecyLevel());
                    out.write("file writing completed\n");
                    //out.flush();
                }else if (subjects.get(login).getIntSecrecyLevel() > objects.get(fileName).getIntSecrecyLevel()) {
                    subjects.get(login).setTempSecrecyLevel(objects.get(fileName).getSecrecyLevel());
                    objects.get(fileName).setMaterial(material);
                    accessMatrix.get(subjects.get(login)).put(objects.get(fileName), "w");
                    out.write("file writing completed\n");
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
            subjectsBackUp(subjects);
            objectsBackUp(objects);
            matrixBackUp(accessMatrix);
        }
        private void commandRead(String fileName) throws IOException {
            if (!objects.containsKey(fileName)){
                out.write("such file does not exist\n");
                //out.flush();
            }else {
                if (subjects.get(login).getIntTempSecrecyLevel() >= objects.get(fileName).getIntSecrecyLevel() ||
                        subjects.get(login).getIntSecrecyLevel() >= objects.get(fileName).getIntSecrecyLevel()) {
                    accessMatrix.get(subjects.get(login)).put(objects.get(fileName), "r");
                    subjects.get(login).setTempSecrecyLevel(objects.get(fileName).getSecrecyLevel());
                    out.write(String.format("file material: %s\n", objects.get(fileName).getMaterial()));
                    //out.flush();
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
            subjectsBackUp(subjects);
            objectsBackUp(objects);
            matrixBackUp(accessMatrix);
        }
        private void commandGSecrecyLevel(String subjectLogin, String secrecyLevel) throws IOException {
            if (!subjects.containsKey(subjectLogin)){
                out.write("such subject does not exist\n");
                //out.flush();
            }else {
                if (subjects.get(login).getIntSecrecyLevel() >= getIntSecrecyLevel(secrecyLevel)){
                        subjects.get(subjectLogin).setSecrecyLevel(secrecyLevel);
                        out.write("secrecy level has been transferred\n");
                }else{
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
            subjectsBackUp(subjects);
            objectsBackUp(objects);
            matrixBackUp(accessMatrix);
        }
        private void commandSSecrecyLevel(String objectLogin, String secrecyLevel) throws IOException {
            if (!objects.containsKey(objectLogin)){
                out.write("such object does not exist\n");
                //out.flush();
            }else {
                if (subjects.get(login).getIntSecrecyLevel() >= getIntSecrecyLevel(secrecyLevel)){
                    objects.get(objectLogin).setSecrecyLevel(secrecyLevel);

                    out.write("secrecy level has been set\n");
                }else{
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
            subjectsBackUp(subjects);
            objectsBackUp(objects);
            matrixBackUp(accessMatrix);
        }
        private void commandProcessing(String message) throws IOException {
            String[] components = message.split(" ");
            if (components.length >= 2){
                switch (components[0]){
                    case "/cr":
                        if (components.length == 6){
                            commandCreate(components[1]
                                    ,new Document(components[1]
                                            ,components[2]
                                            ,components[2]));
                        }else {
                            out.write("no such command\n");
                        }
                        break;
                    case "/dl":
                        if (components.length == 2){
                            commandDelete(components[1]);
                        }else {
                            out.write("no such command\n");
                        }
                        break;
                    case "/wr":
                        if (components.length == 3){
                            commandWrite(components[1], components[2].replaceAll("\"", ""));
                        }else {
                            out.write("no such command\n");
                        }
                        break;
                    case "/rd":
                        if (components.length == 2){
                            commandRead(components[1]);
                        }else {
                            out.write("no such command\n");
                        }
                        break;
                    case "/gv":
                        if (components.length == 3 && components[2].split(",").length == 2){
                            String subjectLogin = components[2].split(",")[0].replaceAll("\"", "");
                            String attributeName = components[2].split(",")[1].replaceAll("\"", "");
                            if ("sl".equals(components[1])) {
                                commandGSecrecyLevel(subjectLogin, attributeName);
                            } else {
                                out.write("no such command\n");
                            }
                        }else {
                            out.write("no such command\n");
                        }
                        break;
                    case "/set":
                        if (components.length == 3 && components[2].split(",").length == 2) {
                            String objectLogin = components[2].split(",")[0].replaceAll("\"", "");
                            String attributeName = components[2].split(",")[1].replaceAll("\"", "");
                            if ("sl".equals(components[1])) {
                                commandSSecrecyLevel(objectLogin, attributeName);
                            } else {
                                out.write("no such command\n");
                            }
                        }else {
                            out.write("no such command\n");
                        }
                }
            }else {
                out.write("no such command\n");
            }

        }
        private void objectsBackUp(HashMap<String, Document> objects) throws IOException {
            Ini objectsFile = new Ini(new File("objects.ini"));
            for (java.lang.Object object:objects.keySet()
                 ) {
                objectsFile.put(String.valueOf(object), "secrecyLevel", objects.get(object).getSecrecyLevel());
                objectsFile.put(String.valueOf(object), "material", objects.get(object).getMaterial());
            }
            objectsFile.store();
        }
        private void subjectsBackUp(HashMap<String, Subject> subjects) throws IOException {
            Ini subjectsFile = new Ini(new File("subjects.ini"));
            for (java.lang.Object subject:subjects.keySet()
            ) {
                subjectsFile.put(String.valueOf(subject), "secrecyLevel", subjects.get(subject).getSecrecyLevel());
                subjectsFile.put(String.valueOf(subject), "tempSecrecyLevel", subjects.get(subject).getTempSecrecyLevel());
            }
            subjectsFile.store();
        }
        private void matrixBackUp(HashMap<Subject, HashMap<Document, String>> accessMatrix) throws IOException{
            BufferedWriter writer = new BufferedWriter(new FileWriter("accessMatrix.csv"));
            List<String> objectsOrder = new ArrayList<>();
            StringJoiner line = new StringJoiner(";");
            line.add(" ");
            for (String o:objects.keySet()
                 ) {
                objectsOrder.add(o);
                line.add(o);
            }
            writer.write(line+"\n");
            writer.flush();
            for (Subject s:accessMatrix.keySet()
                 ) {
                line = new StringJoiner(";");
                line.add(s.getLogin());
                for (String o:objectsOrder
                     ) {
                    line.add(accessMatrix.get(s).get(objects.get(o)));
                }
                writer.write(line +"\n");
                writer.flush();
            }
            writer.close();
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
                    if (!subjects.containsKey(login)) {
                        subjects.put(login, new Subject("null", "null", "null"));
                        accessMatrix.put(subjects.get(login), new HashMap<>());
                        for (String o:objects.keySet()
                             ) {
                            accessMatrix.get(subjects.get(login)).put(objects.get(o), "null");
                        }
                        subjectsBackUp(subjects);
                    }

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
                System.out.println("looks like there was a forced shutdown of the client");
                //throw new RuntimeException(e);
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


    private static void initMaps(String subPath, String objPath, String matrixPath) throws IOException {
        Ini subjectFile = new Ini(new File(subPath));
        for (java.lang.Object login: subjectFile.keySet()
        ) {
            subjects.put(String.valueOf(login),
                    new Subject(String.valueOf(login),
                            subjectFile.get(login, "secrecyLevel"),
                            subjectFile.get(login, "tempSecrecyLevel")));
        }
        Ini objectFile = new Ini(new File(objPath));
        for (java.lang.Object login: objectFile.keySet()
        ) {
            objects.put(String.valueOf(login),
                    new Document(String.valueOf(login),
                            objectFile.get(login, "secrecyLevel"),
                            objectFile.get(login, "material")));
        }
        BufferedReader reader = new BufferedReader(new FileReader(matrixPath));
        String[] objectsOrder = reader.readLine().split(";");
        String[] line;
        while (reader.ready()){
            line = reader.readLine().split(";");
            accessMatrix.put(subjects.get(line[0]), new HashMap<>());
            for (int i = 0; i < objectsOrder.length; i++) {
                accessMatrix.get(subjects.get(line[0])).put(objects.get(objectsOrder[i]),line[i]);
            }
        }
        reader.close();
    }
    public static void main(String[] args) throws IOException {
        initMaps("subjects.ini", "objects.ini", "accessMatrix.csv");
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