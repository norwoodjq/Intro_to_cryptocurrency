import java.util.ArrayList;
import java.security.*;

public class MaxFeeTxHandler{

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool utxoPoolCopy;
    public MaxFeeTxHandler(UTXOPool utxoPool) {
    	this.utxoPoolCopy = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public double isValidTx(Transaction tx) {
    	// this array used for check for duplicate inputs
    	ArrayList<UTXO> UTXOlist = new ArrayList<UTXO>();
    	double inputSum = 0;
    	double outputSum = 0;
        for (int i=0; i<tx.numInputs(); i++){
        	Transaction.Input input = tx.getInput(i);
        	if (input==null){
  //      		System.out.println("null input");
        		return -1;
        	}
        	// verify #1 - check to see if tx is in the UTXO pool
        	UTXO thisUTXO = new UTXO(input.prevTxHash, input.outputIndex);
        	if (!utxoPoolCopy.contains(thisUTXO)){
  //       		System.out.println("input not in UTXO,,");
        		return -1; 
         	}
        	// verify #2 - valid signature
        	Transaction.Output prevTxOutput = utxoPoolCopy.getTxOutput(thisUTXO);
        	PublicKey pk = prevTxOutput.address;
 //       	System.out.println("checking signature1");
 //       	System.out.println(input.outputIndex);
        	byte[] msg = tx.getRawDataToSign(i);
        	byte[] sig = input.signature;
        	if (!Crypto.verifySignature(pk, msg, sig)){
  //      		System.out.println("signature not valid");
        		return -1;
        	}
        	inputSum+=prevTxOutput.value;    	
        	// verify #3 no UTXO is claimed more than once
        	if (UTXOlist.contains(thisUTXO)){
  //      		System.out.println("multiple claims on UTXO");
        		return -1;
        	}
        	UTXOlist.add(thisUTXO);	
        }
        for (int i=0; i<tx.numOutputs(); i++){
  //      	UTXO thisUTXO = new UTXO(tx.getHash(),i);
 //       	System.out.println(thisUTXO.getTxHash());
 //       	System.out.println(thisUTXO.getIndex());
        	// verify #4
        	if (tx.getOutput(i).value<0){
   //     		System.out.println("output value < 0");
        		return -1;
        	}
        	else outputSum+=tx.getOutput(i).value;
        }
        	// verify #5
        if (outputSum>inputSum){
        	System.out.println("output greater than input");
        	return -1;
        }
        else { 
  //          System.out.println("valid TX");
        	double fee = inputSum-outputSum;
        	return fee; 
        }
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	if (possibleTxs==null) return new Transaction[0];
    	Transaction[] validTxArray = new Transaction[possibleTxs.length];
    	int numValid = 0;
    	double maxFee = 0;
    	double[] txFee = new double[possibleTxs.length];
    	int maxFeeIndex;
    	while (maxFee>=0){
    		maxFeeIndex = 0;
    		maxFee = -1;	//  if -1 stays the maxFee, no more valid tx are left
    		for (int i=0; i<possibleTxs.length; i++){  // find tx fees for all transactions
    			Transaction Tx = possibleTxs[i];
    			txFee[i] = isValidTx(Tx);
        		if (txFee[i]>maxFee){					// cherry pick the tx with max fee
        			maxFee = txFee[i];
        			maxFeeIndex=i;
        		}
    		}
    		if (maxFee>=0){ // handle the tx with max fee
    			Transaction Tx = possibleTxs[maxFeeIndex];
    			for (int j=0; j<Tx.numInputs(); j++){  // trace input back to UTXO then remove it from pool
    					Transaction.Input input = Tx.getInput(j);
    					UTXO thisUTXO = new UTXO(input.prevTxHash, input.outputIndex);
    					utxoPoolCopy.removeUTXO(thisUTXO);
    				}
    				for (int k=0; k<Tx.numOutputs(); k++){  // add outputs to UTXO pool
    					Transaction.Output output = Tx.getOutput(k);
    					UTXO newUTXO = new UTXO(Tx.getHash(), k);
    					utxoPoolCopy.addUTXO(newUTXO, output); 
    				}
    				validTxArray[numValid]=Tx;	// add to the list of valid txs found
    				numValid++;
    		} // end of valid tx handling
    	} // end of while loop
	//		System.out.print("final numvalid " + numValid + "\n");
 //   	Transaction[] validTxReturn = new Transaction[numValid];  // compacts array
 //   	for (int i=0; i<numValid; i++){
 //   		validTxReturn[i] = validTxArray[i];
 //   	}
        return validTxArray;
    } // end handleTxs
}	// end class
