package DataTransform;

import graphapi.Triple;
import org.apache.jena.base.Sys;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RemoveIsolatedPoint {
    private Map<String, Integer> relation2id;
    private Map<String, Integer> node2id;
    private int nodeIndex;
    private int relationIndex;
    private String separator = File.separator;
    private int numOfIsolatedPoint;
    private int numOfTestItem;


    public RemoveIsolatedPoint(){
        numOfIsolatedPoint = 0;
        numOfTestItem = 0;
        relation2id = new LinkedHashMap<>();
        node2id = new LinkedHashMap<>();
        nodeIndex = 0;
        relationIndex = 0;

    }

    public void readTriple(String[] args){
        try{
            BufferedReader in = new BufferedReader(new FileReader(args[0] + separator + "amie_str_train_xry.tsv"));
            //BufferedWriter out_node2id = new BufferedWriter(new FileWriter(args[1] + separator + "node2id.txt"));
            //BufferedWriter out_relation2id = new BufferedWriter(new FileWriter(args[1] + separator + "relation2id.txt"));

            int node1, node2, relation1;
            String str;
            while ((str = in.readLine())!=null){
                String[] split = str.split("\t");
                if (relation2id.containsKey(split[1])){
                    relation1 = relation2id.get(split[1]);
                }
                else{
                    relation1 = relationIndex++;
                    relation2id.put(split[1], relation1);
                }

                if (node2id.containsKey(split[0])){
                    node1 = node2id.get(split[0]);
                }
                else{
                    node1 = nodeIndex++;
                    node2id.put(split[0], node1);
                }

                if (node2id.containsKey(split[2])){
                    node2 = node2id.get(split[2]);
                }
                else{
                    node2 = nodeIndex++;
                    node2id.put(split[2], node2);
                }
            }
            in.close();

            BufferedReader in_relation = new BufferedReader(new FileReader(args[0] + separator + "amie_input_relations.tsv"));
            String relationName;
            while((relationName = in_relation.readLine()) != null){
                BufferedReader in_gt = new BufferedReader(new FileReader(args[0] + separator + "groundtruth" + separator + relationName + ".tsv"));
                String item;
                Set<Triple<String, String, String>> testItems = new LinkedHashSet<>();
                while ((item = in_gt.readLine()) != null){
                    numOfTestItem++;
                    String[] split = item.split("\t");
                    if (!(node2id.containsKey(split[0]) && node2id.containsKey(split[1]))){
                        numOfIsolatedPoint++;
                    }
                    else{
                        testItems.add(new Triple(split[0], split[1], split[2]));
                    }
                }
                in_gt.close();
                BufferedWriter out_gt = new BufferedWriter(new FileWriter(args[0] + separator + "groundtruth" + separator + relationName + ".tsv"));
                for (Triple t : testItems){
                    out_gt.write(t.first + "\t" + t.second + "\t" + t.third);
                    out_gt.newLine();
                }
                out_gt.close();
            }

            System.out.println("Number of test items:" + numOfTestItem);
            System.out.println("Number of isolated item" + numOfIsolatedPoint);

        }
        catch (IOException e){
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        //args[0]: train test
        //args[1]: groundtruth root
        //args[2]: relation root
        RemoveIsolatedPoint r = new RemoveIsolatedPoint();
        r.readTriple(args);
    }
}
