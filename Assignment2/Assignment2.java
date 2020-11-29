import java.math.*;
import java.util.*;
import java.io.*;
import java.security.*;

public class Assignment2 {

   private static final BigInteger e = BigInteger.valueOf(65537);

   private static final String modFile = "Modulus.txt";
   // The values p and q have been hardcoded as requested in lecture
   private static final String pString = "c353aae85c9ee77da50f9e523382655d9e704b049897c2d7570e399bf103a3c94a301b3759a20b81cc5c73e34b77b39d9593f80bd3c245bcfee0b21136ca790b";
   private static final String qString = "b851d8bd3c12ad7819e042472da768bf723c0a50cee6af37d4d361bb993dd1a127ada7add03b951742ced594fb0373c623f1d62d85b5971e3c2ef6951325171f";
   private static final String phiString = "8ca295af10e69ca7a9888b2cdb0d416c9d257ef9aabf3d794809deb37334aee8ce8b360f4e9f50ca8827f4420c9f82581918cb2441199b44fde7a28ff661b04fde3de531f7d62e825e100e2e810628b1c80895b1232e4f352683a674900278119a0ab0bca9e14e1ea017d704ec5bc7e8c20567d44b483b549dd66b01d90c152c";

   public static void main(String[] args) throws Exception {

      File input_file = getFile(args);

      BigInteger p = new BigInteger(pString, 16);
      BigInteger q = new BigInteger(qString, 16);
      BigInteger n = p.multiply(q); 
      BigInteger phiOfn = new BigInteger(phiString, 16);

      /* 
      These functions were used before numbers were hardcoded
      BigInteger[] values = generateValues();
      BigInteger p = values[0];
      BigInteger q = values[1];   
      BigInteger phiOfn = values[2];
      */

      // Calculate the private key d
      BigInteger d = mulInverse(e, phiOfn);

      // Read the input file into a byte array
      byte[] input = readFile(input_file);

      // The 256-bit digest will be produced using SHA-256
      byte[] hashedPlainText = hashFile(input);

      // Ciphertext c
      BigInteger c = new BigInteger(1, hashedPlainText);

      // Signed message using CRT
      decryptMessage(c, d, p, q, n);
   }

   // Method to retrieve file from input arguments
   public static File getFile(String[] args) throws Exception {
      File input;

      if (args.length == 1) {
         input = new File(args[0]);
      } 
      
      else {
         // Give the user a second chance to enter an input filename
         Scanner scanner = new Scanner(System.in);
         System.out.print("Please enter an input file name: ");
         input = new File(scanner.next());
         scanner.close();
      }

      return input;
   }

   // This function was used to generate the p and q values which have been hardcoded above 
   // as well as writing n to Modulus.txt

   public static BigInteger[] generateValues() throws IOException {

      int bitLength = 512;
      SecureRandom secureRandomNumber = new SecureRandom();

      // Generate two distinct 512-bit probable primes p and q
      BigInteger p = generateProbablePrime(bitLength, secureRandomNumber);
      BigInteger q = generateProbablePrime(bitLength, secureRandomNumber);

      // p and q need to be different
      if (p.compareTo(q) == 0) { 
         generateValues(); 
      }

      // Calculate the product of these two primes n = pq
      BigInteger n = p.multiply(q); 

      BigInteger phiOfn = phi(p, q);

      // Check that e is relatively prime to phiOfN, recalculate p and q if not
      if(!(gcd(e, phiOfn).equals(BigInteger.ONE))){
         generateValues();
      }

      writeToFile(modFile, n.toString(16));

      return new BigInteger[] {p, q, phiOfn };
   }

   // Generate a 512 bit probable prime
   public static BigInteger generateProbablePrime(int bitLength, SecureRandom secureRandomNumber){
      return BigInteger.probablePrime(bitLength, secureRandomNumber);
   }


