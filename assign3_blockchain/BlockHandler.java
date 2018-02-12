
import java.util.ArrayList;
import java.util.Arrays;

import DropboxTestBlockChain.ForwardBlockNode;

import java.io.*;
import java.security.*;

public class BlockHandler {
    private BlockChain blockChain;

    /** assume blockChain has the genesis block */
    public BlockHandler(BlockChain blockChain) {
        this.blockChain = blockChain;
    }

    /**
     * add {@code block} to the block chain if it is valid.
     * 
     * @return true if the block is valid and has been added, false otherwise
     */
    public boolean processBlock(Block block) {
    	
        if (block == null){
        	System.out.println("null block not processed");
            return false;
        }
        System.out.println("process block");
        return blockChain.addBlock(block);
    }

    /** create a new {@code block} over the max height {@code block} */
    public Block createBlock(PublicKey myAddress) {
    	System.out.println("create block");
        Block parent = blockChain.getMaxHeightBlock();   	
    	byte[] parentHash=null;
    	try {
    		parentHash = parent.getHash();
    //		System.out.println("parent hash:"+parentHash);
    	}
    	catch (NullPointerException e){
    	}
 //   	System.out.println("create new block with parent hash");
        Block current = new Block(parentHash, myAddress);
 //       System.out.println("create new UTXO pool");
        UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
 //   	System.out.println("get tx pool");
        TransactionPool txPool = blockChain.getTransactionPool();
   // 	System.out.println("create tx handler");
        TxHandler handler = new TxHandler(uPool);
  //  	System.out.println("get tx from pool");
        Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
    	System.out.println("get rtxs from pool");
        Transaction[] rTxs = handler.handleTxs(txs);
    	System.out.println("loop to add rtx to current");
        for (int i = 0; i < rTxs.length; i++)
            current.addTransaction(rTxs[i]);
   // 	System.out.println("finalize current");
        current.finalize();
    	System.out.println("add block to blockchain");
        if (blockChain.addBlock(current)){
  //      	System.out.println("block added successfully");
            return current;
        }
        else {
        	System.out.println("block not added");
            return null;
        }
    }

    /** process a {@code Transaction} */
    public void processTx(Transaction tx) {
        blockChain.addTransaction(tx);
    }   
    
    public int test15(BlockChain blockChain, BlockHandler blockHandler, Block genesisBlock, ArrayList<KeyPair> people) {
        System.out.println("Process a block containing a transaction that claims a UTXO already claimed by a transaction in its parent");
       
 //       BlockChain blockChain = new BlockChain(genesisBlock);
 //       BlockHandler blockHandler = new BlockHandler(blockChain);
        
        boolean passes = true;
        
        Block block = new Block(genesisBlock.getHash(), people.get(1).getPublic());
        Transaction spendCoinbaseTx = new Transaction();
        spendCoinbaseTx.addInput(genesisBlock.getCoinbase().getHash(), 0);
        spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
   //     
        PrivateKey privkey = people.get(0).getPrivate();
        byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
        byte[] signature = null;
        Signature sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.initSign(privkey);
        } catch (InvalidKeyException e) {
        	e.printStackTrace();
        }
        try {
            sig.update(dataToSign);
            signature = sig.sign(); 
        } catch (SignatureException e) {
        	e.printStackTrace();
        }  

		spendCoinbaseTx.addSignature(signature, 0); // dataToSign is not byte[] signature             
       // privkey.sign(dataToSign) =  people.get(0).getPrivate().sign(spendCoinbaseTx.getRawDataToSign(0)), 0
      // privkey.sign(dataToSign) = people.get(0).getPrivate().sign(dataToSign) = people.get(0).getPrivate().sign(spendCoinbaseTx.getRawDataToSign(0))
      //  spendCoinbaseTx.addSignature(people.get(0).getPrivate().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
        spendCoinbaseTx.finalize();
        block.addTransaction(spendCoinbaseTx);
  //      System.out.println("here!!  1  !!!");
        block.finalize();

        
        passes = passes && blockHandler.processBlock(block);
        if (passes){
        	System.out.println("passes test 15 first part");
        } else System.out.println("fails test 15 first part");
        
        Block prevBlock = block;
        
