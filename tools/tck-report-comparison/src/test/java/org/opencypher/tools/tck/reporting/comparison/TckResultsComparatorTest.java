/*
 * Copyright (c) 2015-2023 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.tools.tck.reporting.comparison;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.UUID;
import junit.framework.AssertionFailedError;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TckResultsComparatorTest {
    private static String test0;
    private static String test1;
    private static String test2;
    private static String test3;
    private static String test4;
    private static String test5;

    @ClassRule
    public static TemporaryFolder temp = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {
        test0 = write(
            "feature,scenario,status,reason\n" +
                "Feature1,Scenario1,passed,n/a\n" +
                "Feature1,Scenario2,passed,n/a\n" +
                "Feature2,Scenario3,passed,n/a",
            ".csv"
        );

        test1 = write(
            "feature,scenario,status\n" +
                "Feature1,Scenario1,failed\n" +
                "Feature1,Scenario2,passed\n" +
                "Feature2,Scenario3,passed\n",
            ".csv"
        );

        test2 = write(
            "feature,scenario,status,reason\n" +
                "Feature1,Scenario1,failed,n/a\n" +
                "Feature1,Scenario2,failed,n/a\n",
            ".csv"
        );

        test3 = write(
            "feature,scenario,status,reason\n" +
                "Feature1,Scenario1,failed,n/a\n" +
                "Feature1,Scenario2,failed,n/a\n" +
                "Feature2,Scenario3,failed,n/a",
            ".csv"
        );

        test4 = write(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<testsuite name=\"org.opencypher.gremlin.tck.TckTest\" tests=\"958\" skipped=\"0\" failures=\"80\" errors=\"0\" timestamp=\"2019-05-15T11:38:52\" hostname=\"blkbox\" time=\"84.597\">\n" +
                "  <properties/>\n" +
                "  <testcase name=\"Feature &quot;Feature1&quot;: Scenario &quot;Scenario1&quot;\" classname=\"org.opencypher.tools.tck.TckTest\" time=\"5.638\">\n" +
                "     <failure message=\"org.opencypher.tools.tck.api.Scenario$ScenarioFailedException:\"/>\n" +
                "  </testcase>  \n" +
                "  <testcase name=\"Feature &quot;Feature1&quot;: Scenario &quot;Scenario2&quot;\" classname=\"org.opencypher.tools.tck.TckTest\" time=\"5.638\"/>\n" +
                "  <testcase name=\"Feature &quot;Feature2&quot;: Scenario &quot;Scenario3&quot;\" classname=\"org.opencypher.tools.tck.TckTest\" time=\"5.638\"/>\n" +
                "</testsuite>\n",
            ".xml"
        );

        String cucumber = getResource("cucumber.json");
        test5 = write(cucumber, ".json");
    }

    @Test
    public void testCompareImprove() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        comparator(test0, "--compareTo", test1, "--output", path);
        String content = read(path, "comparison.html");

        assertThat(content, containsString("TCK coverage improved"));
        assertThat(content, containsString(passing(1)));
    }

    @Test
    public void testCompareNoChange() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        comparator(test0, "--compareTo", test0, "--output", path);
        String content = read(path, "comparison.html");

        assertThat(content, containsString("Nothing changed"));
    }

    @Test
    public void testCompareDegrade() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        assertThat(comparatorException(test1, "--compareTo", test0, "--output", path),
            containsString("has failures"));

        String content = read(path, "comparison.html");

        assertThat(content, containsString("TCK coverage degraded"));
        assertThat(content, containsString(failing(1)));
    }

    @Test
    public void testCompareXml() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        assertThat(comparatorException(test4, "--compareTo", test0, "--output", path),
            containsString("has failures"));

        String content = read(path, "comparison.html");

        assertThat(content, containsString("TCK coverage degraded"));
        assertThat(content, containsString(failing(1)));
    }

    @Test
    public void testCompareJson() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        assertThat(comparatorException(test5, "--compareTo", test0, "--output", path),
            containsString("has failures"));

        String content = read(path, "comparison.html");

        assertThat(content, containsString("TCK coverage degraded"));
        assertThat(content, containsString(failing(1)));
    }

    @Test
    public void testDiffSize() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        assertThat(comparatorException(test1, "--compareTo", test2, "--output", path),
            containsString("number of all scenarios changed"));

        assertThat(comparatorException(test2, "--compareTo", test1, "--output", path),
            containsString("number of all scenarios changed"));

        assertThat(new File(path, "comparison.html").exists(), is(false));
    }


    @Test
    public void testVerifyOnlyFailed() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        assertThat(comparatorException(test0, "--verifyFailures", test1, "--output", path),
            containsString("only failed scenarios"));

        assertThat(new File(path, "comparison.html").exists(), is(false));
    }

    @Test
    public void testVerifyImprove() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        assertThat(comparatorException(test0, "--verifyFailures", test2, "--output", path),
            containsString("does not match"));

        String content = read(path, "comparison.html");

        assertThat(content, containsString(passing(2)));
    }

    @Test
    public void testVerifyNoChange() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        comparator(test2, "--verifyFailures", test2, "--output", path);

        String content = read(path, "comparison.html");

        assertThat(content, containsString("Nothing changed"));
    }

    @Test
    public void testVerifyDegrade() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        assertThat(comparatorException(test3, "--verifyFailures", test2, "--output", path),
            containsString("does not match"));

        String content = read(path, "comparison.html");

        assertThat(content, containsString(failing(1)));
    }

    @Test
    public void testConvertXmlCsv() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        comparator(test4, "--saveCsv", "--output", path);

        String actual = read(path, "base.csv").replaceAll("\r\n", "\n");

        String expected = read("", test1);

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testConvertJsonCsv() throws Exception {
        String path = temp.newFolder().getAbsolutePath();

        comparator(test5, "--saveCsv", "--output", path);

        String actual = read(path, "base.csv").replaceAll("\r\n", "\n");

        String expected = read("", test1);

        assertThat(actual, equalTo(expected));
    }

    private void comparator(String... args) throws Exception {
        TckResultsComparator.main(args);
    }

    private String comparatorException(String... args) {
        try {
            comparator(args);
            throw new AssertionFailedError("Exception expected");
        } catch (Exception t) {
            return t.getMessage();
        }
    }

    private String passing(int i) {
        return format("Newly passing scenarios: <span class=\"label label-success\">%s</span>", i);
    }

    private String failing(int i) {
        return format("Newly failed scenarios: <span class=\"label label-danger\">%s</span>", i);
    }


    private static String write(String content, String extension) throws Exception {
        File file = temp.newFile(UUID.randomUUID() + extension);
        Files.write(file.toPath(), content.getBytes(), StandardOpenOption.CREATE);
        return file.getAbsolutePath();
    }

    private String read(String path, String file) throws IOException {
        return new String(readAllBytes(new File(path, file).toPath()));
    }

    static String getResource(String path) throws IOException {
        InputStream resource = TckResultsComparatorTest.class.getResourceAsStream(path);
        if (resource == null) {
            throw new IOException(path + " not found");
        }
        return new Scanner(resource, "UTF-8").useDelimiter("\\A").next();
    }
}
