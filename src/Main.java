import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class Main {
    public static void main(String[] args){
        String inputPath, outputPath, timeFrame;
        int threshold, multiuserThreshold;
        boolean detailed = false;
        try {
            if(args[1].compareToIgnoreCase("-help") == 0){
                System.out.println("TODO help");
                return;
            }

            inputPath = args[1];
            outputPath = args[2];
            timeFrame = args[3];
            threshold = Integer.parseInt(args[4]);
            multiuserThreshold = Integer.parseInt(args[5]);
            if(args.length >= 7){
                detailed = Boolean.parseBoolean(args[6]);
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

        Map<String, RequestExtract> analyzerOutput = InputAnalyzer.analyzeInput(input,threshold);
        if(analyzerOutput == null){
            System.err.println("Unexpected error while interpreting input");
            return;
        }
        Map<String, String> result = TextGenerator.generateTextFromExtracts(analyzerOutput,timeFrame,detailed,threshold,multiuserThreshold);


        try{
            FileWriter writer = new FileWriter(outputPath);
            if(result.isEmpty()){
                writer.write("There were no Results");
            } else {
                for (Map.Entry<String,String> e : result.entrySet()){
                    writer.write("""
                            ==============================================================================
                                                       """ + e.getKey() + "\n \n" +
                            e.getValue()+ """
                            
                            ==============================================================================
                            
                            """);
                }
            }
            writer.close();
        } catch (FileNotFoundException e){
            System.err.println("Output file not found. Please enter valid file path.");
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
