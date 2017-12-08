package Evaluation;

import graphapi.Triple;
import org.apache.jena.base.Sys;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Ensemble {
    private static final String Separator = File.separator;
    private DecimalFormat df   =new   DecimalFormat("#.0000");
    private Map<Triple<String, String, String>, Boolean> transD;
    private Map<Triple<String, String, String>, Boolean> amie;
    private Map<Triple<String, String, String>, Boolean> pra;
    private Map<Triple<String, String, String>, Boolean> sfe;
    private Map<Triple<String, String, String>, Boolean> sfe2;
    private Map<Triple<String, String, String>, Boolean> kgMiner;
    private Map<Triple<String, String, String>, Boolean> gt;
    private Map<Triple<String, String, String>, Boolean> voteResult;
    private List<String> relationList;

    public int sum_Integer(Map<String, Integer> map){
        int cal = 0;
        for (String str : map.keySet())
            cal += map.get(str);
        return cal;
    }
    public double sum_Double(Map<String, Double> map){
        double cal = 0.0;
        for (String str : map.keySet())
            cal += map.get(str);
        return cal;
    }
    public double average_Double(Map<String, Double> map){
        double cal = sum_Double(map);
        return (cal / map.keySet().size());
    }

    public void readTransD(String inpath){
        Map<String, String> id2node = new LinkedHashMap<>();
        Map<String, String> id2relation = new LinkedHashMap<>();
        int numOfRelations = 0;
        int numOfNodes = 0;
        String str;
        try{
            BufferedReader in_node = new BufferedReader(new FileReader(inpath + Separator + "node2id.txt"));
            BufferedReader in_relation = new BufferedReader(new FileReader(inpath + Separator + "relation2id.txt"));
            str = in_node.readLine();
            numOfNodes = Integer.valueOf(str);
            str = in_relation.readLine();
            numOfRelations = Integer.valueOf(str);
            while ((str = in_node.readLine()) != null){
                String[] tokens = str.split("\t");
                id2node.put(tokens[1], tokens[0]);
            }
            while ((str = in_relation.readLine()) != null){
                String[] tokens = str.split("\t");
                id2relation.put(tokens[1], tokens[0]);
            }
            in_node.close();
            in_relation.close();

            BufferedReader in_prediction = new BufferedReader(new FileReader(inpath + Separator + "testResult.tsv"));
            while  ((str = in_prediction.readLine()) != null){
                String[] tokens = str.split("\t");
                String x = id2node.get(tokens[0]);
                String r = id2relation.get(tokens[1]);
                String y = id2node.get(tokens[2]);
                if (!relationList.contains(r)) continue;
                Boolean check = tokens[3].equals("true") ? true : false;
                transD.put(new Triple(x, r, y), check);
            }
            //System.out.println("Number of predictions: " + transD.keySet().size());
            in_prediction.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void readAMIE(String inpath){
        String str;
        try{
            for (String relationName : relationList){
                BufferedReader in_prediction = new BufferedReader(new FileReader(inpath + Separator + "AMIE+_result" + Separator +relationName + ".tsv"));
                while ((str = in_prediction.readLine()) != null){
                    String[] tokens = str.split("\t");
                    String x = tokens[0];
                    String y = tokens[1];
                    Boolean check = tokens[2].equals("true") ? true : false;
                    amie.put(new Triple<>(x, relationName, y), check);
                }
                in_prediction.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void readPRA(String inpath, String type){
        String str;
        try{
            BufferedReader in_prediction = new BufferedReader(new FileReader(inpath + Separator + "result.tsv"));
            while ((str = in_prediction.readLine()) != null){
                String[] tokens = str.split("\t");
                String x = tokens[0];
                String r = tokens[1];
                String y = tokens[2];
                if (!relationList.contains(r)) continue;
                Boolean check = tokens[3].equals("true") ? true : false;
                if (type.equals("pra")){
                    pra.put(new Triple<>(x, r, y), check);
                }
                else if (type.equals("sfe")){
                    sfe.put(new Triple<>(x, r, y), check);
                }
                else if (type.equals("sfe2")){
                    sfe2.put(new Triple<>(x, r, y), check);
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void readKGMiner(String inpath){
        String str;
        try{
            BufferedReader in_prediction = new BufferedReader(new FileReader(inpath + Separator + "result.tsv"));

            while ((str = in_prediction.readLine()) != null){
                String[] tokens = str.split("\t");
                String x = tokens[0];
                String r = tokens[1];
                String y = tokens[2];
                if (!relationList.contains(r)) continue;
                Boolean check = tokens[3].toLowerCase().equals("true") ? true : false;
                kgMiner.put(new Triple<>(x, r, y), check);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void readGroundtruth(String inpath){
        String str;
        try{
            for (String relationName : relationList){
                BufferedReader in_prediction = new BufferedReader(new FileReader(inpath + Separator + "groundtruth" + Separator + relationName + ".tsv"));
                while ((str = in_prediction.readLine()) != null){
                    String[] tokens = str.split("\t");
                    String x = tokens[0];
                    String y = tokens[1];
                    Boolean check = tokens[2].equals("true") ? true : false;
                    gt.put(new Triple<>(x, relationName, y), check);
                }
                in_prediction.close();
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void readRelations(String inpath){
        String relationName;
        try{
            BufferedReader in_relations = new BufferedReader(new FileReader(inpath + Separator + "amie_input_relations.tsv"));
            while ((relationName = in_relations.readLine()) != null) {
                relationList.add(relationName);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void checkForPrediction(String remove){
        voteResult = new LinkedHashMap<>();
        List<Map<Triple<String, String, String>, Boolean>> algorithms = new LinkedList<>();
        algorithms.add(transD);
        algorithms.add(amie);
        algorithms.add(pra);
        if (!remove.equals("sfe1")) algorithms.add(sfe);
        if (!remove.equals("sfe2")) algorithms.add(sfe2);
        algorithms.add(kgMiner);

        for (Triple<String, String, String> triple : gt.keySet()){
            int voteForP = 0;
            int voteForN = 0;
            Boolean result;
            for (Map<Triple<String, String, String>, Boolean> algorithm : algorithms){
                if (!algorithm.keySet().contains(triple)) continue;
                if (algorithm.get(triple))
                    voteForP++;
                else
                    voteForN++;
            }

            if (voteForP == 0 && gt.get(triple)) System.out.println(uniteString(triple.first, triple.second, triple.third, gt.get(triple).toString()));
            if (voteForN == 0 && !gt.get(triple)) System.out.println(uniteString(triple.first, triple.second, triple.third, gt.get(triple).toString()));

            if (voteForP > voteForN)
                result = true;
            else if (voteForN > voteForP)
                result = false;
            else
                result = sfe.get(triple);

            voteResult.put(triple, result);
        }
    }

    public Map<String, Map<String, Integer>> calStatisticalValue(Map<Triple<String, String, String>, Boolean> algorithm){
        Map<String, Integer> tp, fp, fn, tn;
        Map<String, Map<String, Integer>> statisticalValue;
        tp = new LinkedHashMap<>();
        fp = new LinkedHashMap<>();
        fn = new LinkedHashMap<>();
        tn = new LinkedHashMap<>();
        for (String relation : relationList){
            tp.put(relation, 0);
            fp.put(relation, 0);
            fn.put(relation, 0);
            tn.put(relation, 0);
        }
        statisticalValue = new LinkedHashMap<>();
        for (Triple<String, String, String> triple : gt.keySet()) {
            Boolean gtResult = gt.get(triple);
            Boolean result = algorithm.get(triple);
            String relationName = triple.second();
            //calculate the result
            if (gtResult == result) {
                if (gtResult)
                    tp.put(relationName, tp.get(relationName) + 1);
                else
                    tn.put(relationName, tn.get(relationName) + 1);
            } else {
                if (gtResult)
                    fn.put(relationName, fn.get(relationName) + 1);
                else
                    fp.put(relationName, fp.get(relationName) + 1);
            }
        }
        statisticalValue.put("tp", tp);
        statisticalValue.put("fp", fp);
        statisticalValue.put("fn", fn);
        statisticalValue.put("tn", tn);
        return statisticalValue;
    }

    public Map<String, Map<String, Double>> analyzeResult(Map<Triple<String, String, String>, Boolean> algorithm){
        Map<String, Map<String, Integer>> statisticalValue = calStatisticalValue(algorithm);
        Map<String, Integer> tp, fp, fn, tn;
        Map<String, Double> accuracy, precision, recall, f1;
        Map<String, Map<String, Double>> analysis;
        tp = statisticalValue.get("tp");
        fp = statisticalValue.get("fp");
        fn = statisticalValue.get("fn");
        tn = statisticalValue.get("tn");
        accuracy = new LinkedHashMap<>();
        precision = new LinkedHashMap<>();
        recall = new LinkedHashMap<>();
        f1 = new LinkedHashMap<>();
        analysis = new LinkedHashMap<>();
        //calculate

        for (String relation : relationList){
            int tp_value = tp.get(relation);
            int tn_value = tn.get(relation);
            int fp_value = fp.get(relation);
            int fn_value = fn.get(relation);

            accuracy.put(relation, 1.0*(tp_value + tn_value)/(tp_value + fp_value + fn_value + tn_value));
            if (tp_value + fp_value == 0)
                precision.put(relation, 0.0);
            else
                precision.put(relation, 1.0*tp_value / (tp_value + fp_value));
            if (tp_value + fn_value == 0)
                recall.put(relation, 0.0);
            else
                recall.put(relation, 1.0*tp_value / (tp_value + fn_value));
            f1.put(relation, 2.0*tp_value/(2*tp_value + fp_value + fn_value));
        }
        analysis.put("accuracy", accuracy);
        analysis.put("precision", precision);
        analysis.put("recall", recall);
        analysis.put("f1", f1);
        return analysis;
    }

    public void printResultOfAlgorithms(String nameOfAlgorithm, Map<String, Map<String, Double>> analysis){
        Map<String, Double> accuracy, precision, recall, f1;
        accuracy = analysis.get("accuracy");
        precision = analysis.get("precision");
        recall = analysis.get("recall");
        f1 = analysis.get("f1");
        System.out.println(nameOfAlgorithm);
        for (String relation : relationList){
            System.out.println(uniteString(relation, df.format(accuracy.get(relation)), df.format(precision.get(relation)), df.format(recall.get(relation)), df.format(f1.get(relation))));
        }
        System.out.println(uniteString("Mean", df.format(average_Double(accuracy)), df.format(average_Double(precision)), df.format(average_Double(recall)), df.format(average_Double(f1))));

    }

    public void printCSVResult(Map<Triple<String, String, String>, Boolean> algorithm, String outpath){
        try{
            File f = new File(outpath);
            if (!f.exists()) f.mkdirs();
            int index = 0;
            for (String relationName : relationList){
                System.out.println(relationName);
                index++;
                BufferedWriter out = new BufferedWriter(new FileWriter(outpath + Separator + index + ".csv"));
                out.write("actual, predicted");
                out.newLine();
                for (Triple<String, String, String> triple : gt.keySet()){
                    if (!triple.second().equals(relationName)) continue;
                    out.write(gt.get(triple).toString().toUpperCase() + "," + algorithm.get(triple).toString().toUpperCase());
                    out.newLine();
                }
                out.close();

            }

            //Map<String, Map<String, Integer>> statisticalValue = calStatisticalValue(algorithm);
            //System.out.println("TP:" + sum_Integer(statisticalValue.get("tp")));
            //System.out.println("FP:" + sum_Integer(statisticalValue.get("fp")));
            //System.out.println("FN:" + sum_Integer(statisticalValue.get("fn")));
            //System.out.println("TN:" + sum_Integer(statisticalValue.get("tn")));
        }catch(IOException e){

        }

    }

    public void printComparation(List<Map<String, Map<String, Double>>> algorithmsResult){

        System.out.print(uniteString("Relation", "amie", "pra", "kgminer", "sfe(r=2)", "transD","ensemble(no sfe1)"));
        System.out.print(uniteString( "amie", "pra", "kgminer", "sfe(r=2)", "transD", "ensemble(no sfe1)"));
        System.out.print(uniteString( "amie", "pra", "kgminer", "sfe(r=2)", "transD", "ensemble(no sfe1)"));
        System.out.println(uniteString( "amie", "pra", "kgminer", "sfe(r=2)", "transD", "ensemble(no sfe1)"));


        for (String relation : relationList){
            System.out.print(relation + "\t");
            for (Map<String, Map<String, Double>> algorithmResult : algorithmsResult){
                System.out.print(uniteString(df.format(algorithmResult.get("accuracy").get(relation))));
            }
            for (Map<String, Map<String, Double>> algorithmResult : algorithmsResult){
                System.out.print(uniteString(df.format(algorithmResult.get("precision").get(relation))));
            }
            for (Map<String, Map<String, Double>> algorithmResult : algorithmsResult){
                System.out.print(uniteString(df.format(algorithmResult.get("recall").get(relation))));
            }
            for (Map<String, Map<String, Double>> algorithmResult : algorithmsResult){
                System.out.print(uniteString(df.format(algorithmResult.get("f1").get(relation))));
            }

            System.out.println();
        }
    }
    public String uniteString(String... args){
        String str = "";
        for (String arg : args){
            str += (arg + "\t");
        }
        return str;
    }

    public Ensemble(String[] args){
        String inpath = args[0];
        transD = new LinkedHashMap<>();
        amie = new LinkedHashMap<>();
        pra = new LinkedHashMap<>();
        sfe = new LinkedHashMap<>();
        kgMiner = new LinkedHashMap<>();
        gt = new LinkedHashMap<>();
        relationList = new LinkedList<>();
        sfe2 = new LinkedHashMap<>();
        voteResult = new LinkedHashMap<>();

        readRelations(inpath);
        readTransD(inpath + Separator + "transD");
        readAMIE(inpath + Separator + "amie");
        readPRA(inpath + Separator + "pra", "pra");
        readPRA(inpath + Separator + "sfe", "sfe");
        //readPRA(inpath + Separator + "sfe2", "sfe2");
        readKGMiner(inpath + Separator + "KGMiner");
        readGroundtruth(inpath);
        checkForPrediction("sfe2");
        Map<String, Map<String, Double>> resultOfvote1 = analyzeResult(voteResult);
        //checkForPrediction("sfe2");
        //Map<String, Map<String, Double>> resultOfvote2 = analyzeResult(voteResult);


        Map<String, Map<String, Double>> resultOfamie = analyzeResult(amie);
        Map<String, Map<String, Double>> resultOftransD = analyzeResult(transD);
        Map<String, Map<String, Double>> resultOfpra = analyzeResult(pra);
        Map<String, Map<String, Double>> resultOfsfe = analyzeResult(sfe);
        //Map<String, Map<String, Double>> resultOfsfe2 = analyzeResult(sfe2);
        Map<String, Map<String, Double>> resultOfKGMiner = analyzeResult(kgMiner);

        List<Map<String, Map<String, Double>>> algorithmsResult = new LinkedList();
        algorithmsResult.add(resultOfamie);
        algorithmsResult.add(resultOfpra);
        algorithmsResult.add(resultOfKGMiner);
        algorithmsResult.add(resultOfsfe);
        //algorithmsResult.add(resultOfsfe2);
        algorithmsResult.add(resultOftransD);
        algorithmsResult.add(resultOfvote1);
        //algorithmsResult.add(resultOfvote2);


        //printResultOfAlgorithms("Result", resultOfpra);
        //printResultOfAlgorithms("Result", resultOfsfe);
        //printResultOfAlgorithms("Result", resultOfsfe2);

        //printCSVResult(amie, args[1] + Separator + "amie_pvalue");
        //printCSVResult(kgMiner, args[1] + Separator + "kgminer_pvalue");
        //printCSVResult(pra, args[1] + Separator + "pra_pvalue");
        //printCSVResult(sfe, args[1] + Separator + "sfe_pvalue");
        //printCSVResult(sfe2, args[1] + Separator + "sfe2_pvalue");
        //printCSVResult(transD, args[1] + Separator + "transD_pvalue");
        //printCSVResult(voteResult, args[1] + Separator + "ensemble_pvalue");
        //printComparation(algorithmsResult);




    }


    public static void main(String[] args){
        //args[0]: input path
        //args[1]: out path
        Ensemble ensemble = new Ensemble(args);
    }
}
