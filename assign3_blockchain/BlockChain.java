import java.util.ArrayList;
// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    
    public ArrayList<BlockNode> blockNodeArray;
    private int maxHeight=0;
    private Block maxHeightBlock=null;
    private UTXOPool maxHeightPool;
    private TransactionPool txPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */    
    public BlockChain(Block genesisBlock) {
        this.maxHeightPool = new UTXOPool();
    	Transaction coinbase = genesisBlock.getCoinbase();
        UTXO thisUTXO = new UTXO(coinbase.getHash(), 0);
        this.maxHeightPool.addUTXO(thisUTXO, coinbase.getOutput(0));
      //System.out.println("coinbase value: "+ coinbase.getOutput(0).value);
        this.txPool = new TransactionPool();
        this.blockNodeArray = new ArrayList<BlockNode>();
        this.maxHeight = 1;
        this.maxHeightBlock = genesisBlock;
        BlockNode gnode = new BlockNode(genesisBlock);
        this.blockNodeArray.add(gnode);
        System.out.println("genesis ht: "+gnode.blockHeight);    
    }
    
    private class BlockNode{
    	private Block block;
    	private int blockHeight;
    	private UTXOPool upool;
    	
    	public BlockNode(Block block){
    		this.block = block;								// each block node is associated with a valid block
    		this.blockHeight = maxHeight;					// stores the current block height
    		this.upool = new UTXOPool(maxHeightPool);   	// saves a snapshot copy of the max height UTXOPool	to associate with the block
    	}
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock(){
        return maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return maxHeightPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
    	boolean valid = false;
    	boolean sameMax = false;
    	int blockHeight=0;
    	UTXOPool blockPool = new UTXOPool(); 	// makes a copy of the maxHeightPool
       	byte[] parentHash = block.getPrevBlockHash();
 //   	System.out.println("check for null parenthash: "+parentHash); 	
    	if (parentHash==null) return false; 			// rule 1
    	else{
    		for (BlockNode bnode : blockNodeArray){
    			System.out.println("parenthash:" +parentHash+ " bnode:" + bnode.block.getHash());
    			if (parentHash==bnode.block.getHash()){								// blocks parent must be in block node array (should be a hashmap or hashset?)
    				valid = true;
    				System.out.println("parent block height"+bnode.blockHeight);
    				blockHeight=bnode.blockHeight+1; 							// hypothetical block height for would-be node
    				if (blockHeight>(maxHeight - CUT_OFF_AGE)){ // rule 2
    					if (blockHeight==maxHeight){
    						sameMax=true;
    						blockPool=new UTXOPool(bnode.upool);  // overwrites maxHeightPool with parent UTXO pool	if another block is at same height
    					}
    					else blockPool=new UTXOPool(maxHeightPool);
    					break;	
    				}															// passes both checks so moving on...
    				else {
    					System.out.println("fails block height"+blockHeight);
    					return false;
    				} // end if - block height
    			} // end if - parent has block node
    		} //  end for loop through block nodes
    		if (!valid){
    			System.out.println("parentHash not found");
    			return false;
    		} // end if
    	}   // end else from null parent hash 	
    	
       	TxHandler handler = new TxHandler(blockPool);					// uses blockPool so as not to corrupt maxHeightPool if block is rejected
    //   	TxHandler handler = new TxHandler(maxHeightPool);
       	Transaction[] btxs = block.getTransactions().toArray(new Transaction[0]);
       	for (Transaction btx: btxs){
       		System.out.println("block tx added to txPool: " +btx.getHash());
       		txPool.addTransaction(btx);  // adds block transactions to pool
       	}  // end for
       	Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
 //      	System.out.println("txs length: "+txs.length);
//    	for (Transaction tx: txs){
//    		System.out.println("valid tx?"+handler.isValidTx(tx)); 
//    	}
  //     	System.out.println("txs length: "+txs.length);
    	Transaction[] rtxs = handler.handleTxs(txs);					// returns only valid tx set
    	System.out.println("rtxs length: "+rtxs.length);
    	if (rtxs.length!=txs.length){								// make sure all tx in pool are valid
    		System.out.println("invalid tx processed");
//    		System.out.println("tx len:"+txs.length+" rtxs len:"+rtxs.length);
    		return false;  												// last validity check!
    	}  // end if 
    	
    	// if it makes it this far, the block is valid so go ahead and add it to the blockchain
    //	System.out.println("rtxs.length:" +rtxs.length); 
    //	System.out.println("rtxs[0] hash:" +rtxs[0].getHash());  
    //	for (Transaction rtx : rtxs){			// clears the txPool by removing valid txs
    //		try{
    //			System.out.println("rtx hash:" +rtx.getHash());
    //			txPool.removeTransaction(rtx.getHash());
    //		} catch (NullPointerException e){
    //			System.out.println("null Pointer removing tx");
    //		} // end try/catch
    	
    //	}  // end for
    	txPool = new TransactionPool();
							
        maxHeight=blockHeight;						// this block goes on top
        if (!sameMax) maxHeightBlock = block;
        maxHeightPool = handler.getUTXOPool();		// overwrite the max Height UTXO pool with the blockPool
        Transaction coinbase = block.getCoinbase();
   //   System.out.println("coinbase"+coinbase.getHash());
        UTXO thisUTXO = new UTXO(coinbase.getHash(), 0);
        maxHeightPool.addUTXO(thisUTXO, coinbase.getOutput(0));  // add coinbase to blockchain UTXO pool, next block can spend it
        BlockNode bnode = new BlockNode(block);		// creates new BlockNode for valid block
        blockNodeArray.add(bnode);
        System.out.println("block added");
        System.out.println("blockHash:" + block.getHash() + " prevHash:" + block.getPrevBlockHash() + " block Height:" +bnode.blockHeight);
        System.out.println("maxHeight: "+maxHeight);
        return true;
    	}  // end of add block method
    
    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
    	TxHandler h = new TxHandler(maxHeightPool);
    	if (h.isValidTx(tx)){
    		txPool.addTransaction(tx);
    	} else System.out.println("invalid Tx");
    } 
}