package no.moldesoft.app.sha256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/*
 * Created by ermo0633 on 29.03.2016.
 */
public class VerifierTest {

    @org.junit.Test
    public void optionsSingleArgumentTest() {
        Verifier verifier = new Verifier();
        String[] args = "filename".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();

        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);
        assertThat(noOptionsArgs, equalTo(Arrays.asList(args)));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.file));
        assertThat(options.get(Key.digest), equalTo("SHA-256"));
        assertThat(options.containsKey(Key.hash), is(false));
    }

    @org.junit.Test
    public void optionsDoubleArgumentTest() {
        Verifier verifier = new Verifier();
        String[] args = "cae8824cfbf1f5bc35e6fdf386cfb403e06194e1d0563b305fc8ec4961cfb764b7999bcaf1fcc9de7b90806e38e8ff4eb59c8ffea03db73231560b7495002f07 filename".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();

        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Arrays.asList(args)));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.hash, Key.file));
        assertThat(options.get(Key.digest), equalTo("SHA-512,SHA3-512"));
        assertThat(options.get(Key.hash), equalTo(args[0]));
        assertThat(options.get(Key.file), equalTo(args[1]));
    }

    @org.junit.Test
    public void optionsNamedArgumentFileWithEqualsign() {
        Verifier verifier = new Verifier();
        String[] args = "-file=filename".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.emptyList()));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.file));
        assertThat(options.get(Key.digest), equalTo("SHA-256"));
        assertThat(options.get(Key.file), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentHashWithEqualsignSingleArgument() {
        Verifier verifier = new Verifier();
        String[] args = "-hash=hash filename -debug".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[1])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.hash, Key.file));
        assertThat(options.get(Key.digest), equalTo("SHA-256"));
        assertThat(options.get(Key.hash), equalTo("hash"));
        assertThat(options.get(Key.file), equalTo("filename"));
        assertThat(options.containsKey(Key.debug), is(true));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestWithEqualsignSingleArgument() {
        Verifier verifier = new Verifier();
        String[] args = "-digest=SHA-1 filename".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[1])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.file));
        assertThat(options.get(Key.digest), equalTo("SHA-1"));
        assertThat(options.get(Key.file), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileWithEqualsignSingleArgument() {
        Verifier verifier = new Verifier();
        String[] args = "-digest=MD5 -file=filename hash".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[2])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.hash, Key.file));
        assertThat(options.get(Key.digest), equalTo("MD5"));
        assertThat(options.get(Key.hash), equalTo("hash"));
        assertThat(options.get(Key.file), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileWithSpaceSingleArgument() {
        Verifier verifier = new Verifier();
        String[] args = "-digest MD5 -file filename hash".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[4])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.hash, Key.file));
        assertThat(options.get(Key.digest), equalTo("MD5"));
        assertThat(options.get(Key.hash), equalTo("hash"));
        assertThat(options.get(Key.file), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileWithAbbreviatedOptionsAndSpaceSingleArgument() {
        Verifier verifier = new Verifier();
        String[] args = "-d MD5 -fi filename hash".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[4])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.hash, Key.file));
        assertThat(options.get(Key.digest), equalTo("MD5"));
        assertThat(options.get(Key.hash), equalTo("hash"));
        assertThat(options.get(Key.file), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileNoArg() {
        Verifier verifier = new Verifier();
        String[] args = "-f filename -d MD5".split(" ");
        Map<Key, String> options = new EnumMap<>(Key.class);
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.emptyList()));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems(Key.digest, Key.file));
        assertThat(options.get(Key.digest), equalTo("MD5"));
        assertThat(options.get(Key.file), equalTo("filename"));
    }
}
