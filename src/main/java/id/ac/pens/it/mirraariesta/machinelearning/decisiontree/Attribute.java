/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.ac.pens.it.mirraariesta.machinelearning.decisiontree;

import id.ac.pens.it.mirraariesta.machinelearning.decisiontree.etc.TreantJsModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mirra.Ariesta
 */
public class Attribute {
    
    public class Result {

        String key;
        int count = 0;

        public Result(String key) {
            this.key = key;
        }

        public void add() {
            count++;
        }
    }

    public static boolean DEBUG = false;

    String name;
    HashMap<String, List<Result>> rows = new HashMap<>();
    HashMap<String, List<List<String>>> rawData = new HashMap<>();

    public Attribute(String name) {
        this.name = name;
    }

    public static void initData(List<Attribute> attrs, List<List<String>> data) {
        for (List<String> d : data) {
            newData(attrs, d);
        }
    }

    public static void newData(List<Attribute> attrs, List<String> data) {
        //last index is result
        for (int i = 0; i < data.size() - 1; i++) {
            String curretCol = data.get(i);
            Attribute current = attrs.get(i);
            if (current == null) {
                continue;
            }
            current.add(curretCol, data.get(data.size() - 1));
            current.addRawData(curretCol, data);
        }
    }

    public static Attribute remove(List<Attribute> attrs, String name) {
        for (int i = 0; i < attrs.size(); i++) {
            if (attrs.get(i).name.equals(name)) {
                return attrs.remove(i);
            }
        }
        return null;
    }

    public static void reset(List<Attribute> attrs) {
        for (Attribute c : attrs) {
            c.rawData.clear();
            c.rows.clear();
        }
    }

    public void addRawData(String attrValue, List<String> data) {
        if (!rawData.containsKey(attrValue)) {
            rawData.put(attrValue, new ArrayList<>());
        }
        List<List<String>> rows = rawData.get(attrValue);

        if (rows == null) {
            rows = new ArrayList<>();
        }

        List<String> copyOfData = new ArrayList<>(data);

        copyOfData.remove(attrValue);

        rows.add(copyOfData);
    }

    public void add(String attrValue, String rowResult) {
        if (!rows.containsKey(attrValue)) {
            rows.put(attrValue, new ArrayList<>());
        }
        List<Result> results = rows.get(attrValue);
        Result r = get(rowResult, results);
        if (r == null) {
            r = new Result(rowResult);
            results.add(r);
        }
        r.add();
    }

    public static Result get(String key, List<Result> sets) {
        for (Result r : sets) {
            if (r.key.equals(key)) {
                return r;
            }
        }
        return null;
    }

    public double dataCount() {
        int total = 0;

        for (List<Result> data : rows.values()) {
            for (Result r : data) {
                total += r.count;
            }
        }

        return total;
    }

    public double sumKey(String name) {
        int total = 0;
        for (Result r : rows.get(name)) {
            total += r.count;
        }
        return total;
    }

    public double entropy() {
        double e = 0;
        double rowsCount = dataCount();
        for (String key : rows.keySet()) {
            e += ((sumKey(key) / rowsCount) * q(key));
        }
        return e;
    }

    public double q(String name) {
        double q = 0;
        double sumKey = sumKey(name);
        for (Result r : rows.get(name)) {
            q += (-xLog2x(r.count / sumKey));
        }
        return q;
    }

    public static double xLog2x(double x) {
        return x * logN(x, 2);
    }

    public String qPerKey() {
        String str = "";

        for (String key : rows.keySet()) {
            str += (key + " : " + q(key) + "\n");
        }

        return str;
    }

