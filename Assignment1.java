import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Scanner;

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


    public static byte[] createIV() {
        // The IV for this encryption will be a randomly generated 128-bit value.
        SecureRandom randomNum = new SecureRandom();
        byte[] iv = new byte[16];
        randomNum.nextBytes(iv);

        return iv;
    }

    public static void main(String[] args)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {

        Scanner in = new Scanner(System.in);
        // Create a placeholder for the filename
        String fileName = null;

        // Check if file to be used as input was present
        // on the command line
        if (args.length == 1) {
			fileName = args[0];
        }
        
        // Give the user a second chance to enter a file name
        else {
			System.out.print("Please enter an input file name: ");
			fileName = in.next();
			in.close();
		}

        // Generate a random 1023-bit integer, this will be your secret value b.
        BigInteger b = new BigInteger(1023, new SecureRandom());
        // System.out.println(b.toString(16));

        // Generate your public shared value B given by g^b (mod p)
        BigInteger B = modularExp(g, b, p);
        // System.out.println(B.toString(16));

        // Calculate the shared secret s given by A^b (mod p)
        BigInteger s = modularExp(A, b, p);
        // System.out.println(s.toString(16));

        // TODO: Implement this in a standalone function
        // Use SHA-256 to produce a 256-bit digest from the shared secret s        
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(s.toByteArray());
        // Create a new key k and 
        // tell Java that this is an AES key using SecretKeySpec
        SecretKey k = new SecretKeySpec(hash, "AES");
        
        // Create the 128-bit IV in hex
        IvParameterSpec IV = new IvParameterSpec(createIV());


        // Now, we need to instantiate the cipher
        // Indicte we will be encrypting using AES in CBC mode and 
        // that we will implement the padding scheme ourselves
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        // Now initilaise the Cipher object with the secret key k
        // in encryption mode
        cipher.init(Cipher.ENCRYPT_MODE, k);


        // Read in the file from the supplied fileName
        File inputFile = new File(fileName);

        // Calculate the input file length for padding
        int inputLength = (int) inputFile.length();

        // See how much padding is needed mod 16 bytes so
        // that the block size of 128 bits is matched
        int lengthPadding = 16 - (inputLength % 16);

        // Initialise an array of bytes matching the input file length
        // with the padding attached
        byte[] fileAsBytes = new byte[inputLength + lengthPadding];

        // Read data in bytes from the input file
        FileInputStream fs = new FileInputStream(inputFile);

        // Easiest way to read the file and parse the bytes into
        // the byte array
        fs.read(fileAsBytes);
        fs.close();


        // TODO: Padding the array after the data
        // if the final part of the message is less than the block size, append a 1-bit and fill the rest of the block with 0-bits; 
        // if the final part of the message is equal to the block size, then create an extra block starting with a 1-bit and fill the rest of the block with 0-bits. 
           
    }
}