package br.com.staroski.zipdiff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipDumper {

    private final List<String> ignoreds;
    private final List<String> expandeds;
    private final ZipInputStream zip;

    public ZipDumper(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public ZipDumper(InputStream input) throws IOException {
        this.zip = input instanceof ZipInputStream ? (ZipInputStream) input : new ZipInputStream(input);
        this.ignoreds = new ArrayList<>();
        this.expandeds = new ArrayList<>();
    }

    public ZipDumper(String path) throws IOException {
        this(new File(path));
    }

    public List<String> dump() throws IOException {
        List<String> entries = analize(zip, new ArrayList<>(), "");
        Collections.sort(entries);
        return entries;
    }

    public ZipDumper expand(String... filters) {
        for (String filter : filters) {
            expandeds.add(filter);
        }
        return this;
    }

    public ZipDumper ignore(String... filters) {
        for (String filter : filters) {
            ignoreds.add(filter);
        }
        return this;
    }

    private List<String> analize(ZipInputStream zip, List<String> entries, String parent) throws IOException {
        ZipEntry entry = null;
        while ((entry = zip.getNextEntry()) != null) {
            String path = parent + entry.getName();
            if (shouldIgnore(path)) {
                continue;
            }
            entries.add(path);
            if (shouldExpand(path)) {
                analize(readZipEntry(zip), entries, path + "/");
            }
        }
        zip.close();
        return entries;
    }

    private boolean filterApplies(List<String> filters, String entry) {
        for (String filter : filters) {
            if (matchWildCard(filter, entry)) {
                return true;
            }
            if (filter.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchWildCard(String filter, String entry) {
        if (filter.contains("*")) {
            // *word*
            if (filter.startsWith("*") && filter.endsWith("*") && entry.contains(filter.substring(1, filter.length() - 1))) {
                return true;
            }
            // *word
            if (filter.startsWith("*") && entry.endsWith(filter.substring(1))) {
                return true;
            }
            // word*
            if (filter.endsWith("*") && entry.startsWith(filter.substring(0, filter.length() - 1))) {
                return true;
            }
            // word*word
            if (entry.startsWith(filter.substring(0, filter.indexOf("*"))) && entry.endsWith(filter.substring(filter.indexOf("*") + 1))) {
                return true;
            }
        }
        return false;
    }

    private ZipInputStream readZipEntry(ZipInputStream zip) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] bytes = new byte[8192];
        for (int read = -1; (read = zip.read(bytes)) != -1; output.write(bytes, 0, read)) {}
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        return new ZipInputStream(input);
    }

    private boolean shouldExpand(String entry) {
        return filterApplies(expandeds, entry);
    }

    private boolean shouldIgnore(String entry) {
        return filterApplies(ignoreds, entry);
    }
}
