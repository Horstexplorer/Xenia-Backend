/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.xenia.backend.processor.root.info.metrics;

import de.netbeacon.utils.sql.connectionpool.SQLConnectionPool;
import de.netbeacon.xenia.backend.client.objects.Client;
import de.netbeacon.xenia.backend.processor.RequestProcessor;
import de.netbeacon.xenia.backend.processor.ws.PrimaryWebsocketProcessor;
import io.javalin.http.Context;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class InfoMetrics extends RequestProcessor {

    private final CollectorRegistry collectorRegistry;

    public InfoMetrics(SQLConnectionPool sqlConnectionPool, PrimaryWebsocketProcessor websocketProcessor) {
        super("metrics", sqlConnectionPool, websocketProcessor);
        this.collectorRegistry = CollectorRegistry.defaultRegistry;
    }

    @Override
    public void get(Client client, Context ctx) {
        String contentType = TextFormat.chooseContentType(ctx.header("Accept"));
        Set<String> includedParam = ctx.pathParamMap().containsKey("name[]") ? new HashSet<>(ctx.queryParams("name[]")) : Collections.emptySet();
        ctx.status(200);
        ctx.contentType(contentType);
        StringBuilder stringBuilder = new StringBuilder();
        TextFormat.writeFormat(contentType, stringBuilder, collectorRegistry.filteredMetricFamilySamples(includedParam));
        ctx.result(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }


    /**
     * This is a copy of `io.prometheus.client.exporter.common.TextFormat` but the writer is switched to a string builder because that will actually work
     * A different license may apply
     */
    protected static class TextFormat {
        /**
         * Content-type for Prometheus text version 0.0.4.
         */
        public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

        /**
         * Content-type for Openmetrics text version 1.0.0.
         */
        public final static String CONTENT_TYPE_OPENMETRICS_100 = "application/openmetrics-text; version=1.0.0; charset=utf-8";

        /**
         * Return the content type that should be used for a given Accept HTTP header.
         */
        public static String chooseContentType(String acceptHeader) {
            if (acceptHeader == null) {
                return CONTENT_TYPE_004;
            }

            for (String accepts : acceptHeader.split(",")) {
                if ("application/openmetrics-text".equals(accepts.split(";")[0].trim())) {
                    return CONTENT_TYPE_OPENMETRICS_100;
                }
            }

            return CONTENT_TYPE_004;
        }

        /**
         * Write out the given MetricFamilySamples in a format per the contentType.
         */
        public static void writeFormat(String contentType, StringBuilder writer, Enumeration<Collector.MetricFamilySamples> mfs) {
            if (CONTENT_TYPE_004.equals(contentType)) {
                write004(writer, mfs);
                return;
            }
            if (CONTENT_TYPE_OPENMETRICS_100.equals(contentType)) {
                writeOpenMetrics100(writer, mfs);
                return;
            }
            throw new IllegalArgumentException("Unknown contentType " + contentType);
        }

        /**
         * Write out the text version 0.0.4 of the given MetricFamilySamples.
         */
        public static void write004(StringBuilder writer, Enumeration<Collector.MetricFamilySamples> mfs) {
            Map<String, Collector.MetricFamilySamples> omFamilies = new TreeMap<String, Collector.MetricFamilySamples>();
            /* See http://prometheus.io/docs/instrumenting/exposition_formats/
             * for the output format specification. */
            while(mfs.hasMoreElements()) {
                Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
                String name = metricFamilySamples.name;
                writer.append("# HELP ");
                writer.append(name);
                if (metricFamilySamples.type == Collector.Type.COUNTER) {
                    writer.append("_total");
                }
                if (metricFamilySamples.type == Collector.Type.INFO) {
                    writer.append("_info");
                }
                writer.append(' ');
                writeEscapedHelp(writer, metricFamilySamples.help);
                writer.append('\n');

                writer.append("# TYPE ");
                writer.append(name);
                if (metricFamilySamples.type == Collector.Type.COUNTER) {
                    writer.append("_total");
                }
                if (metricFamilySamples.type == Collector.Type.INFO) {
                    writer.append("_info");
                }
                writer.append(' ');
                writer.append(typeString(metricFamilySamples.type));
                writer.append('\n');

                String createdName = name + "_created";
                String gcountName = name + "_gcount";
                String gsumName = name + "_gsum";
                for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
                    /* OpenMetrics specific sample, put in a gauge at the end. */
                    if (sample.name.equals(createdName)
                            || sample.name.equals(gcountName)
                            || sample.name.equals(gsumName)) {
                        Collector.MetricFamilySamples omFamily = omFamilies.get(sample.name);
                        if (omFamily == null) {
                            omFamily = new Collector.MetricFamilySamples(sample.name, Collector.Type.GAUGE, metricFamilySamples.help, new ArrayList<Collector.MetricFamilySamples.Sample>());
                            omFamilies.put(sample.name, omFamily);
                        }
                        omFamily.samples.add(sample);
                        continue;
                    }
                    writer.append(sample.name);
                    if (sample.labelNames.size() > 0) {
                        writer.append('{');
                        for (int i = 0; i < sample.labelNames.size(); ++i) {
                            writer.append(sample.labelNames.get(i));
                            writer.append("=\"");
                            writeEscapedLabelValue(writer, sample.labelValues.get(i));
                            writer.append("\",");
                        }
                        writer.append('}');
                    }
                    writer.append(' ');
                    writer.append(Collector.doubleToGoString(sample.value));
                    if (sample.timestampMs != null){
                        writer.append(' ');
                        writer.append(sample.timestampMs.toString());
                    }
                    writer.append('\n');
                }
            }
            // Write out any OM-specific samples.
            if (!omFamilies.isEmpty()) {
                write004(writer, Collections.enumeration(omFamilies.values()));
            }
        }

        private static void writeEscapedHelp(StringBuilder writer, String s) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '\\':
                        writer.append("\\\\");
                        break;
                    case '\n':
                        writer.append("\\n");
                        break;
                    default:
                        writer.append(c);
                }
            }
        }

        private static void writeEscapedLabelValue(StringBuilder writer, String s) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '\\':
                        writer.append("\\\\");
                        break;
                    case '\"':
                        writer.append("\\\"");
                        break;
                    case '\n':
                        writer.append("\\n");
                        break;
                    default:
                        writer.append(c);
                }
            }
        }

        private static String typeString(Collector.Type t) {
            switch (t) {
                case GAUGE:
                    return "gauge";
                case COUNTER:
                    return "counter";
                case SUMMARY:
                    return "summary";
                case HISTOGRAM:
                    return "histogram";
                case GAUGE_HISTOGRAM:
                    return "histogram";
                case STATE_SET:
                    return "gauge";
                case INFO:
                    return "gauge";
                default:
                    return "untyped";
            }
        }

        /**
         * Write out the OpenMetrics text version 1.0.0 of the given MetricFamilySamples.
         */
        public static void writeOpenMetrics100(StringBuilder writer, Enumeration<Collector.MetricFamilySamples> mfs) {
            while(mfs.hasMoreElements()) {
                Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
                String name = metricFamilySamples.name;

                writer.append("# TYPE ");
                writer.append(name);
                writer.append(' ');
                writer.append(omTypeString(metricFamilySamples.type));
                writer.append('\n');

                if (!metricFamilySamples.unit.isEmpty()) {
                    writer.append("# UNIT ");
                    writer.append(name);
                    writer.append(' ');
                    writer.append(metricFamilySamples.unit);
                    writer.append('\n');
                }

                writer.append("# HELP ");
                writer.append(name);
                writer.append(' ');
                writeEscapedLabelValue(writer, metricFamilySamples.help);
                writer.append('\n');

                for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {
                    writer.append(sample.name);
                    if (sample.labelNames.size() > 0) {
                        writer.append('{');
                        for (int i = 0; i < sample.labelNames.size(); ++i) {
                            if (i > 0) {
                                writer.append(",");
                            }
                            writer.append(sample.labelNames.get(i));
                            writer.append("=\"");
                            writeEscapedLabelValue(writer, sample.labelValues.get(i));
                            writer.append("\"");
                        }
                        writer.append('}');
                    }
                    writer.append(' ');
                    writer.append(Collector.doubleToGoString(sample.value));
                    if (sample.timestampMs != null){
                        writer.append(' ');
                        long ts = sample.timestampMs.longValue();
                        writer.append(Long.toString(ts / 1000));
                        writer.append(".");
                        long ms = ts % 1000;
                        if (ms < 100) {
                            writer.append("0");
                        }
                        if (ms < 10) {
                            writer.append("0");
                        }
                        writer.append(Long.toString(ts % 1000));

                    }
                    writer.append('\n');
                }
            }
            writer.append("# EOF\n");
        }

        private static String omTypeString(Collector.Type t) {
            switch (t) {
                case GAUGE:
                    return "gauge";
                case COUNTER:
                    return "counter";
                case SUMMARY:
                    return "summary";
                case HISTOGRAM:
                    return "histogram";
                case GAUGE_HISTOGRAM:
                    return "gauge_histogram";
                case STATE_SET:
                    return "stateset";
                case INFO:
                    return "info";
                default:
                    return "unknown";
            }
        }
    }
}
