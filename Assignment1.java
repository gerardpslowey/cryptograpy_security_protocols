import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Assignment1 {

    private static String primeModP = "b59dd79568817b4b9f6789822d22594f376e6a9abc0241846de426e5dd8f6eddef00b465f38f509b2b18351064704fe75f012fa346c5e2c442d7c99eac79b2bc8a202c98327b96816cb8042698ed3734643c4c05164e739cb72fba24f6156b6f47a7300ef778c378ea301e1141a6b25d48f1924268c62ee8dd3134745cdf7323";
    private final static BigInteger p = new BigInteger(primeModP, 16);

    private static String generator = "44ec9d52c8f9189e49cd7c70253c2eb3154dd4f08467a64a0267c9defe4119f2e373388cfa350a4e66e432d638ccdc58eb703e31d4c84e50398f9f91677e88641a2d2f6157e2f4ec538088dcf5940b053c622e53bab0b4e84b1465f5738f549664bd7430961d3e5a2e7bceb62418db747386a58ff267a9939833beefb7a6fd68";
    private final static BigInteger g = new BigInteger(generator, 16);

    private static String publicSharedA = "5af3e806e0fa466dc75de60186760516792b70fdcd72a5b6238e6f6b76ece1f1b38ba4e210f61a2b84ef1b5dc4151e799485b2171fcf318f86d42616b8fd8111d59552e4b5f228ee838d535b4b987f1eaf3e5de3ea0c403a6c38002b49eade15171cb861b367732460e3a9842b532761c16218c4fea51be8ea0248385f6bac0d";
    private final static BigInteger A = new BigInteger(publicSharedA, 16);

    // Modular exponentiation implementation
    // Using the right to left variant y = a^x (mod n)
    // the exponent x is k bits long
    private static BigInteger modularExp(BigInteger baseValue, BigInteger exponent, BigInteger modulus) {
        int k = exponent.bitLength();

        // y value set to 1 initially
        BigInteger y = BigInteger.ONE;

        for (int i = k - 1; i >= 0; i--) {
            y = y.multiply(y).mod(modulus);

            // Check if bit is set
            if (exponent.testBit(i)) {
                // Square and multiply by base value
                y = y.multiply(baseValue).mod(modulus);
            }
        }
        return y;
    }

    public static byte[] aes256(BigInteger s) throws NoSuchAlgorithmException {
        // Attempt to create AES key k(256 bit size) using the shared key generated
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(s.toByteArray());

        return hash;
    }

    public static byte[] createIV() {
        // The IV for this encryption will be a randomly generated 128-bit value.
        byte[] iv = new byte[16];
        SecureRandom randomNum = new SecureRandom();
        randomNum.nextBytes(iv);

        return iv;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {

        // File to be used as input
        // String fileName = args[0];

        // Generate a random 1023-bit integer, this will be your secret value b.
        BigInteger b = new BigInteger(1023, new SecureRandom());
        System.out.println(b.toString(16));

        // Generate your public shared value B given by g^b (mod p)
        BigInteger B = modularExp(g, b, p);
        System.out.println(B.toString(16));

        // Calculate the shared secret s given by A^b (mod p)
        BigInteger s = modularExp(A, b, p);
        System.out.println(s.toString(16));

        // Create a new key k and 
        // Tell Java that this is an AES key using SecretKeySpec
        SecretKey k = new SecretKeySpec(aes256(s), "AES");
        
        //create the 128-bit IV in hex
        IvParameterSpec IV = new IvParameterSpec(createIV());
           
    }
}