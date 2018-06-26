package no.moldesoft.app.sha256;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * Created by Erling Molde on 17.03.2016.
 */
public class Verifier {
    private Map<Key, String> options;

    public static void main(String[] args) {
        try {
            new Verifier().run(args);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void run(String[] args) throws IOException, NoSuchAlgorithmException {
        options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = options(args, options, unknownOptions);
        if (unknownOptions.isEmpty()) {
            processNoOptionsArgs(options, noOptionsArgs);
            if (options.containsKey(Key.file)) {
                if (options.containsKey(Key.hash))
                    verifyHash(options.get(Key.hash), options.get(Key.file));
                else
                    showHash(options.get(Key.file));
            } else {
                help();
            }
        } else {
            System.out.println("Unknown options: " + unknownOptions);
            help();
        }
    }

    void processNoOptionsArgs(Map<Key, String> options, List<String> noOptionsArgs) {
        switch (noOptionsArgs.size()) {
            case 1:
                if (!options.containsKey(Key.file))
                    options.put(Key.file, noOptionsArgs.get(0).trim());
                else if (!options.containsKey(Key.hash)) {
                    String hashValue = noOptionsArgs.get(0).trim();
                    options.put(Key.hash, hashValue);
                }
                break;
            case 2:
                if (!options.containsKey(Key.file))
                    options.put(Key.file, noOptionsArgs.get(1).trim());
                if (!options.containsKey(Key.hash)) {
                    String hashValue = noOptionsArgs.get(0).trim();
                    options.put(Key.hash, hashValue);
                    checkDigest(options);
                }
                break;
        }
        options.putIfAbsent(Key.digest, "SHA-256");
    }

    private void checkDigest(Map<Key, String> options) {
        options.computeIfAbsent(Key.digest, key -> lookup(options.get(Key.hash)));
    }

    private String lookup(String hash) {
        switch (hash.length()) {
            case 40:
                return "SHA-1";
            case 64:
                return "SHA-256";
            case 96:
                return "SHA-384";
            case 128:
                return "SHA-512";
            case 32:
                return "MD5";
            default:
                throw new RuntimeException("Unknown length of hash string, can't guess which algorithm");
        }
    }

    enum Key {digest, hash, file}

    List<String> options(String[] allArgs, Map<Key, String> options, List<String> unknownOptions) {
        List<String> args = new ArrayList<>();
        List<String> argsList = Arrays.asList(allArgs);
        Key[] keys = Key.values();
        for (Iterator<String> iterator = argsList.iterator(); iterator.hasNext(); ) {
            String arg = iterator.next();
            int ix = 0;
            while (ix < arg.length() && arg.charAt(ix) == '-')
                ix++;
            if (ix > 0) {
                String option = arg.substring(ix);
                String name;
                String value = null;
                int ixEq = option.indexOf('=');
                if (ixEq == -1) {
                    name = option.trim();
                    if (iterator.hasNext())
                        value = iterator.next();
                } else {
                    name = option.substring(0, ixEq).trim();
                    value = option.substring(ixEq + 1).trim();
                }
                Key keyToUse = null;
                for (Key key: keys) {
                    if (name.regionMatches(true, 0, key.name(), 0, name.length())) {
                        keyToUse = key;
                        break;
                    }
                }
                if (keyToUse == null) {
                    unknownOptions.add(name);
                } else {
                    options.put(keyToUse, value);
                }
            } else {
                args.add(arg);
            }
        }
        return args;
    }

    private void verifyHash(String hashToMatch, String fileName) throws IOException, NoSuchAlgorithmException {
        String digestAlgorithm = getHashAlgorithm();
        String hash = getHash(fileName, digestAlgorithm);
        System.out.printf("Match using %s: %b%n", digestAlgorithm, hash.equalsIgnoreCase(hashToMatch));
    }

    private String getHashAlgorithm() {
        return System.getProperty("digest", options.get(Key.digest));
    }

    private void showHash(String fileName) throws IOException, NoSuchAlgorithmException {
        String digestAlgorithm = getHashAlgorithm();
        String hash = getHash(fileName, digestAlgorithm);
        System.out.printf("Hash %s: %s%n", digestAlgorithm, hash);
    }

    private String getHash(String fileName, String digestAlgorithm) throws NoSuchAlgorithmException, IOException {
        Path path = Paths.get(fileName);
        MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
        try (FileChannel byteChannel = (FileChannel) Files.newByteChannel(path, StandardOpenOption.READ)) {
            MappedByteBuffer mappedByteBuffer = byteChannel.map(FileChannel.MapMode.READ_ONLY, 0, byteChannel.size());
            messageDigest.update(mappedByteBuffer);
        }
        byte[] digest = messageDigest.digest();
        return toHexString(digest);
    }

    private String toHexString(byte[] digest) {
        char[] hexDigs = new char[digest.length << 1];
        for (int i = 0; i < digest.length; i++) {
            byte b = digest[i];
            hexDigs[i << 1] = Character.forDigit(b >> 4 & 0xf, 16);
            hexDigs[(i << 1) | 1] = Character.forDigit(b & 0xf, 16);
        }
        return String.valueOf(hexDigs);
    }

    private void help() {
        System.out.println("Usage:");
        System.out.println("  Supply one or two arguments.");
        System.out.println("  One argument version: supply name of file to be checked as argument");
        System.out.println("  Two argument version: first argument: hash to match, second argument: name of file");
        System.out.println("  With named arguments (names may be abbreviated):");
        System.out.println(
                "    -digest=<algorithm> or -digest <algorithm>, algorithm is any available algorithm from Java");
        System.out.println("    -file=<file> or -file <file>, file to check");
        System.out.println("    -hash=<hash> or -hash <hash>, hash to verify");
        System.out.println("  System variables:");
        System.out.println("    -Ddigest=<algorithm>, default algorithm is SHA-256");
        System.out.println("  Standard hash algorithms as of Java 8:");
        System.out.println("    MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512");
    }
}
