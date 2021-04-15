/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dwaipayan 
 */
public class DocTermStat {

    String docid;
    int luceneDocid;

    double doclen;
    double avgdl;
    double docScore;

    List<TermStats> terms;

    public DocTermStat(String docid, int luceneDocid) {
        this.docid = docid;
        this.luceneDocid = luceneDocid;
        terms = new ArrayList<>();
    }

    public void setDocLen(double doclen) {this.doclen = doclen;}
    public void setAvgDocLen(double avgdl) {this.avgdl = avgdl;}
    
}
