package Evaluation;

import graphapi.Triple;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransDEvaluation {
    private String separator = File.separator;
    private Map<Triple<Integer, Integer, Integer>, Boolean> result;
    private DecimalFormat df   =new   DecimalFormat("#.0000");


    public TransDEvaluation(String[] args){
        result = new LinkedHashMap<>();
        System.out.printf("%s\t%s\t%s\t%s\t%s\n", "Relation", "Accuracy", "Precision", "Recall", "F1");


        try{
            BufferedReader in_result;
            in_result = new BufferedReader(new FileReader(args[0] + separator + "testResult.tsv"));

            String str;
            while ((str = in_result.readLine()) != null){
                String[] split = str.split("\t");
                result.put(new Triple(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2])), Boolean.parseBoolean(split[3]));
            }
            in_result.close();
            BufferedReader in_relation = new BufferedReader(new FileReader(args[0] + separator + "amie_input_relations.tsv"));
            int sumOfRelation = 0;
            double sumOfPrecision = 0.0;
            double sumOfRecall = 0.0;
            double sumOfF1 = 0.0;
            double sumOfAccuracy = 0.0;
            String relation;
            while ((relation = in_relation.readLine()) != null){
                sumOfRelation++;
                BufferedReader in_gt = new BufferedReader(new FileReader(args[0] + separator + "groundtruth" + separator + relation + ".tsv"));
                int sumOfFact = 0;
                int TP = 0, FN = 0, FP = 0, correct = 0;
                String item;
                while ((item = in_gt.readLine()) != null){
                    sumOfFact++;
                    String split[] = item.split("\t");
                    Boolean isPositive;
                    if (split[3].equals("1")) isPositive = true;
                    else isPositive = false;
                    Triple tmp = new Triple(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
                    if (result.containsKey(tmp)){
                        if (result.get(tmp) && isPositive) {
                            TP++;
                            correct++;
                        }
                        else if (!result.get(tmp) && isPositive) FP++;
                        else if (result.get(tmp) && !isPositive) FN++;
                        else if (!result.get(tmp) && !isPositive) correct++;
                    }
                }
                in_gt.close();
                double precision = (TP+FP == 0)? 0 : 1.0 * TP / (TP+FP);
                double recall = (TP+FN == 0)? 0 : 1.0 * TP / (TP+FN);
                double f1 = (precision+recall == 0)? 0 : 2.0 * precision * recall / (precision + recall);
                double accuracy = 1.0 * correct / sumOfFact;
                sumOfAccuracy += accuracy;
                sumOfF1 += f1;
                sumOfPrecision += precision;
                sumOfRecall += recall;
                System.out.printf("%s\t%s\t%s\t%s\t%s\n", relation, df.format(accuracy), df.format(precision), df.format(recall), df.format(f1));


            }
            in_relation.close();
            System.out.printf("%s\t%s\t%s\t%s\t%s\n", "Mean", df.format(sumOfAccuracy/sumOfRelation), df.format(sumOfPrecision/sumOfRelation), df.format(sumOfRecall/sumOfRelation), df.format(sumOfF1/sumOfRelation));

        }catch(IOException e){
            e.printStackTrace();
        }



    }


    public static void main(String[] args) {
        //args[0]: data root
        TransDEvaluation t = new TransDEvaluation(args);
    }
}
