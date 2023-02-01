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

import static freemarker.template.Configuration.VERSION_2_3_23;
import static freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.opencypher.tools.tck.reporting.comparison.TckReportModel.Diff;


public class TckComparisonReport {
    public static void generate(File outputDir, Diff diff) throws IOException, TemplateException {
        File report = new File(outputDir, "comparison.html");

        Configuration cfg = new Configuration(VERSION_2_3_23);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(RETHROW_HANDLER);
        ClassTemplateLoader templateLoader = new ClassTemplateLoader(TckComparisonReport.class, "");
        cfg.setTemplateLoader(templateLoader);

        Map<String, Object> input = new HashMap<>();
        input.put("diff", diff);

        try (Writer fileWriter = new FileWriter(report)) {
            Template template = cfg.getTemplate("ComparisonReport.ftl");
            template.process(input, fileWriter);
            System.out.println("\nComparison report saved to " + report.toURI());
        }
    }
}
