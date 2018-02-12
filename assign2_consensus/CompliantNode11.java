import java.util.ArrayList;
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
	private Set<Transaction> pendingTransactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution= p_txDistribution;
        this.numRounds=numRounds;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions=pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        return this.pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	// create a new set of pending transactions from transactions
    	// received from followee
    	this.counter+=1;
    	int numTx=0;
    	int randex = 0;
    	Candidate test = null;
    	Random random = new Random();
    	try {
    		randex = random.nextInt()%(this.pendingTransactions.size()-1);
    	} catch (java.lang.ArithmeticException e){
    		return;
    	}
    	Set<Transaction> goodTx = new HashSet<Transaction>();
        for (Candidate c: candidates){
        	if(followees[c.sender]){
        		goodTx.add(c.tx);
        		if (numTx==randex){
        			test = c;  // pick a random node
        			numTx+=1;
        		}
        	}
        }
        if (!goodTx.isEmpty()){ // do not forward empty tx list
        	if (this.counter>1){  // skip this check the first go round
        			if (this.pendingTransactions.contains(test)){ // check to see if random node is there
        				setPendingTransaction(goodTx);
        			}
        	}
        	else {
        		setPendingTransaction(goodTx);
        	}
        }	
        }
    }
