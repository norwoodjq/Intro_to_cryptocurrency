import java.util.ArrayList;
// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    
    public ArrayList<BlockNode> blockNodeArray;
    private int maxHeight=0;
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
        BlockNode gnode = new BlockNode(genesisBlock);
        this.blockNodeArray.add(gnode);
        System.out.println("genesis ht: "+gnode.blockHeight);
    }
    
    private class BlockNode{
    	private Block block;
    	private int blockHeight;
    	private UTXOPool upool;
    	
    	public BlockNode(Block block){
    		this.block = block;
    		this.blockHeight = maxHeight;
    		this.upool = maxHeightPool;   		
    	}
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock(){
    	Block mHB = null;
        for (BlockNode bNode: blockNodeArray){
        	if (bNode.blockHeight==maxHeight){
        		return bNode.block;
        	}
        }
        return mHB;
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
    	boolean isValidBlock = false;
       	byte[] parentHash = block.getPrevBlockHash();
 //   	System.out.println("check for null parenthash: "+parentHash); 	
    	if (parentHash==null) return false; 			// rule 1
    	else{
    		for (BlockNode bnode : blockNodeArray){
    			if (bnode.block.getHash()==parentHash){
    				int blockHeight=bnode.blockHeight+1; 				
    				if (blockHeight>maxHeight - CUT_OFF_AGE){ // rule 2
    					maxHeightPool=bnode.upool;
    					
    					isValidBlock=true;
    					break;
    				}
    				else {
    					System.out.println("fails block height"+blockHeight);
    					return false;
    				}
    			}
    		}
    	}
       	Transaction[] btxs = block.getTransactions().toArray(new Transaction[0]);
       	for (Transaction btx: btxs){
       		System.out.println("block tx added to txPool: " +btx.getHash());
       		txPool.addTransaction(btx);  // adds block transactions to pool
       	}
       	Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
       	System.out.println("txs length: "+txs.length);
       	TxHandler handler = new TxHandler(maxHeightPool);
//    	for (Transaction tx: txs){
//    		System.out.println("valid tx?"+handler.isValidTx(tx)); 
//    	}
    	
    	Transaction[] rtxs = handler.handleTxs(txs);					// returns only valid tx set
    	System.out.println("rtxs length: "+rtxs.length);
    	if (!(rtxs.length==txs.length)){
    		try {
    			for (Transaction rtx: rtxs){
    				System.out.println("rtx hash"+rtx.getHash());
    				txPool.removeTransaction(rtx.getHash());
    			}
    		} catch (NullPointerException e) {
    			e.printStackTrace();
    		}
    		for (Transaction tx: txs){
    			System.out.println("tx hash"+tx.getHash());
    		}
    		System.out.println("invalid tx processed");
    		System.out.println("tx len:"+txs.length+" rtxs len:"+rtxs.length);
    		return false;  	
    	}
    	   	
    	for (Transaction rtx : rtxs){
    		try{
    			txPool.removeTransaction(rtx.getHash());
    		} catch (NullPointerException e){
    			System.out.println("null Pointer removing tx");
    		}
    	}

        if (isValidBlock){							
        	maxHeight++;
        	maxHeightPool = handler.getUTXOPool();
        	Transaction coinbase = block.getCoinbase();
   //     	System.out.println("coinbase"+coinbase.getHash());
            UTXO thisUTXO = new UTXO(coinbase.getHash(), 0);
            maxHeightPool.addUTXO(thisUTXO, coinbase.getOutput(0));
        	BlockNode bnode = new BlockNode(block);			// creates new BlockNode for valid block
        	maxHeightPool = bnode.upool;
        	blockNodeArray.add(bnode);
        	System.out.println("block added");
        	System.out.println("blockHash:" + block.getHash() + " prevHash:" + block.getPrevBlockHash());
        	System.out.println("block height:"+bnode.blockHeight);
        	return true;
        }             
        else {
        	System.out.println("failed for some other reason");
        	return false;
        }
    }   
    
    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
    } 
}