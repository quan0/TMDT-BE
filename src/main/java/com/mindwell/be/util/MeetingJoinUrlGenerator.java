package com.mindwell.be.util;

import com.mindwell.be.entity.MeetingPlatform;

import java.security.SecureRandom;

public final class MeetingJoinUrlGenerator {
    private MeetingJoinUrlGenerator() {
    }

    public static String generate(MeetingPlatform platform) {
        String key = platform == null ? null : platform.getPlatformKey();
        SecureRandom random = new SecureRandom();

        if ("google_meet".equals(key)) {
            return "https://meet.google.com/" + randomMeetCode(random);
        }
        if ("zoom".equals(key)) {
            long meetingId = 100_000_000L + Math.abs(random.nextLong() % 900_000_000L);
            return "https://zoom.us/j/" + meetingId;
        }
        if ("ms_teams".equals(key)) {
            return "https://teams.microsoft.com/l/meetup-join/" + java.util.UUID.randomUUID();
        }
        return null;
    }

    private static String randomMeetCode(SecureRandom random) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        sb.append('-');
        for (int i = 0; i < 4; i++) sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        sb.append('-');
        for (int i = 0; i < 3; i++) sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return sb.toString();
    }
}
