// Example of a Simulation. This test runs the nodes on a random graph.
// At the end, it will print out the Transaction ids which each node
// believes consensus has been reached upon. You can use this simulation to
// test your nodes. You will want to try creating some deviant nodes and
// mixing them in the network to fully test.

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;

public class Simulation {

   public static void main(String[] args) {

      // There are four required command line arguments: p_graph (.1, .2, .3),
      // p_malicious (.15, .30, .45), p_txDistribution (.01, .05, .10), 
      // and numRounds (10, 20). You should try to test your CompliantNode
      // code for all 3x3x3x2 = 54 combinations.
      int numNodes = 100;
      double[] p_graph = {.1,.2,.3}; // parameter for random graph: prob. that an edge will exist
      double[] p_malicious = {.10,.30,.45}; // prob. that a node will be set to be malicious
      double[] p_txDistribution = {.01,.05,.10}; // probability of assigning an initial transaction to each node 
      int[] numRounds = {10,20}; // number of simulation rounds your nodes will run for
      int malnodecount;
      Set<Transaction> lastTxSet= new HashSet<Transaction>();
      
      try {
          FileWriter writer = new FileWriter("trial.csv", true);
          writer.write("p_graph, p_malicious, p_txDistribution, numRounds, agree,compliant nodes, %agree");
          writer.write("\r\n");   // write new line
          writer.close();
      } catch (IOException e) {
          e.printStackTrace();
      }

      
      // output header
//      System.out.println("p_graph, p_malicious, p_txDistribution, numRounds, numNodes, agree, compliant");
      for (int n = 0; n<3; n++){
    	  for (int p = 0; p<3; p++){
    		  for (int q=0; q<3; q++){
    			  for (int m=0; m<2; m++){

      // pick which nodes are malicious and which are compliant
      Node[] nodes = new Node[numNodes];
      malnodecount=0;
      for (int i = 0; i < numNodes; i++) {
         if(Math.random() < p_malicious[p]){
            // When you are ready to try testing with malicious nodes, replace the
            // instantiation below with an instantiation of a MaliciousNode
            nodes[i] = new MaliciousNode(p_graph[n], p_malicious[p], p_txDistribution[q], numRounds[m]);
            malnodecount++;
         }
         else
            nodes[i] = new CompliantNode(p_graph[n], p_malicious[p], p_txDistribution[q], numRounds[m]);
      }


      // initialize random follow graph
      boolean[][] followees = new boolean[numNodes][numNodes]; // followees[i][j] is true iff i follows j
      for (int i = 0; i < numNodes; i++) {
         for (int j = 0; j < numNodes; j++) {
            if (i == j) continue;
            if(Math.random() < p_graph[n]) { // p_graph is .1, .2, or .3
               followees[i][j] = true;
            }
         }
      }

      // notify all nodes of their followees
      for (int i = 0; i < numNodes; i++)
         nodes[i].setFollowees(followees[i]);

      // initialize a set of 500 valid Transactions with random ids
      int numTx = 500;
      HashSet<Integer> validTxIds = new HashSet<Integer>();
      Random random = new Random();
      for (int i = 0; i < numTx; i++) {
         int r = random.nextInt();
         validTxIds.add(r);
      }


      // distribute the 500 Transactions throughout the nodes, to initialize
      // the starting state of Transactions each node has heard. The distribution
      // is random with probability p_txDistribution for each Transaction-Node pair.
      for (int i = 0; i < numNodes; i++) {
         HashSet<Transaction> pendingTransactions = new HashSet<Transaction>();
         for(Integer txID : validTxIds) {
            if (Math.random() < p_txDistribution[q]) // p_txDistribution is .01, .05, or .10.
               pendingTransactions.add(new Transaction(txID));
         }
         nodes[i].setPendingTransaction(pendingTransactions);
      }


      // Simulate for numRounds times
      for (int round = 0; round < numRounds[m]; round++) { // numRounds is either 10 or 20

         // gather all the proposals into a map. The key is the index of the node receiving
         // proposals. The value is an ArrayList containing 1x2 Integer arrays. The first
         // element of each array is the id of the transaction being proposed and the second
         // element is the index # of the node proposing the transaction.
         HashMap<Integer, Set<Candidate>> allProposals = new HashMap<>();

         for (int i = 0; i < numNodes; i++) {
            Set<Transaction> proposals = nodes[i].sendToFollowers();
            for (Transaction tx : proposals) {
               if (!validTxIds.contains(tx.id))
                  continue; // ensure that each tx is actually valid

               for (int j = 0; j < numNodes; j++) {
                  if(!followees[j][i]) continue; // tx only matters if j follows i

                  if (!allProposals.containsKey(j)) {
                	  Set<Candidate> candidates = new HashSet<>();
                	  allProposals.put(j, candidates);
                  }
                  
                  Candidate candidate = new Candidate(tx, i);
                  allProposals.get(j).add(candidate);
               }

            }
         }

         // Distribute the Proposals to their intended recipients as Candidates
         for (int i = 0; i < numNodes; i++) {
            if (allProposals.containsKey(i))
               nodes[i].receiveFromFollowees(allProposals.get(i));
         }
      }
       
      
      // print results
      int conscnt = 0;
      for (int i = 0; i < numNodes; i++) {
         Set<Transaction> transactions = nodes[i].sendToFollowers();
 //        System.out.println("Transaction ids that Node " + i + " believes consensus on:");
 //        boolean agree = true;
         if (i==0) lastTxSet=transactions;
         if (transactions.equals(lastTxSet)) conscnt++;
         lastTxSet=transactions;
      	}
      int compnodes = numNodes-malnodecount;
      int percent_compliant=Math.floorDiv(100*conscnt, compnodes);
      try {
          FileWriter writer = new FileWriter("trial.csv", true);
          writer.write(p_graph[n]+", "+p_malicious[p]+", "+p_txDistribution[q]+", "+numRounds[m]+", "+ conscnt+", "+compnodes +", "+percent_compliant);
          writer.write("\r\n");   // write new line
          writer.close();
      } catch (IOException e) {
          e.printStackTrace();
      }
 //     System.out.println(p_graph[n]+", "+p_malicious[p]+", "+p_txDistribution[q]+", "+numRounds[m]+", "+ conscnt+", "+compnodes);
  	System.out.println("p_graph=" + p_graph[n]);
    		  } // n loop, p_graph
    		    	System.out.println("p_malicious=" + p_malicious[p]);
    	  } // p loop, p_malicious
    		  System.out.println("p_txDistribution");
      } // q loop, p_txDistribution
    	  System.out.println("numRounds");
   } // m loop, numRounds
      	
}
}

