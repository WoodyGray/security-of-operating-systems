

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Server {
    private static final int PORT = 8001;
    private static boolean runUsl = true;
    private static HashMap<String, HashMap<String, Integer>> accessMatrix;
    private static ArrayList<String> fileNames;
    private static List<ClientThread> clients = new ArrayList<>();

    public static class Breaker extends Thread{
        private BufferedReader console;

        public Breaker(InputStream console){
            this.console = new BufferedReader(new InputStreamReader(console));
            start();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (console.readLine().equals("/store matrix")){
                        System.out.println("storing matrix...");
                        printMatrix(accessMatrix, "accessMatrix.csv");
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

        }
        private void printMatrix(HashMap<String, HashMap<String, Integer>> accessMatrix, String fileName) throws IOException {
            BufferedWriter matrixFile = new BufferedWriter(new FileWriter(fileName));
            StringJoiner line = new StringJoiner(";");
            line.add("+");
            for (String v:fileNames
                 ) {
                line.add(v);
            }
            matrixFile.write(line + "\n");
            for (String client: accessMatrix.keySet()
                 ) {
                line = new StringJoiner(";");
                line.add(client);
                for (String v:fileNames
                     ) {
                    line.add(String.valueOf(accessMatrix.get(client).get(v)));
                }
                matrixFile.write(line + "\n");
            }
            matrixFile.close();
            System.out.println("storing complete");
        }
    }

    public static class ClientThread extends Thread{
        private BufferedReader in;
        private BufferedWriter out;
        private Socket connection;
        private String login;

        private void commandCreate(String fileName) throws IOException {
            if (fileNames.contains(fileName)){
                out.write("such a file exist, try another name\n");
                //out.flush();
            }else {
                fileNames.add(fileName);
                for (String client : accessMatrix.keySet()
                ) {
                    if (client.equals(login)) {
                        accessMatrix.get(client).put(fileName, 3);
                    } else {
                        accessMatrix.get(client).put(fileName, 0);
                    }
                }
                out.write("file creation completed\n");
                //out.flush();
            }
        }
        private void commandDelete(String fileName) throws IOException {
            if (!fileNames.contains(fileName)){
                out.write("such file does not exist\n");
                //out.flush();
            }else {
                if (accessMatrix.get(login).get(fileName) == 3){
                    for (String client: accessMatrix.keySet()
                         ) {
                        accessMatrix.get(client).remove(fileName);
                    }
                    out.write("file deletion completed\n");
                    //out.flush();
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
        }
        private void commandWrite(String fileName) throws IOException {
            if (!fileNames.contains(fileName)){
                out.write("such file does not exist\n");
                //out.flush();
            }else {
                if (accessMatrix.get(login).get(fileName) >=2){
                    out.write("file writing completed\n");
                    //out.flush();
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
        }
        private void commandRead(String fileName) throws IOException {
            if (!fileNames.contains(fileName)){
                out.write("such file does not exist");
                //out.flush();
            }else {
                if (accessMatrix.get(login).get(fileName) >=1){
                    out.write("file reading completes\n");
                    //out.flush();
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
        }
        private void commandGRead(String fileName, String clientLogin) throws IOException {
            if (!fileNames.contains(fileName)) {
                out.write("such file does not exist\n");
                //out.flush();
            } else if (!accessMatrix.containsKey(clientLogin)) {
                out.write("such client does not exist\n");
                //out.flush();
            } else {
                if (accessMatrix.get(login).get(fileName) == 3){
                    accessMatrix.get(clientLogin).put(fileName, 1);
                    out.write("the right to read has been transferred\n");
                    //out.flush();
                }else {
                    out.write("u don't have privileges on this command\n");
                    //out.flush();
                }
            }
        }
        private void commandGWrite(String fileName, String clientLogin) throws IOException {
            if (!fileNames.contains(fileName)) {
                out.write("such file does not exist\n");
                //out.flush();
            }else if (!accessMatrix.containsKey(clientLogin)){
                out.write("such client does not exist\n");
                //out.flush();
            }else {
                if (accessMatrix.get(login).get(fileName)==3){
                    accessMatrix.get(clientLogin).put(fileName, 2);
                    out.write("the right to write has been transferred\n");
                    //out.flush();
                }else {
                    out.write("u have no privileges on this command\n");
                    //out.flush();
                }
            }
        }
        private void commandGDelete(String fileName, String clientLogin) throws IOException {
            if (!fileNames.contains(fileName)){
                out.write("such file does not exist\n");
                //out.flush();
            }else if (!accessMatrix.containsKey(clientLogin)){
                out.write("such client does not exist\n");
                //out.flush();
            }else {
                if (accessMatrix.get(login).get(fileName) == 3){
                    accessMatrix.get(clientLogin).put(fileName, 3);
                    out.write("rights to delete has been transferred\n");
                    //out.flush();
                }else {
                    out.write("u have no privileges on this command\n");
                    //out.flush();
                }
            }
        }
        private void commandProcessing(String message) throws IOException {
            String[] components = message.split(" ");
            if (components.length ==2) {
                switch (components[0]) {
                    case "/cr":
                        commandCreate(components[1]);
                        break;
                    case "/dl":
                        commandDelete(components[1]);
                        break;
                    case "/wr":
                        commandWrite(components[1]);
                        break;
                    case "/rd":
                        commandRead(components[1]);
                        break;
                    default:
                        out.write("no such command\n");
                        //out.flush();
                }
            }else if (components.length == 3){
                String command = components[0] + " " + components[1];
                String file = components[2].split(",")[0].trim().replaceAll("\"", "");
                String clientLogin = components[2].split(",")[1].trim().replaceAll("\"", "");
                switch (command){
                    case "/rt rd":
                        commandGRead(file, clientLogin);
                        break;
                    case "/rt wr":
                        commandGWrite(file, clientLogin);
                        break;
                    case "/rt dl":
                        commandGDelete(file, clientLogin);
                        break;
                    default:
                        out.write("no such command\n");
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
                    if (!accessMatrix.containsKey(login)) {
                        accessMatrix.put(login, new HashMap<>());
                        for (String name : fileNames
                        ) {
                            accessMatrix.get(login).put(name, 0);
                        }
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


    public static void main(String[] args) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader("accessMatrix.csv"));
        accessMatrix = new HashMap<>();
        String[] files = fileReader.readLine().split(";");
        fileNames = new ArrayList<>(List.of(files));
        fileNames.remove(0);
        String[] clientAccesses;
        while (fileReader.ready()){
            clientAccesses = fileReader.readLine().split(";");
            accessMatrix.put(clientAccesses[0], new HashMap<>());
            for (int i = 1; i < clientAccesses.length; i++) {
                accessMatrix.get(clientAccesses[0]).put(files[i], Integer.parseInt(clientAccesses[i]));
            }
        }
        fileReader.close();

        Breaker breaker = new Breaker(System.in);
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

    public static BigInteger generatePrime(int size) {
        BigInteger prime;
        Random rnd = new Random();

        // Генерируем случайное нечетное число заданного размера бит
        BigInteger num = new BigInteger(size, rnd).setBit(size - 1).setBit(0);

        // Проверяем, является ли число простым
        while (!num.isProbablePrime(100)) {
            // Увеличиваем число на 2, чтобы получить следующее нечетное число
            num = num.add(BigInteger.TWO);
        }

        prime = num;
        return prime;
    }
}