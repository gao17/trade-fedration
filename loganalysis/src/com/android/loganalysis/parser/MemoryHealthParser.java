/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.loganalysis.parser;

import com.android.loganalysis.item.MemoryHealthItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the memory health file generated by the tests.
 */
public class MemoryHealthParser implements IParser {
    private Map<String, Map<String, Long>> mForeground;
    private Map<String, Map<String, Long>> mBackground;
    private static final Map<String, String> SECTION_MAPPINGS;

    static {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("Average Dalvik Heap", MemoryHealthItem.DALVIK_AVG);
        mappings.put("Average Native Heap", MemoryHealthItem.NATIVE_AVG);
        mappings.put("Average PSS", MemoryHealthItem.PSS_AVG);
        mappings.put("Peak Dalvik Heap", MemoryHealthItem.DALVIK_PEAK);
        mappings.put("Peak Native Heap", MemoryHealthItem.NATIVE_PEAK);
        mappings.put("Peak PSS", MemoryHealthItem.PSS_PEAK);
        SECTION_MAPPINGS = Collections.unmodifiableMap(mappings);
    }

    private static final Pattern COUNT_PATTERN = Pattern.compile("^Count (\\d+)$");
    private static final Pattern METRIC_PATTERN = Pattern.compile("^([^:]+): (\\d+)$");
    private static final Pattern PROCESS_PATTERN = Pattern.compile("^\\S+$");

    @Override
    /**
     * {@inheritDoc}
     */
    public MemoryHealthItem parse(List<String> lines) {
        Map<String, Map<String, Long>> currentSection = null;
        Map<String, Long> currentProcess = new HashMap<String, Long>();
        String processName = null;
        for (String line : lines) {
            if (line.contains("Foreground")) {  // switch to parsing foreground
                mForeground = new HashMap<String, Map<String, Long>>();
                currentSection = mForeground;
            } else if (line.contains("Background")) { //switch to parsing background
                mBackground= new HashMap<String, Map<String, Long>>();
                currentSection = mBackground;
            } else if (COUNT_PATTERN.matcher(line).matches()) {
                // commit current process once we get to count
                currentProcess.put("count", parseLong(line));
                currentSection.put(processName, currentProcess);
            } else if (METRIC_PATTERN.matcher(line).matches()) {
                Matcher m = METRIC_PATTERN.matcher(line);
                m.matches();
                Long value = parseLong(m.group(2));
                String key = SECTION_MAPPINGS.get(m.group(1));
                if (key == null) {
                    continue;
                }
                currentProcess.put(key, value);
            } else if (PROCESS_PATTERN.matcher(line).matches()) {
                processName = line;
                currentProcess = new HashMap<String, Long>();
            }
        }

        return new MemoryHealthItem(mForeground, mBackground);
    }

    private long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public MemoryHealthItem parse(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        return parse(lines);
    }
}
