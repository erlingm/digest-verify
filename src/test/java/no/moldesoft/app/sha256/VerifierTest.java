package no.moldesoft.app.sha256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsCollectionContaining.*;
import static org.junit.Assert.*;

/*
 * Created by ermo0633 on 29.03.2016.
 */
public class VerifierTest {

    @org.junit.Test
    public void optionsSingleArgumentTest() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "filename".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();

        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);
        assertThat(noOptionsArgs, equalTo(Arrays.asList(args)));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "file"));
        assertThat(options.get("digest"), equalTo("SHA-256"));
        assertThat(options.containsKey("hash"), is(false));
    }

    @org.junit.Test
    public void optionsDoubleArgumentTest() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "hash filename".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();

        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Arrays.asList(args)));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "hash", "file"));
        assertThat(options.get("digest"), equalTo("SHA-256"));
        assertThat(options.get("hash"), equalTo(args[0]));
        assertThat(options.get("file"), equalTo(args[1]));
    }

    @org.junit.Test
    public void optionsNamedArgumentFileWithEqualsign() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "-file=filename".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.emptyList()));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "file"));
        assertThat(options.get("digest"), equalTo("SHA-256"));
        assertThat(options.get("file"), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentHashWithEqualsignSingleArgument() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "-hash=hash filename".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[1])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "hash", "file"));
        assertThat(options.get("digest"), equalTo("SHA-256"));
        assertThat(options.get("hash"), equalTo("hash"));
        assertThat(options.get("file"), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestWithEqualsignSingleArgument() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "-digest=SHA-1 filename".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[1])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "file"));
        assertThat(options.get("digest"), equalTo("SHA-1"));
        assertThat(options.get("file"), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileWithEqualsignSingleArgument() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "-digest=MD5 -file=filename hash".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[2])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "hash", "file"));
        assertThat(options.get("digest"), equalTo("MD5"));
        assertThat(options.get("hash"), equalTo("hash"));
        assertThat(options.get("file"), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileWithSpaceSingleArgument() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "-digest MD5 -file filename hash".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[4])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "hash", "file"));
        assertThat(options.get("digest"), equalTo("MD5"));
        assertThat(options.get("hash"), equalTo("hash"));
        assertThat(options.get("file"), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileWithAbbreviatedOptionsAndSpaceSingleArgument() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "-d MD5 -fi filename hash".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.singletonList(args[4])));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "hash", "file"));
        assertThat(options.get("digest"), equalTo("MD5"));
        assertThat(options.get("hash"), equalTo("hash"));
        assertThat(options.get("file"), equalTo("filename"));
    }

    @org.junit.Test
    public void optionsNamedArgumentDigestFileNoArg() throws Exception {
        Verifier verifier = new Verifier();
        String[] args = "-f filename -d MD5".split(" ");
        Map<String, String> options = new LinkedHashMap<>();
        List<String> unknownOptions = new ArrayList<>();
        List<String> noOptionsArgs = verifier.options(args, options, unknownOptions);

        assertThat(noOptionsArgs, equalTo(Collections.emptyList()));
        assertThat(unknownOptions, equalTo(Collections.emptyList()));

        verifier.processNoOptionsArgs(options, noOptionsArgs);
        assertThat(options.keySet(), hasItems("digest", "file"));
        assertThat(options.get("digest"), equalTo("MD5"));
        assertThat(options.get("file"), equalTo("filename"));
    }
}