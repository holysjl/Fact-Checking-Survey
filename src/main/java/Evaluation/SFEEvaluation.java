package Evaluation;

import graphapi.Pair;

import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.Set;

public class SFEEvaluation {
    private String separator;
    private Set<Pair<String, String>> fact;
    private DecimalFormat df   =new   DecimalFormat("#.0000");

    public SFEEvaluation(String[] args){
        separator = File.separator;
        System.out.printf("%s\t%s\t%s\t%s\t%s\n", "", "Accuracy", "Precision", "Recall", "F1");
        try{
            BufferedReader in_relation = new BufferedReader(new FileReader(args[0]));
            BufferedWriter out_result = new BufferedWriter(new FileWriter(args[1] + separator + "result.tsv"));
            String relationName;
            int sumOfRelation = 0;
            double sumOfAccuracy = 0.0;
            double sumOfPrecision = 0.0;
            double sumOfF1 = 0.0;
            double sumOfRecall = 0.0;
            while((relationName = in_relation.readLine()) != null){
                if (relationName.equals("")) continue;
                sumOfRelation++;
                fact = new LinkedHashSet<>();
                BufferedReader in_fact = new BufferedReader(new FileReader(args[1] + separator + relationName + separator +"scores.tsv"));
                String prediction;
                while ((prediction = in_fact.readLine()) != null){
                    if (prediction.equals("")) continue;
                    String[] split = prediction.split("\t");
                    fact.add(new Pair(split[0], split[1]));
                }
                in_fact.close();

                BufferedReader in_check = new BufferedReader(new FileReader(args[2] + separator + relationName + separator +"testing.tsv"));
                String check;
                int TP = 0, FN = 0, FP = 0, correct = 0, num = 0;
                while ((check = in_check.readLine()) != null){
                    if (check.equals("")) continue;
                    num++;
                    String[] split = check.split("\t");
                    if (fact.contains(new Pair(split[0], split[1]))){
                        if (split[2].equals("1")) {
                            TP++;
                            correct++;
                        }
                        else FP++;

                        out_result.write(split[0] + "\t" + relationName + "\t" + split[1] + "\t" + "true");
                        out_result.newLine();
                    }
                    else{
                        if (split[2].equals("1")) FN++;
                        else correct++;
                        out_result.write(split[0] + "\t" + relationName + "\t" + split[1] + "\t" + "false");
                        out_result.newLine();
                    }
                }
                double precision = (TP+FP == 0)? 0 : 1.0 * TP / (TP+FP);
                double recall = (TP+FN == 0)? 0 : 1.0 * TP / (TP+FN);
                double f1 = (precision+recall == 0)? 0 : 2.0 * precision * recall / (precision + recall);
                double accuracy = 1.0 * correct / num;
                sumOfAccuracy += accuracy;
                sumOfPrecision += precision;
                sumOfF1 += f1;
                sumOfRecall += recall;

                System.out.printf("%s\t%s\t%s\t%s\t%s\n", relationName, df.format(accuracy), df.format(precision), df.format(recall), df.format(f1));
                in_check.close();
            }
            in_relation.close();
            out_result.close();

            System.out.printf("%s\t%s\t%s\t%s\t%s\n", "Mean", df.format(sumOfAccuracy/sumOfRelation), df.format(sumOfPrecision/sumOfRelation), df.format(sumOfRecall/sumOfRelation), df.format(sumOfF1/sumOfRelation));


        }catch(IOException e){
            e.printStackTrace();
        }


    }


    public static void main(String[] args){
        //args[0]: relation list
        //args[1]: result root
        //args[2]: groundtruth root
        File f = new File(args[1]);
        String[] tmp = new String[3];
        tmp[0] = args[0];
        tmp[2] = args[2];

        if (f.listFiles().length != 0){
            for (String str : f.list()){
                tmp[1] = args[1] + File.separator + str;
                System.out.println(str);
                SFEEvaluation s = new SFEEvaluation(tmp);
            }
        }
        //SFEEvaluation s = new SFEEvaluation(args);

    }
}
