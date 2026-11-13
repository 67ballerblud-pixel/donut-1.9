package com.dilly.donutpay;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

import java.util.Random;

/**
 * Universal "juice" wrapped around EVERY full-screen show (Shows.draw calls under / camera / over):
 *   under  - big-payment sunburst + glow behind the show
 *   camera - slam zoom on impact, soft zoom beat every 0.9s, slow push-in, tiny handheld sway
 *   over   - impact flash + shockwave rings + anime speed lines + anamorphic streak, beat rings, light sweeps,
 *            floating glow motes, vignette, a running light that circles the screen frame, extra impact sound
 * Everything scales with the payment size (inten) and takes its colour from the amount (gold -> cyan -> purple...).
 * Turn it off with "juice": false in config.json.
 */
final class Juice {
    private Juice() {}

    /** 0.5 at $1M up to 1.6 at $1T+. */
    static double inten(long amount) {
        double m = Math.max(1.0, amount / 1_000_000.0);
        return Math.max(0.5, Math.min(1.6, 0.5 + Math.log10(m) / 6.0));
    }

    /** Accent colour that shifts with the amount and slowly cycles. */
    static int color(long amount, long el) {
        double m = Math.max(1.0, amount / 1_000_000.0);
        double t = Math.max(0, Math.min(1, Math.log10(m) / 6.0));
        return Shows.hsv(0.12 + 0.75 * t + el / 9000.0, 0.65, 1.0);
    }

    private static double fade(long el, int dur) {
        return Math.max(0, Math.min(1, Math.min(el / 250.0, (dur - el) / 500.0)));
    }

    // ================================================================ camera (transform only, no push/pop)
    static void camera(Matrix3x2fStack ms, PaymentHud.Note n, long now, int w, int h) {
        if (!Config.get().juice) return;
        long el = now - n.start;
        int dur = n.tier.ms;
        double in = inten(n.amount);
        double punch = 0.05 * in * Math.exp(-el / 180.0);
        double beat = 0;
        for (int k = 1; k <= 8; k++) {
            double d = el - k * 900.0;
            if (d >= 0) beat += 0.012 * in * Math.exp(-d / 150.0);
        }
        double push = 0.035 * in * Math.min(1, el / (double) dur);
        double zoom = 1 + punch + beat + push;
        double sway = Math.sin(el / 900.0) * 0.004 * in;
        ms.translate(w / 2f, h / 2f);
        ms.rotate((float) sway);
        ms.scale((float) zoom, (float) zoom);
        ms.translate(-w / 2f, -h / 2f);
    }

    // ================================================================ behind the show (big payments only)
    static void under(DrawContext ctx, TextRenderer tr, PaymentHud.Note n, long now, int w, int h) {
        if (!Config.get().juice) return;
        double in = inten(n.amount);
        if (in < 0.9) return;
        long el = now - n.start;
        double f = fade(el, n.tier.ms);
        int col = color(n.amount, el), cx = w / 2, cy = h / 2;
        for (int i = 0; i < 4; i++) disc(ctx, cx, cy, 50 + i * 47, argb(0.03 * f * in, col));
        Matrix3x2fStack ms = ctx.getMatrices();
        int rays = 16, reach = Math.max(w, h);
        for (int i = 0; i < rays; i++) {
            ms.pushMatrix();
            ms.translate((float) cx, (float) cy);
            ms.rotate((float) (el / 5000.0 + i * 2 * Math.PI / rays));
            ctx.fill(18, -1, reach, 2, argb(0.07 * f * in, col));
            ms.popMatrix();
        }
    }