   // Simple function to take in file data as a string and write it to a file given in outputFileName
   public static void writeToFile(String outputFileName, String fileData) throws IOException {
      File outputFile = new File(outputFileName);
      FileOutputStream stream = new FileOutputStream(outputFile);
      // Encode data using the UTF-8 charset
      stream.write(fileData.getBytes("UTF-8"));
      stream.close();
   }


   // Get phi value
   public static BigInteger phi(BigInteger p, BigInteger q) {
      BigInteger phiOfp = p.subtract(BigInteger.ONE);
      BigInteger phiOfq = q.subtract(BigInteger.ONE);

      return phiOfp.multiply(phiOfq);
   }


   // Calculate the GCD recursively
   public static BigInteger gcd(BigInteger x, BigInteger y) {
      if (y.compareTo(BigInteger.ZERO) == 0) {
         return x;
      }
      
      else {
         return gcd(y, x.mod(y));
      }
   } 


   // Calculate d = gcd(a,N) = ax+yN
   public static BigInteger [] xGCD (BigInteger a, BigInteger N) {
      BigInteger [] ans = new BigInteger[3];
      BigInteger ax, yN, temp;
      
      if (N.equals(BigInteger.ZERO)) {
         ans[0] = a;
         ans[1] = BigInteger.ONE;
         ans[2] = BigInteger.ZERO;
      return ans;
      }

      ans = xGCD (N, a.mod (N));
      ax = ans[1];
      yN = ans[2];
      ans[1] = yN;
      temp = a.divide(N);
      temp = yN.multiply(temp);
      ans[2] = ax.subtract(temp);

      return ans;
   }


   // Calculate multiplicative mulInverse of a%n using the extended euclidean GCD algorithm
   public static BigInteger mulInverse (BigInteger a, BigInteger N){
      BigInteger [] ans = xGCD(a,N);
   
      if (ans[1].compareTo(BigInteger.ZERO) == 1) {
         return ans[1];
      }
      else {
         return ans[1].add(N);
      }
   }


   // Read input file
   private static byte[] readFile(File input_file) throws IOException {
      FileInputStream input = new FileInputStream(input_file);
      byte[] bytes = new byte[(int) input_file.length()];
      input.read(bytes);
      input.close();
      
      return bytes;
   }


   // SHA-256 hashing a file
   private static byte[] hashFile(byte[] message) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(message);

      return md.digest();
   }


   // Decryption
   private static void decryptMessage(BigInteger c, BigInteger d, BigInteger p, BigInteger q, BigInteger n) {
      BigInteger message = crt(c, d, p, q, n);
      System.out.println(message.toString(16));
   }


   private static BigInteger crt(BigInteger c, BigInteger d, BigInteger p, BigInteger q, BigInteger n){
      BigInteger a1 = modularExp(c.mod(p), d, p);
      BigInteger n1 = n.divide(p);
      BigInteger y1 = mulInverse(n1,p);

      BigInteger a2 = modularExp(c.mod(q), d, q);
      BigInteger n2 = n.divide(q);
      BigInteger y2 = mulInverse(n2,q);
      
      BigInteger m1 = a1.multiply(n1).multiply(y1);
      BigInteger m2 = a2.multiply(n2).multiply(y2);

      return (m1.add(m2)).mod(n);
   }

   
   // Modular exponentiation using the Right-to-left approach 
   // Calculating y = a^x (mod n) using the square and multiply algorithm
   private static BigInteger modularExp(BigInteger baseValue, BigInteger exponent, BigInteger modulus) {
      // The exponent is k bits long
      int k = exponent.bitLength();
      // y is initially set to one
      BigInteger y = BigInteger.ONE;

      for (int i = 0; i < k; i++) {
         // If the bit is set
         if (exponent.testBit(i)) {
            // y = y * a (mod n)
            y = y.multiply(baseValue).mod(modulus);
         }
         // a = a^2 (mod n)
         baseValue = (baseValue.multiply(baseValue)).mod(modulus);
      }

      return y;
  }

}