/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.util.Comparator;

/**
 *
 * @author Dwaipayan 
 */
public class TermStats {
    
    String term;

    long cf;
    long df;
    float idf;
    float tf;
    float collectionProbability;
    float score;

    public void setTerm(String term) {this.term = term;}
    public void setCF(long cf) {this.cf = cf;}
    public void setDF(long df) {this.df = df;}
    public void setIDF(float idf) {this.idf = idf;}
    public void setTF(float tf) {this.tf = tf;}
    public void setCollProba(float collProba) {this.collectionProbability = collProba;}
    public void setScore(float score) {this.score = score;}
}

class sortByTermScore implements Comparator<TermStats> {
    @Override
    public int compare(TermStats a, TermStats b) {
        return a.score<b.score?1:a.score==b.score?0:-1;
    }
}