import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	
	private double p_graph;
	private double p_malicious;
	private double p_txDistribution;
	private int numRounds;
	private int counter;
	
	private boolean[] followees;
	private boolean[] trusted;
	private Set<Transaction> pendingTransactions;
	private HashMap<Transaction, Integer> txcount;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution= p_txDistribution;
        this.numRounds=numRounds;
        this.txcount = new HashMap<Transaction, Integer>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        this.trusted = new boolean[followees.length];
        Arrays.fill(trusted, true);  // benefit of the doubt to start
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions=pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        return this.pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	// create a new set of pending transactions from transactions
    	// received from followees

    	Set<Transaction> goodTx = new HashSet<Transaction>();
    	counter+=1; 
    	for (Candidate c: candidates){
    		int cnt=0;
 //  maybe try adding a check to see if random followees are in the set of candidate.senders
 //   		Random random = new Random();
 //   		int randi = random.nextInt(followees.length-1);
  //  		while(!followees[randi]){
  //  			randi = random.nextInt();
   // 		}
    		if (!txcount.containsKey(c.tx)){
    			txcount.put(c.tx, 1);
    		}
    		else {
    			cnt = txcount.get(c.tx)+1;
    			txcount.put(c.tx, cnt);
    		}
    	}
    	for (Transaction tx: txcount.keySet()){
//    		if (counter<2&&txcount.get(tx)>2) {
 //   		System.out.println("counter:"+counter+ "txcount:"+txcount.get(tx));
    		if (counter<4){
   // 			System.out.println("counter:"+counter+ "txcount:"+txcount.get(tx));
    			goodTx.add(tx);
    		}
    		else if (counter>3&&txcount.get(tx)>counter){
   		//	    		System.out.println("counter:"+counter+ "txcount:"+txcount.get(tx));
    			goodTx.add(tx);
    		}
//    		else if ((txcount.get(tx)>9*(counter/10))){
    		else {
    			txcount.put(tx,0);
    		}
    }
    }
}
    
