package br.com.staroski.zipdiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ZipDiff {

    public static List<ZipDiff> compare(List<String> leftDump, List<String> rightDump) {
        List<ZipDiff> diffList = new ArrayList<>();
        List<String> leftCopy = new ArrayList<>(leftDump);
        List<String> rightCopy = new ArrayList<>(rightDump);
        while (!leftCopy.isEmpty()) {
            String entry = leftCopy.remove(0);
            ZipDiff diff = new ZipDiff(entry);
            diff.onLeft = true;
            if (rightCopy.contains(entry)) {
                diff.onRight = true;
                rightCopy.remove(entry);
            }
            diffList.add(diff);
        }
        while (!rightCopy.isEmpty()) {
            String entry = rightCopy.remove(0);
            ZipDiff diff = new ZipDiff(entry);
            diff.onRight = true;
            diffList.add(diff);
        }
        Collections.sort(diffList, (a, b) -> a.entry.compareTo(b.entry));
        return diffList;
    }

    private boolean onLeft;
    private boolean onRight;

    private final String entry;

    private ZipDiff(String entry) {
        this.entry = entry;
    }

    public String getEntry() {
        return entry;
    }

    public boolean isOnLeft() {
        return onLeft;
    }

    public boolean isOnRight() {
        return onRight;
    }
}