        block = new Block(prevBlock.getHash(), people.get(2).getPublic());
        spendCoinbaseTx = new Transaction();
        spendCoinbaseTx.addInput(genesisBlock.getCoinbase().getHash(), 0);
        spendCoinbaseTx.addOutput(Block.COINBASE - 1, people.get(2).getPublic()); 
        
        dataToSign = spendCoinbaseTx.getRawDataToSign(0);
        try {
            sig.update(dataToSign);
            signature = sig.sign(); 
        } catch (SignatureException e) {
        	e.printStackTrace();
        } 
       
        spendCoinbaseTx.addSignature(signature, 0);     
    //    spendCoinbaseTx.addSignature(people.get(1).getPrivate().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
        spendCoinbaseTx.finalize();
        block.addTransaction(spendCoinbaseTx);
        block.finalize();
        System.out.println("2nd part: "+ blockHandler.processBlock(block));
        
    //    passes = passes && !blockHandler.processBlock(block);
        
        if (passes){
        	System.out.println("passes test 15 second part");
        } else System.out.println("fails test 15 second part");
        
        int val = 0;
        return val;
     }
    
    public int test25(BlockChain bc, BlockHandler bh, Block genesisBlock, ArrayList<KeyPair> people) {
        System.out.println("Process a transaction, create a block, then process a block on top of the genesis block with a transaction claiming a UTXO from that transaction");
        
  //      BlockChain blockChain = new BlockChain(genesisBlock);
  //      BlockHandler blockHandler = new BlockHandler(blockChain);
        
        Transaction spendCoinbaseTx = new Transaction();
        System.out.println("add input to coinbase tx");
        spendCoinbaseTx.addInput(genesisBlock.getCoinbase().getHash(), 0);
        System.out.println("add output to coinbase tx"+genesisBlock.getCoinbase().getHash());
        spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
        PrivateKey privkey = people.get(0).getPrivate();
        byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
        byte[] signature = null;
        Signature sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.initSign(privkey);
        } catch (InvalidKeyException e) {
        	e.printStackTrace();
        }
        try {
            sig.update(dataToSign);
            signature = sig.sign(); 
        } catch (SignatureException e) {
        	e.printStackTrace();
        } 
        
        spendCoinbaseTx.addSignature(signature, 0);
     //   spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
        spendCoinbaseTx.finalize();
        System.out.println("spend coinbase tx");
        bh.processTx(spendCoinbaseTx);
        System.out.println("coinbase added to tx pool!!!!");
        Block createdBlock = bh.createBlock(people.get(1).getPublic());
        System.out.println("new block created");
        Block newBlock = new Block(genesisBlock.getHash(), people.get(1).getPublic());
        Transaction spendTx = new Transaction();
        System.out.println("add input to coinbase tx in new block");
        spendTx.addInput(spendCoinbaseTx.getHash(), 0);
        System.out.println("add output to coinbase tx");
        spendTx.addOutput(Block.COINBASE, people.get(2).getPublic());
        
        PrivateKey privkey2 = people.get(0).getPrivate();
        dataToSign = spendCoinbaseTx.getRawDataToSign(0);
        signature = null;
        sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.initSign(privkey2);
        } catch (InvalidKeyException e) {
        	e.printStackTrace();
        }
        try {
            sig.update(dataToSign);
            signature = sig.sign(); 
        } catch (SignatureException e) {
        	e.printStackTrace();
        } 
        
        
        spendTx.addSignature(signature, 0);
        spendTx.finalize();
        newBlock.addTransaction(spendTx);
        newBlock.finalize();
        System.out.println(" createdBlock!null:"+createdBlock);
        System.out.println(" prevhash==genblockhash:"+(createdBlock.getPrevBlockHash().equals(genesisBlock.getHash())));
        System.out.println(" tx.size:"+createdBlock.getTransactions().size());
        System.out.println(" tx(0)==spendcoinbase:"+createdBlock.getTransaction(0).equals(spendCoinbaseTx) + " blockhandler.processBlock(newBlock):" +bh.processBlock(newBlock));
        int val = 0;
        return val;
 //       return UtilCOS.printPassFail(createdBlock != null && createdBlock.getPrevBlockHash().equals(genesisBlock.getHash()) && createdBlock.getTransactions().size() == 1 && createdBlock.getTransaction(0).equals(spendCoinbaseTx) && !blockHandler.processBlock(newBlock));
     }
    
    public int test18(BlockChain blockChain, BlockHandler blockHandler, Block genesisBlock, ArrayList<KeyPair> people) {
        System.out.println("Process multiple blocks directly on top of the genesis block, then create a block");
        
     //   Block genesisBlock = new Block(null, people.get(0).getPublic());
     //   genesisBlock.finalize();
        
     //   BlockChain blockChain = new BlockChain(genesisBlock);
     //   BlockHandler blockHandler = new BlockHandler(blockChain);
        
        boolean passes = true;
        Block block;
        Block firstBlock = null;
        Transaction spendCoinbaseTx;
        
        for (int i = 0; i < 5; i++) {
        	// genesis bock is parent, coinbase goes to people(1)
           block = new Block(genesisBlock.getHash(), people.get(1).getPublic());
           if (i == 0)
              firstBlock = block;
           spendCoinbaseTx = new Transaction();
     //     System.out.println("spencoinbase add input");
            spendCoinbaseTx.addInput(genesisBlock.getCoinbase().getHash(), 0);
      //     System.out.println("spencoinbase add output");
           spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
           //

           
           byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
           byte[] signature = null;
           
           PrivateKey privkey = people.get(0).getPrivate();
           Signature sig = null;
           try {
           	sig = Signature.getInstance("SHA256withRSA");

           } catch (NoSuchAlgorithmException e) {
               e.printStackTrace();
           }
           try {
               sig.initSign(privkey);
           } catch (InvalidKeyException e) {
           	e.printStackTrace();
           }
           try {
               sig.update(dataToSign);
               signature = sig.sign(); 
           } catch (SignatureException e) {
           	e.printStackTrace();
           }         
           spendCoinbaseTx.addSignature(signature, 0);   
           //
     //      spendCoinbaseTx.addSignature(people.get(0).getPrivate().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
           spendCoinbaseTx.finalize();
     //      System.out.println("spencoinbase add spendcoinbase");
           block.addTransaction(spendCoinbaseTx);
           block.finalize();
           passes = passes && blockHandler.processBlock(block);
        }
        Block createdBlock = blockHandler.createBlock(people.get(1).getPublic());
        System.out.println("passes:"+passes);
        System.out.println("createdBlock!=null:"+createdBlock);
        System.out.println("createdBlockgetPrevBlockHash==firstBlockGetHash:"+createdBlock.getPrevBlockHash().equals(firstBlock.getHash()));
        System.out.println("createdBlock.getTransactions().size()==0: "+ (createdBlock.getTransactions().size() == 0));   
        int val=0;
        return val;
    //    return UtilCOS.printPassFail(createdBlock != null && createdBlock.getPrevBlockHash().equals(firstBlock.getHash()) && createdBlock.getTransactions().size() == 0);
     }
    
    public void test23(BlockChain blockChain, BlockHandler blockHandler, Block genesisBlock, ArrayList<KeyPair> people) {
        System.out.println("Process a transaction, create a block, process a transaction, create a block, ...");
   
        boolean passes = true;
        Transaction spendCoinbaseTx=null;
        Block prevBlock = genesisBlock;
        Block createdBlock=null;
        
        for (int i = 0; i < 3; i++) {
           spendCoinbaseTx = new Transaction();
           spendCoinbaseTx.addInput(prevBlock.getCoinbase().getHash(), 0);
           spendCoinbaseTx.addOutput(Block.COINBASE, people.get(0).getPublic());
           
           byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
           byte[] signature = null;
           PrivateKey privkey = people.get(0).getPrivate();
           Signature sig = null;
           try {
           	sig = Signature.getInstance("SHA256withRSA");

           } catch (NoSuchAlgorithmException e) {
               e.printStackTrace();
           }
           try {
               sig.initSign(privkey);
           } catch (InvalidKeyException e) {
           	e.printStackTrace();
           }
           try {
               sig.update(dataToSign);
               signature = sig.sign(); 
           } catch (SignatureException e) {
           	e.printStackTrace();
           }         
           spendCoinbaseTx.addSignature(signature, 0);           
           
     //      spendCoinbaseTx.addSignature(people.get(0).getPrivate().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
           spendCoinbaseTx.finalize();
      //     System.out.println("process spend coinbase TX");
           blockHandler.processTx(spendCoinbaseTx);
    //       System.out.println("here!!!!!!!!!!!!!!!");
           createdBlock = blockHandler.createBlock(people.get(0).getPublic());
           
      //     passes = passes && createdBlock != null && createdBlock.getPrevBlockHash().equals(prevBlock.getHash()) && createdBlock.getTransactions().size() == 1 && createdBlock.getTransaction(0).equals(spendCoinbaseTx);
 
           prevBlock = createdBlock;          
        }
        System.out.println("passes:" +passes);
        System.out.println("created block!=null:" +createdBlock);
        System.out.println("createdblock prv block hash==prev block hash: " +createdBlock.getPrevBlockHash().equals(prevBlock.getHash()));
        System.out.println("created block prev block hash:" +createdBlock.getPrevBlockHash() + " previous block hash:" + prevBlock.getHash());
        System.out.println("created block tx size==1: "+ (createdBlock.getTransactions().size() == 1));
        System.out.println("created block tx equals spend coinbase tx: " +createdBlock.getTransaction(0).equals(spendCoinbaseTx));  
     }
    
    public void test19(BlockChain blockChain, BlockHandler blockHandler, Block genesisBlock, ArrayList<KeyPair> people) {
        System.out.println("Create a block after a valid transaction has been processed that is already in a block in the longest valid branch");
              
        boolean passes = true;
        
        Block block = new Block(genesisBlock.getHash(), people.get(1).getPublic());
        
        Transaction spendCoinbaseTx = new Transaction();
        spendCoinbaseTx.addInput(genesisBlock.getCoinbase().getHash(), 0);
        spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
        
        byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
        byte[] signature = null;
        PrivateKey privkey = people.get(0).getPrivate();
        Signature sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.initSign(privkey);
        } catch (InvalidKeyException e) {
        	e.printStackTrace();
        }
        try {
            sig.update(dataToSign);
            signature = sig.sign(); 
        } catch (SignatureException e) {
        	e.printStackTrace();
        }         
        spendCoinbaseTx.addSignature(signature, 0);
            
        spendCoinbaseTx.finalize();
        block.addTransaction(spendCoinbaseTx);
        block.finalize();
        
        passes = passes && blockHandler.processBlock(block);
        
          blockHandler.processTx(spendCoinbaseTx);
   //     System.out.println("people(1)public:"+ people.get(1).getPublic() );
        Block createdBlock = blockHandler.createBlock(people.get(1).getPublic());
        System.out.println("passes:" +passes);
  //      System.out.println("createdBlock!null:" +createdBlock);
  //      System.out.println("prvBH==block.gH:" +createdBlock.getPrevBlockHash().equals(block.getHash()));
  //      System.out.println("no tx in created block==0:" +(createdBlock.getTransactions().size() == 0));
     }
    
    private class ForwardBlockNode {
        public Block b;
        public ForwardBlockNode child;
        
        public ForwardBlockNode(Block b) {
           this.b = b;
           this.child = null;
        }
        
        public void setChild(ForwardBlockNode child) {
           this.child = child;
        }
     }
     
     
    public void test27(BlockChain blockChain, BlockHandler blockHandler, Block genesisBlock, ArrayList<KeyPair> people) {
        System.out.println("Similar to previous test, but then try to process blocks whose parents are at height < maxHeight - CUT_OFF_AGE");
        
        boolean passes = true;
        boolean flipped = false;
        Block block;
        Block firstBranchPrevBlock = genesisBlock;
        ForwardBlockNode firstBranch = new ForwardBlockNode(firstBranchPrevBlock);
        ForwardBlockNode firstBranchTracker = firstBranch;
        Block secondBranchPrevBlock = genesisBlock;
        ForwardBlockNode secondBranch = new ForwardBlockNode(secondBranchPrevBlock);
        ForwardBlockNode secondBranchTracker = secondBranch;
        Transaction spendCoinbaseTx;
        
        for (int i = 0; i < 3*BlockChain.CUT_OFF_AGE; i++) {
           spendCoinbaseTx = new Transaction();
           if (i % 2 == 0) {
              if (!flipped) {
                 spendCoinbaseTx.addInput(firstBranchPrevBlock.getCoinbase().getHash(), 0);
                 spendCoinbaseTx.addOutput(Block.COINBASE, people.get(0).getPublic());
                 
                 byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                 byte[] signature = null;
                 PrivateKey privkey = people.get(0).getPrivate();
                 Signature sig = null;
                 try {
                 	sig = Signature.getInstance("SHA256withRSA");

                 } catch (NoSuchAlgorithmException e) {
                     e.printStackTrace();
                 }
                 try {
                     sig.initSign(privkey);
                 } catch (InvalidKeyException e) {
                 	e.printStackTrace();
                 }
                 try {
                     sig.update(dataToSign);
                     signature = sig.sign(); 
                 } catch (SignatureException e) {
                 	e.printStackTrace();
                 }         
                 spendCoinbaseTx.addSignature(signature, 0); 
                   
                 
            //     spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                 spendCoinbaseTx.finalize();
                 blockHandler.processTx(spendCoinbaseTx);
                 
                 block = blockHandler.createBlock(people.get(0).getPublic());
                 
                 passes = passes && block != null && block.getPrevBlockHash().equals(firstBranchPrevBlock.getHash()) && block.getTransactions().size() == 1 && block.getTransaction(0).equals(spendCoinbaseTx);
                 ForwardBlockNode newNode = new ForwardBlockNode(block);
                 firstBranchTracker.setChild(newNode);
                 firstBranchTracker = newNode;
                 firstBranchPrevBlock = block;
              } else {
                 spendCoinbaseTx.addInput(secondBranchPrevBlock.getCoinbase().getHash(), 0);
                 spendCoinbaseTx.addOutput(Block.COINBASE, people.get(0).getPublic());
           
                 byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                 byte[] signature = null;
                 Signature sig = null;
                 PrivateKey privkey = people.get(0).getPrivate();
                 try {
                 	sig = Signature.getInstance("SHA256withRSA");

                 } catch (NoSuchAlgorithmException e) {
                     e.printStackTrace();
                 }
                 try {
                     sig.initSign(privkey);
                 } catch (InvalidKeyException e) {
                 	e.printStackTrace();
                 }
                 try {
                     sig.update(dataToSign);
                     signature = sig.sign(); 
                 } catch (SignatureException e) {
                 	e.printStackTrace();
                 }         
                 spendCoinbaseTx.addSignature(signature, 0); 
                 
                 
     //            spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                 spendCoinbaseTx.finalize();
                 blockHandler.processTx(spendCoinbaseTx);
                 
                 block = blockHandler.createBlock(people.get(0).getPublic());
                 
                 passes = passes && block != null && block.getPrevBlockHash().equals(secondBranchPrevBlock.getHash()) && block.getTransactions().size() == 1 && block.getTransaction(0).equals(spendCoinbaseTx);
                 ForwardBlockNode newNode = new ForwardBlockNode(block);
                 secondBranchTracker.setChild(newNode);
                 secondBranchTracker = newNode;
                 secondBranchPrevBlock = block;
              }
           } else {
              if (!flipped) {
                 // add two blocks two second branch
                 block = new Block(secondBranchPrevBlock.getHash(), people.get(0).getPublic());
                 spendCoinbaseTx = new Transaction();
                 spendCoinbaseTx.addInput(secondBranchPrevBlock.getCoinbase().getHash(), 0);
                 spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
     
                 byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                 byte[] signature = null;
                 Signature sig = null;
                 PrivateKey privkey = people.get(0).getPrivate();
                 try {
                 	sig = Signature.getInstance("SHA256withRSA");

                 } catch (NoSuchAlgorithmException e) {
                     e.printStackTrace();
                 }
                 try {
                     sig.initSign(privkey);
                 } catch (InvalidKeyException e) {
                 	e.printStackTrace();
                 }
                 try {
                     sig.update(dataToSign);
                     signature = sig.sign(); 
                 } catch (SignatureException e) {
                 	e.printStackTrace();
                 }         
                 spendCoinbaseTx.addSignature(signature, 0);             
                 
                 
     //            spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                 spendCoinbaseTx.finalize();
                 block.addTransaction(spendCoinbaseTx);
                 block.finalize();
                 
                 passes = passes && blockHandler.processBlock(block);
                 ForwardBlockNode newNode = new ForwardBlockNode(block);
                 secondBranchTracker.setChild(newNode);
                 secondBranchTracker = newNode;
                 secondBranchPrevBlock = block;
                 
                 block = new Block(secondBranchPrevBlock.getHash(), people.get(0).getPublic());
                 spendCoinbaseTx = new Transaction();
                 spendCoinbaseTx.addInput(secondBranchPrevBlock.getCoinbase().getHash(), 0);
                 spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
             
                 dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                 signature = null;
             //    Signature sig = null;
                 privkey = people.get(0).getPrivate();
                 try {
                 	sig = Signature.getInstance("SHA256withRSA");

                 } catch (NoSuchAlgorithmException e) {
                     e.printStackTrace();
                 }
                 try {
                     sig.initSign(privkey);
                 } catch (InvalidKeyException e) {
                 	e.printStackTrace();
                 }
                 try {
                     sig.update(dataToSign);
                     signature = sig.sign(); 
                 } catch (SignatureException e) {
                 	e.printStackTrace();
                 }         
                 spendCoinbaseTx.addSignature(signature, 0); 
                 
       //          spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                 spendCoinbaseTx.finalize();
                 block.addTransaction(spendCoinbaseTx);
                 block.finalize();
                 
                 passes = passes && blockHandler.processBlock(block);
                 newNode = new ForwardBlockNode(block);
                 secondBranchTracker.setChild(newNode);
                 secondBranchTracker = newNode;
                 secondBranchPrevBlock = block;
                 
                 if (i > 1) {
                    block = new Block(secondBranchPrevBlock.getHash(), people.get(0).getPublic());
                    spendCoinbaseTx = new Transaction();
                    spendCoinbaseTx.addInput(secondBranchPrevBlock.getCoinbase().getHash(), 0);
                    spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
             
                    dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                    signature = null;
                  //  Signature sig = null;
                    privkey = people.get(0).getPrivate();
                    try {
                    	sig = Signature.getInstance("SHA256withRSA");

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    try {
                        sig.initSign(privkey);
                    } catch (InvalidKeyException e) {
                    	e.printStackTrace();
                    }
                    try {
                        sig.update(dataToSign);
                        signature = sig.sign(); 
                    } catch (SignatureException e) {
                    	e.printStackTrace();
                    }         
                    spendCoinbaseTx.addSignature(signature, 0);  
                    
                    
           //       spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                    spendCoinbaseTx.finalize();
                    block.addTransaction(spendCoinbaseTx);
                    block.finalize();
                    
                    passes = passes && blockHandler.processBlock(block);
                    newNode = new ForwardBlockNode(block);
                    secondBranchTracker.setChild(newNode);
                    secondBranchTracker = newNode;
                    secondBranchPrevBlock = block;
                 }
              } else {
                 block = new Block(firstBranchPrevBlock.getHash(), people.get(0).getPublic());
                 spendCoinbaseTx = new Transaction();
                 spendCoinbaseTx.addInput(firstBranchPrevBlock.getCoinbase().getHash(), 0);
                 spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
    
                 byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                 byte[] signature = null;
                 Signature sig = null;
                 PrivateKey privkey = people.get(0).getPrivate();
                 try {
                 	sig = Signature.getInstance("SHA256withRSA");

                 } catch (NoSuchAlgorithmException e) {
                     e.printStackTrace();
                 }
                 try {
                     sig.initSign(privkey);
                 } catch (InvalidKeyException e) {
                 	e.printStackTrace();
                 }
                 try {
                     sig.update(dataToSign);
                     signature = sig.sign(); 
                 } catch (SignatureException e) {
                 	e.printStackTrace();
                 }         
                 spendCoinbaseTx.addSignature(signature, 0);  
                 
                 
                 
         //        spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                 spendCoinbaseTx.finalize();
                 block.addTransaction(spendCoinbaseTx);
                 block.finalize();
                 
                 passes = passes && blockHandler.processBlock(block);
                 ForwardBlockNode newNode = new ForwardBlockNode(block);
                 firstBranchTracker.setChild(newNode);
                 firstBranchTracker = newNode;
                 firstBranchPrevBlock = block;
                 
                 block = new Block(firstBranchPrevBlock.getHash(), people.get(0).getPublic());
                 spendCoinbaseTx = new Transaction();
                 spendCoinbaseTx.addInput(firstBranchPrevBlock.getCoinbase().getHash(), 0);
                 spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
           
                 dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                 signature = null;
                 sig = null;
                 try {
                 	sig = Signature.getInstance("SHA256withRSA");

                 } catch (NoSuchAlgorithmException e) {
                     e.printStackTrace();
                 }
                 try {
                     sig.initSign(privkey);
                 } catch (InvalidKeyException e) {
                 	e.printStackTrace();
                 }
                 try {
                     sig.update(dataToSign);
                     signature = sig.sign(); 
                 } catch (SignatureException e) {
                 	e.printStackTrace();
                 }         
                 spendCoinbaseTx.addSignature(signature, 0);  
                 
         //        spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                 spendCoinbaseTx.finalize();
                 block.addTransaction(spendCoinbaseTx);
                 block.finalize();
                 
                 passes = passes && blockHandler.processBlock(block);
                 newNode = new ForwardBlockNode(block);
                 firstBranchTracker.setChild(newNode);
                 firstBranchTracker = newNode;
                 firstBranchPrevBlock = block;
                 
                 if (i > 1) {
                    block = new Block(firstBranchPrevBlock.getHash(), people.get(0).getPublic());
                    spendCoinbaseTx = new Transaction();
                    spendCoinbaseTx.addInput(firstBranchPrevBlock.getCoinbase().getHash(), 0);
                    spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
             
                    dataToSign = spendCoinbaseTx.getRawDataToSign(0);
                    signature = null;
                    privkey = people.get(0).getPrivate();
                    try {
                    	sig = Signature.getInstance("SHA256withRSA");

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    try {
                        sig.initSign(privkey);
                    } catch (InvalidKeyException e) {
                    	e.printStackTrace();
                    }
                    try {
                        sig.update(dataToSign);
                        signature = sig.sign(); 
                    } catch (SignatureException e) {
                    	e.printStackTrace();
                    }         
                    spendCoinbaseTx.addSignature(signature, 0);        
                    
          //        spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
                    spendCoinbaseTx.finalize();
                    block.addTransaction(spendCoinbaseTx);
                    block.finalize();
                    
                    passes = passes && blockHandler.processBlock(block);
                    newNode = new ForwardBlockNode(block);
                    firstBranchTracker.setChild(newNode);
                    firstBranchTracker = newNode;
                    firstBranchPrevBlock = block;
                 }
              }
              flipped = !flipped;
           }
        }
        
        int firstBranchHeight = 0;
        firstBranchTracker = firstBranch;
        while (firstBranchTracker != null) {
           firstBranchTracker = firstBranchTracker.child;
           firstBranchHeight++;
        }
        
        int secondBranchHeight = 0;
        secondBranchTracker = secondBranch;
        while (secondBranchTracker != null) {
           secondBranchTracker = secondBranchTracker.child;
           secondBranchHeight++;
        }
        
        int maxHeight = Math.max(firstBranchHeight, secondBranchHeight);
        
        int firstBranchCount = 0;
        firstBranchTracker = firstBranch;
        while (firstBranchTracker.child != null) {
           block = new Block(firstBranchTracker.b.getHash(), people.get(0).getPublic());
           spendCoinbaseTx = new Transaction();
           spendCoinbaseTx.addInput(firstBranchTracker.b.getCoinbase().getHash(), 0);
           spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
       
           byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
           byte[] signature = null;
           Signature sig = null;
           PrivateKey privkey = people.get(0).getPrivate();
           try {
           	sig = Signature.getInstance("SHA256withRSA");

           } catch (NoSuchAlgorithmException e) {
               e.printStackTrace();
           }
           try {
               sig.initSign(privkey);
           } catch (InvalidKeyException e) {
           	e.printStackTrace();
           }
           try {
               sig.update(dataToSign);
               signature = sig.sign(); 
           } catch (SignatureException e) {
           	e.printStackTrace();
           }         
           spendCoinbaseTx.addSignature(signature, 0);      
              
           
       //    spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
           spendCoinbaseTx.finalize();
           block.addTransaction(spendCoinbaseTx);
           block.finalize();
           
           if (firstBranchCount < maxHeight - BlockChain.CUT_OFF_AGE - 1) {
               passes = passes && !blockHandler.processBlock(block);
           } else {
               passes = passes && blockHandler.processBlock(block);
           }
          
           firstBranchTracker = firstBranchTracker.child;
           firstBranchCount++;
        }
        
        int secondBranchCount = 0;
        secondBranchTracker = secondBranch;
        while (secondBranchTracker != null) {
           block = new Block(secondBranchTracker.b.getHash(), people.get(0).getPublic());
           spendCoinbaseTx = new Transaction();
           spendCoinbaseTx.addInput(secondBranchTracker.b.getCoinbase().getHash(), 0);
           spendCoinbaseTx.addOutput(Block.COINBASE, people.get(1).getPublic());
           
           byte[] dataToSign = spendCoinbaseTx.getRawDataToSign(0);
           byte[] signature = null;
           Signature sig = null;
           PrivateKey privkey = people.get(0).getPrivate();
           try {
           	sig = Signature.getInstance("SHA256withRSA");

           } catch (NoSuchAlgorithmException e) {
               e.printStackTrace();
           }
           try {
               sig.initSign(privkey);
           } catch (InvalidKeyException e) {
           	e.printStackTrace();
           }
           try {
               sig.update(dataToSign);
               signature = sig.sign(); 
           } catch (SignatureException e) {
           	e.printStackTrace();
           }         
           spendCoinbaseTx.addSignature(signature, 0);  
           
           
           
     //      spendCoinbaseTx.addSignature(people.get(0).getPrivateKey().sign(spendCoinbaseTx.getRawDataToSign(0)), 0);
           spendCoinbaseTx.finalize();
           block.addTransaction(spendCoinbaseTx);
           block.finalize();
           
           if (secondBranchCount < maxHeight - BlockChain.CUT_OFF_AGE - 1) {
           //    passes = passes && !blockHandler.processBlock(block);
               System.out.println("passes:" +passes+ " !processblock:"+(!blockHandler.processBlock(block)));
           } else {
           //    passes = passes && blockHandler.processBlock(block);
               System.out.println("passes:" +passes+ " !processblock:"+(blockHandler.processBlock(block)));
           }
           
           secondBranchTracker = secondBranchTracker.child;
           secondBranchCount++;
        }
        
      //  return UtilCOS.printPassFail(passes);
     }
    
    
    public static void main(String []args) {
    	
    	final KeyPairGenerator keyGen;
    	final PublicKey pub;
    	final Block genesisBlock;
    	
   		try{
   			keyGen = KeyPairGenerator.getInstance("RSA");
   			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
   			keyGen.initialize(1024, random);
   	    	KeyPair pair = keyGen.generateKeyPair();
   //	    	PrivateKey priv = pair.getPrivate();
 //  	    	System.out.println("private key" + priv);
   	    	pub = pair.getPublic();
   	    	genesisBlock = new Block(null, pub);
   	        genesisBlock.finalize(); 
//   	    	System.out.println("public key" + pub);
   	  	    ArrayList<KeyPair> people = new ArrayList<KeyPair>();
   	  	    people.add(pair);
   	  	    for (int i = 1; i < 20; i++){
   	  	    	KeyPair newpair = keyGen.generateKeyPair();
   	           	people.add(newpair);
   	  	    }
   	  	    
   	        System.out.println("genesisblock");
   	        System.out.println("blockHash:" + genesisBlock.getHash() + " prevHash:" + genesisBlock.getPrevBlockHash());
   	    	BlockChain bc = new BlockChain(genesisBlock);
   	    	BlockHandler bh = new BlockHandler(bc);
   	    	
   	    	// test 5
   	    	
   	 //       byte[] hash = genesisBlock.getHash();
   	 //       byte[] hashCopy = Arrays.copyOf(hash, hash.length);
   	 //       hashCopy[0]++;
   	 //       Block block = new Block(hashCopy, people.get(1).getPublic());
   	 //       block.finalize();
   	        
   	 //       System.out.println("block handler rejects bad:" +(!bh.processBlock(block)));
   	    	
   	    	
	    
   	//    	bh.test18(bc, bh, genesisBlock, people);
   //	        bh.test15(bc, bh, genesisBlock, people);  
   //	        bh.test25(bc, bh, genesisBlock, people);
   	//     		bh.test23(bc, bh, genesisBlock, people);
   	 //   		bh.test19(bc, bh, genesisBlock, people);
   	    		bh.test27(bc, bh, genesisBlock, people);
   		}
   	        
   		catch (NoSuchAlgorithmException e) {
   			System.out.println("No Such Algorithm Exception");
   		}
   		catch (NoSuchProviderException f) {
   			System.out.println("No Such Provider Exception");
   		} 
     }  
}
