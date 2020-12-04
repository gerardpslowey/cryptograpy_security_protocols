import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Assignment1 {

    // Diffie-Hellman Key exchange values, from assignment spec
    private static String primeModP = "b59dd79568817b4b9f6789822d22594f376e6a9abc0241846de426e5dd8f6eddef00b465f38f509b2b18351064704fe75f012fa346c5e2c442d7c99eac79b2bc8a202c98327b96816cb8042698ed3734643c4c05164e739cb72fba24f6156b6f47a7300ef778c378ea301e1141a6b25d48f1924268c62ee8dd3134745cdf7323";
    private static String generator = "44ec9d52c8f9189e49cd7c70253c2eb3154dd4f08467a64a0267c9defe4119f2e373388cfa350a4e66e432d638ccdc58eb703e31d4c84e50398f9f91677e88641a2d2f6157e2f4ec538088dcf5940b053c622e53bab0b4e84b1465f5738f549664bd7430961d3e5a2e7bceb62418db747386a58ff267a9939833beefb7a6fd68";
    private static String publicSharedA = "5af3e806e0fa466dc75de60186760516792b70fdcd72a5b6238e6f6b76ece1f1b38ba4e210f61a2b84ef1b5dc4151e799485b2171fcf318f86d42616b8fd8111d59552e4b5f228ee838d535b4b987f1eaf3e5de3ea0c403a6c38002b49eade15171cb861b367732460e3a9842b532761c16218c4fea51be8ea0248385f6bac0d";

    // Chosen secure random set once
    private static SecureRandom secureRandomNumber = new SecureRandom();

    public static void main(String[] args) throws Exception {

        // Get the input filename from the supplied arguments
        File input_file = getFile(args);

        // Setting the required filenames for later
        String DH_File = "DH.txt";
        String IV_File = "IV.txt";

        // Create BigIntegers from the strings above
        BigInteger p = new BigInteger(primeModP, 16);
        BigInteger g = new BigInteger(generator, 16);
        BigInteger A = new BigInteger(publicSharedA, 16);

        // Generating a random 1023-bit integer, this will be my secret value b
        BigInteger b = new BigInteger(1023, secureRandomNumber);

        // Calculating my public shared value B given by g^b (mod p) 
        // Then change to a hex string and store in file
        BigInteger B = modularExp(g, b, p);
        String publicHexKey = B.toString(16);
        writeToFile(DH_File, publicHexKey);

        // Calculating the shared secret s given by A^b (mod p)
        BigInteger s = modularExp(A, b, p);

        // Get a 256 bit deigest of the shared secret s
        byte[] aesKey = sha256(s);

        // Get the random IV Then store this as a hex string in file
        byte[] iv = createIV();
        IvParameterSpec IV = new IvParameterSpec(iv);
        writeToFile(IV_File, ArraytoHexString(iv));

        // Using the 256-bit digest to give a 256-bit AES key k
        SecretKey k = new SecretKeySpec(aesKey, "AES");

        // Then encrypt an input binary file using AES in CBC mode 
        // using the 256-bit key k and a block size of 128-bits
        encrypt(input_file, k, IV);
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

    // Simple function to take in file data as a string and write it to a file given in outputFileName
    public static void writeToFile(String outputFileName, String fileData) throws IOException {
        File outputFile = new File(outputFileName);
        FileOutputStream stream = new FileOutputStream(outputFile);
        // Encode data using the UTF-8 charset
        stream.write(fileData.getBytes("UTF-8"));
        stream.close();
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

    // The IV for this encryption will be a randomly generated 128-bit value
    public static byte[] createIV() throws IOException {
        // 16 bytes = 128 bits
        byte[] iv = new byte[16];
        // Place the generated random bytes into an the iv byte array.
        secureRandomNumber.nextBytes(iv);

        return iv;
    }

    /*
     * Build a string by appending bytes using "%02x" format specifier 
     * Link used for reference: 
     * https://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal
     */
    public static String ArraytoHexString(byte[] bytes) {
        StringBuilder str = new StringBuilder();
        for (byte b : bytes) {
            str.append(String.format("%02x", b));
        }

        return str.toString();
    }

    // Using SHA-256 to produce a 256-bit digest from the shared secret s
    public static byte[] sha256(BigInteger secretS) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String key = secretS.toString();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(key.getBytes("UTF-8"));
        return digest;
    }
    
    /* Final encryption function
    Encrypt an input binary file using AES in CBC mode with the 256-bit key k and a block size of 128-bits */
    private static void encrypt(File input_file, SecretKey k, IvParameterSpec IV)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, BadPaddingException {
        
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, k, IV);

        // Get the padded file
        byte[] paddedFile = getPaddedFile(input_file);

        byte[] encryptedFile = cipher.doFinal(paddedFile);

        // Finally change the array to a hex string and store to file using output redirection
        String encryption = ArraytoHexString(encryptedFile);
        System.out.print(encryption);        
    }

    
    public static byte[] getPaddedFile(File input_file) throws IOException {
        // Get the original length
        int inputFileLength = (int) input_file.length();
        
        /* Get the padding size needed
        Message less than block size: 16 - (13 % 16) = 3 bytes to fill
        Message same size as block: 16 - (0 % 16) = 16, another 16 bytes to fill */
        int paddingNeeded = 16 - (inputFileLength % 16);
        
        // Create a new byte array with the needed size increase
        byte[] paddedFile = new byte[inputFileLength + paddingNeeded];

        // Read from input_file into byte array
        FileInputStream fs = new FileInputStream(input_file);
        // Fill into paddedFile array
        fs.read(paddedFile);
        fs.close();

        // 1st bit set to 1
        paddedFile[inputFileLength] = (byte) 1;
        for (int i = inputFileLength + 1; i < paddingNeeded; i++) {
            // Fill the rest with zeros
            paddedFile[i] = (byte) 0;
        }

        return paddedFile;
    }
}