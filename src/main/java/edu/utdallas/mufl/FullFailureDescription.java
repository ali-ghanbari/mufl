package edu.utdallas.mufl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FullFailureDescription extends FailureDescription {
    public FullFailureDescription(String firstLine, String[] stackTrace) {
        final int afterAt = 3;
        final String strc = Arrays.stream(stackTrace)
                .map(String::trim)
                .map(s -> s.substring(afterAt))
                .filter(s -> s.startsWith(Config.GROUP_ID))
                .collect(Collectors.joining(","));
        final String info = firstLine + ":" + strc;
        this.setDigestedInfo(info);
    }
}
