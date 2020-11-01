package com.yjq.springbootdemoprometheus.colletor;


import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author monstar
 * @create 2020/11/1 15:34
 */
@Component
public class StrCollectorRegistry {
    public static final StrCollectorRegistry defaultRegistry = new StrCollectorRegistry(true);
    private final Map<StrCollector, List<String>> collectorsToNames;
    private final Map<String, StrCollector> namesToCollectors;
    private final boolean autoDescribe;

    public StrCollectorRegistry() {
        this(false);
    }

    public StrCollectorRegistry(boolean autoDescribe) {
        this.collectorsToNames = new HashMap();
        this.namesToCollectors = new HashMap();
        this.autoDescribe = autoDescribe;
    }

    public void register(StrCollector m) {
        List<String> names = this.collectorNames(m);
        synchronized(this.collectorsToNames) {
            Iterator var4 = names.iterator();

            String name;
            while(var4.hasNext()) {
                name = (String)var4.next();
                if (this.namesToCollectors.containsKey(name)) {
                    throw new IllegalArgumentException("Collector already registered that provides name: " + name);
                }
            }

            var4 = names.iterator();

            while(var4.hasNext()) {
                name = (String)var4.next();
                this.namesToCollectors.put(name, m);
            }

            this.collectorsToNames.put(m, names);
        }
    }

    public void unregister(StrCollector m) {
        synchronized(this.collectorsToNames) {
            List<String> names = (List)this.collectorsToNames.remove(m);
            Iterator var4 = names.iterator();

            while(var4.hasNext()) {
                String name = (String)var4.next();
                this.namesToCollectors.remove(name);
            }

        }
    }

    public void clear() {
        synchronized(this.collectorsToNames) {
            this.collectorsToNames.clear();
            this.namesToCollectors.clear();
        }
    }

    private Set<StrCollector> collectors() {
        synchronized(this.collectorsToNames) {
            return new HashSet(this.collectorsToNames.keySet());
        }
    }

    private List<String> collectorNames(StrCollector m) {
        List mfs;
        if (m instanceof StrCollector.Describable) {
            mfs = ((StrCollector.Describable)m).describe();
        } else if (this.autoDescribe) {
            mfs = m.collect();
        } else {
            mfs = Collections.emptyList();
        }

        List<String> names = new ArrayList();
        Iterator var4 = mfs.iterator();

        while(var4.hasNext()) {
            StrCollector.MetricFamilySamples family = (StrCollector.MetricFamilySamples)var4.next();
            switch(family.type) {
//                case SUMMARY:
//                    names.add(family.name + "_count");
//                    names.add(family.name + "_sum");
//                    names.add(family.name);
//                    break;
//                case HISTOGRAM:
//                    names.add(family.name + "_count");
//                    names.add(family.name + "_sum");
//                    names.add(family.name + "_bucket");
//                    names.add(family.name);
//                    break;
                default:
                    names.add(family.name);
            }
        }

        return names;
    }

    public Enumeration<StrCollector.MetricFamilySamples> metricFamilySamples() {
        return new StrCollectorRegistry.MetricFamilySamplesEnumeration();
    }

    public Enumeration<StrCollector.MetricFamilySamples> filteredMetricFamilySamples(Set<String> includedNames) {
        return new StrCollectorRegistry.MetricFamilySamplesEnumeration(includedNames);
    }

    public String getSampleValue(String name) {
        return this.getSampleValue(name, new String[0], new String[0]);
    }

    public String getSampleValue(String name, String[] labelNames, String[] labelValues) {
        Iterator var4 = Collections.list(this.metricFamilySamples()).iterator();

        while(var4.hasNext()) {
            StrCollector.MetricFamilySamples metricFamilySamples = (StrCollector.MetricFamilySamples)var4.next();
            Iterator var6 = metricFamilySamples.samples.iterator();

            while(var6.hasNext()) {
                StrCollector.MetricFamilySamples.Sample sample = (StrCollector.MetricFamilySamples.Sample)var6.next();
                if (sample.name.equals(name) && Arrays.equals(sample.labelNames.toArray(), labelNames) && Arrays.equals(sample.labelValues.toArray(), labelValues)) {
                    return sample.value;
                }
            }
        }

        return null;
    }

    class MetricFamilySamplesEnumeration implements Enumeration<StrCollector.MetricFamilySamples> {
        private final Iterator<StrCollector> collectorIter;
        private Iterator<StrCollector.MetricFamilySamples> metricFamilySamples;
        private StrCollector.MetricFamilySamples next;
        private Set<String> includedNames;

        MetricFamilySamplesEnumeration(Set<String> includedNames) {
            this.includedNames = includedNames;
            this.collectorIter = this.includedCollectorIterator(includedNames);
            this.findNextElement();
        }

        private Iterator<StrCollector> includedCollectorIterator(Set<String> includedNames) {
            if (includedNames.isEmpty()) {
                return StrCollectorRegistry.this.collectors().iterator();
            } else {
                HashSet<StrCollector> collectors = new HashSet();
                synchronized(StrCollectorRegistry.this.namesToCollectors) {
                    Iterator var4 = StrCollectorRegistry.this.namesToCollectors.entrySet().iterator();

                    while(var4.hasNext()) {
                        Map.Entry<String, StrCollector> entry = (Map.Entry)var4.next();
                        if (includedNames.contains(entry.getKey())) {
                            collectors.add(entry.getValue());
                        }
                    }

                    return collectors.iterator();
                }
            }
        }

        MetricFamilySamplesEnumeration() {
            this(Collections.emptySet());
        }

        private void findNextElement() {
            this.next = null;

            while(this.metricFamilySamples != null && this.metricFamilySamples.hasNext()) {
                this.next = this.filter((StrCollector.MetricFamilySamples)this.metricFamilySamples.next());
                if (this.next != null) {
                    return;
                }
            }

            if (this.next == null) {
                while(this.collectorIter.hasNext()) {
                    this.metricFamilySamples = ((StrCollector)this.collectorIter.next()).collect().iterator();

                    while(this.metricFamilySamples.hasNext()) {
                        this.next = this.filter((StrCollector.MetricFamilySamples)this.metricFamilySamples.next());
                        if (this.next != null) {
                            return;
                        }
                    }
                }
            }

        }

        private StrCollector.MetricFamilySamples filter(StrCollector.MetricFamilySamples next) {
            if (this.includedNames.isEmpty()) {
                return next;
            } else {
                Iterator it = next.samples.iterator();

                while(it.hasNext()) {
                    if (!this.includedNames.contains(((StrCollector.MetricFamilySamples.Sample)it.next()).name)) {
                        it.remove();
                    }
                }

                if (next.samples.size() == 0) {
                    return null;
                } else {
                    return next;
                }
            }
        }

        public StrCollector.MetricFamilySamples nextElement() {
            StrCollector.MetricFamilySamples current = this.next;
            if (current == null) {
                throw new NoSuchElementException();
            } else {
                this.findNextElement();
                return current;
            }
        }

        public boolean hasMoreElements() {
            return this.next != null;
        }
    }
}

