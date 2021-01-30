import java.util.*;

public class TextGenerator {

    public static Map<String,String> generateTextFromExtracts(Map<String,RequestExtract> input, String timeframe, boolean detailed, int threshold, int multiuserThreshold){
        Map<String,String> res = new HashMap<>();
        for (Map.Entry<String,RequestExtract> entry : input.entrySet()){
            res.put(entry.getKey(), generateMessage(entry.getValue(),timeframe,detailed,threshold,multiuserThreshold));
        }
        return res;
    }

    private static String generateMessage(RequestExtract extract, String timeframe, boolean detailed, int threshold, int multiuserThreshold) {
        String res = "";
        if (extract.highRequesters.isEmpty() && extract.negatives.isEmpty() && extract.performance.isEmpty()){
            res = "There was no extraordinary data access recorded within the last " + timeframe + ".";
            if(detailed) res += " (" + extract.total + " accesses)";
            return  res;
        }

        boolean many = false;
        if (extract.uniqueUsers > multiuserThreshold){
            res = "Many people accessed your data within the last " + timeframe + ".";
            if (detailed) res += "(" + extract.uniqueUsers +  " people, " + extract.total + " accesses)";
            many = true;
        }

        boolean high = false;
        String[] highRequesterNames = extract.highRequesters.keySet().toArray(new String[0]);
        if (!extract.highRequesters.isEmpty()){
            high = true;
            if(many) res += " Specifically your "; else res += "Your ";
            res += "colleague";

            if (extract.highRequesters.size() == 1){
                res += " " + highRequesterNames[0];
            } else{
                res += "s";
                StringBuilder temp = new StringBuilder(); //String builder is preferable to simple + concatenation in loops
                for (int i = 0; i < extract.highRequesters.size() -2; i++){
                    temp.append(" ").append(highRequesterNames[i]).append(",");
                }
                res += temp.toString();
                res += " " + highRequesterNames[extract.highRequesters.size() -2] +  " and " + highRequesterNames[extract.highRequesters.size() -1];
            }
            res += " seemed very interested in ";

            if(extract.highRequesters.size() == 1){
                RequestExtract.Counter c = extract.highRequesters.entrySet().iterator().next().getValue();
                String temp = "";
                if (c.creator  > threshold) temp += "created and ";
                if (c.assignee > threshold) temp += "were working on and ";
                if (c.reporter > threshold) temp += "reported";

                if (temp.isEmpty()) temp = "your activity"; else res += "which issues you ";
                if (temp.substring(temp.length()-3).compareTo("nd ") == 0) temp = temp.substring(0,temp.length()-5);
                if (temp.length() > 32) temp = temp.replace("ed and ", "ed, ");
                res += temp;
            } else {res += " your activity";}

            if(!many) res += " over the last " + timeframe;
            res +=".";

            if (detailed){
                res += "\n";
                if (highRequesterNames.length == 1){
                    RequestExtract.Counter c = extract.highRequesters.get(highRequesterNames[0]);
                    res += "(" + c.assignee + " requests querying if you were the assignee on an issue, " + c.creator + " if you were the creator and "
                            + c.reporter + " if you were the reporter)";
                } else {
                    StringBuilder temp = new StringBuilder(); //String builder is preferable to simple + concatenation in loops
                    temp.append("(");
                    for (String user: highRequesterNames) {
                        RequestExtract.Counter c = extract.highRequesters.get(user);
                        temp.append(user).append(" made ")
                                .append(c.assignee).append(" requests  querying if you were the assignee of an issue, ")
                                .append(c.creator).append(" if you were the creator and ")
                                .append(c.reporter).append(" if you were the reporter,\n");
                    }
                    res += temp.toString();
                    res = res.substring(0, res.length()-2);
                    res += ")";
                }
                res += "\n";
            }
            res += "\n";
        }


        boolean neg = false;
        if (!extract.negatives.isEmpty()){
            neg = true;
            if ((high || many)) res += "Additionally your colleague"; else res += "Your colleague";

            String[] negRequesters = extract.negatives.keySet().toArray(new String[0]);
            List<Integer> l = new ArrayList<>();
            if(extract.negatives.size() == 1){
                l = extract.negatives.get(negRequesters[0]);
                res += " " + negRequesters[0];
            } else {
                res += "s";
                StringBuilder temp = new StringBuilder(); //String builder is preferable to simple + concatenation in loops
                for (int i = 0; i < extract.negatives.size() -2; i++){
                    temp.append(" ").append(negRequesters[i]).append(",");
                    l.addAll(extract.negatives.get(negRequesters[i]));
                }
                l.addAll(extract.negatives.get(negRequesters[extract.negatives.size() -2]));
                l.addAll(extract.negatives.get(negRequesters[extract.negatives.size() -1]));
                res += temp.toString();
                res += " " + negRequesters[extract.negatives.size() -2] +  " and " + negRequesters[extract.negatives.size() -1];
            }

            res += " made ";
            if(extract.negatives.size() == 1 && l.size() == 1) res += "a request"; else res += "requests";
            res += " from which they can deduce on which issues you were not the ";

            String temp = "";
            if (l.contains(0) || l.contains(3)) temp += "assignee or ";
            if (l.contains(1) || l.contains(4)) temp += "reporter or ";
            if (l.contains(2) || l.contains(5)) temp += "creator";

            if (temp.substring(temp.length()-3).compareTo("or ") == 0) temp = temp.substring(0,temp.length()-4);
            if (temp.length() > 29) temp = temp.replace("ee or ", "ee, ");
            res += temp;

            if(!many && !high) res += " over the last " + timeframe;
            res +=".";

            if (detailed){
                res += "\n";
                if (negRequesters.length == 1){
                    l = extract.negatives.get(negRequesters[0]);
                    res += "(" + l.stream().filter(i -> (i == 0 ||i ==3)).count() + " requests querying if you were no the assignee on an issue, "
                               + l.stream().filter(i -> (i == 1 ||i ==4)).count() + " if you were not the reporter and "
                               + l.stream().filter(i -> (i == 2 ||i ==5)).count() + " if you were not the creator)";
                } else {
                    StringBuilder temp2 = new StringBuilder(); //String builder is preferable to simple + concatenation in loops
                    temp2.append("(");
                    for (String user: negRequesters) {
                        l = extract.negatives.get(user);
                        temp2.append(user).append(" made ")
                                .append(l.stream().filter(i -> (i == 0 ||i ==3)).count()).append(" requests  querying if you were not the assignee of an issue, ")
                                .append(l.stream().filter(i -> (i == 1 ||i ==4)).count()).append(" if you were the not reporter and ")
                                .append(l.stream().filter(i -> (i == 2 ||i ==5)).count()).append(" if you were the not creator,\n");
                    }
                    res += temp2.toString();
                    res = res.substring(0, res.length()-2);
                    res += ")";
                }
                res += "\n";
            }
            res += "\n";
        }

        if(!extract.performance.isEmpty()){
            String[] respRequesters = extract.performance.keySet().toArray(new String[0]);
            if(extract.performance.size() == 1){
                res += "Your colleague " + respRequesters[0];
            }
            else res += "Some of your colleagues";

            if(many || high || neg) res += " also";

            res += " made";
            if(extract.performance.size() == 1 && extract.performance.get(respRequesters[0]).size() == 1) res += " a request"; else res += " requests";
            res += " which could be related to tracking your performance or responsibilities:\n";

            List<RequestExtract.Details> l = new ArrayList<>();
            if (extract.performance.size() == 1){
                l = extract.performance.get(respRequesters[0]);
                res += "They";

                if (l.stream().anyMatch(d -> (d.type == 6 || d.type == 7))){
                    String temp = " were interested in which issues you";
                    if (l.stream().anyMatch(d -> d.type == 6)) temp += " did";
                    if (l.stream().anyMatch(d -> d.type == 6) && l.stream().anyMatch(d -> d.type == 7)) temp += " and";
                    if (l.stream().anyMatch(d -> d.type == 7)) temp += " did not";
                    if (temp.substring(temp.length()-3).compareTo("did") == 0) temp = temp.replace("did", "resolved"); else temp += " resolve";
                    res += temp;
                }
                if (l.stream().anyMatch(d -> d.option != -1 && d.type != 6 && d.type != 7)){
                    if(l.stream().anyMatch(d -> (d.type == 6 || d.type == 7))) res += " and";
                    res += " checked if you were involved in an issue within a certain time frame.";
                }
            } else {
                List<String> resolved = new ArrayList<>(), notResolved = new ArrayList<>(), options = new ArrayList<>();
                for (String user : respRequesters){
                    l = extract.performance.get(user);
                    for (RequestExtract.Details d : l){
                        if (!resolved.contains(user) && d.type == 6) resolved.add(user);
                        if (!notResolved.contains(user) && d.type == 7) notResolved.add(user);
                        if (!options.contains(user) && d.option != -1 && d.type != 6 && d.type != 7) options.add(user);
                    }
                }

                StringBuilder temp = new StringBuilder(); //String builder is preferable to simple + concatenation in loops
                if (!resolved.isEmpty()){
                    if (resolved.size() != 1){
                        for (int i = 0; i < resolved.size() -2; i++){
                            temp.append(resolved.get(i)).append(", ");
                        }
                        res += temp.toString();
                        res += resolved.get(resolved.size()-2) +  " and " + resolved.get(resolved.size()-1);
                        res += " were interested in which issues you resolved";
                    }else {
                        res += resolved.get(0) + " was interested in which issued you resolved";
                    }
                }
                if (!notResolved.isEmpty()){
                    if (!resolved.isEmpty()) { if(options.isEmpty()) res += " and "; else res += ", ";}
                    temp = new StringBuilder();
                    if (notResolved.size() != 1){
                        for (int i = 0; i < notResolved.size() -2; i++){
                            temp.append(notResolved.get(i)).append(", ");
                        }
                        res += temp.toString();
                        res += notResolved.get(notResolved.size()-2) +  " and " + notResolved.get(notResolved.size()-1);
                        res += " were interested in which issues you did not resolve";
                    }else {
                        res += notResolved.get(0) + " was interested in which issued you did not resolve";
                    }
                }
                if (!options.isEmpty()){
                    if (!notResolved.isEmpty() && !resolved.isEmpty()) res += "\nand ";
                    else if (!(notResolved.isEmpty() && resolved.isEmpty())) res += " and ";
                    temp = new StringBuilder();
                    if (options.size() != 1){
                        for (int i = 0; i < options.size() -2; i++){
                            temp.append(options.get(i)).append(", ");
                        }
                        res += temp.toString();
                        res += options.get(options.size()-2) +  " and " + options.get(options.size()-1);
                        res += " checked if you were involved in an issue within a certain time frame.";
                    }else {
                        res += options.get(0) + " checked if you were involved in an issue within a certain time frame.";
                    }
                }
            }



            if (detailed){
                StringBuilder temp = new StringBuilder(); //String builder is preferable to simple + concatenation in loops
                temp.append("\n(") ;
                for(String user : respRequesters){
                    for (RequestExtract.Details d : l){
                        temp.append(user).append(" made ");
                        if (d.type == 6 || d.type == 7){
                            temp.append("a request for issues you ");
                            if(d.type == 6) temp.append("resolved"); else temp.append("did not resolve");
                        } else{
                            temp.append("a request for issues you in which you were the ").append(d.field);
                        }

                        if(d.option != -1){
                            String opt = "";
                            switch (d.option) {
                                case 0 -> {
                                    opt = " during the period of the ";
                                    d.date = d.date.substring(1, d.date.length() - 1);
                                    String[] dates = d.date.split(",");
                                    if(d.date.contains("\"")) opt += dates[0].substring(1, dates[0].length() - 1) + " and the " + dates[1].substring(1, dates[1].length() - 1);
                                    else opt += dates[0] + " and the " + dates[1];
                                }
                                case 1 -> {if(d.date.contains("\"")) d.date = d.date.substring(1, d.date.length() - 1); opt = " after the " + d.date;}
                                case 2 -> {if(d.date.contains("\"")) d.date = d.date.substring(1, d.date.length() - 1); opt = " on the " + d.date;}
                                case 3 -> {if(d.date.contains("\"")) d.date = d.date.substring(1, d.date.length() - 1); opt = " before the " + d.date;}
                            }
                            temp.append(opt);
                        }
                        temp.append(",\n");
                    }
                }
                temp.replace(temp.length()-2, temp.length(), "");
                temp.append(")");

                res += temp;
            }
        }



        return res;
    }
}