    public static Map<String, Object> decisionTree(List<String> attrsName, List<List<String>> dataAsList) {
        Map<String, Object> tree = new HashMap<>();

        List<Attribute> attrs = new ArrayList<>();
        for (String name : attrsName) {
            attrs.add(new Attribute(name));
        }

        Attribute.initData(attrs, dataAsList);

        List<Attribute> copyOfAttr = new ArrayList<>(attrs);

        List<Attribute> copyOfUnsortedAttr = new ArrayList<>(attrs);
        copyOfAttr.sort(Comparator.comparing(Attribute::entropy));

        if (DEBUG) {
            for (Attribute a : copyOfAttr) {
                System.out.println(a);
            }

            System.out.println("\n");
        }

        Attribute lowest = copyOfAttr.remove(0);
        attrs.remove(lowest.name);

        List<String> copyOfAttrName = new ArrayList<>(attrsName);

        copyOfAttrName.remove(lowest.name);

        tree.put(lowest.name, new ArrayList<>());

        for (String key : lowest.rows.keySet()) {
            List<Result> current = lowest.rows.get(key);
            if (current.size() == 1) {
                HashMap<String, String> r = new HashMap<>();
                r.put(key, current.get(0).key);
                if (DEBUG) {
                    System.out.println(lowest.name + " : " + key + "," + current.get(0).key);
                }
                ((ArrayList) tree.get(lowest.name)).add(r);
            } else {
                if (attrs.size() == 1) {
                    String rStr = "";
                    for (Result x : current) {
                        rStr += x.key;
                        if (current.indexOf(x) < current.size() - 1) {
                            rStr += "/";
                        }
                    }
                    HashMap<String, String> r = new HashMap<>();
                    r.put(key, rStr);
                    if (DEBUG) {
                        System.out.println(lowest.name + " : " + key + "," + rStr);
                    }
                    ((ArrayList) tree.get(lowest.name)).add(r);
                } else {
                    HashMap<String, Object> r = new HashMap<>();
                    try {
                        if (DEBUG) {
                            System.out.println(lowest.name + " : " + key + " ====");
                        }
                        r.put(key, decisionTree(copyOfAttrName, lowest.rawData.get(key)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ((ArrayList) tree.get(lowest.name)).add(r);
                }
            }
        }

        return tree;
    }

    public static String checkResult(Map<String, Object> tree, List<String> attributeNames, List<String> input) {
        for (String key : tree.keySet()) {
            if (attributeNames.indexOf(key) >= 0 || input.indexOf(key) >= 0) {
                if (tree.get(key) instanceof String) {
                    return (tree.get(key).toString());
                } else if (tree.get(key) instanceof Map) {
                    return checkResult((Map<String, Object>) tree.get(key), attributeNames, input);
                } else if (tree.get(key) instanceof List) {
                    for (Map map : ((List<HashMap>) tree.get(key))) {
                        String result = checkResult(map, attributeNames, input);
                        if (!result.equals("Undefined")) {
                            return result;
                        }
                    }
                }
            }
        }

        return "Undefined";
    }

    public static TreantJsModel toTreantJS(String head, Object values) {
        TreantJsModel model = new TreantJsModel(new TreantJsModel.Text(head));

        if (values instanceof String) {
            model.add(new TreantJsModel(new TreantJsModel.Text(values.toString())));
        } else if (values instanceof Map) {
            for (Object key : ((Map) values).keySet()) {
                model.add(toTreantJS(key.toString(), ((Map) values).get(key)));
            }
        } else if (values instanceof List) {
            for (Map map : ((List<Map>) values)) {
                for (Object key : map.keySet()) {
                    model.add(toTreantJS(key.toString(), map.get(key)));
                }
            }
        }

        return model;
    }

    private static boolean duplicateEdge(List<List> edges, List edge) {

        for (List e : edges) {
            if (e.get(0).equals(edge.get(0)) && e.get(1).equals(edge.get(1))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return name + " { E : " + entropy() + " }"; //To change body of generated methods, choose Tools | Templates.
    }
    
    public static double logN(double v1, double v2) {
        return Math.log(v1) / Math.log(v2);
    }
}
