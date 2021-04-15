/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.util.Comparator;

/**
 *
 * @author Dwaipayan 
 */
public class TermStats {
    
    String term;

    long cf;
    long df;
    double idf;
    double tf;
    double collectionProbability;
    double score;

    public void setTerm(String term) {this.term = term;}
    public void setCF(long cf) {this.cf = cf;}
    public void setDF(long df) {this.df = df;}
    public void setIDF(double idf) {this.idf = idf;}
    public void setTF(double tf) {this.tf = tf;}
    public void setCollProba(double collProba) {this.collectionProbability = collProba;}
    public void setScore(double score) {this.score = score;}
}

class sortByTermScore implements Comparator<TermStats> {
    @Override
    public int compare(TermStats a, TermStats b) {
        return a.score<b.score?1:a.score==b.score?0:-1;
    }
}