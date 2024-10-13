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
    private final Map<Key, String> options = new EnumMap<>(Key.class);

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
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = options(args, options, unknownOptions);
        if (unknownOptions.isEmpty()) {
            processNoOptionsArgs(options, noOptionsArgs);
            if (options.containsKey(Key.debug)) {
                System.out.println("options = " + options);
            }
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
                    options.put(Key.file, noOptionsArgs.getFirst().trim());
                } else if (!options.containsKey(Key.hash)) {
                    String hashValue = noOptionsArgs.getFirst().trim();
                    options.put(Key.hash, hashValue);
                }
            }
            case 2 -> {
                if (!options.containsKey(Key.file)) {
                    options.put(Key.file, noOptionsArgs.get(1).trim());
                }
                if (!options.containsKey(Key.hash)) {
                    String hashValue = noOptionsArgs.getFirst().trim();
                    options.put(Key.hash, hashValue);
                    checkDigest(options);
                }
            }
        }
        options.putIfAbsent(Key.digest, getHashAlgorithm());
    }

    private void checkDigest(Map<Key, String> options) {
        options.computeIfAbsent(Key.digest, _ -> lookup(options.get(Key.hash)));
    }

    private String lookup(String hash) {
        return switch (hash.length() >>> 1) {
            case 16 -> "MD5,MD2";
            case 20 -> "SHA-1";
            case 28 -> "SHA-512/224,SHA3-224,SHA-224";
            case 32 -> "SHA-256,SHA-512/256,SHA3-256";
            case 48 -> "SHA-384";
            case 64 -> "SHA-512,SHA3-512";

            default -> throw new RuntimeException("Unknown length of hash string, can't guess which algorithm");
        };
    }

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
        if (arg != null && arg.length() >= 2 && arg.charAt(0) == '-') {
            return Optional.of(arg.substring(1));
        }
        return Optional.empty();
    }

    private static void processOption(String option, Map<Key, String> options, List<String> unknownOptions, Key[] keys, Iterator<String> iterator) {
        NameValue nameValue = parseKeyValue(option, iterator, keys);
        Arrays.stream(keys)
                .filter(key -> nameValue.name().regionMatches(true, 0, key.name(), 0, nameValue.name().length()))
                .findFirst()
                .ifPresentOrElse(key -> options.put(key, nameValue.value()), () -> unknownOptions.add(nameValue.name()));
    }

    private static NameValue parseKeyValue(String option, Iterator<String> iterator, Key[] keys) {
        int ix = option.indexOf('=');
        String optionKey;
        String optionValue = null;
        if (ix == -1) {
            optionKey = option.trim();
            Optional<Key> matchedKey = Arrays.stream(keys).filter(key -> optionKey.regionMatches(true, 0, key.name(), 0, optionKey.length())).findFirst();
            if (matchedKey.isPresent() && matchedKey.get().optionType() == OptionType.property && iterator.hasNext()) {
                optionValue = iterator.next();
            }
        } else {
            optionKey = option.substring(0, ix).trim();
            optionValue = option.substring(ix + 1).trim();
        }
        return new NameValue(optionKey, optionValue);
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
        String digest = options.get(Key.digest);
        if (digest != null) {
            return digest;
        }
        digest = System.getProperty("digest", System.getenv("ms-digest"));
        if (digest != null) {
            return digest;
        }
        return "SHA-256";
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
                          Environment variables (optional):
                            ms-digest=<algorithm>
                          Standard hash algorithms as of Java 23:
                            MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512, SHA-512/224, SHA-512/256, SHA3-224, SHA3-256, SHA3-512""";
        System.out.println(helpText);
    }

}
