import java.math.*;
import java.util.*;
import java.io.*;
import java.io.FileInputStream;
import java.security.*;

public class Assignment2{

   // Probable primes bit size
   private static final int bitLength = 512;
   // Required files
   public static final String modFile = "Modulus.txt";
   public static final String sigFile = "Signature.txt";
   
   public static void main(String[] args) throws Exception {
      File input_file = getFile(args);

      BigInteger p, q, n, phiOfn, d;
      SecureRandom secureRandomNumber = new SecureRandom();
      // Encryption exponent
      BigInteger e = BigInteger.valueOf(65537);

      while(true){
         // Generate two distinct 512-bit probable primes p and q
         p = generateProbablePrime(bitLength, secureRandomNumber);
         q = generateProbablePrime(bitLength, secureRandomNumber);

         // Calculate the product of these two primes n = pq
         n = p.multiply(q); 
         writeToFile(modFile, n.toString());

         // Calculate the Euler totient function phi(n)
         phiOfn = phi(p, q);

         // Check that e is relatively prime to phiOfN, recalculate p and q if not
         if(gcd(e, phiOfn).equals(BigInteger.ONE))
         break;	
      }

      // Calculate the private key d
      d = mulInverse(e, phiOfn);

      // Read the input file into a byte array
      byte[] input = readFile(input_file);

      // The 256-bit digest will be produced using SHA-256
      byte [] hashedPlainText = hashFile(input);   

      // Ciphertext c
      BigInteger c = new BigInteger(1, hashedPlainText);

      // Signed message using CRT
      encrypt(d, p, q, c);
   }


   // Method to retrieve file from input arguments
   public static File getFile(String[] args) throws Exception {
      File input;

      if (args.length == 1) {
            input = new File(args[0]);
      } else {
            // Give user second chance to enter input filename
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please enter an input file name: ");
            input = new File(scanner.next());
            scanner.close();
      }

      return input;
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
   public static BigInteger phi(BigInteger p, BigInteger q){
      BigInteger phiOfp = p.subtract(BigInteger.ONE);
      BigInteger phiOfq = q.subtract(BigInteger.ONE);

      return phiOfp.multiply(phiOfq);
   }


   // Calculate the GCD recursively
   public static BigInteger gcd(BigInteger x, BigInteger y){
      if(y.compareTo(BigInteger.ZERO) == 0){
         return x;
      }
      
      else {
         return gcd(y, x.mod(y));
      }
   } 


   // Calculate d = gcd(a,N) = ax+yN
   public static BigInteger [] xGCD (BigInteger a, BigInteger N){
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
   
      if (ans[1].compareTo(BigInteger.ZERO) == 1){
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
      byte[] digest = md.digest();

      return digest;
   }


   // Encryption
   private static void encrypt(BigInteger d, BigInteger p, BigInteger q, BigInteger c) {
      BigInteger message = crt(d,p,q,c);
      System.out.println(message.toString(16));
   }


   // Decryption
   public static void decrypt(BigInteger e, BigInteger p, BigInteger q,BigInteger m){
      BigInteger signedMessage = crt(e,p,q,m);
      System.out.print(signedMessage.toString(16));
   }


   // Chinese Remainder Theorem
   public static BigInteger crt(BigInteger d, BigInteger p, BigInteger q, BigInteger m){
      BigInteger dp, dq, qInverse, m1, m2, h;
   
      dp = d.mod(p.subtract(BigInteger.ONE));
      dq = d.mod(q.subtract(BigInteger.ONE));
      qInverse = mulInverse(q,p);
   
      m1 = m.modPow(dp,p);
      m2 = m.modPow(dq,q);
      h = qInverse.multiply(m1.subtract(m2)).mod(p);
      m = m2.add(h.multiply(q));
   
      return m;
   }
}