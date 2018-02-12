//import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;
 
public class Main {
 
    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
 
        // Crypto setup
        // You need the following JAR for RSA http://www.bouncycastle.org/download/bcprov-jdk15on-156.jar
        // More information https://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)
 //       Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
 
        // Generating two key pairs, one for Scrooge and one for Alice
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey private_key_scrooge = pair.getPrivate();
        PublicKey public_key_scrooge = pair.getPublic();
        
        pair = keyGen.generateKeyPair();
        PrivateKey private_key_alice = pair.getPrivate();
        PublicKey public_key_alice = pair.getPublic();

        pair = keyGen.generateKeyPair();
        PrivateKey private_key_bob = pair.getPrivate();
        PublicKey public_key_bob =  pair.getPublic();

 
        // START - ROOT TRANSACTION
        // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
        // By thin air I mean that this tx will not be validated, I just need it to get a proper Transaction.Output
        // which I then can put in the UTXOPool, which will be passed to the TXHandler
        
        	Transaction tx00 = new Transaction();
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);
        	tx00.addOutput(10, public_key_scrooge);      
  
        // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
        	byte[] initialHash = BigInteger.valueOf(1956929291).toByteArray();
        	tx00.addInput(initialHash, 0);
        	 
        	Signature signature = Signature.getInstance("SHA256withRSA");
        	signature.initSign(private_key_scrooge);
        	signature.update(tx00.getRawDataToSign(0));
        	byte[] sig = signature.sign();
        	tx00.addSignature(sig, 0);
        	
        	tx00.finalize();
        // END -ROOT TRANSACTION
 
        // The transaction output of the root transaction is unspent output
        	UTXOPool utxoPool = new UTXOPool();
        	UTXO utxo1 = new UTXO(tx00.getHash(),0);
        	UTXO utxo2 = new UTXO(tx00.getHash(),1);
        	UTXO utxo3 = new UTXO(tx00.getHash(),2);
        	UTXO utxo4 = new UTXO(tx00.getHash(),3);
        	UTXO utxo5 = new UTXO(tx00.getHash(),4);
        	UTXO utxo6 = new UTXO(tx00.getHash(),5);
        	UTXO utxo7 = new UTXO(tx00.getHash(),6);
        	UTXO utxo8 = new UTXO(tx00.getHash(),7);
        	UTXO utxo9 = new UTXO(tx00.getHash(),8);
        	UTXO utxo10 = new UTXO(tx00.getHash(),9);
        	
        	utxoPool.addUTXO(utxo1, tx00.getOutput(0));
        	utxoPool.addUTXO(utxo2, tx00.getOutput(1));
        	utxoPool.addUTXO(utxo3, tx00.getOutput(2));
        	utxoPool.addUTXO(utxo4, tx00.getOutput(3));
        	utxoPool.addUTXO(utxo5, tx00.getOutput(4));
        	utxoPool.addUTXO(utxo6, tx00.getOutput(5));
        	utxoPool.addUTXO(utxo7, tx00.getOutput(6));
        	utxoPool.addUTXO(utxo8, tx00.getOutput(7));
        	utxoPool.addUTXO(utxo9, tx00.getOutput(8));
        	utxoPool.addUTXO(utxo10, tx00.getOutput(9));
        	
        	// check to see if there are 10 coins of value 10 in UTXO pool
        	
   //         ArrayList<UTXO> UTXOlist = new ArrayList<UTXO>();
   //         UTXOlist = utxoPool.getAllUTXO();
   //         System.out.println(UTXOlist.size());
 
 
        // START - PROPER TRANSACTION
        Transaction tx2 = new Transaction();
        Transaction tx3 = new Transaction();
//        System.out.println("starting TX2");  
        
        // the Transaction.Output of tx at position 0, 1 has a value of 10
        tx2.addInput(tx00.getHash(), 0);
        tx3.addInput(tx00.getHash(), 1);

 
        // I split the coin of value 10 into 3 coins and send some to Alice some to Bob
        tx2.addOutput(5, public_key_alice);
        tx2.addOutput(3, public_key_alice);
        tx2.addOutput(2, public_key_alice);
        tx3.addOutput(15, public_key_bob);
        
 
        // There are two (at position 0, 1) Transaction.Input in tx2
        // both coins from Scrooge, therefore I have to sign with the private key from Scrooge
        
        signature.initSign(private_key_scrooge);
        signature.update(tx2.getRawDataToSign(0));
        sig = signature.sign();
        tx2.addSignature(sig, 0);
        tx2.finalize();
        
        signature.initSign(private_key_scrooge);
        signature.update(tx3.getRawDataToSign(0));
        sig = signature.sign();
        tx3.addSignature(sig, 0);
        tx3.finalize();
        
 //       System.out.println("finishing TX2");
    
        // remember that the utxoPool contains a single unspent Transaction.Output which is the coin from Scrooge
        MaxFeeTxHandler maxFeeTxHandler = new MaxFeeTxHandler(utxoPool);
        
//        System.out.println("test  inValid, should be false");
        
//       System.out.println(tx3.numOutputs());
        
//          System.out.println(maxFeeTxHandler.isValidTx(tx2));
 //       System.out.println("test isValid, should be false");

          Transaction[] testTXlist = new Transaction[]{tx2, tx3};
          Transaction[] testOut = maxFeeTxHandler.handleTxs(testTXlist);
          System.out.println(testOut.length);
    }
}