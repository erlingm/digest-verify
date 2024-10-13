package no.moldesoft.app.sha256;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/*
 * Created by Erling Molde on 17.03.2016.
 */
public class Verifier {
    private static final Pattern commaSplitter = Pattern.compile(",");
    private Map<Key, String> options;

    public static void main(String[] args) {
        try {
            new Verifier().run(args);
        } catch (FileException e) {
            System.out.printf("File \"%s\" not found%n", e.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run(String[] args) {
        options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = options(args, options, unknownOptions);
        if (unknownOptions.isEmpty()) {
            processNoOptionsArgs(options, noOptionsArgs);
            if (options.containsKey(Key.file)) {
                if (options.containsKey(Key.hash)) {
                    verifyHash(options.get(Key.hash), options.get(Key.file));
                } else {
                    showHash(options.get(Key.file));
                }
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
            case 1 -> {
                if (!options.containsKey(Key.file)) {
                    options.put(Key.file, noOptionsArgs.get(0).trim());
                } else if (!options.containsKey(Key.hash)) {
                    String hashValue = noOptionsArgs.get(0).trim();
                    options.put(Key.hash, hashValue);
                }
            }
            case 2 -> {
                if (!options.containsKey(Key.file)) {
                    options.put(Key.file, noOptionsArgs.get(1).trim());
                }
                if (!options.containsKey(Key.hash)) {
                    String hashValue = noOptionsArgs.get(0).trim();
                    options.put(Key.hash, hashValue);
                    checkDigest(options);
                }
            }
        }
        options.putIfAbsent(Key.digest, "SHA-256");
    }

    private void checkDigest(Map<Key, String> options) {
        options.computeIfAbsent(Key.digest, key -> lookup(options.get(Key.hash)));
    }

    private String lookup(String hash) {
        return switch (hash.length() >>> 1) {
            case 16 -> "MD5,MD4,MD2";
            case 20 -> "SHA-1";
            case 28 -> "SHA-512/224,SHA3-224,SHA-224";
            case 32 -> "SHA-256,SHA-512/256,SHA3-256";
            case 48 -> "SHA-384";
            case 64 -> "SHA-512,SHA3-512";

            default -> throw new RuntimeException("Unknown length of hash string, can't guess which algorithm");
        };
    }

    enum Key {digest, hash, file}

    List<String> options(String[] allArgs, Map<Key, String> options, List<String> unknownOptions) {
        List<String> args = new ArrayList<>();
        List<String> argsList = Arrays.asList(allArgs);
        Key[] keys = Key.values();
        for (Iterator<String> iterator = argsList.iterator(); iterator.hasNext(); ) {
            String arg = iterator.next();
            parseOption(arg).ifPresentOrElse(option -> processOption(option, options, unknownOptions, keys, iterator), () -> args.add(arg));
        }
        return args;
    }

    private static Optional<String> parseOption(String arg) {
        int ix = 0;
        while (ix < arg.length() && arg.charAt(ix) == '-') {
            ix++;
        }
        return ix > 0 && ix < arg.length() ? Optional.of(arg.substring(ix)) : Optional.empty();
    }

    private static void processOption(String option, Map<Key, String> options, List<String> unknownOptions, Key[] keys, Iterator<String> iterator) {
        NameValue nameValue = parseValue(option, iterator);
        Arrays.stream(keys)
                .filter(key -> nameValue.name().regionMatches(true, 0, key.name(), 0, nameValue.name().length()))
                .findFirst()
                .ifPresentOrElse(key -> options.put(key, nameValue.value()), () -> unknownOptions.add(nameValue.name()));
    }

    private static NameValue parseValue(String option, Iterator<String> iterator) {
        int ix = option.indexOf('=');
        return ix == -1
                ? new NameValue(option.trim(), iterator.hasNext() ? iterator.next() : null)
                : new NameValue(option.substring(0, ix).trim(), option.substring(ix + 1).trim());
    }

    private void verifyHash(String hashToMatch, String fileName) {
        String digestAlgorithm = getHashAlgorithm();
        String[] algorithms = commaSplitter.split(digestAlgorithm);
        Arrays.stream(algorithms)
                .filter(algorithm -> getHash(fileName, algorithm).equalsIgnoreCase(hashToMatch))
                .findFirst()
                .ifPresentOrElse(s -> System.out.printf("Match using %s: %b%n", s, true),
                                 () -> System.out.printf("Match using %s: %b%n", String.join(" or ", algorithms), false));
    }

    private String getHashAlgorithm() {
        return System.getProperty("digest", options.get(Key.digest));
    }

    private void showHash(String fileName) {
        commaSplitter.splitAsStream(getHashAlgorithm())
                .forEach(algorithm -> System.out.printf("Hash %s: %s%n", algorithm, getHash(fileName, algorithm)));
    }

    private String getHash(String fileName, String digestAlgorithm) {
        Path path = Paths.get(fileName);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
            try (FileChannel byteChannel = (FileChannel) Files.newByteChannel(path, StandardOpenOption.READ)) {
                MappedByteBuffer mappedByteBuffer = byteChannel.map(FileChannel.MapMode.READ_ONLY, 0, byteChannel.size());
                messageDigest.update(mappedByteBuffer);
            } catch (NoSuchFileException e) {
                throw new FileException(e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            byte[] digest = messageDigest.digest();
            return toHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
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
        String helpText =
                """
                Version: 2.2
                Usage:
                  Supply one or two arguments.
                  One argument version: supply name of file to be checked as argument
                  Two argument version: first argument: hash to match, second argument: name of file
                  With named arguments (names may be abbreviated):
                    -digest=<algorithm> or -digest <algorithm>, algorithm is any available algorithm from Java
                    -file=<file> or -file <file>, file to check
                    -hash=<hash> or -hash <hash>, hash to verify
                  System variables:
                    -Ddigest=<algorithm>, default algorithm is SHA-256
                  Standard hash algorithms as of Java 17:
                    MD2, MD4, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512, SHA-512/224, SHA-512/256, SHA3-224, SHA3-256, SHA3-512""";
        System.out.println(helpText);
    }

    private record NameValue(String name, String value) {}

    private static class FileException extends RuntimeException {

        private final String fileName;

        public FileException(NoSuchFileException e) {
            fileName = e.getFile();
        }

        public String getFileName() {
            return fileName;
        }
    }
}
