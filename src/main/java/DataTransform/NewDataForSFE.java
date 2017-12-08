package DataTransform;

import graphapi.LinkedMultiValueMap;
import graphapi.MultiValueMap;
import graphapi.Pair;

import java.io.*;

public class NewDataForSFE {
    private MultiValueMap<String, Pair<String, String>> relations;

    public NewDataForSFE(String[] args){
        relations = new LinkedMultiValueMap<>();
        String root = args[1];

        try{
            BufferedReader in = new BufferedReader(new FileReader(args[0]));
            String str;
            while ((str = in.readLine()) != null){
                String[] tokens = str.split("\t");
                relations.add(tokens[1], new Pair<>(tokens[0], tokens[2]));
            }
            in.close();

            for (String key : relations.keySet()){
                File file = new File(root + File.separator + key);
                file.mkdirs();
                BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsolutePath() + File.separator + "training.tsv"));
                for (Pair<String, String> pair : relations.getValues(key)){
                    out.write(pair.first() + "\t" + pair.second);
                    out.newLine();
                }
                out.close();
            }

        }catch(IOException e){
            e.printStackTrace();
        }



    }

    public static void main(String[] args){
        NewDataForSFE newDataForSFE = new NewDataForSFE(args);
    }
}
