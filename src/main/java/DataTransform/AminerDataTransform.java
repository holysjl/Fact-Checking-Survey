package DataTransform;
import graphapi.LinkedMultiValueMap;
import graphapi.MultiValueMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

public class AminerDataTransform {
    private MultiValueMap<String, String> nodes;
    private Map<String, String> collaboration;


    public AminerDataTransform(String[] args){
        nodes = new LinkedMultiValueMap<>();
        collaboration = new LinkedHashMap<>();

        try {
            BufferedWriter bw_relations = new BufferedWriter(new FileWriter(args[1]));
            BufferedWriter bw_nodes = new BufferedWriter(new FileWriter(args[2]));
            File file = new File(args[0]);
            if (file.exists()) {
                File[] files = file.listFiles();
                for (File inputFile : files) {
                    if (!inputFile.getName().endsWith(".txt")) continue;
                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    String s;
                    while ((s = br.readLine()) != null) {
                        try {

                            JSONObject dataJson = new JSONObject(s);
                            String id = dataJson.get("id").toString();
                            if (!dataJson.get("lang").equals("en")) continue;
                            //addNode(id, "id");

                            if (dataJson.keySet().contains(("doc_type")))
                                addNode(id, dataJson.get("doc_type").toString());
                            else addNode(id, "paper");

                            for (String key : dataJson.keySet()){
                                if (key.equals("lang")) continue;
                                if (key.equals("abstract")) continue;
                                if (key.equals("url")) continue;
                                if (key.equals("pdf")) continue;
                                if (key.equals("doi")) continue;
                                if (key.equals("isbn")) continue;
                                if (key.equals("issn")) continue;
                                if (key.equals("page_start")) continue;
                                if (key.equals("page_end")) continue;
                                if (key.equals("volume")) continue;
                                if (key.equals("issue")) continue;
                                if (key.equals("url")) continue;

                                if (dataJson.isNull(key) || key.equals("id")) continue;
                                Object json = dataJson.get(key);

                                if (key.equals("authors") || key.equals("keywords") || key.equals("fos") || key.equals("references")) {

                                    JSONArray item = dataJson.getJSONArray(key);

                                    List<String> coAuthors = new LinkedList<>();
                                    if (key.equals("authors")) {
                                        for (int i = 0; i < item.length(); i++){
                                            if (i >2 ) break;
                                            JSONObject info = item.getJSONObject(i);
                                            if (!info.has("name") || info.isNull("name")) continue;
                                            String name = info.getString("name");
                                            if (name.equals("")) continue;

                                            addNode(name, "author");
                                            for (String a : coAuthors){
                                                if (collaboration.containsKey(a)){
                                                    if (!collaboration.get(a).equals(name))
                                                        collaboration.put(a, name);
                                                }
                                                else{
                                                    collaboration.put(a, name);
                                                }
                                            }
                                            coAuthors.add(name);

                                            bw_relations.write(uniteString(id, key + "Is", name));
                                            bw_relations.newLine();

                                            if (info.has("org") && !info.isNull("name")){
                                                String org = info.getString("org");
                                                addNode(org, "affiliation");
                                                bw_relations.write(uniteString(name,"affiliationIs", org));
                                                bw_relations.newLine();

                                            }
                                        }
                                    }
                                    else if (key.equals("references")){
                                        for(int i = 0; i < item.length(); i++){
                                            if (i >= 10) break;
                                            String info = item.getString(i).toLowerCase();
                                            //addNode(info, key);
                                            bw_relations.write(uniteString(id, key + "Is", info));
                                            bw_relations.newLine();
                                        }

                                    }
                                    else {
                                        for(int i = 0; i < item.length(); i++){
                                            String info = item.getString(i).toLowerCase();

                                            if (key.equals("keywords")){
                                                addNode(info, "keys_" + info);
                                            }
                                            if (key.equals("fos")){
                                                //System.out.println(info);
                                                addNode(info, "fields_" + info);
                                            }

                                            //addNode(info, key);
                                            bw_relations.write(uniteString(id, key + "Is", info));
                                            bw_relations.newLine();
                                        }
                                    }
                                }
                                else{
                                    String info = json.toString().toLowerCase();

                                    if (key.equals("year")){
                                        addNode(info, "year");
                                    }
                                    if (key.equals("venue")){
                                        addNode(info, "venue");
                                    }

                                    //addNode(info, key);
                                    bw_relations.write(uniteString(id, key + "Is", info));
                                    bw_relations.newLine();
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println(s);
                        }
                    }
                    for (String name : collaboration.keySet()){
                        bw_relations.write(uniteString(name,"hasCollaboration", collaboration.get(name)));
                        bw_relations.newLine();
                    }
                    br.close();

                }
            }
            for (String key : nodes.keySet()){
                for (String label : nodes.getValues(key)){
                    bw_nodes.write(uniteString(key, label));
                    bw_nodes.newLine();
                }
            }

            bw_relations.close();
            bw_nodes.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNode(String value, String label){
        if (nodes.containsKey(value) && nodes.getValues(value).contains(label))
            return;
        else{
            nodes.add(value, label);
        }
    }

    public String uniteString(String a, String b){
        a = a.replaceAll("\n","_").replaceAll("\t", "_");
        b = b.replaceAll("\n","_").replaceAll("\t", "_");
        return (a.replaceAll(" ", "_") + "\t" + b.replaceAll(" ", "_"));
    }
    public String uniteString(String a, String b, String c){
        c = c.replaceAll("\n","_").replaceAll("\t", "_");
        return (uniteString(a, b) + "\t" + c.replaceAll(" ", "_"));
    }



    public static void main(String[] args){
        AminerDataTransform newTransform = new AminerDataTransform(args);
    }
}
