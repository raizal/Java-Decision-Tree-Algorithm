package id.ac.pens.it.mirraariesta.machinelearning.decisiontree;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

public class Main {

    public static void main(String[] args) {

        Spark.port(8080);

        Spark.staticFileLocation("/");
        
        Spark.get("/", new Route() {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception {
                return new VelocityTemplateEngine().render(new ModelAndView(new HashMap<Object, Object>(), "/index.html"));
            }
        });

        Spark.post("/tree", "*/*", new Route() {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception {

                String dataStr = rqst.queryParams("data").toLowerCase();
                List<List<String>> dataAsList = new ArrayList<>();
                for (String line : dataStr.split("\n")) {
                    if (dataAsList.size() == 0) {
                        line = line.toUpperCase();
                    }
                    List<String> currentRow = new ArrayList(Arrays.asList(line.split(",")));
                    dataAsList.add(currentRow);
                }
                List<String> attrsName = dataAsList.remove(0);
                attrsName.remove(attrsName.size() - 1);
                Map<String, Object> tree = Attribute.decisionTree(attrsName, dataAsList);
                
                rspns.type("application/json");
                String top = "";
                for(String key : tree.keySet()){
                    top = key;
                }
                
                return new Gson().toJson(Attribute.toTreantJS(top, tree.get(top)));
            }
        });

        Spark.post("/try", "*/*", new Route() {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception {
                String dataStr = rqst.queryParams("data").toLowerCase();
                String input = rqst.queryParams("input").toLowerCase();

                List<List<String>> dataAsList = new ArrayList<>();
                for (String line : dataStr.split("\n")) {
                    if (dataAsList.size() == 0) {
                        line = line.toUpperCase();
                    }
                    List<String> currentRow = new ArrayList(Arrays.asList(line.split(",")));
                    dataAsList.add(currentRow);
                }
                List<String> attrsName = dataAsList.remove(0);
                List<String> copyOfAttrsName = new ArrayList<>(attrsName);
                attrsName.remove(attrsName.size() - 1);
                Map<String, Object> tree = Attribute.decisionTree(attrsName, dataAsList);

                String jawaban = "";

                jawaban = Attribute.checkResult(tree, copyOfAttrsName, (List<String>) Arrays.asList(input.split(",")));

                rspns.type("text/html");

                return jawaban == null ? "" : jawaban;
            }
        });
    }
}
