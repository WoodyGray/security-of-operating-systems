import org.ini4j.Ini;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Server {
    private static final String url = "jdbc:mysql://localhost:3306/bos";
    private static final String user = "root";
    private static final String password = "123";
    private static final int N = 1000;
    private static final int counterValue = 5;
    private static DBThread dbThread;
    private static final int PORT = 8001;
    private static List<ClientThread> clients = new ArrayList<>();
    private static HashMap<String, Integer> clientsMap = new HashMap<>();
    public static class ClientThread extends Thread{
        private BufferedReader in;
        private BufferedWriter out;
        private Socket connection;
        public ClientThread(Socket connection) throws IOException {
            this.connection = connection;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            start();
        }

        @Override
        public void run() {
            try {
                int A = (int) (Math.random()*600 + 200);
                out.write(A + "\n");
                out.flush();
                String message = in.readLine();
                String clientHash = bytesToHex(Hashing(A, stringToByteArray(message)));
                boolean usl = false;
                for (String client: clientsMap.keySet()
                     ) {
                    if (clientHash.equals(client)){
                        if (clientsMap.get(client) >= counterValue){
                            clientsMap.remove(client);
                            out.write("-1\n");
                            out.flush();
                        }else {
                            clientsMap.put(client, clientsMap.get(client) + 1);
                            out.write("1\n");
                            out.flush();
                        }
                        usl = true;
                        break;
                    }
                }
                if (!usl){
                    out.write("0\n");
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("error1");
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("error2");
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
    public static class DBThread extends Thread{
        private static Connection con;
        private static Statement stmt;
        private static ResultSet rs;

        public DBThread(String url,String user,String password) {
            try {
                // opening database connection to MySQL server
                con = DriverManager.getConnection(url, user, password);

                // getting Statement object to execute query
                stmt = con.createStatement();
                start();
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }

        @Override
        public void run() {
            String query = "SELECT HASH FROM `account` WHERE Id = 1;";
            try {
                // executing SELECT query
                rs = stmt.executeQuery(query);

                while (rs.next()) {
                    String count = rs.getString(1);
                    System.out.println(count);
                }
            }catch (Exception e){

            } finally {
                //close connection ,stmt and resultset here
                try {
                    con.close();
                } catch (SQLException se) { /*can't do anything */ }
                try {
                    stmt.close();
                } catch (SQLException se) { /*can't do anything */ }
                try {
                    rs.close();
                } catch (SQLException se) { /*can't do anything */ }
            }
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        //dbThread = new DBThread(url, user, password);
        //System.out.println("DBThread running...");
        Ini ini = new Ini(new File("D:\\study\\ВлГУ\\BOS\\3 лаба\\java\\untitled\\src\\configReg.ini"));
        for (int i = 1; i <= 3; i++) {
            String var = "var1." + i;
            tryRegistration(ini.get(var, "login"), ini.get(var, "password"));
        }
        System.out.println("registrations completed");
        boolean runUsl = true;
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
    private static void tryRegistration(String login, String password) throws NoSuchAlgorithmException {
        String client = bytesToHex(Hashing(N, login + password));
        if (!clientsMap.containsKey(client)){
            clientsMap.put(client, 0);
        }
        System.out.println("new client add");
    }
    public static byte[] stringToByteArray (String s) {
        byte[] byteArray = new byte[s.length()/2];
        String[] strBytes = new String[s.length()/2];
        int k = 0;
        for (int i = 0; i < s.length(); i=i+2) {
            int j = i+2;
            strBytes[k] = s.substring(i,j);
            byteArray[k] = (byte)Integer.parseInt(strBytes[k], 16);
            k++;
        }
        return byteArray;
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public static byte[] Hashing(int n, byte[] hash) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = messageDigest.digest(hash);
        for (int i = 0; i < n - 1; i++) {
            result = messageDigest.digest(result);
        }
        return result;
    }
    public static byte[] Hashing(int n, String line) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = messageDigest.digest(line.getBytes(StandardCharsets.UTF_8));
        for (int i = 0; i < n - 1; i++) {
            result = messageDigest.digest(result);
        }
        return result;
    }
}