import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
/*
 * Cases:
 * 0 - Nodes not communicating at all
 * 1 - Nodes communicating only its own initial transactions
 * 2 - Nodes communicating transactions randomly
 * 3 - Nodes communicating only at the final round
 * 4 - Nodes communicating only at even or odd rounds
 */

public class MaliciousNode implements Node {
	private int malform;
	private Set<Transaction> pendingTransactions;

    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
		Random random = new Random();
    	this.malform = random.nextInt(4);
    	this.pendingTransactions = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        return;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
    	this.pendingTransactions=pendingTransactions;
        return;
    }

    public Set<Transaction> sendToFollowers() {
    	if (this.malform==4){
    		return this.pendingTransactions;
    	}
        return new HashSet<Transaction>();
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	if(this.malform==4||this.malform==2){
    		for (Candidate c: candidates){
    			this.pendingTransactions.add(c.tx);
    		}
    	}
        return;
    }
}
