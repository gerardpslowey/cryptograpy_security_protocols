import java.math.*;
import java.security.SecureRandom;

public class Assignment2{
   
   public static void main(String[] args){

      int bitLength = 512;
      SecureRandom secureRandomNumbeRandom = new SecureRandom();

      // Generate two distinct 512-bit probable primes p and q
      BigInteger p = BigInteger.probablePrime(bitLength, secureRandomNumbeRandom);
      BigInteger q = BigInteger.probablePrime(bitLength, secureRandomNumbeRandom);

      // TODO check they are not the same
      if (p.compareTo(q) == 0) {

      }

      // Calculate the product of these two primes n = pq
      BigInteger n = p.multiply(q); 

      // Calculate the Euler totient function phi(n)
      BigInteger phiN = phi(p, q);
   }

   // Get phi value
   public static BigInteger phi(BigInteger p, BigInteger q){
      BigInteger phiP = p.subtract(BigInteger.ONE);
      BigInteger phiQ = q.subtract(BigInteger.ONE);

      return phiP.multiply(phiQ);
   }

   // Calculate the GCD recursively
   public static BigInteger gcd(BigInteger x, BigInteger y){

      if(y.compareTo(BigInteger.ZERO) == 0){
          return x;
      }else return gcd(y, x.mod(y));
  }
   
}