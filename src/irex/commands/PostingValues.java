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
public class PostingValues {

    long luceneDocid;
    long tf;

    public PostingValues() {
    }

    public PostingValues(long luceneDocid, long tf) {
        this.luceneDocid = luceneDocid;
        this.tf = tf;
    }

    @Override
    public String toString(){
        return luceneDocid + " " + tf;
    }
}

class sortByTermTF implements Comparator<PostingValues> {
    @Override
    public int compare(PostingValues a, PostingValues b) {
        return a.tf<b.tf?1:a.tf==b.tf?0:-1;
    }

}    
