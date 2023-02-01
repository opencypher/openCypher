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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.CucumberFeature;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.Diff;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.Scenario;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.Scenarios;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.TestSuite;

public class TckResultsComparator {
    public static final String CSV = "saveCsv";
    public static final String COMPARE = "compareTo";
    public static final String VERIFY = "verifyFailures";
    public static final String OUTPUT = "output";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("s", CSV, false, "save `base_report` as csv");
        options.addOption("c", COMPARE, true, "compare `base_report` with `arg`. Fail if there are new failed scenarios in `base_report`");
        options.addOption("v", VERIFY, true, "verify that all failing scenarios in `base_report` are in `arg`");
        options.addOption("o", OUTPUT, true, "set output directory");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        checkState(cmd.getArgs().length == 1, "Expected single `base_report` argument. Got %s", cmd.getArgList());

        File baseFile = new File(cmd.getArgs()[0]);
        Scenarios base = getFeatures(baseFile);

        String outputPath = cmd.getOptionValue(OUTPUT, "build/reports/tests/");
        File out = new File(outputPath);
        out.mkdirs();

        if (cmd.hasOption(CSV)) {
            saveCsv(new File(out, "base.csv"), base);
        }

        if (cmd.hasOption(COMPARE)) {
            File otherFile = new File(cmd.getOptionValue(COMPARE));
            Scenarios other = getFeatures(otherFile);
            Diff diff = base.compare(other);
            TckComparisonReport.generate(out, diff);

            checkState(diff.getNewlyFailedScenarios().isEmpty(),
                "Scenarios in %s has failures relatively to %s. See comparison report at %s.",
                otherFile.toURI(),
                baseFile.toURI(),
                out.toURI()
            );
        }

        if (cmd.hasOption(VERIFY)) {
            File otherFile = new File(cmd.getOptionValue(VERIFY));
            Scenarios other = getFeatures(otherFile);

            Diff diff = base.verify(other);
            TckComparisonReport.generate(out, diff);

            checkState(diff.getNewlyFailedScenarios().isEmpty() && diff.getNewlyPassingScenarios().isEmpty(),
                "Scenarios in %s does not match %s. See comparison report at %s.",
                otherFile.toURI(),
                baseFile.toURI(),
                out.toURI());
        }

        if (cmd.getOptions().length == 0 || (cmd.getOptions().length == 1 && cmd.hasOption(OUTPUT))) {
            printHelp(options);
        }

    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("TckResultsComparator base_report", "", options,
            "`base_report` and `arg` could be .xml or .csv", true);
    }

    private static void saveCsv(File file, Scenarios base) throws IOException {
        FileWriter out = new FileWriter(file);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader("feature", "scenario", "status"))) {
            for (Scenario scenario : base.all()) {
                printer.printRecord(scenario.getFeatureName(), scenario.getName(), scenario.getStatus().toLowerCase());
            }
        }
        System.out.println("Result saved to " + file.toURI());
    }

    private static Scenarios getFeatures(File reportFile) throws IOException {
        checkState(reportFile.exists(), "File %s doesn't exist, but is required for TCK reports comparison.", reportFile.toURI());

        String ext = reportFile.getName().substring(reportFile.getName().lastIndexOf("."));

        switch (ext) {
            case ".xml":
                return getFeaturesFromXml(reportFile);
            case ".csv":
                return getFeaturesFromCsv(reportFile);
            case ".json":
                return getFeaturesFromJson(reportFile);
            default:
                throw new IllegalStateException(format("File %s must have .xml or .csv extension", reportFile.toURI()));
        }
    }

    private static Scenarios getFeaturesFromXml(File reportFile) throws IOException {
        ObjectMapper xmlMapper = new XmlMapper();

        xmlMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        TestSuite value = xmlMapper.readValue(reportFile, TestSuite.class);

        return Scenarios.create(value.getScenarios());
    }

    private static Scenarios getFeaturesFromCsv(File reportFile) throws IOException {
        Reader in = new FileReader(reportFile);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .parse(in);

        List<Scenario> scenarios = new ArrayList<>();
        for (CSVRecord r : records) {
            boolean failed = !r.isMapped("status") || !"passed".equalsIgnoreCase(r.get("status"));
            Scenario scenario = new Scenario(r.get("feature"), r.get("scenario"), failed);
            scenarios.add(scenario);
        }

        return Scenarios.create(scenarios);
    }

    private static Scenarios getFeaturesFromJson(File reportFile) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();

        jsonMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        jsonMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        CucumberFeature[] features = jsonMapper.readValue(reportFile, CucumberFeature[].class);

        List<Scenario> scenarios = Arrays.stream(features)
            .flatMap(f -> f.getScenarios().stream())
            .collect(Collectors.toList());

        return Scenarios.create(scenarios);
    }

    static void checkState(boolean expression, String error, Object... args) {
        if (!expression) {
            throw new IllegalStateException(format(error, args));
        }
    }
}
