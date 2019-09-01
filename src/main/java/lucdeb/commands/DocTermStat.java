/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dwaipayan 
 */
public class DocTermStat {

    String docid;
    int luceneDocid;

    float doclen;
    float avgdl;
    float docScore;

    List<TermStats> terms;

    public DocTermStat(String docid, int luceneDocid) {
        this.docid = docid;
        this.luceneDocid = luceneDocid;
        terms = new ArrayList<>();
    }

    public void setDocLen(float doclen) {this.doclen = doclen;}
    public void setAvgDocLen(float avgdl) {this.avgdl = avgdl;}
    
}
