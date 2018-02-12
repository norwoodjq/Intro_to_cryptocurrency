import java.util.ArrayList;
import java.security.*;

public class TxHandler{

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool utxoPoolCopy;
    public TxHandler(UTXOPool utxoPool) {
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
    public boolean isValidTx(Transaction tx) {
    	// this array used for check for duplicate inputs
    	ArrayList<UTXO> UTXOlist = new ArrayList<UTXO>();
    	double inputSum = 0;
    	double outputSum = 0;
        for (int i=0; i<tx.numInputs(); i++){
        	Transaction.Input input = tx.getInput(i);
        	if (input==null){
        		System.out.println("null input");
        		return false;
        	}
        	// verify #1 - check to see if tx is in the UTXO pool
        	UTXO thisUTXO = new UTXO(input.prevTxHash, input.outputIndex);
        	if (!utxoPoolCopy.contains(thisUTXO)){
         		System.out.println("input not in UTXO");
        		return false; 
         	}
        	// verify #2 - valid signature
        	Transaction.Output prevTxOutput = utxoPoolCopy.getTxOutput(thisUTXO);
        	PublicKey pk = prevTxOutput.address;
 //       	System.out.println("checking signature1");
 //       	System.out.println(input.outputIndex);
        	byte[] msg = tx.getRawDataToSign(i);
        	byte[] sig = input.signature;
        	if (!Crypto.verifySignature(pk, msg, sig)){
        		System.out.println("signature not valid");
        		return false;
        	}
        	inputSum+=prevTxOutput.value;    	
        	// verify #3 no UTXO is claimed more than once
        	if (UTXOlist.contains(thisUTXO)){
        		System.out.println("multiple claims on UTXO");
        		return false;
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
        		return false;
        	}
        	else outputSum+=tx.getOutput(i).value;
        }
        	// verify #5
        if (outputSum>inputSum){
        	System.out.println("output greater than input");
        	return false;
        }
        else { 
  //          System.out.println("valid TX");
        	return true;
        }
    }
    
    public UTXOPool getUTXOPool(){
    	return utxoPoolCopy;
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
    	boolean keepGoing = true;
    	while (keepGoing){
    		keepGoing = false;	// will turn on if another valid tx is added
    		for (int i=0; i<possibleTxs.length; i++){
    			Transaction Tx = possibleTxs[i];
    			if (isValidTx(Tx)){
   // 				System.out.println("valid TX among possible");
   // 				System.out.println(numValid);
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
    				keepGoing = true;  // valid tx found, so loop again
    			}
    		}
	//		System.out.print("final numvalid " + numValid + "\n");
    	}
 //   	Transaction[] validTxReturn = new Transaction[numValid];  // compacts array
 //   	for (int i=0; i<numValid; i++){
 //   		validTxReturn[i] = validTxArray[i];
 //   	}
        return validTxArray;
    } // end handleTxs
}	// end class
