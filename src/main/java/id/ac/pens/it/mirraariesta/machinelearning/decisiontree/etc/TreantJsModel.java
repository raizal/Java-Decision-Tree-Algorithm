/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package id.ac.pens.it.mirraariesta.machinelearning.decisiontree.etc;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mirra.Ariesta
 */
public class TreantJsModel {
    public static class Text{
        public String name;

        public Text(String name) {
            this.name = name;
        }
        
    }

    public TreantJsModel() {
    }

    
    public TreantJsModel(Text text) {
        this.text = text;
    }        
    
    public Text text;
    
    private List<TreantJsModel> children;
    
    public void add(TreantJsModel child){
        if(children==null)
            children = new ArrayList<>();
        children.add(child);
    }
}
