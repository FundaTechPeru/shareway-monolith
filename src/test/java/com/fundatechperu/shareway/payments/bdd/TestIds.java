package com.fundatechperu.shareway.payments.bdd;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Readable UUIDs for the scenarios: the alias 300 (or {@code {uuid:300}} inside a request body or
 * path) stands for {@code 00000000-0000-0000-0000-000000000300}.
 */
final class TestIds {

  private static final Pattern ALIAS = Pattern.compile("\\{uuid:(\\d+)}");

  static UUID uuid(long alias) {
    return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(alias));
  }

  /** Replaces every {@code {uuid:N}} alias of the text by its UUID. */
  static String expand(String text) {
    Matcher matcher = ALIAS.matcher(text);
    StringBuilder expanded = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(expanded, uuid(Long.parseLong(matcher.group(1))).toString());
    }
    matcher.appendTail(expanded);
    return expanded.toString();
  }

  private TestIds() {}
}
