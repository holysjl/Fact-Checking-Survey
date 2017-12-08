package DataTransform;

import java.io.*;

public class NewGroundtruthForSFE {

    public NewGroundtruthForSFE(String[] args){

        try{
            File root = new File(args[0]);
            File resultFile = new File(args[1]);
            if (!resultFile.exists())
                resultFile.mkdirs();
            resultFile.mkdirs();
            for (File file : root.listFiles()){
                if (!file.getName().endsWith(".tsv")) continue;
                BufferedReader in = new BufferedReader(new FileReader(file));
                String relationName = file.getName();
                relationName = relationName.replace(".tsv", "");
                File dir = new File(args[1] + File.separator + relationName);
                dir.mkdirs();
                BufferedWriter out = new BufferedWriter(new FileWriter(dir.getAbsoluteFile() + File.separator + "testing.tsv"));

                String str;
                while ((str = in.readLine()) != null){
                    String[] tokens = str.split("\t");
                    if (tokens[2].equals("true"))
                        out.write(tokens[0] + "\t" + tokens[1] + "\t" + "1");
                    else if (tokens[2].equals("false"))
                        out.write(tokens[0] + "\t" + tokens[1] + "\t" + "-1");
                    out.newLine();
                }

                in.close();
                out.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args){
        NewGroundtruthForSFE newGroundtruthForSFE = new NewGroundtruthForSFE(args);
    }
}
