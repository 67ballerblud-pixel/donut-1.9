package com.dilly.donutpay;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Every full-screen show. A payment picks randomly among the shows whose [min, max) range contains it
 * (never the same show twice in a row), so bigger payments get bigger, different animations.
 */
public enum Tier {
    COIN_RAIN(1_000_000L, 10_000_000L, 3400, "$1M Coin Rain", false),
    SLOTS(1_000_000L, 25_000_000L, 5400, "$1M Slots", false),
    DEAL_WITH_IT(5_000_000L, 25_000_000L, 4300, "$5M Shades", false),
    BREAKING_NEWS(10_000_000L, 50_000_000L, 5600, "$10M News", false),
    STONKS(10_000_000L, 100_000_000L, 5200, "$10M Stonks", false),
    WANTED(50_000_000L, 100_000_000L, 5200, "$50M Wanted", false),
    LIGHTNING(50_000_000L, 500_000_000L, 5200, "$50M Lightning", false),
    DIAMOND(50_000_000L, 500_000_000L, 5400, "$50M Diamond", false),
    BIG(100_000_000L, 500_000_000L, 5500, "$100M Energy", false),
    VAULT(100_000_000L, 1_000_000_000L, 6200, "$100M Vault", false),
    FIREWORKS(100_000_000L, 1_000_000_000L, 6400, "$100M Fireworks", false),
    LEGENDARY(500_000_000L, 5_000_000_000L, 6500, "$500M Legend", false),
    METEOR(500_000_000L, 5_000_000_000L, 5800, "$500M Meteor", false),
    MEGA(1_000_000_000L, 5_000_000_000L, 5500, "$1B Rocket", true),
    UFO(1_000_000_000L, 10_000_000_000L, 6600, "$1B UFO", true),
    JACKPOT(5_000_000_000L, 50_000_000_000L, 6500, "$5B Jackpot", true),
    SUPERNOVA(10_000_000_000L, 100_000_000_000L, 8000, "$10B Nova", true),
    PORTAL(10_000_000_000L, 100_000_000_000L, 7200, "$10B Portal", true),
    GOD(50_000_000_000L, 1_000_000_000_000L, 7500, "$50B God", true),
    ECLIPSE(50_000_000_000L, 1_000_000_000_000L, 7600, "$50B Eclipse", true),
    RICH(100_000_000_000L, 1_000_000_000_000L, 6500, "$100B Rich", true),
    STORM(100_000_000_000L, Long.MAX_VALUE, 7000, "$100B Storm", true),
    LEGEND(1_000_000_000_000L, Long.MAX_VALUE, 8500, "$1T Legend", true),
    BIGBANG(1_000_000_000_000L, Long.MAX_VALUE, 9200, "$1T Big Bang", true),

    // ---- second pack (Shows2)
    XP_LEVEL(1_000_000L, 25_000_000L, 4600, "$1M XP Level", false),
    CANNONS(1_000_000L, 10_000_000L, 4800, "$1M Confetti", false),
    TOTEM(5_000_000L, 100_000_000L, 4800, "$5M Totem Pop", false),
    WAVES(5_000_000L, 50_000_000L, 5000, "$5M Waves", false),
    DISCO(10_000_000L, 250_000_000L, 5600, "$10M Disco", false),
    TNT(10_000_000L, 250_000_000L, 5200, "$10M TNT", false),
    MATRIX(25_000_000L, 500_000_000L, 6000, "$25M Matrix", false),
    HOLOGRAM(25_000_000L, 500_000_000L, 5800, "$25M Hologram", false),
    INFERNO(50_000_000L, 500_000_000L, 5600, "$50M Inferno", false),
    TRAIN(50_000_000L, 1_000_000_000L, 6200, "$50M Hype Train", false),
    NEON(100_000_000L, 1_000_000_000L, 6400, "$100M Neon", false),
    BEACON(100_000_000L, 1_000_000_000L, 6400, "$100M Beacon", false),
    GLITCH(100_000_000L, 5_000_000_000L, 5800, "$100M Glitch", false),
    NUKE(500_000_000L, 10_000_000_000L, 6600, "$500M Nuke", true),
    MIDAS(500_000_000L, 10_000_000_000L, 6600, "$500M Midas", true),
    HYPERDRIVE(1_000_000_000L, 100_000_000_000L, 7000, "$1B Hyperdrive", true),
    SHATTER(1_000_000_000_000L, Long.MAX_VALUE, 8000, "$1T Shatter", true);

    public final long min, max;
    public final int ms;
    public final String btn;
    /** Big shows get cinematic letterbox bars. */
    public final boolean cinema;

    Tier(long min, long max, int ms, String btn, boolean cinema) {
        this.min = min; this.max = max; this.ms = ms; this.btn = btn; this.cinema = cinema;
    }

    private static Tier last;

    /** Random show for this amount (avoids repeating the previous one), or null if none fits. */
    public static Tier pick(long amount, Random r) {
        List<Tier> c = new ArrayList<>();
        for (Tier t : values()) if (amount >= t.min && amount < t.max) c.add(t);
        if (c.isEmpty()) return null;
        if (c.size() > 1) c.remove(last);
        Tier t = c.get(r.nextInt(c.size()));
        last = t;
        return t;
    }
}