    // ================================================================ on top of the show
    static void over(DrawContext ctx, TextRenderer tr, PaymentHud.Note n, long now, int w, int h) {
        if (!Config.get().juice) return;
        long el = now - n.start;
        int dur = n.tier.ms;
        double in = inten(n.amount), f = fade(el, dur);
        int col = color(n.amount, el), cx = w / 2, cy = h / 2;
        Matrix3x2fStack ms = ctx.getMatrices();

        // extra low impact under whatever sound the show already plays
        PaymentHud.cue(n, 30, el, 0, "minecraft:entity.firework_rocket.large_blast", 0.35f, 0.6f + (float) (in * 0.25));

        // vignette
        for (int i = 0; i < 12; i++) {
            double a = 0.34 * f * Math.pow(1 - i / 12.0, 2);
            ctx.fill(i * 2, 0, i * 2 + 2, h, argb(a, 0x000000));
            ctx.fill(w - i * 2 - 2, 0, w - i * 2, h, argb(a, 0x000000));
        }
        ctx.fillGradient(0, 0, w, h / 6, argb(0.30 * f, 0x000000), argb(0, 0x000000));
        ctx.fillGradient(0, h - h / 6, w, h, argb(0, 0x000000), argb(0.30 * f, 0x000000));

        // impact: flash, shockwaves, anamorphic streak, speed lines
        if (el < 900) {
            double t = el;
            if (el < 240) ctx.fill(0, 0, w, h, argb((1 - t / 240.0) * 0.5 * Math.min(1, in), mix(col, 0xFFFFFF, 0.6)));
            double a1 = 1 - t / 900.0;
            ring(ctx, cx, cy, t * 1.1, argb(a1, 0xFFFFFF), 2);
            ring(ctx, cx, cy, t * 0.65, argb(a1 * 0.8, col), 2);
            if (el < 500) {
                int sw = (int) (w * PaymentHud.ease(Math.min(1, t / 200.0)));
                double sa = 1 - t / 500.0;
                ctx.fill(cx - sw / 2, cy - 4, cx + sw / 2, cy + 4, argb(0.18 * sa, col));
                ctx.fill(cx - sw / 2, cy - 1, cx + sw / 2, cy + 1, argb(0.9 * sa, 0xFFFFFF));
            }
            if (el < 450) {
                Random sr = new Random(n.start ^ 0x5EEDL);
                for (int i = 0; i < 28; i++) {
                    double ang = sr.nextDouble() * Math.PI * 2, r0 = 24 + t * (1.0 + sr.nextDouble());
                    double len = 20 + sr.nextDouble() * 50;
                    ms.pushMatrix();
                    ms.translate((float) cx, (float) cy);
                    ms.rotate((float) ang);
                    ctx.fill((int) r0, 0, (int) (r0 + len), 1, argb((1 - t / 450.0) * 0.8, 0xFFFFFF));
                    ms.popMatrix();
                }
            }
        }

        // soft energy ring on every zoom beat
        for (int k = 1; k <= 8; k++) {
            double d = el - k * 900.0;
            if (d < 0 || d > 700) continue;
            ring(ctx, cx, cy, 12 + d * 0.5, argb(0.22 * (1 - d / 700.0) * f * in, col), 1);
        }

        // two light sweeps across the whole screen
        double[] sweeps = {dur * 0.30, dur * 0.62};
        for (double s0 : sweeps) {
            double d = el - s0;
            if (d < 0 || d > 700) continue;
            double bx = -80 + (d / 700.0) * (w + 160);
            ms.pushMatrix();
            ms.translate((float) bx, h / 2f);
            ms.rotate(0.5f);
            ctx.fill(-11, (int) (-h * 0.85), 11, (int) (h * 0.85), argb(0.10 * f, 0xFFFFFF));
            ctx.fill(-1, (int) (-h * 0.85), 2, (int) (h * 0.85), argb(0.22 * f, 0xFFFFFF));
            ms.popMatrix();
        }

        // glow motes drifting up the whole screen
        int count = (int) (30 + 50 * in);
        Random mr = new Random(n.start ^ 0x707L);
        for (int i = 0; i < count; i++) {
            double x0 = mr.nextDouble() * w, spd = 18 + mr.nextDouble() * 42;
            double off = mr.nextDouble() * (h + 8), ph = mr.nextDouble() * 6.28;
            int c = i % 3 == 0 ? col : (i % 3 == 1 ? 0xFFFFFF : 0xFFD700);
            int y = (int) (h + 4 - ((el * spd / 1000.0 + off) % (h + 8)));
            double tw = 0.5 + 0.5 * Math.sin(el / 170.0 + ph);
            int x = (int) (x0 + Math.sin(el / 700.0 + ph) * 5);
            ctx.fill(x, y, x + 1, y + 1, argb(0.75 * tw * f, c));
            if (tw > 0.85) {
                ctx.fill(x - 1, y, x + 2, y + 1, argb(0.45 * f, c));
                ctx.fill(x, y - 1, x + 1, y + 2, argb(0.45 * f, c));
            }
        }

        // frame with a light running around it
        int inset = 3;
        ctx.fill(inset, inset, w - inset, inset + 1, argb(0.14 * f, col));
        ctx.fill(inset, h - inset - 1, w - inset, h - inset, argb(0.14 * f, col));
        ctx.fill(inset, inset, inset + 1, h - inset, argb(0.14 * f, col));
        ctx.fill(w - inset - 1, inset, w - inset, h - inset, argb(0.14 * f, col));
        double perim = 2.0 * (w - 2 * inset) + 2.0 * (h - 2 * inset);
        double head = ((el / 2200.0) % 1.0) * perim;
        for (int i = 0; i < 18; i++) {
            double s = head - i * 5;
            if (s < 0) s += perim;
            int[] p = perim(s, w, h, inset);
            ctx.fill(p[0], p[1], p[0] + 2, p[1] + 2, argb((1 - i / 18.0) * 0.85 * f, i < 2 ? 0xFFFFFF : col));
        }
    }

    // ================================================================ helpers
    private static int[] perim(double s, int w, int h, int inset) {
        int iw = w - 2 * inset, ih = h - 2 * inset;
        if (s < iw) return new int[]{(int) (inset + s), inset};
        s -= iw;
        if (s < ih) return new int[]{w - inset - 2, (int) (inset + s)};
        s -= ih;
        if (s < iw) return new int[]{(int) (w - inset - s - 2), h - inset - 2};
        s -= iw;
        return new int[]{inset, (int) (h - inset - s - 2)};
    }

    /** Dotted circle outline. */
    private static void ring(DrawContext ctx, int cx, int cy, double r, int argb, int size) {
        if (r < 2) return;
        int dots = (int) Math.max(12, Math.min(500, 2 * Math.PI * r / 2.2));
        for (int i = 0; i < dots; i++) {
            double a = i * 2 * Math.PI / dots;
            int x = (int) Math.round(cx + Math.cos(a) * r), y = (int) Math.round(cy + Math.sin(a) * r);
            ctx.fill(x, y, x + size, y + size, argb);
        }
    }

    /** Filled circle, drawn in 2px rows (cheap soft glow). */
    private static void disc(DrawContext ctx, int cx, int cy, int r, int argb) {
        for (int dy = -r; dy < r; dy += 2) {
            int hw = (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            ctx.fill(cx - hw, cy + dy, cx + hw, cy + dy + 2, argb);
        }
    }

    private static int mix(int a, int b, double t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return (((int) (ar + (br - ar) * t)) << 16) | (((int) (ag + (bg - ag) * t)) << 8) | ((int) (ab + (bb - ab) * t));
    }

    private static int argb(double alpha, int rgb) {
        return (Math.max(0, Math.min(255, (int) (alpha * 255))) << 24) | (rgb & 0xFFFFFF);
    }
}
