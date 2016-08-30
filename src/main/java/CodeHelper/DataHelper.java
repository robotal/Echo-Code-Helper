package CodeHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by taldavidi on 7/13/16.
 */
public class DataHelper {


    public static void writeDefsIntoFile(String outputFile, List<String[]> entries){

        File outFile = new File(outputFile);
        FileWriter fw;
        try {
            fw = new FileWriter(outFile);

            for (int i = 0; i < entries.size(); i++) {

                String entry[] = entries.get(i);

                String csvLine = "";
                for (String item : entry) {
                    csvLine += "\"" + item + "\",";
                }

                try {
                    fw.write(csvLine.substring(0, csvLine.lastIndexOf(',')) + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            fw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String[]> loadCMDefinitions() {

        List<String[]> returnValue = new ArrayList<>();

        InputStream in = CodeHelperSpeechlet.class.getClassLoader().getResourceAsStream("icd10CM.desc");

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;


        try {
            //skip header line
            reader.readLine();
            String entry[] = "code\tdescription\tis_valid\tlaterality\tencounter\tanatomy\tqualifiers\tcode_set".split("\t");
            returnValue.add(entry);

            while((line = reader.readLine()) != null){

                line=line.trim();


                if(line.length()==0){
                    continue;
                }

                entry = line.split("\t");

                //flip is_truncated to is_valid
                entry[2] = (entry[2].equals("1") ? "0" : "1");

                returnValue.add(entry);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    private static List<String[]> loadPCSDefinitions() {

        List<String[]> returnValue = new ArrayList<>();

        InputStream in = CodeHelperSpeechlet.class.getClassLoader().getResourceAsStream("icd10pcs_order_2013.txt");

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;

        String header[] = {"code", "is_valid", "short_desc", "description"};
        returnValue.add(header);

        try {

            while((line = reader.readLine()) != null){

                line=line.trim();

                if(line.length()==0){
                    continue;
                }

                String code = line.substring(6,13).trim();
                String isValid = line.substring(14,15);
                String shortDesc = line.substring(16,77).trim();
                String desc = line.substring(77).trim();

                String entry[] = {code,isValid,shortDesc,desc};

                returnValue.add(entry);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnValue;
    }


    public static void main(String[] args) {

        writeDefsIntoFile("i10cm.csv", loadCMDefinitions());
        writeDefsIntoFile("i10pcs.csv", loadPCSDefinitions());

    }
}
