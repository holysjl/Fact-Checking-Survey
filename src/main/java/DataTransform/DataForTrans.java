package DataTransform;


import graphapi.Triple;
import org.apache.jena.base.Sys;

import javax.management.relation.Relation;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataForTrans {
    private Map<String, Integer> relation2id;
    private Map<String, Integer> node2id;
    private Set<Triple<Integer, Integer, Integer>> triples;
    private Set<Triple<Integer, Integer, Integer>> all_test;



    public DataForTrans(String[] args){
        relation2id = new LinkedHashMap<>();
        node2id = new LinkedHashMap<>();
        triples = new LinkedHashSet<>();
        all_test = new LinkedHashSet<>();
        int nodeIndex = 0;
        int relationIndex = 0;
        String separator = File.separator;

        try{
            BufferedReader in = new BufferedReader(new FileReader(args[0]));
            BufferedWriter out_train = new BufferedWriter(new FileWriter(args[1] + separator + "train.txt"));
            BufferedWriter out_graph_info = new BufferedWriter(new FileWriter(args[1] + separator + "graph_info.txt"));
            BufferedWriter out_node2id = new BufferedWriter(new FileWriter(args[1] + separator + "node2id.txt"));
            BufferedWriter out_relation2id = new BufferedWriter(new FileWriter(args[1] + separator + "relation2id.txt"));

            String str;
            int node1, node2, relation1;
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
                triples.add(new Triple(node1, relation1, node2));
            }
            in.close();


            out_train.write(triples.size()+"");
            out_train.newLine();
            for (Triple t : triples){
                out_train.write(t.first + "\t" + t.second + "\t" + t.third);
                out_train.newLine();
            }


            out_train.close();

            //if (Integer.parseInt(args[2]) == 1){
            //    triples = new LinkedHashSet<>();
            //    //BufferedReader in_valid = new BufferedReader(new FileReader(args[4]));
            //    BufferedWriter out_valid = new BufferedWriter(new FileWriter(args[1] + separator + "valid.txt"));
            //    while ((str = in_valid.readLine()) != null){
            //        String[] split = str.split("\t");
            //        if (!(node2id.containsKey(split[0]) && node2id.containsKey(split[2]) && relation2id.containsKey(split[1])))
            //            continue;
            //        triples.add(new Triple(node2id.get(split[0]),relation2id.get(split[1]),relation2id.get(split[2])));
            //    }
            //    in_valid.close();
            //    out_valid.write(triples.size() + "");
            //    out_valid.newLine();
            //    for (Triple t : triples){
            //        out_valid.write(t.first + " " + t.second + " " + t.third);
            //        out_valid.newLine();
            //    }
            //    out_valid.close();
            //}
            int num = 0;
            if (Integer.parseInt(args[2]) == 1){
                BufferedReader in_relationList = new BufferedReader(new FileReader(args[3]));
                while ((str = in_relationList.readLine()) != null){
                    String relationName = str;
                    triples = new LinkedHashSet<>();
                    str += ".tsv";
                    BufferedReader in_test = new BufferedReader(new FileReader(args[4] + separator + str));
                    String tripleStr;
                    if (!relation2id.containsKey(relationName)) continue;

                    while ((tripleStr = in_test.readLine()) != null){
                        num++;
                        String[] split = tripleStr.split("\t");
                        if (!(node2id.containsKey(split[0]) && node2id.containsKey(split[1]))){
                            if (!node2id.containsKey(split[0])) {
                                System.out.println(relationName + ":" + split[0]);
                                node2id.put(split[0], nodeIndex++);
                            }
                            if (!node2id.containsKey(split[1])) {
                                System.out.println(relationName + ":" + split[1]);
                                node2id.put(split[1], nodeIndex++);
                            }
                        }
                        int isPositive = split[2].equals("true")? 1 : 0;
                        triples.add(new Triple(node2id.get(split[0]), node2id.get(split[1]), Integer.valueOf(isPositive)));
                        all_test.add(new Triple(node2id.get(split[0]), relation2id.get(relationName) ,node2id.get(split[1])));
                    }


                    in_test.close();
                    File gt = new File(args[1] + separator + "groundtruth");
                    if (!gt.exists()) gt.mkdirs();

                    BufferedWriter out_test = new BufferedWriter(new FileWriter(args[1] + separator + "groundtruth" + separator + str));
                    //out_test.write(triples.size() + "");
                    //out_test.newLine();
                    for (Triple t : triples){
                        out_test.write(t.first + "\t" + relation2id.get(relationName) + "\t" + t.second + "\t" + t.third);
                        out_test.newLine();
                    }
                    out_test.close();
                }
                in_relationList.close();

                BufferedWriter out_all_test = new BufferedWriter(new FileWriter(args[1] + separator + "test.txt"));
                out_all_test.write(all_test.size() + "");
                out_all_test.newLine();
                for (Triple t : all_test){
                    out_all_test.write(t.first + "\t" + t.second + "\t" + t.third);
                    out_all_test.newLine();
                }
                out_all_test.close();
                //
                out_node2id.write(nodeIndex + "");
                out_node2id.newLine();
                for (String nodeName : node2id.keySet()){
                    out_node2id.write(nodeName + "\t" + node2id.get(nodeName));
                    out_node2id.newLine();
                }
                //
                out_graph_info.write(nodeIndex + " " + relationIndex);
                //
                out_relation2id.write(relationIndex + "");
                out_relation2id.newLine();
                for (String relationName : relation2id.keySet()){
                    out_relation2id.write(relationName + "\t" + relation2id.get(relationName));
                    out_relation2id.newLine();
                }
                out_node2id.close();
                out_relation2id.close();
                out_graph_info.close();

            }
            System.out.println(num);

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        //args[0]: input data
        //args[1]: output root
        //args[2]: if there is test data
        //args[3]: test relation list
        //args[4]: test data root
        DataForTrans dataForTrans = new DataForTrans(args);
    }
}
