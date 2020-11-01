package com.yjq.springbootdemoprometheus.colletor;


import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @Author monstar
 * @create 2020/11/1 18:24
 */
public class TextFormat {
    public static final String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";

    public TextFormat() {
    }

    public static void write004(Writer writer, Enumeration<StrCollector.MetricFamilySamples> mfs) throws IOException {
        while(mfs.hasMoreElements()) {
            StrCollector.MetricFamilySamples metricFamilySamples = (StrCollector.MetricFamilySamples)mfs.nextElement();
            writer.write("# HELP ");
            writer.write(metricFamilySamples.name);
            writer.write(32);
            writeEscapedHelp(writer, metricFamilySamples.help);
            writer.write(10);
            writer.write("# TYPE ");
            writer.write(metricFamilySamples.name);
            writer.write(32);
            writer.write(typeString(metricFamilySamples.type));
            writer.write(10);

            for(Iterator var3 = metricFamilySamples.samples.iterator(); var3.hasNext(); writer.write(10)) {
                StrCollector.MetricFamilySamples.Sample sample = (StrCollector.MetricFamilySamples.Sample)var3.next();
                writer.write(sample.name);
                if (sample.labelNames.size() > 0) {
                    writer.write(123);

                    for(int i = 0; i < sample.labelNames.size(); ++i) {
                        writer.write((String)sample.labelNames.get(i));
                        writer.write("=\"");
                        writeEscapedLabelValue(writer, (String)sample.labelValues.get(i));
                        writer.write("\",");
                    }

                    writer.write(125);
                }

                writer.write(32);
                writer.write(sample.value);
                if (sample.timestampMs != null) {
                    writer.write(32);
                    writer.write(sample.timestampMs.toString());
                }
            }
        }

    }

    private static void writeEscapedHelp(Writer writer, String s) throws IOException {
        for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch(c) {
                case '\n':
                    writer.append("\\n");
                    break;
                case '\\':
                    writer.append("\\\\");
                    break;
                default:
                    writer.append(c);
            }
        }

    }

    private static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
        for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch(c) {
                case '\n':
                    writer.append("\\n");
                    break;
                case '"':
                    writer.append("\\\"");
                    break;
                case '\\':
                    writer.append("\\\\");
                    break;
                default:
                    writer.append(c);
            }
        }

    }

    private static String typeString(StrCollector.Type t) {
        return "str";
//        switch(t) {
//            case GAUGE:
//                return "gauge";
//            case COUNTER:
//                return "counter";
//            case SUMMARY:
//                return "summary";
//            case HISTOGRAM:
//                return "histogram";
//            default:
//                return "untyped";
//        }
    }
}
