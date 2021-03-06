import java.util.*;

public class InputAnalyzer {


    public static Map<String, RequestExtract> analyzeInput(List<String[]> input, int threshold){
        Map<String, RequestExtract> res = new HashMap<>();
        for (String[] req : input) {
            try {
                req[1] = req[1].split("jql=")[1].trim(); //cutting requests to only the JQL query
            } catch (ArrayIndexOutOfBoundsException e){
                System.err.println("All input requests must contain a JQL search query. A query must start with \"search?jql=\"");
                return null;
            }
            String[] query = req[1].split(" "); //query must contain spaces to separate operators from fields and values


            //checking for query options
            int options = switch (query[query.length - 2].toUpperCase(Locale.ROOT)) {
                case "DURING" -> 0;
                case "AFTER" -> 2;
                case "ON" -> 3;
                case "BEFORE" -> 4;
                default -> -1;
            };

            int type = -1;
            String requestedUser = "default";
            boolean multiple = false;

            //determining request type and user(s) targeted by search
            switch (query[1].toUpperCase(Locale.ROOT)){
                case "=":
                    type = 0;
                    requestedUser = query[2];
                    break;

                case "!=":
                    type = 1;
                    requestedUser = query[2];
                    break;

                case "IN":
                    type = 2;
                    requestedUser = query[2];
                    requestedUser = requestedUser.substring(1,requestedUser.length()-1);
                    if (requestedUser.contains(",")){
                        multiple = true;
                    }
                    break;

                case "NOT":
                    type = 3;
                    requestedUser = query[3];
                    requestedUser = requestedUser.substring(1,requestedUser.length()-1);
                    if (requestedUser.contains(",")){
                        multiple = true;
                    }
                    break;

                case "WAS":
                    if(query[2].compareToIgnoreCase("NOT") == 0){
                        if(query[3].compareToIgnoreCase("\"Resolved\"") == 0 || query[3].compareToIgnoreCase("Resolved") == 0){
                            requestedUser = query[5];
                            type = 7;
                        } else {
                            requestedUser = query[3];
                            type = 5;
                        }
                    } else if(query[2].compareToIgnoreCase("\"Resolved\"") == 0 || query[2].compareToIgnoreCase("Resolved") == 0){
                        requestedUser = query[4];
                        type = 6;
                    }else {
                        requestedUser = query[2];
                        type = 4;
                    }
                    break;
            }


            if(multiple){   //requests with multiple users must be parsed differently since the are in brackets: (user,user,user)
                String[] reqUsers = requestedUser.split(",");
                if(type == 3){
                    if(query[0].compareToIgnoreCase("reporter")== 0){type ++;}
                    if(query[0].compareToIgnoreCase("creator")== 0) {type += 2;}
                    for (String u : reqUsers){
                        RequestExtract extract = res.getOrDefault(u, new RequestExtract());

                        RequestExtract.Counter c = extract.highRequesters.getOrDefault(req[0], extract.new Counter());
                        extract.total ++;
                        if(type == 3){c.assignee++; }
                        else if(type == 4){c.reporter++;}
                        else{c.creator++;}
                        extract.highRequesters.putIfAbsent(req[0], c);

                        List<Integer> l = extract.negatives.getOrDefault(req[0], new ArrayList<>());
                        l.add(type);
                        extract.negatives.putIfAbsent(req[0], l);
                        res.putIfAbsent(u,extract);
                    }
                } else {
                    for (String u : reqUsers){
                        RequestExtract extract = res.getOrDefault(u, new RequestExtract());

                        RequestExtract.Counter c = extract.highRequesters.getOrDefault(req[0], extract.new Counter());
                        extract.total ++;
                        if(query[0].compareToIgnoreCase("assignee")==0){c.assignee++; }
                        else if(query[0].compareToIgnoreCase("reporter")== 0){c.reporter++;}
                        else{c.creator++;}
                        extract.highRequesters.putIfAbsent(req[0], c);
                        res.putIfAbsent(u,extract);
                    }
                }
            } else {
                RequestExtract extract = res.getOrDefault(requestedUser, new RequestExtract());
                if (options != -1 || type == 7 || type == 6){  //performance related request
                    extract.total++;
                    RequestExtract.Details d = extract.new Details();

                    RequestExtract.Counter c = extract.highRequesters.getOrDefault(req[0], extract.new Counter());
                    if(type == 7 || type == 6 || query[0].compareToIgnoreCase("assignee")==0){c.assignee++; d.field = "assignee";}
                    else if(query[0].compareToIgnoreCase("reporter")== 0){c.reporter++; d.field = "reporter";}
                    else{c.creator++; d.field = "creator";}
                    extract.highRequesters.putIfAbsent(req[0], c);

                    List<RequestExtract.Details> l = extract.performance.getOrDefault(req[0], new ArrayList<>());
                    d.option = options;
                    d.type = type;
                    if(options != -1){d.date = query[query.length-1];}
                    l.add(d);
                    extract.performance.putIfAbsent(req[0], l);
                } else {
                    RequestExtract.Counter c = extract.highRequesters.getOrDefault(req[0], extract.new Counter());
                    switch (type) {  //negative requests
                        case 5, 3, 1 -> {
                            extract.total++;
                            //for negative requests the type attribute is mapped to 0-5 to denote single/multiple people that were targeted and which field was targeted
                            // (see section 4.3.1, page 4, last paragraph)
                            if (type == 5 || type == 1) {
                                type = 0;
                            }
                            if (query[0].compareToIgnoreCase("assignee") == 0) {
                                c.assignee++;
                            }
                            else if (query[0].compareToIgnoreCase("reporter") == 0) {
                                c.reporter++;
                                type++;
                            } else {
                                c.creator++;
                                type += 2;
                            }
                            extract.highRequesters.putIfAbsent(req[0], c);
                            List<Integer> l = extract.negatives.getOrDefault(req[0], new ArrayList<>());
                            l.add(type);
                            extract.negatives.putIfAbsent(req[0], l);
                        }
                        case 0, 2, 4 -> {   //simple requests
                            extract.total++;
                            if (query[0].compareToIgnoreCase("assignee") == 0) {
                                c.assignee++;
                            }
                            else if (query[0].compareToIgnoreCase("reporter") == 0) {
                                c.reporter++;
                            } else {
                                c.creator++;
                            }
                            extract.highRequesters.putIfAbsent(req[0], c);
                        }
                    }
                }
                extract.uniqueUsers = extract.highRequesters.size();
                res.putIfAbsent(requestedUser,extract);
            }
        }

        //removing users from highRequesters attribute that did not issue enough queries to pass the threshold
        for (Map.Entry<String, RequestExtract> entry : res.entrySet()) {
            RequestExtract extract = res.get(entry.getKey());
            extract.highRequesters.entrySet().removeIf(ent -> ent.getValue().sum() <= threshold);
        }
        
        return res;
    }
}
