import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class Main {
    public static void main(String[] args){
        String inputPath, outputPath, timeFrame;
        int threshold, multiuserThreshold;
        boolean detailed = false;
        try {
            if(args[0].compareToIgnoreCase("-help") == 0){
                System.out.println("""
                                      								Input format:
                                      .txt file with tuples of requesting user and issued request seperated by { character
                                      	
                                      	John{https://jira.atlassian.com/rest/api/latest/search?jql=creator = Peter
                                      
                                      - the search query must contain spaces between operators and fields
                                      - one request per line
                                      - empty lines may be placed inbetween requests
                                      
                                      
                                      
                                      								Output format:
                                      .txt file
                                      
                                      
                                      
                                      							   Program Parameters:
                                      - path to input file
                                      - path to output file
                                      - the time frame for which this program is run (e.g.: week, 12 days, month, etc.)
                                      - threshold of request issued by a single user which are considered not noteworthy (must be int)
                                      - threshold of request targeting a single user which are considered not noteworthy (must be int)
                                      - [option for detailed output, if wished give "detailed" without quotation marks as last parameter]""");
                return;
            }

            inputPath = args[0];
            outputPath = args[1];
            timeFrame = args[2];
            threshold = Integer.parseInt(args[3]);
            multiuserThreshold = Integer.parseInt(args[4]);
            if(args.length >= 6 && args[6].compareToIgnoreCase("detailed") == 0){
                detailed = true;
            }

        } catch (NumberFormatException e){
            System.err.println("Threshold and multiuser threshold must be an int. Use -help for instructions.");
            return;
        } catch (ArrayIndexOutOfBoundsException e){
            System.err.println("Five Arguments required. Use -help for instructions.");
            return;
        }

        ArrayList<String[]> input = new ArrayList<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputPath));

            String line;
            while((line = reader.readLine()) != null){
                if(line.trim().isEmpty()) continue;
                input.add(line.split("\\{"));

                if (input.get(input.size()-1).length != 2 ){
                    System.err.println("""
                            The Input was not formatted correctly. Correct format is:
                            requestingUser{HTTPRequest
                            Requests and usernames must not contain {. Use -help for instructions.""");
                    return;
                }
            }
            reader.close();
        } catch (FileNotFoundException e){
            System.err.println("Input file not found. Please enter valid file path.");
            return;
        } catch (IOException e){
            e.printStackTrace();
        }

        //run of the InputAnalyzer Phase
        Map<String, RequestExtract> analyzerOutput = InputAnalyzer.analyzeInput(input,threshold);
        if(analyzerOutput == null){
            System.err.println("Unexpected error while interpreting input");
            return;
        }

        //run of the TextGenerator Phase
        Map<String, String> result = TextGenerator.generateTextFromExtracts(analyzerOutput,timeFrame,detailed,threshold,multiuserThreshold);


        //output formatting and writing
        try{
            FileWriter writer = new FileWriter(outputPath);
            if(result.isEmpty()){
                writer.write("There were no Results");
            } else {
                for (Map.Entry<String,String> e : result.entrySet()){
                    writer.write("""
                            ============================================================================================================================================================
                            """ + "                                                               " +  e.getKey() + "\n \n" +
                            e.getValue()+ """
                                                        
                            
                            """);
                }
                writer.write("============================================================================================================================================================");
            }
            writer.close();
        } catch (FileNotFoundException e){
            System.err.println("Output file not found. Please enter valid file path.");
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
