import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestExtract {
    public class Counter {
        int assignee = 0;
        int creator = 0;
        int reporter = 0;

        public int sum(){
            return assignee + creator +reporter;
        }
    }

    public class Details {
        int type = -1;
        int option;
        String field = "";
        String date = "";
    }

    public int total, uniqueUsers;
    public Map<String, Counter> highRequesters;
    public Map<String, List<Integer>> negatives;
    public Map<String, List<Details>> performance;


    public RequestExtract() {
        total = 0;
        uniqueUsers = 0;
        this.highRequesters = new HashMap<>();
        this.negatives = new HashMap<>();
        this.performance = new HashMap<>();
    }
}
