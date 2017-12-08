package DataTransform;

import graphapi.LinkedMultiValueMap;
import graphapi.MultiValueMap;
import scala.annotation.meta.field;

import java.io.*;
import java.util.*;

public class MAGTransform {

    private static String Separator = File.separator;
    private Set<String> Targets;
    private MultiValueMap<String, String> paper2field;
    private Set<String> papers;
    private Set<String> authors;
    private Set<String> affiliations;
    private Set<String> journals;
    private Set<String> conferences;
    private Set<String> years;
    private Set<String> fields;
    private MultiValueMap<String, String> collaborations;
    private Map<String, Map<Integer, String>> paper2author;
    private MultiValueMap<String, String> references;
    private Map<String, String> id2field;



    public MAGTransform(String[] args){
        Targets = new LinkedHashSet<>();
        Targets.add("04984686");   //Database
        Targets.add("0724DFBA");   //Machine learning
        Targets.add("0765A2E4");   //Data mining
        Targets.add("093C4716");   //Artificial intelligence
        paper2field = new LinkedMultiValueMap<>();
        papers = new HashSet<>();
        authors = new HashSet<>();
        paper2author = new HashMap<>();
        affiliations = new HashSet<>();
        collaborations = new LinkedMultiValueMap<>();
        journals = new HashSet<>();
        conferences = new HashSet<>();
        years = new HashSet<>();
        references = new LinkedMultiValueMap<>();
        id2field = new HashMap<>();
        fields = new HashSet<>();

        String inpath = args[0];
        String outpath = args[1];
        String str;

        try{
            //output file
            BufferedWriter out_relations = new BufferedWriter(new FileWriter(outpath + Separator + "relations.tsv"));
            BufferedWriter out_nodes = new BufferedWriter(new FileWriter(outpath + Separator + "nodes.tsv"));
            //input id to field
            BufferedReader in_field = new BufferedReader(new FileReader(inpath + Separator + "FieldsOfStudy.txt"));
            while ((str = in_field.readLine()) != null){
                String[] splits = str.split("\t");
                id2field.put(splits[0], splits[1].replaceAll(" ", "_"));
            }
            in_field.close();
            //input related fields
            for (String field : Targets)
                fields.add(field);
            BufferedReader in_hierarchy = new BufferedReader(new FileReader(inpath + Separator + "FieldOfStudyHierarchy.txt"));
            while ((str  =in_hierarchy.readLine()) != null){
                String[] splits = str.split("\t");
                if (Targets.contains(splits[2]))
                    fields.add(splits[0]);
            }
            in_hierarchy.close();

            for (String field : fields){
                out_nodes.write(uniteString(field, id2field.get(field)));
                out_nodes.newLine();
            }

            //find papers related to target field
            BufferedReader in_keyword = new BufferedReader(new FileReader(inpath + Separator + "PaperKeywords.txt"));
            while((str = in_keyword.readLine()) != null){
                String[] tokens = str.split("\t");
                if (tokens.length < 3) continue;
                String paperID = tokens[0];
                String fieldID = tokens[2];
                if (fields.contains(fieldID)) {
                    papers.add(paperID);
                    paper2field.add(paperID, fieldID);
                }
            }
            in_keyword.close();

            //delete papers published before args[2]
            //input paper
            BufferedReader in_year = new BufferedReader(new FileReader(inpath + Separator + "Papers.txt"));
            while((str = in_year.readLine()) != null) {
                String[] tokens = str.split("\t");
                if (tokens.length < 11) continue;
                String paperID = tokens[0];
                String yearID = tokens[3];
                //System.out.println("Year Limitation: " + Integer.valueOf(args[2]));
                if (Integer.valueOf(yearID) < Integer.valueOf(args[2]) && papers.contains(paperID)){
                    papers.remove(paperID);
                    paper2field.remove(paperID);
                }
            }
            in_year.close();
            System.out.println("Targets Paper number: " + papers.size());
            for (String key : paper2field.keySet()){
                for (String field : paper2field.getValues(key)){
                    out_nodes.write(uniteString(key, id2field.get(field)));
                    out_nodes.newLine();
                }
            }
            //input referenced paper
            BufferedReader in_references = new BufferedReader(new FileReader(inpath + Separator + "PaperReferences.txt"));
            while((str = in_references.readLine()) != null){
                String[] tokens = str.split("\t");
                if (tokens.length < 2) continue;
                String paperID = tokens[0];
                String referencePaperID = tokens[1];
                if (!paper2field.containsKey(referencePaperID)) continue;
                if (references.containsKey(paperID))
                    if (references.getValues(paperID).size() > 10) continue;

                references.add(paperID, referencePaperID);
                out_relations.write(uniteString(paperID, "referenceIs", referencePaperID));
                out_relations.newLine();
            }
            in_references.close();

            //input author
            BufferedReader in_author = new BufferedReader(new FileReader(inpath + Separator + "PaperAuthorAffiliations.txt"));

            while((str = in_author.readLine()) != null) {
                String[] tokens = str.split("\t");
                if (tokens.length < 6) continue;
                String paperID = tokens[0];
                String authorID = tokens[1];
                String affiliationID = tokens[2];
                int authorSequence = Integer.valueOf(tokens[5]);
                if (!papers.contains(paperID)) continue;
                if (authorSequence > 3) continue;
                authors.add(authorID);
                affiliations.add(affiliationID);
                if (paper2author.containsKey(paperID)){
                    Map<Integer, String> tmp = paper2author.get(paperID);
                    tmp.put(authorSequence, authorID);
                    paper2author.remove(paperID);
                    paper2author.put(paperID, tmp);
                }
                else{
                    Map<Integer, String> tmp = new HashMap<>();
                    tmp.put(authorSequence, authorID);
                    paper2author.put(paperID, tmp);
                }
                if (!authorID.equals("") && !authorID.equals(" ")){
                    out_relations.write(uniteString(paperID, "authorIs", authorID));
                    out_relations.newLine();
                }
                if (!affiliationID.equals("") & !affiliationID.equals(" ")){
                    out_relations.write(uniteString(authorID, "affiliationIs", affiliationID));
                    out_relations.newLine();
                }

            }

            //input paper
            BufferedReader in_paper = new BufferedReader(new FileReader(inpath + Separator + "Papers.txt"));
            while((str = in_paper.readLine()) != null) {
                String[] tokens = str.split("\t");
                if (tokens.length < 11) continue;
                String paperID = tokens[0];
                String yearID = tokens[3];
                String journalID = tokens[8];
                String conferenceID = tokens[9];

                if (!papers.contains(paperID)) continue;
                if (Integer.valueOf(yearID) < Integer.valueOf(args[2])) continue;
                if (!(yearID.equals(" ") || yearID.equals(""))){
                    years.add(yearID);
                    out_relations.write(uniteString(paperID, "yearsIn", yearID));
                    out_relations.newLine();
                }

                if (!(journalID.equals(" ") || journalID.equals(""))){
                    journals.add(journalID);
                    out_relations.write(uniteString(paperID, "publishedIn", journalID));
                    out_relations.newLine();
                }

                if (!(conferenceID.equals(" ") || conferenceID.equals(""))){
                    conferences.add(conferenceID);
                    out_relations.write(uniteString(paperID, "publishedIn", conferenceID));
                    out_relations.newLine();
                }
            }
            in_paper.close();

            //output nodes
            for (String author : authors){
                out_nodes.write(uniteString(author, "author"));
                out_nodes.newLine();
            }
            for (String affiliation : affiliations){
                out_nodes.write(uniteString(affiliation, "affiliation"));
                out_nodes.newLine();
            }
            for (String year : years){
                out_nodes.write(uniteString(year, "year"));
                out_nodes.newLine();
            }
            for (String journal : journals){
                out_nodes.write(uniteString(journal, "journal"));
                out_nodes.newLine();
            }
            for (String conference : conferences){
                out_nodes.write(uniteString(conference, "conference"));
                out_nodes.newLine();
            }
            for (String paper : paper2author.keySet()){
                Set<String> coAuthor = new LinkedHashSet<>();
                Map<Integer, String> tmp = paper2author.get(paper);
                if (tmp.keySet().size() == 1) continue;
                for (int i = 1; i <= tmp.keySet().size() + 1; i++){
                    for (String index : coAuthor){
                        collaborations.add(index, tmp.get(i));
                        //out_relations.write(uniteString(index, "collaborationWith", tmp.get(i)));
                        //out_relations.newLine();
                    }
                    coAuthor.add(tmp.get(i));
                }
            }

            for (String author1 : collaborations.keySet()){
                for (String author2 : collaborations.getValues(author1)){
                    out_relations.write(uniteString(author1, "collaborationWith", author2));
                    out_relations.newLine();
                }

            }


            out_relations.close();
            out_nodes.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String uniteString(String a, String b){
        return (a + "\t" + b);
    }
    public String uniteString(String a, String b, String c){
        return (uniteString(a, b) + "\t" + c);
    }




    public static void main(String[] args){
        //args[0]: input path
        //args[1]: output path
        //args[2]: years
        MAGTransform magDataTransform = new MAGTransform(args);
    }
}
