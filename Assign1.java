import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Assign1 {

    private static String primeModP = "b59dd79568817b4b9f6789822d22594f376e6a9abc0241846de426e5dd8f6eddef00b465f38f509b2b18351064704fe75f012fa346c5e2c442d7c99eac79b2bc8a202c98327b96816cb8042698ed3734643c4c05164e739cb72fba24f6156b6f47a7300ef778c378ea301e1141a6b25d48f1924268c62ee8dd3134745cdf7323";
    private static String generator = "44ec9d52c8f9189e49cd7c70253c2eb3154dd4f08467a64a0267c9defe4119f2e373388cfa350a4e66e432d638ccdc58eb703e31d4c84e50398f9f91677e88641a2d2f6157e2f4ec538088dcf5940b053c622e53bab0b4e84b1465f5738f549664bd7430961d3e5a2e7bceb62418db747386a58ff267a9939833beefb7a6fd68";
    private static String publicSharedA = "5af3e806e0fa466dc75de60186760516792b70fdcd72a5b6238e6f6b76ece1f1b38ba4e210f61a2b84ef1b5dc4151e799485b2171fcf318f86d42616b8fd8111d59552e4b5f228ee838d535b4b987f1eaf3e5de3ea0c403a6c38002b49eade15171cb861b367732460e3a9842b532761c16218c4fea51be8ea0248385f6bac0d";

    public static void main(String[] args) throws Exception {

        String input_file = args[0];
        String output_file = args[1];
        String dh_File = "DH.txt";
        String iv_File = "IV.txt";

        BigInteger p = new BigInteger(primeModP, 16);
        BigInteger g = new BigInteger(generator, 16);
        BigInteger A = new BigInteger(publicSharedA, 16);

        BigInteger b = new BigInteger(1023, new SecureRandom());

        BigInteger B = modularExp(g, b, p);
        String publicHexKey = B.toString(16);
        writeToFile(dh_File, publicHexKey);

        BigInteger s = modularExp(A, b, p);

        byte[] iv = createIV();
        writeToFile(iv_File, ArraytoString(iv));

        byte[] aesKey = sha256(s);
        SecretKey k = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec IV = new IvParameterSpec(iv);

        encrypt(input_file, output_file, k, IV);
    }


    private static void encrypt(String input_file, String output_file, SecretKey k, IvParameterSpec IV)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, BadPaddingException {
        
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, k, IV);

        byte[] paddedFile = getPaddedFile(input_file);

        byte[] encryptedFile = cipher.doFinal(paddedFile);

        // TODO: FIX STANDARD OUTPUT
        File outputFile = new File(output_file);
        FileOutputStream fOut = new FileOutputStream(outputFile);
        fOut.write(encryptedFile);
        fOut.close();

        System.out.println("Encryption Complete!");
        
    }

    private static BigInteger modularExp(BigInteger baseValue, BigInteger exponent, BigInteger modulus) {
        int k = exponent.bitLength();
        BigInteger y = BigInteger.ONE;

        for (int i = k - 1; i >= 0; i--) {
            y = (y.multiply(y)).mod(modulus);

            if (exponent.testBit(i)) {
                y = y.multiply(baseValue).mod(modulus);
            }
        }
        return y;
    }


    public static byte[] sha256(BigInteger secretS) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(secretS.toByteArray());

        return digest;
    }


    public static byte[] createIV() throws IOException {
        SecureRandom randomNum = new SecureRandom();
        byte[] iv = new byte[16];
        randomNum.nextBytes(iv);

        return iv;
    }


    public static String ArraytoString(byte[] bytes){
        StringBuilder str = new StringBuilder();
        for (byte b : bytes) {
            str.append(String.format("%02x", b));
        }

        return str.toString();
    }

    

    // TODO: FIX PADDING
    public static byte[] getPaddedFile(String input_file) throws IOException {
        File inputFile = new File(input_file);

        int inputFileLength = (int) inputFile.length();
        int paddingNeeded = 16 - (inputFileLength % 16);
        
        byte[] paddedFile = new byte[inputFileLength + paddingNeeded];

        FileInputStream fs = new FileInputStream(inputFile);
        fs.read(paddedFile);
        fs.close();

        paddedFile[inputFileLength] = (byte) 128;
        for (int i = 1; i < paddingNeeded; i++) {
            paddedFile[inputFileLength + 1] = (byte) 0;
        }

        return paddedFile;
    }


    public static void writeToFile(String input_file, String fileData) throws IOException {
        File outputFile = new File(input_file);
        FileOutputStream stream = new FileOutputStream(outputFile);
        stream.write(fileData.getBytes("UTF-8"));
        stream.close();
    }
}