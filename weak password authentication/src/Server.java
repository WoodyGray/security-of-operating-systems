import java.io.*;
import java.util.StringJoiner;

public class Server {
    private static String soli;
    private static void generateSoli(int lengthSoli) throws IOException, InterruptedException {
        String configPath = "D:\\study\\ВлГУ ебаное\\BOS\\2 лаба\\py\\config";
        String pyBBSPath = "D:\\study\\ВлГУ ебаное\\BOS\\2 лаба\\py\\BBS.py";
        String soliPath = "D:\\study\\ВлГУ ебаное\\BOS\\2 лаба\\py\\soli.txt";
        String absolutePyPath = "D:\\study\\ВлГУ ебаное\\BOS\\2 лаба\\py\\venv\\Scripts\\python";
        // read config lines
        BufferedReader configReader = new BufferedReader(new FileReader(configPath));
        String[] configLines = new String[2];
        for (int i = 0; i < configLines.length; i++) {
            configLines[i] = configReader.readLine();
        }
        configReader.close();
        // setting config
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(configPath));
        String[] buffer = configLines[0].split(" ");
        if (buffer.length > 1){
            buffer[1] = String.valueOf(lengthSoli) + "\n";
            configLines[0] = buffer[0] + " " + buffer[1];
        }
        for (String line: configLines
             ) {
            configWriter.write(line);
        }
        configWriter.close();
        // run py BBS
        System.out.println("start generate soli");
        ProcessBuilder processBuilder = new ProcessBuilder(absolutePyPath, pyBBSPath);
        processBuilder.redirectErrorStream(true);

        Process soliGenerate = processBuilder.start();
        Reader reader = new InputStreamReader(soliGenerate.getInputStream());
        BufferedReader bf = new BufferedReader(reader);
        String s;
        while ((s = bf.readLine()) != null) {
            System.out.print(s);
        }
        soliGenerate.waitFor();
        System.out.println("generate complete");


        // show soli
        BufferedReader soliReader = new BufferedReader(new FileReader(soliPath));
        StringJoiner bites = new StringJoiner("");
        while (soliReader.ready()){
            bites.add(soliReader.readLine());
        }
        for (byte simbol:bites.toString().getBytes()
             ) {
            System.out.print((char) simbol);
        }


    }
    public static void main(String[] args) throws IOException, InterruptedException {
        generateSoli(16);
    }
}