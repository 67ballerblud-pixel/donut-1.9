package com.dilly.donutpay;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.Random;

/**
 * 17 more shows (second pack). Same conventions as {@link Shows}: everything is plain fills, text and rotated
 * rectangles, driven only by the elapsed time since the show started.
 *
 *  $1M  XP Level, Confetti Cannons      $5M  Totem Pop, Waves       $10M Disco, TNT
 *  $25M Matrix, Hologram                $50M Inferno, Hype Train    $100M Neon, Beacon, Glitch
 *  $500M Nuke, Midas Touch              $1B Hyperdrive              $1T Reality Shatter
 */
final class Shows2 {
    private Shows2() {}

    private static final int GOLD = 0xFFD700, ORANGE = 0xFFAA00, GREEN = 0x55FF55, WHITE = 0xFFFFFF, RED = 0xFF5555,
        CYAN = 0x38E8FF, GRAY = 0xAAAAAA;
    private static final int[] RAINBOW = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF};

    private static final class G {
        final DrawContext ctx; final TextRenderer tr; final PaymentHud.Note n; final Matrix3x2fStack ms;
        final int w, h, cx, dur; final long el; final double fade; final SkinTextures skin;
        G(DrawContext ctx, TextRenderer tr, PaymentHud.Note n, long now, int w, int h) {
            this.ctx = ctx; this.tr = tr; this.n = n; this.ms = ctx.getMatrices();
            this.w = w; this.h = h; this.cx = w / 2; this.dur = n.tier.ms; this.el = now - n.start;
            this.fade = Math.max(0, Math.min(1, Math.min(el / 250.0, (dur - el) / 500.0)));
            this.skin = PaymentHud.skin(n.name);
        }
    }

    // ================================================================ entry point
    static void draw(DrawContext ctx, TextRenderer tr, PaymentHud.Note n, long now, int w, int h) {
        G g = new G(ctx, tr, n, now, w, h);
        double amp = shakeAmp(g);
        g.ms.pushMatrix();
        if (amp > 0.2) {
            Random r = new Random((g.el / 35) * 31 + n.start);
            g.ms.translate((float) ((r.nextDouble() - 0.5) * 2 * amp), (float) ((r.nextDouble() - 0.5) * 2 * amp));
        }
        switch (n.tier) {
            case XP_LEVEL -> xp(g);
            case CANNONS -> cannons(g);
            case TOTEM -> totem(g);
            case WAVES -> waves(g);
            case DISCO -> disco(g);
            case TNT -> tnt(g);
            case MATRIX -> matrix(g);
            case HOLOGRAM -> hologram(g);
            case INFERNO -> inferno(g);
            case TRAIN -> train(g);
            case NEON -> neon(g);
            case BEACON -> beacon(g);
            case GLITCH -> glitch(g);
            case NUKE -> nuke(g);
            case MIDAS -> midas(g);
            case HYPERDRIVE -> hyperdrive(g);
            case SHATTER -> shatter(g);
            default -> { }
        }
        g.ms.popMatrix();
    }

    private static double shakeAmp(G g) {
        Tier t = g.n.tier;
        long el = g.el;
        if (t == Tier.TNT) { long k = el - 2300; return k > 0 && k < 700 ? 6 * (1 - k / 700.0) : 0; }
        if (t == Tier.NUKE) { long k = el - 1800; return k > 0 && k < 1800 ? 8 * (1 - k / 1800.0) : 0; }
        if (t == Tier.SHATTER) { long k = el - 1500; return k > 0 && k < 900 ? 7 * (1 - k / 900.0) : 0; }
        if (t == Tier.HYPERDRIVE) { long k = el - 3500; return k > 0 && k < 700 ? 5 * (1 - k / 700.0) : 0; }
        return 0;
    }

    // ================================================================ $1M  XP LEVEL
    private static void xp(G g) {
        long el = g.el;
        int barW = 182, barX = g.cx - 91, barY = g.h - 62, total = 44;
        double hp = PaymentHud.ease(c01(el / 400.0));
        head(g, g.cx - 14, (int) (g.h * 0.10), 28);
        text(g, g.n.name, g.cx, (int) (g.h * 0.10) + 32, 1.1f, WHITE, g.fade * hp);

        Random r = new Random(g.n.start ^ 0x1A2BL);
        int arrived = 0;
        for (int i = 0; i < total; i++) {
            double x0 = r.nextDouble() * g.w, y0 = r.nextDouble() * g.h * 0.55;
            double tx = g.cx - 40 + r.nextDouble() * 80, jitter = r.nextDouble();
            double t = (el - (i * 40L + jitter * 200)) / 600.0;
            if (t >= 1) { arrived++; continue; }
            if (t < 0) continue;
            double side = (i % 2 == 0) ? 1 : -1;
            for (int k = 4; k >= 0; k--) {
                double tt = t - k * 0.035;
                if (tt < 0) continue;
                double e = tt * tt * (3 - 2 * tt);
                int px = (int) (x0 + (tx - x0) * e + Math.sin(tt * Math.PI) * 30 * side);
                int py = (int) (y0 + (barY - y0) * e - Math.sin(tt * Math.PI) * 20);
                int col = (Math.sin(el / 150.0 + i) > 0) ? 0x80FF20 : 0xFFFF55;
                double a = (k == 0 ? 1.0 : 0.28 / k) * g.fade;
                g.ctx.fill(px - 2, py - 2, px + 2, py + 2, argb(a * 0.4, col));
                if (k == 0) g.ctx.fill(px - 1, py - 1, px + 1, py + 1, argb(a, col));
            }
        }
        double prog = arrived / (double) total;
        g.ctx.fill(barX - 1, barY - 1, barX + barW + 1, barY + 8, argb(0.8 * g.fade, 0x000000));
        g.ctx.fill(barX, barY, barX + barW, barY + 7, argb(g.fade, 0x2A2A2A));
        int fw = (int) (barW * prog);
        if (fw > 0) g.ctx.fillGradient(barX, barY, barX + fw, barY + 7, argb(g.fade, 0xB6FF5A), argb(g.fade, 0x4FA800));
        outlined(g, String.valueOf((int) (prog * 30)), g.cx, barY - 16, 2.0f, 0x80FF20, 0x000000, g.fade);
        if (el > 300 && el < 2500) PaymentHud.tick(g.n, el, 110, "minecraft:entity.experience_orb.pickup", 0.5f, 1.3f);
        if (el >= 2520) {
            long k = el - 2520;
            fillA(g, 0, 0, g.w, g.h, Math.max(0, 0.4 * (1 - k / 500.0)) * g.fade, 0x9CFF3C);
            PaymentHud.cue(g.n, 0, el, 2520, "minecraft:entity.player.levelup", 1.0f, 1.0f);
            double pop = PaymentHud.easeOutBack(c01(k / 350.0));
            outlined(g, "LEVEL UP!", g.cx, (int) (g.h * 0.30), (float) (1.6 * pop), 0x80FF20, 0x103000, g.fade);
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.30) + 20, (float) (3.0 * pop), GOLD, 0x2A1A00, g.fade);
        }
    }

    // ================================================================ $1M  CONFETTI CANNONS
    private static void cannons(G g) {
        long el = g.el;
        int by = g.h - 10;
        long[] shots = {400, 1200, 2000};
        for (int k = 0; k < shots.length; k++)
            PaymentHud.cue(g.n, k, el, shots[k], "minecraft:entity.firework_rocket.blast", 0.6f, 1.3f + k * 0.15f);
        for (int side = 0; side < 2; side++) {
            double sx = side == 0 ? 16 : g.w - 16;
            double aim = side == 0 ? -1.05 : -Math.PI + 1.05;
            double bx = sx + Math.cos(aim) * 14, bsy = by + Math.sin(aim) * 14;
            rotRect(g, bx, bsy, 34, 11, aim, argb(g.fade, 0x3A3A4A));
            rotRect(g, sx + Math.cos(aim) * 28, by + Math.sin(aim) * 28, 8, 14, aim, argb(g.fade, 0xFFC83A));
            ellipse(g.ctx, (int) sx, by + 3, 9, 9, argb(g.fade, 0x24242E));
            ellipse(g.ctx, (int) sx, by + 3, 4, 4, argb(g.fade, 0x70707E));
            for (int s = 0; s < shots.length; s++) {
                double t = (el - shots[s]) / 1000.0;
                if (t < 0 || t > 2.6) continue;
                Random rr = new Random(g.n.start ^ shots[s] ^ (side * 977L));
                for (int i = 0; i < 46; i++) {
                    double ang = aim + (rr.nextDouble() - 0.5) * 0.75, spd = 220 + rr.nextDouble() * 230;
                    double flut = rr.nextDouble() * 6.28;
                    double d = (1 - Math.exp(-2.2 * t)) / 2.2;
                    double px = sx + Math.cos(ang) * spd * d + Math.sin(t * 6 + flut) * 4;
                    double py = by + Math.sin(ang) * spd * d + 130 * t * t;
                    double a = c01(1 - t / 2.6) * g.fade;
                    rotRect(g, px, py, 2, 4, flut + t * 8, argb(a, RAINBOW[i % RAINBOW.length]));
                }
                if (t < 0.25) {
                    double mx = sx + Math.cos(aim) * 34, my = by + Math.sin(aim) * 34;
                    ellipse(g.ctx, (int) mx, (int) my, 6 + t * 60, 6 + t * 60, argb((1 - t / 0.25) * 0.8 * g.fade, 0xFFF2A0));
                }
            }
        }
        double sw = Math.sin(el / 400.0) * 0.06;
        int by2 = (int) (g.h * 0.16);
        double bp = PaymentHud.easeOutBack(c01(el / 500.0));
        g.ms.pushMatrix();
        g.ms.translate((float) g.cx, (float) by2);
        g.ms.rotate((float) sw);
        g.ms.scale((float) bp, (float) bp);
        g.ctx.fill(-80, -12, 80, 12, argb(0.9 * g.fade, 0xE02060));
        g.ctx.fill(-80, -12, 80, -10, argb(g.fade, 0xFFFFFF));
        g.ctx.fill(-80, 10, 80, 12, argb(g.fade, 0xFFFFFF));
        text(g, "CONGRATS!", 0, -8, 1.8f, WHITE, g.fade);
        g.ms.popMatrix();
        if (el > 700) {
            double pop = PaymentHud.easeOutBack(c01((el - 700) / 350.0));
            text(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.30), (float) (2.8 * pop), GOLD, g.fade);
            text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.30) + 28, 1.2f, WHITE, g.fade);
        }
    }

    // ================================================================ $5M  TOTEM POP
    private static final String[] TOTEM = {
        "...KKKKK...", "..KYYYYYK..", ".KYyYYYyYK.", ".KYGYYYGYK.", ".KYgYYYgYK.", ".KYYYYYYYK.", "KKYYYDYYYKK",
        "KYYYYDYYYYK", "KYYDDDDDYYK", ".KYYYYYYYK.", "..KYYGYYK..", "..KYYYYYK..", "...KYYYK...", "...KDDDK...", "....KKK...."
    };

    private static int totemColor(char c) {
        switch (c) {
            case 'Y': return 0xF2C230;
            case 'y': return 0xFFE680;
            case 'D': return 0xB07A12;
            case 'G': return 0x2FBF4B;
            case 'g': return 0x1B7A31;
            case 'K': return 0x2A1A05;
            default: return -1;
        }
    }

    private static void totem(G g) {
        long el = g.el;
        int px = 5, cy = (int) (g.h * 0.40);
        double riseT = PaymentHud.ease(c01(el / 1100.0));
        int ty = (int) (g.h + 40 - (g.h + 40 - cy) * riseT);
        double flip = el < 1400 ? Math.cos(el / 130.0) : 1.0;
        double pulse = el >= 1400 ? 1 + 0.04 * Math.sin(el / 120.0) : 1.0;
        if (el >= 1400) {
            ellipse(g.ctx, g.cx, cy, 64 * pulse, 64 * pulse, argb(0.10 * g.fade, 0x7CFF6A));
            ellipse(g.ctx, g.cx, cy, 44 * pulse, 44 * pulse, argb(0.14 * g.fade, 0xF2C230));
        }
        for (int row = 0; row < TOTEM.length; row++) {
            for (int c = 0; c < 11; c++) {
                int col = totemColor(TOTEM[row].charAt(c));
                if (col < 0) continue;
                double sx = g.cx + (c - 5) * px * flip * pulse;
                double cw = Math.max(1, Math.abs(px * flip * pulse));
                int x1 = (int) Math.round(sx - cw / 2), x2 = (int) Math.round(sx + cw / 2);
                if (x2 <= x1) x2 = x1 + 1;
                int y1 = ty + (int) ((row - 7.5) * px * pulse);
                g.ctx.fill(x1, y1, x2, y1 + (int) Math.ceil(px * pulse), argb(g.fade, col));
            }
        }
        PaymentHud.cue(g.n, 1, el, 0, "minecraft:entity.player.levelup", 0.5f, 0.6f);
        if (el >= 1400) {
            long k = el - 1400;
            PaymentHud.cue(g.n, 0, el, 1400, "minecraft:item.totem.use", 1.0f, 1.0f);
            fillA(g, 0, 0, g.w, g.h, Math.max(0, 0.5 * (1 - k / 450.0)) * g.fade, 0xC8FF70);
            Random r = new Random(g.n.start ^ 0x707EL);
            for (int i = 0; i < 80; i++) {
                double ang = r.nextDouble() * Math.PI * 2, spd = 60 + r.nextDouble() * 230;
                double life = 900 + r.nextDouble() * 1100;
                int sz = 2 + r.nextInt(2), col = i % 2 == 0 ? 0x3CD04B : 0xF2C230;
                if (k > life) continue;
                double s = k / 1000.0;
                int qx = (int) (g.cx + Math.cos(ang) * spd * s * (1 - 0.3 * s));
                int qy = (int) (cy + Math.sin(ang) * spd * s + 110 * s * s);
                g.ctx.fill(qx, qy, qx + sz, qy + sz, argb((1 - k / life) * g.fade, col));
            }
            double pop = PaymentHud.easeOutBack(c01((k - 300) / 400.0));
            outlined(g, "TOTEM OF UNDYING", g.cx, cy + 44, (float) (1.2 * pop), GOLD, 0x2A1A00, g.fade);
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, cy + 58, (float) (2.6 * pop), 0x7CFF6A, 0x0A2A0A, g.fade);
            text(g, "from " + g.n.name, g.cx, cy + 84, 1.1f, WHITE, g.fade);
        }
    }

    // ================================================================ $5M  WAVES
    private static final int[] WAVE_COL = {0x1B5FC4, 0x2E86EC, 0x6CC4FF};
    private static final double[] WAVE_AMP = {9, 7, 5}, WAVE_SPD = {1.0, -1.4, 1.9}, WAVE_BASE = {0.58, 0.64, 0.70};

    private static double waveY(G g, int layer, double x, double lvl) {
        double rest = g.h + 12, full = g.h * WAVE_BASE[layer];
        return rest - (rest - full) * lvl
            + Math.sin(x / (20 + 6 * layer) + g.el / 300.0 * WAVE_SPD[layer]) * WAVE_AMP[layer] * (0.4 + 0.6 * lvl);
    }

    private static void waves(G g) {
        long el = g.el;
        double lvl = Math.min(PaymentHud.ease(c01(el / 1400.0)), PaymentHud.ease(c01((g.dur - el) / 900.0)));
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.generic.splash", 0.8f, 1.0f);
        PaymentHud.cue(g.n, 1, el, 900, "minecraft:entity.player.splash.high_speed", 0.6f, 1.2f);
        for (int L = 0; L < 3; L++) {
            for (int x = 0; x < g.w; x += 2) {
                int y = (int) waveY(g, L, x, lvl);
                g.ctx.fill(x, y, x + 2, g.h, argb(0.62, WAVE_COL[L]));
                if (L == 2) g.ctx.fill(x, y, x + 2, y + 2, argb(0.85, 0xFFFFFF));
            }
            if (L == 1) {
                for (int i = 0; i < 12; i++) {
                    double x = ((i * 37 + el * 0.04 * (1 + i % 3)) % (g.w + 20)) - 10;
                    int cy = (int) waveY(g, 1, x, lvl) - 3;
                    ellipse(g.ctx, (int) x, cy, 3, 3, argb(0.95, 0xFFD700));
                    ellipse(g.ctx, (int) x, cy, 1.5, 1.5, argb(0.95, 0xFFF2A0));
                }
            }
        }
        if (lvl > 0.3) {
            int hy = (int) waveY(g, 1, g.cx, lvl) - 26 + (int) (Math.sin(el / 260.0) * 2);
            head(g, g.cx - 14, hy, 28);
        }
        Random r = new Random(g.n.start ^ 0x5AL);
        for (int i = 0; i < 30; i++) {
            double x0 = g.cx + (r.nextDouble() - 0.5) * g.w * 0.8, vx = (r.nextDouble() - 0.5) * 60, vy = 140 + r.nextDouble() * 140;
            double t = (el - r.nextDouble() * 500) / 1000.0;
            if (t < 0 || t > 1.4) continue;
            int px = (int) (x0 + vx * t), py = (int) (g.h * 0.7 - vy * t + 300 * t * t);
            g.ctx.fill(px, py, px + 2, py + 2, argb((1 - t / 1.4) * g.fade, 0xBFE8FF));
        }
        if (el > 600) {
            double pop = PaymentHud.easeOutBack(c01((el - 600) / 400.0));
            outlined(g, "MAKING WAVES", g.cx, (int) (g.h * 0.14), (float) (2.0 * pop), WHITE, 0x0A2E66, g.fade);
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.14) + 22, (float) (2.8 * pop), GOLD, 0x2A1A00, g.fade);
            text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.14) + 52, 1.1f, WHITE, g.fade);
        }
    }

    // ================================================================ $10M  DISCO
    private static void disco(G g) {
        long el = g.el;
        fillA(g, 0, 0, g.w, g.h, 0.62 * g.fade, 0x05020E);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.note_block.pling", 0.8f, 1.5f);
        PaymentHud.tick(g.n, el, 400, "minecraft:block.note_block.basedrum", 0.8f, 1.0f);
        int bx = g.cx, by = 26;
        for (int b = 0; b < 6; b++) {
            double base = Math.PI / 2 + Math.sin(el / 600.0 + b * 1.1) * 0.9, hue = el / 1500.0 + b * 0.16;
            for (int k = -3; k <= 3; k++) {
                double a = base + k * 0.025;
                line(g, bx, by, bx + Math.cos(a) * 420, by + Math.sin(a) * 420, 3, argb(0.10 * g.fade, Shows.hsv(hue, 0.8, 1)));
            }
        }
        int tw = Math.max(8, g.w / 14);
        for (int row = 0; row < 3; row++) {
            for (int c = 0; c * tw < g.w; c++) {
                double v = 0.5 + 0.5 * Math.sin(el / 160.0 + c * 0.7 + row * 1.3);
                int x = c * tw, y = g.h - 48 + row * 16;
                fillA(g, x + 1, y + 1, x + tw - 1, y + 15, (0.25 + 0.4 * v) * g.fade, Shows.hsv(el / 1200.0 + c * 0.07 + row * 0.1, 0.8, 1));
            }
        }
        g.ctx.fill(bx, 0, bx + 1, by - 14, argb(0.7 * g.fade, 0xCCCCCC));
        ellipse(g.ctx, bx, by, 15, 15, argb(g.fade, 0x8A8A98));
        for (int i = 0; i < 26; i++) {
            double a = el / 500.0 + i * 0.9, rr = 3 + (i * 7) % 11;
            int sx = (int) (bx + Math.cos(a) * rr), sy = (int) (by + Math.sin(i * 1.7 + el / 300.0) * 10);
            if (Math.cos(a) > -0.2) g.ctx.fill(sx, sy, sx + 2, sy + 2, argb(0.9 * g.fade, Shows.hsv(i * 0.09 + el / 700.0, 0.5, 1)));
        }
        int hy = (int) (g.h * 0.47) - (int) (Math.abs(Math.sin(el / 180.0)) * 14);
        ellipse(g.ctx, g.cx, g.h - 52, 22, 4, argb(0.35 * g.fade, 0x000000));
        head(g, g.cx - 20, hy, 40);
        double pulse = 1 + 0.06 * Math.abs(Math.sin(el / 180.0));
        rainbow(g, "PARTY TIME", g.cx, 54, (float) (1.8 * pulse));
        double pop = PaymentHud.easeOutBack(c01((el - 300) / 400.0));
        rainbow(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.47) + 46, (float) (2.6 * pop));
        text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.47) + 72, 1.1f, WHITE, g.fade);
    }

    // ================================================================ $10M  TNT
    private static void tnt(G g) {
        long el = g.el;
        int cy = (int) (g.h * 0.50);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.tnt.primed", 1.0f, 1.0f);
        if (el < 2300) {
            double swell = 1 + 0.35 * (el / 2300.0);
            int S = (int) (48 * swell), x = g.cx - S / 2, y = cy - S / 2;
            g.ctx.fill(x, y, x + S, y + S, 0xFFD83A32);
            g.ctx.fill(x, y, x + S, y + 2, 0xFFA52A24);
            g.ctx.fill(x, y + S - 2, x + S, y + S, 0xFFA52A24);
            g.ctx.fill(x, y + S / 3, x + S, y + 2 * S / 3, 0xFFE8E8E8);
            float sc = S / 40f;
            outlined(g, "TNT", g.cx, y + S / 3 + (int) ((S / 3 - 8 * sc) / 2), sc, 0x2A2A2A, 0xFFFFFF, 1.0);
            Random r = new Random(el / 60);
            g.ctx.fill(g.cx - 1, y - 6, g.cx + 1, y, 0xFF3A2A1A);
            for (int i = 0; i < 5; i++) {
                int sx = g.cx + r.nextInt(9) - 4, sy = y - 8 - r.nextInt(8);
                g.ctx.fill(sx, sy, sx + 2, sy + 2, argb(0.95, i % 2 == 0 ? 0xFFE14A : 0xFF8A1A));
            }
            long period = (long) Math.max(90, 320 - el * 0.12);
            if (el > 300 && el % period < period / 2) fillA(g, x, y, x + S, y + S, 0.75, 0xFFFFFF);
            PaymentHud.cue(g.n, 1, el, 1500, "minecraft:entity.creeper.primed", 0.8f, 1.0f);
            text(g, "IT'S PRIMED...", g.cx, (int) (g.h * 0.16), 1.3f, RED, g.fade);
            return;
        }
        long k = el - 2300;
        double s = k / 1000.0;
        PaymentHud.cue(g.n, 2, el, 2300, "minecraft:entity.generic.explode", 1.0f, 0.9f);
        double fa = c01(1 - k / 1500.0);
        ellipse(g.ctx, g.cx, cy, 24 + k * 0.12, 24 + k * 0.12, argb(0.9 * fa, 0xFF6A00));
        ellipse(g.ctx, g.cx, cy, 14 + k * 0.08, 14 + k * 0.08, argb(fa, 0xFFD23C));
        ellipse(g.ctx, g.cx, cy, 6 + k * 0.04, 6 + k * 0.04, argb(fa, 0xFFFFFF));
        Random sm = new Random(g.n.start ^ 0x5304L);
        for (int i = 0; i < 10; i++) {
            double ang = sm.nextDouble() * 6.28, d = 10 + sm.nextDouble() * 50;
            double rr = 10 + k * 0.05 + sm.nextDouble() * 8;
            ellipse(g.ctx, (int) (g.cx + Math.cos(ang) * d * (0.4 + s)), (int) (cy + Math.sin(ang) * d * (0.4 + s) - s * 14),
                rr, rr, argb(0.35 * c01(1 - k / 2600.0) * g.fade, 0x555555));
        }
        ringDots(g.ctx, g.cx, cy, k * 0.55, k * 0.55, 180, 0, 2, argb(c01(1 - k / 1600.0), 0xFFE9A0));
        ringDots(g.ctx, g.cx, cy, k * 0.38, k * 0.38, 120, 0.5, 2, argb(c01(1 - k / 1300.0), 0xFFFFFF));
        int[] dc = {0xD83A32, 0xE8E8E8, 0x7A7A7A, 0xA52A24};
        Random dr = new Random(g.n.start ^ 0xDEB1L);
        for (int i = 0; i < 56; i++) {
            double ang = dr.nextDouble() * Math.PI * 2, spd = 120 + dr.nextDouble() * 300;
            double sz = 3 + dr.nextDouble() * 4;
            double px = g.cx + Math.cos(ang) * spd * s * (1 - 0.35 * s);
            double py = cy + Math.sin(ang) * spd * s * (1 - 0.35 * s) + 260 * s * s;
            rotRect(g, px, py, sz, sz, i + s * 9, argb(c01(1 - k / 2400.0) * g.fade, dc[i % 4]));
        }
        fillA(g, 0, 0, g.w, g.h, Math.max(0, 1 - k / 450.0), 0xFFFFFF);
        if (k > 200) {
            double pop = PaymentHud.easeOutBack(c01((k - 200) / 350.0));
            outlined(g, "KABOOM!", g.cx + (int) (Math.sin(el / 40.0) * 1.5), (int) (g.h * 0.14), (float) (3.2 * pop), 0xFFD23C, 0x7A1500, g.fade);
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.14) + 32, (float) (2.6 * pop), WHITE, 0x7A1500, g.fade);
            text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.14) + 58, 1.2f, WHITE, g.fade);
        }
    }

    // ================================================================ $25M  MATRIX
    private static final String MCH = "01$#%&*+-=<>?/|[]{}ABCDEFGHJKLMNPRSTUVWXYZ";

    private static void matrix(G g) {
        long el = g.el;
        fillA(g, 0, 0, g.w, g.h, 0.80 * g.fade, 0x001207);
        int cs = 9, cols = g.w / cs + 1, trail = 14;
        Random r = new Random(0);
        for (int c = 0; c < cols; c++) {
            r.setSeed(g.n.start ^ (c * 7919L));
            double spd = 55 + r.nextDouble() * 130, off = r.nextDouble() * 400;
            double head = (el * spd / 1000.0 + off) % (g.h + trail * cs);
            for (int j = 0; j < trail; j++) {
                int y = (int) head - j * cs;
                if (y < -cs || y > g.h) continue;
                r.setSeed(c * 131L + j * 17L + el / 110 + g.n.start);
                char ch = MCH.charAt(r.nextInt(MCH.length()));
                double a = (1 - j / (double) trail) * g.fade;
                g.ctx.drawText(g.tr, String.valueOf(ch), c * cs, y, targb(a, j == 0 ? 0xE8FFE8 : 0x00FF41), false);
            }
        }
        String l1 = "Wake up, " + g.n.name + "...", l2 = "The money has you.";
        int n1 = (int) Math.max(0, Math.min(l1.length(), (el - 700) / 75));
        int ty = (int) (g.h * 0.28);
        boolean cur = (el / 300) % 2 == 0;
        if (el > 600) {
            fillA(g, g.cx - 120, ty - 8, g.cx + 120, ty + 34, 0.7 * g.fade, 0x000000);
            textNS(g, l1.substring(0, n1) + (cur ? "_" : " "), g.cx, ty, 1.3f, 0x00FF41, g.fade);
        }
        if (el > 2400) {
            int n2 = (int) Math.max(0, Math.min(l2.length(), (el - 2400) / 75));
            textNS(g, l2.substring(0, n2) + (cur ? "_" : " "), g.cx, ty + 16, 1.3f, 0x00FF41, g.fade);
        }
        if (el > 600 && el < 3400) PaymentHud.tick(g.n, el, 75, "minecraft:block.note_block.bit", 0.25f, 1.7f);
        if (el >= 3700) {
            long k = el - 3700;
            PaymentHud.cue(g.n, 0, el, 3700, "minecraft:block.beacon.activate", 0.8f, 1.4f);
            double pop = PaymentHud.easeOutBack(c01(k / 400.0));
            int hy = (int) (g.h * 0.46);
            head(g, g.cx - 24, hy, 48);
            fillA(g, g.cx - 24, hy, g.cx + 24, hy + 48, 0.35 * g.fade, 0x00FF41);
            fillA(g, 0, 0, g.w, g.h, Math.max(0, 0.3 * (1 - k / 400.0)) * g.fade, 0x00FF41);
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, hy + 54, (float) (2.8 * pop), 0x00FF41, 0x002A0A, g.fade);
            text(g, "from " + g.n.name, g.cx, hy + 80, 1.1f, 0x9BFFB5, g.fade);
        }
    }

    // ================================================================ $25M  HOLOGRAM SCAN
    private static void hologram(G g) {
        long el = g.el;
        int S = 64, hx = g.cx - S / 2, hy = (int) (g.h * 0.26), ey = hy + S + 26;
        fillA(g, 0, 0, g.w, g.h, 0.45 * g.fade, 0x00101C);
        g.ctx.fillGradient(g.cx - 34, hy, g.cx + 34, ey, argb(0.0, CYAN), argb(0.30 * g.fade, CYAN));
        ellipse(g.ctx, g.cx, ey, 40, 7, argb(0.8 * g.fade, CYAN));
        ellipse(g.ctx, g.cx, ey, 32, 5, argb(g.fade, 0x04202A));
        double flick = 0.78 + 0.22 * Math.sin(el / 37.0);
        int jx = (el / 90) % 11 == 0 ? 3 : 0;
        head(g, hx + jx, hy, S);
        fillA(g, hx + jx, hy, hx + jx + S, hy + S, 0.42 * g.fade * flick, CYAN);
        for (int yy = hy; yy < hy + S; yy += 2) fillA(g, hx + jx, yy, hx + jx + S, yy + 1, 0.28 * g.fade, 0x001018);
        double sp = (el % 1500) / 1500.0;
        int sy = hy + (int) (S * (sp < 0.5 ? sp * 2 : (1 - sp) * 2));
        fillA(g, hx - 6, sy, hx + S + 6, sy + 2, g.fade, 0xB8FFFF);
        fillA(g, hx - 6, sy - 3, hx + S + 6, sy, 0.25 * g.fade, CYAN);
        int bl = 10;
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                int bx = a == 0 ? hx - 8 : hx + S + 8, byy = b == 0 ? hy - 8 : hy + S + 8;
                int dx = a == 0 ? 1 : -1, dy = b == 0 ? 1 : -1;
                fillA(g, Math.min(bx, bx + dx * bl), byy, Math.max(bx, bx + dx * bl) + 1, byy + 1, g.fade, CYAN);
                fillA(g, bx, Math.min(byy, byy + dy * bl), bx + 1, Math.max(byy, byy + dy * bl) + 1, g.fade, CYAN);
            }
        }
        for (int rr = 0; rr < 2; rr++)
            ringDots(g.ctx, g.cx, ey - 2 - rr * 6, 46 + rr * 6, 10 + rr * 2, 56, (rr == 0 ? 1 : -1) * el / 600.0, 2, argb(0.9 * g.fade, CYAN));

        int pct = (int) Math.min(100, el * 100 / 3300);
        String hex = Integer.toHexString(new Random(el / 120 + g.n.start).nextInt()).toUpperCase();
        text(g, "IDENTITY SCAN", g.cx, (int) (g.h * 0.08), 1.3f, CYAN, g.fade);
        textL(g, "SUBJECT", 12, hy, 0.8f, CYAN, g.fade);
        textL(g, g.tr.trimToWidth(g.n.name.toUpperCase(), 90), 12, hy + 10, 1.0f, WHITE, g.fade);
        textL(g, "ID 0x" + hex, 12, hy + 24, 0.8f, 0x7FDCEB, g.fade);
        textL(g, "SCAN " + pct + "%", 12, hy + 36, 0.8f, 0x7FDCEB, g.fade);
        fillA(g, 12, hy + 48, 92, hy + 51, 0.4 * g.fade, 0x0A3A46);
        fillA(g, 12, hy + 48, 12 + (int) (80 * pct / 100.0), hy + 51, g.fade, CYAN);
        long shown = (long) (g.n.amount * PaymentHud.ease(c01((el - 300) / 3000.0)));
        textR(g, "NET WORTH", g.w - 12, hy, 0.8f, CYAN, g.fade);
        textR(g, Stats.fmt(shown), g.w - 12, hy + 10, 1.5f, GOLD, g.fade);
        textR(g, "THREAT LEVEL", g.w - 12, hy + 28, 0.8f, CYAN, g.fade);
        textR(g, "MAXIMUM", g.w - 12, hy + 38, 1.0f, (el / 250) % 2 == 0 ? RED : 0x7A2222, g.fade);

        if (el > 600 && el < 3300) PaymentHud.tick(g.n, el, 100, "minecraft:block.note_block.bit", 0.35f, 2.0f);
        if (el >= 3400) {
            long k = el - 3400;
            PaymentHud.cue(g.n, 0, el, 3400, "minecraft:block.beacon.activate", 0.9f, 1.3f);
            fillA(g, 0, 0, g.w, g.h, Math.max(0, 0.25 * (1 - k / 400.0)) * g.fade, 0x55FF55);
            double pop = PaymentHud.easeOutBack(c01(k / 350.0));
            outlined(g, "ACCESS GRANTED", g.cx, ey + 14, (float) (1.5 * pop), GREEN, 0x052A05, g.fade);
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, ey + 32, (float) (2.8 * pop), WHITE, 0x0A3A46, g.fade);
        }
    }

    // ================================================================ $50M  INFERNO
    private static void inferno(G g) {
        long el = g.el;
        double rise = Math.min(PaymentHud.ease(c01(el / 1500.0)), PaymentHud.ease(c01((g.dur - el) / 900.0)));
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:item.firecharge.use", 0.9f, 0.8f);
        PaymentHud.cue(g.n, 1, el, 1200, "minecraft:entity.blaze.shoot", 0.6f, 0.9f);
        PaymentHud.tick(g.n, el, 300, "minecraft:block.fire.ambient", 0.5f, 1.0f);
        int ga = (int) (255 * 0.40 * rise);
        g.ctx.fillGradient(0, (int) (g.h * 0.45), g.w, g.h, 0x00FF5A00, (ga << 24) | 0xFF5A00);
        for (int x = 0; x < g.w; x += 3) {
            double n = Math.sin(x * 0.09 + el * 0.012) + 0.6 * Math.sin(x * 0.21 - el * 0.017) + 0.8 * Math.sin(x * 0.047 + el * 0.007);
            int ih = (int) (g.h * 0.34 * rise * Math.max(0.12, 0.62 + 0.22 * n));
            g.ctx.fill(x, g.h - ih, x + 3, g.h, argb(0.85, 0xCC2200));
            g.ctx.fill(x, g.h - (int) (ih * 0.72), x + 3, g.h, argb(0.85, 0xFF7A00));
            g.ctx.fill(x, g.h - (int) (ih * 0.46), x + 3, g.h, argb(0.9, 0xFFD84A));
            g.ctx.fill(x, g.h - (int) (ih * 0.20), x + 3, g.h, argb(0.95, 0xFFFFC8));
        }
        Random r = new Random(g.n.start ^ 0xE3BEL);
        for (int i = 0; i < 46; i++) {
            double x0 = r.nextDouble() * g.w, spd = 40 + r.nextDouble() * 90;
            double delay = r.nextDouble() * 1200, life = 1600 + r.nextDouble() * 1400;
            double t = el - delay;
            if (t < 0) continue;
            double pos = (t % life) / life;
            int px = (int) (x0 + Math.sin(t / 300.0 + i) * 10), py = (int) (g.h - 4 - pos * g.h * 0.75 * (spd / 90.0));
            g.ctx.fill(px, py, px + 2, py + 2, argb((1 - pos) * rise * g.fade, i % 2 == 0 ? 0xFFB020 : 0xFFE14A));
        }
        int hy = (int) (g.h * 0.36);
        head(g, g.cx - 18, hy, 36);
        for (int i = 0; i < 14; i++) {
            double a = ((el / 700.0 + i / 14.0) % 1.0);
            int wx = (int) (g.cx + Math.sin(i * 1.7 + el / 200.0) * 14 * (1 - a)), wy = (int) (hy + 30 - a * 50);
            int sz = 2 + (i % 2);
            g.ctx.fill(wx, wy, wx + sz, wy + sz, argb((1 - a) * g.fade, a < 0.5 ? 0xFFE14A : 0xFF7A00));
        }
        double jit = Math.sin(el / 50.0) * 1.5;
        outlined(g, "ON FIRE", g.cx + (int) jit, (int) (g.h * 0.10), 3.0f, 0xFFB020, 0x7A1500, g.fade);
        double pop = PaymentHud.easeOutBack(c01((el - 400) / 400.0));
        outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.10) + 32, (float) (2.6 * pop), 0xFFF2A0, 0x7A1500, g.fade);
        text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.10) + 58, 1.1f, WHITE, g.fade);
    }

    // ================================================================ $50M  HYPE TRAIN
    private static void wheel(G g, int cx, int cy, int r) {
        ellipse(g.ctx, cx, cy, r, r, argb(g.fade, 0x1A1A22));
        double a = g.el / 70.0 + cx * 0.3;
        line(g, cx - Math.cos(a) * (r - 1), cy - Math.sin(a) * (r - 1), cx + Math.cos(a) * (r - 1), cy + Math.sin(a) * (r - 1), 1, argb(g.fade, 0x9A9AA8));
        line(g, cx - Math.sin(a) * (r - 1), cy + Math.cos(a) * (r - 1), cx + Math.sin(a) * (r - 1), cy - Math.cos(a) * (r - 1), 1, argb(g.fade, 0x9A9AA8));
    }

    private static void train(G g) {
        long el = g.el;
        int baseY = (int) (g.h * 0.66);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.bell.use", 0.8f, 1.2f);
        PaymentHud.cue(g.n, 1, el, 3200, "minecraft:block.bell.use", 0.8f, 1.5f);
        PaymentHud.tick(g.n, el, 150, "minecraft:block.note_block.hat", 0.35f, 0.9f);
        Random sr = new Random(g.n.start ^ 0x7241L);
        for (int i = 0; i < 18; i++) {
            int y = (int) (g.h * 0.2 + sr.nextDouble() * g.h * 0.5);
            double spd = 600 + sr.nextDouble() * 600, off = sr.nextDouble() * g.w;
            int len = 30 + sr.nextInt(60);
            int x = (int) (g.w - ((el * spd / 1000.0 + off) % (g.w + len)));
            fillA(g, x, y, x + len, y + 1, 0.22 * g.fade, 0xFFFFFF);
        }
        fillA(g, 0, baseY + 22, g.w, baseY + 24, 0.9 * g.fade, 0x8A8A94);
        int x0 = -(int) ((el * 0.35) % 12);
        for (int x = x0; x < g.w; x += 12) fillA(g, x, baseY + 24, x + 6, baseY + 27, 0.9 * g.fade, 0x5A3A1A);

        int wagons = 4, wl = 46, gap = 3, engL = 58;
        int total = wagons * (wl + gap) + engL;
        double p = c01(el / (g.dur - 250.0));
        int headX = (int) (-12 + p * (g.w + total + 24));
        int ex = headX - engL;
        for (int i = 0; i < wagons; i++) {
            int right = ex - gap - i * (wl + gap), x = right - wl;
            if (right < -4 || x > g.w + 4) continue;
            int body = Shows.hsv(i * 0.16 + 0.02, 0.55, 0.85);
            g.ctx.fill(x, baseY - 4, x + wl, baseY + 20, argb(g.fade, body));
            g.ctx.fill(x, baseY + 14, x + wl, baseY + 20, argb(g.fade, 0x2A2A33));
            for (int wnd = 0; wnd < 3; wnd++) g.ctx.fill(x + 6 + wnd * 13, baseY + 1, x + 14 + wnd * 13, baseY + 9, argb(g.fade, 0x1A2438));
            for (int c = 0; c < 3; c++) {
                ellipse(g.ctx, x + 10 + c * 13, baseY - 6, 7, 4, argb(g.fade, 0xFFD700));
                ellipse(g.ctx, x + 10 + c * 13, baseY - 7, 4, 2, argb(g.fade, 0xFFF2A0));
            }
            wheel(g, x + 9, baseY + 22, 5);
            wheel(g, x + wl - 9, baseY + 22, 5);
            g.ctx.fill(x + wl, baseY + 12, x + wl + gap, baseY + 14, argb(g.fade, 0x222222));
        }
        g.ctx.fill(ex + 22, baseY, ex + engL, baseY + 20, argb(g.fade, 0x3A3A44));
        g.ctx.fill(ex + 22, baseY + 6, ex + engL, baseY + 9, argb(g.fade, 0xC8302A));
        g.ctx.fill(ex, baseY - 12, ex + 22, baseY + 20, argb(g.fade, 0x2B2B33));
        g.ctx.fill(ex + 3, baseY - 9, ex + 19, baseY + 6, argb(g.fade, 0x9FD8FF));
        head(g, ex + 4, baseY - 8, 14);
        g.ctx.fill(ex - 1, baseY - 14, ex + 24, baseY - 12, argb(g.fade, 0xC8302A));
        g.ctx.fill(ex + 44, baseY - 10, ex + 50, baseY, argb(g.fade, 0x1A1A22));
        g.ctx.fill(ex + 42, baseY - 13, ex + 52, baseY - 10, argb(g.fade, 0x1A1A22));
        g.ctx.fill(ex + engL, baseY + 4, ex + engL + 4, baseY + 20, argb(g.fade, 0xC8302A));
        ellipse(g.ctx, ex + engL + 4, baseY + 8, 4, 4, argb(g.fade, 0xFFF3A0));
        ellipse(g.ctx, ex + engL + 44, baseY + 8, 46, 11, argb(0.10 * g.fade, 0xFFF3A0));
        wheel(g, ex + 12, baseY + 22, 6);
        wheel(g, ex + 32, baseY + 22, 5);
        wheel(g, ex + 48, baseY + 22, 5);
        for (int i = 0; i < 10; i++) {
            double age = ((el / 120.0 + i) % 10) / 10.0;
            double sx = ex + 47 - age * 70, sy = baseY - 14 - age * 40, rr = 3 + age * 7;
            ellipse(g.ctx, (int) sx, (int) sy, rr, rr, argb(0.5 * (1 - age) * g.fade, 0xDDDDDD));
        }
        rainbow(g, "ALL ABOARD THE HYPE TRAIN", g.cx, (int) (g.h * 0.10), 1.5f);
        if (el > 500) {
            double pop = PaymentHud.easeOutBack(c01((el - 500) / 400.0));
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.20), (float) (3.0 * pop), GOLD, 0x2A1A00, g.fade);
            text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.20) + 30, 1.2f, WHITE, g.fade);
        }
    }

    // ================================================================ $100M  NEON SYNTHWAVE
    private static void neon(G g) {
        long el = g.el;
        int hz = (int) (g.h * 0.55);
        double a = g.fade;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.note_block.pling", 0.8f, 0.8f);
        PaymentHud.tick(g.n, el, 250, "minecraft:block.note_block.bit", 0.35f, 1.2f);
        g.ctx.fillGradient(0, 0, g.w, hz, argb(a, 0x0B0221), argb(a, 0x3A0A5C));
        g.ctx.fillGradient(0, hz, g.w, g.h, argb(a, 0x1A0033), argb(a, 0x05000F));
        stars(g, 60, 0.8 * a);
        int R = (int) (g.h * 0.24), scy = hz - (int) (R * 0.25);
        for (int dy = -R; dy <= R; dy++) {
            int y = scy + dy;
            if (y >= hz) break;
            double t = (dy + R) / (2.0 * R);
            if (dy > -R * 0.1) {
                double per = 7 + (dy + R) * 0.05;
                double ph = ((dy + el / 70.0) % per + per) % per;
                if (ph < 0.15 * per + (dy + R) * 0.035) continue;
            }
            int hw = (int) Math.round(Math.sqrt((double) R * R - (double) dy * dy));
            g.ctx.fill(g.cx - hw, y, g.cx + hw, y + 1, argb(a, lerpRgb(0xFFE14A, 0xFF2A9D, t)));
        }
        int N = 12;
        for (int k = 0; k < N; k++) {
            double t = ((k + el / 450.0) % N) / N;
            int y = hz + (int) ((g.h - hz) * t * t);
            fillA(g, 0, y, g.w, y + 1 + (int) (t * 1.5), (0.35 + 0.6 * t) * a, 0xFF2BD6);
        }
        for (int i = -12; i <= 12; i++) line(g, g.cx + i * 7.0, hz, g.cx + i * (g.w * 0.12), g.h, 1, argb(0.5 * a, 0x2BE8FF));
        fillA(g, 0, hz - 1, g.w, hz + 2, a, 0xFF2BD6);
        for (int y = 0; y < g.h; y += 3) fillA(g, 0, y, g.w, y + 1, 0.10 * a, 0x000000);

        boolean flick = (el / 90) % 13 != 0;
        if (flick) chroma(g, "PAYMENT DETECTED", g.cx, (int) (g.h * 0.06), 1.5f, 1.5, 0xFFFFFF, a);
        int hy = (int) (g.h * 0.14);
        g.ctx.fill(g.cx - 15, hy - 3, g.cx + 15, hy + 31, argb(a, 0xFF2BD6));
        g.ctx.fill(g.cx - 14, hy - 2, g.cx + 14, hy + 30, argb(a, 0x120024));
        head(g, g.cx - 12, hy, 24);
        double pop = PaymentHud.easeOutBack(c01((el - 300) / 400.0));
        glowText(g, "+" + Stats.fmt(g.n.amount), g.cx, scy - 14, (float) (3.4 * pop), 0xFF2BD6, 0xFFFFFF, a);
        glowText(g, "> " + g.n.name + " <", g.cx, scy + 24, 1.3f, 0x2BE8FF, 0xFFFFFF, a);
    }

    // ================================================================ $100M  BEACON
    private static void beacon(G g) {
        long el = g.el;
        int bs = 14, baseY = g.h - 6, cx = g.cx;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.beacon.activate", 1.0f, 1.0f);
        PaymentHud.cue(g.n, 1, el, 1000, "minecraft:block.beacon.ambient", 0.8f, 1.0f);
        PaymentHud.cue(g.n, 2, el, 1800, "minecraft:block.beacon.power_select", 0.9f, 1.2f);
        PaymentHud.cue(g.n, 3, el, 2400, "minecraft:entity.player.levelup", 0.8f, 1.0f);
        fillA(g, 0, 0, g.w, g.h, 0.45 * g.fade, 0x02040C);
        double grow = PaymentHud.ease(c01(el / 1300.0));
        int topY = baseY - 5 * bs;
        int beamTop = (int) (topY - (topY + 10) * grow);
        int bc = Shows.hsv(el / 2500.0, 0.35, 1.0);
        int w1 = 26 + (int) (Math.sin(el / 200.0) * 2), w2 = 14, cw = (int) (3 + 3 * Math.abs(Math.cos(el / 260.0)));
        fillA(g, cx - w1 / 2, beamTop, cx + w1 / 2, topY, 0.16 * g.fade, bc);
        fillA(g, cx - w2 / 2, beamTop, cx + w2 / 2, topY, 0.34 * g.fade, bc);
        fillA(g, cx - cw, beamTop, cx + cw, topY, 0.85 * g.fade, 0xFFFFFF);
        for (int k = 0; k < 6; k++) {
            double t = (el / 900.0 + k / 6.0) % 1.0;
            int y = (int) (topY - t * (topY - beamTop));
            ellipse(g.ctx, cx, y, 13 + t * 4, 3, argb(0.5 * (1 - t) * g.fade * grow, bc));
        }
        Random pr = new Random(g.n.start ^ 0xBEA1L);
        for (int i = 0; i < 40; i++) {
            double x = cx + (pr.nextDouble() - 0.5) * 50, spd = 30 + pr.nextDouble() * 70, ph = pr.nextDouble();
            double y = topY - ((el * spd / 1000.0 + ph * 200) % (topY + 10));
            if (y < beamTop) continue;
            g.ctx.fill((int) x, (int) y, (int) x + 2, (int) y + 2, argb(0.7 * g.fade, bc));
        }
        int[] widths = {9, 7, 5, 3, 1};
        int[] mat = {0xD9D9D9, 0xF5D142, 0x6EEBD6, 0x50C878};
        for (int row = 0; row < 5; row++) {
            int y = baseY - (row + 1) * bs;
            for (int c = 0; c < widths[row]; c++) {
                int x = cx - widths[row] * bs / 2 + c * bs;
                if (row == 4) {
                    g.ctx.fill(x, y, x + bs, y + bs, argb(g.fade, 0xA8E8FF));
                    double pz = 0.6 + 0.4 * Math.sin(el / 160.0);
                    g.ctx.fill(x + 3, y + 3, x + bs - 3, y + bs - 3, argb(g.fade, lerpRgb(0x6CF0FF, 0xFFFFFF, pz)));
                } else {
                    int col = mat[(int) ((row + c + el / 400) % 4)];
                    g.ctx.fill(x, y, x + bs, y + bs, argb(g.fade, 0x55555F));
                    g.ctx.fill(x + 1, y + 1, x + bs - 1, y + bs - 1, argb(g.fade, col));
                    g.ctx.fill(x + 1, y + 1, x + bs - 1, y + 3, argb(0.35 * g.fade, 0xFFFFFF));
                }
            }
        }
        double hp = PaymentHud.ease(c01((el - 500) / 3800.0));
        int hy = (int) ((topY - 30) + (g.h * 0.36 - (topY - 30)) * hp);
        ellipse(g.ctx, cx, hy + 13, 22, 22, argb(0.30 * g.fade, bc));
        head(g, cx - 13, hy, 26);
        if (el > 1400) {
            double pop = PaymentHud.easeOutBack(c01((el - 1400) / 400.0));
            outlined(g, "BEACON ACTIVATED", cx, (int) (g.h * 0.08), (float) (1.5 * pop), 0x7AF0FF, 0x02202A, g.fade);
            outlined(g, "+" + Stats.fmt(g.n.amount), cx, (int) (g.h * 0.08) + 20, (float) (3.0 * pop), GOLD, 0x2A1A00, g.fade);
            text(g, "from " + g.n.name, cx, (int) (g.h * 0.08) + 50, 1.1f, WHITE, g.fade);
        }
    }

    // ================================================================ $100M  GLITCH
    private static void glitch(G g) {
        long el = g.el;
        double inten = el < 2500 ? 0.5 + 0.5 * Math.min(1, el / 2000.0) : Math.max(0.15, 1 - (el - 2500) / 1200.0);
        Random r = new Random(g.n.start ^ (el / 55));
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.note_block.bit", 0.5f, 0.5f);
        PaymentHud.cue(g.n, 1, el, 1300, "minecraft:entity.enderman.teleport", 0.8f, 0.7f);
        PaymentHud.cue(g.n, 2, el, 2500, "minecraft:entity.enderman.teleport", 0.9f, 1.4f);
        if (el < 2500) PaymentHud.tick(g.n, el, 60, "minecraft:block.note_block.bit", 0.15f, 1.9f);
        fillA(g, 0, 0, g.w, g.h, 0.55 * g.fade, 0x000000);
        if (el < 2100 && (el / 60) % 8 == 0) fillA(g, 0, 0, g.w, g.h, 0.85 * g.fade, 0x000000);
        for (int i = 0; i < 140; i++) {
            int x = r.nextInt(g.w), y = r.nextInt(g.h), len = 1 + r.nextInt(3);
            fillA(g, x, y, x + len, y + 1, 0.35 * g.fade, r.nextBoolean() ? 0xFFFFFF : 0x55FFFF);
        }
        int bars = (int) (4 + inten * 16);
        int[] bc = {0x00FFFF, 0xFF00FF, 0xFFFFFF, 0x000000};
        for (int i = 0; i < bars; i++) {
            int y = r.nextInt(g.h), bh = 2 + r.nextInt(12), x = r.nextInt(Math.max(1, g.w / 2)), bw = r.nextInt(Math.max(1, g.w));
            fillA(g, x, y, Math.min(g.w, x + bw), y + bh, 0.22 * inten * g.fade, bc[i % 4]);
        }
        if (el < 2500) {
            String t1 = el < 1300 ? "SYSTEM OVERLOAD" : "TOO MUCH MONEY";
            chroma(g, corrupt(t1, r, inten), g.cx, (int) (g.h * 0.30), 2.2f, 2 + inten * 3, 0xFF3B3B, g.fade);
            if (r.nextInt(6) != 0) head(g, g.cx - 20 + (int) ((r.nextInt(7) - 3) * inten * 2), (int) (g.h * 0.46), 40);
        } else {
            double pop = PaymentHud.easeOutBack(c01((el - 2500) / 350.0));
            head(g, g.cx - 20, (int) (g.h * 0.26), 40);
            chroma(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.26) + 48, (float) (3.6 * pop), 1 + 3 * inten, WHITE, g.fade);
            text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.26) + 84, 1.2f, WHITE, g.fade);
        }
    }

    // ================================================================ $500M  NUKE
    private static void skyline(G g, int gy, double a) {
        Random r = new Random(g.n.start ^ 0x51L);
        int n = 22, bw = g.w / n + 1;
        for (int i = 0; i < n; i++) {
            int bh = 10 + r.nextInt(30);
            g.ctx.fill(i * bw, gy - bh, i * bw + bw - 1, g.h, argb(a, 0x0A0A12));
            if (r.nextInt(3) == 0) g.ctx.fill(i * bw + 2, gy - bh + 3, i * bw + 4, gy - bh + 5, argb(0.8 * a, 0xFFE9A0));
        }
        g.ctx.fill(0, gy, g.w, g.h, argb(a, 0x0A0A12));
    }

    private static void nuke(G g) {
        long el = g.el;
        int gy = (int) (g.h * 0.84);
        if (el < 1800) {
            fillA(g, 0, 0, g.w, g.h, 0.55 * g.fade, 0x000000);
            int n = 3 - (int) (el / 600);
            double ph = (el % 600) / 600.0;
            PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.note_block.pling", 1.0f, 0.7f);
            PaymentHud.cue(g.n, 1, el, 600, "minecraft:block.note_block.pling", 1.0f, 0.9f);
            PaymentHud.cue(g.n, 2, el, 1200, "minecraft:block.note_block.pling", 1.0f, 1.2f);
            double pulse = 0.5 + 0.5 * Math.sin(el / 90.0);
            int a = (int) (150 * pulse * g.fade);
            g.ctx.fillGradient(0, 0, g.w, g.h / 3, (a << 24) | 0xFF1010, 0x00FF1010);
            g.ctx.fillGradient(0, g.h - g.h / 3, g.w, g.h, 0x00FF1010, (a << 24) | 0xFF1010);
            outlined(g, "INCOMING PAYMENT", g.cx, (int) (g.h * 0.16), 1.4f, WHITE, 0x300000, g.fade);
            outlined(g, String.valueOf(Math.max(1, n)), g.cx, (int) (g.h * 0.28), (float) (5 * (0.8 + 0.4 * (1 - ph))), 0xFF3030, 0x300000, g.fade);
            skyline(g, gy, g.fade);
            return;
        }
        long k = el - 1800;
        double s = k / 1000.0;
        double al = c01(1 - (k - 3000) / 1600.0) * g.fade;
        PaymentHud.cue(g.n, 3, el, 1800, "minecraft:entity.generic.explode", 1.0f, 0.8f);
        PaymentHud.cue(g.n, 4, el, 2000, "minecraft:entity.lightning_bolt.thunder", 1.0f, 0.8f);
        PaymentHud.cue(g.n, 5, el, 2600, "minecraft:ui.toast.challenge_complete", 1.0f, 0.8f);
        fillA(g, 0, 0, g.w, g.h, 0.45 * g.fade, 0x1A0600);
        g.ctx.fillGradient(0, 0, g.w, gy, 0x00FF6A00, argb(0.5 * c01(1 - k / 2200.0) * g.fade, 0xFF6A00));

        double stemH = Math.min(1, s / 1.7) * g.h * 0.40;
        int capY = (int) (gy - stemH);
        double rx = 26 + Math.min(s, 2.6) * 22, ry = 16 + Math.min(s, 2.6) * 11;
        int sw = (int) (10 + s * 5);
        g.ctx.fillGradient(g.cx - sw, capY, g.cx + sw, gy, argb(al, 0xC05A14), argb(al, 0x4A1E08));
        ellipse(g.ctx, g.cx, gy, 20 + s * 38, 8 + s * 8, argb(al * 0.9, 0xFF8A1A));
        ellipse(g.ctx, g.cx, gy, 10 + s * 22, 5 + s * 5, argb(al, 0xFFD060));
        for (int j = 0; j < 9; j++) {
            double ang = j * 2 * Math.PI / 9 + 0.4;
            int qx = (int) (g.cx + Math.cos(ang) * rx * 0.75), qy = (int) (capY + Math.sin(ang) * ry * 0.7);
            ellipse(g.ctx, qx, qy, rx * 0.45, ry * 0.62, argb(al, j % 2 == 0 ? 0x8A3410 : 0xB84A12));
        }
        ellipse(g.ctx, g.cx, capY, rx * 0.8, ry * 0.8, argb(al, 0xD2661A));
        ellipse(g.ctx, g.cx, capY - 2, rx * 0.5, ry * 0.5, argb(al, 0xFF9A24));
        ellipse(g.ctx, g.cx, capY - 4, rx * 0.26, ry * 0.28, argb(al * (1 - c01(s / 2.5)), 0xFFE9A0));
        ellipse(g.ctx, g.cx, capY + (int) (ry * 0.8), rx * 1.05, ry * 0.28, argb(al * 0.8, 0x7A2A0C));
        ringDots(g.ctx, g.cx, gy, k * 0.9, k * 0.22, 220, 0, 2, argb(c01(1 - k / 1800.0) * g.fade, 0xFFE9A0));
        Random er = new Random(g.n.start ^ 0xE3B5L);
        for (int i = 0; i < 50; i++) {
            double ang = -Math.PI * (0.1 + er.nextDouble() * 0.8), spd = 120 + er.nextDouble() * 240;
            double px = g.cx + Math.cos(ang) * spd * s, py = gy + Math.sin(ang) * spd * s + 220 * s * s;
            g.ctx.fill((int) px, (int) py, (int) px + 2, (int) py + 2, argb(c01(1 - k / 2200.0) * g.fade, i % 2 == 0 ? 0xFFB020 : 0xFFE14A));
        }
        skyline(g, gy, g.fade);
        fillA(g, 0, 0, g.w, g.h, Math.max(0, 1 - k / 700.0), 0xFFFFFF);
        if (k > 500) {
            double pop = PaymentHud.easeOutBack(c01((k - 500) / 400.0));
            String big = Stats.fmt(g.n.amount) + " DETONATED";
            outlined(g, big, g.cx, (int) (g.h * 0.10), fit(g, big, (float) (2.4 * pop), g.w - 20), 0xFFD23C, 0x7A1500, g.fade);
            text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.10) + 26, 1.2f, WHITE, g.fade);
        }
    }

    // ================================================================ $500M  MIDAS TOUCH
    private static void midas(G g) {
        long el = g.el;
        double p = Math.min(PaymentHud.ease(c01((el - 300) / 2300.0)), PaymentHud.ease(c01((g.dur - el) / 1000.0)));
        PaymentHud.cue(g.n, 0, el, 300, "minecraft:item.armor.equip_gold", 1.0f, 0.8f);
        PaymentHud.cue(g.n, 1, el, 2400, "minecraft:entity.player.levelup", 0.9f, 0.9f);
        if (el > 500 && el < 3000) PaymentHud.tick(g.n, el, 120, "minecraft:entity.experience_orb.pickup", 0.4f, 0.7f);
        int flood = (int) (g.h * 0.92 * p);
        for (int x = 0; x < g.w; x += 2) {
            double wob = Math.sin(x * 0.07 + el * 0.004) * 6 + Math.sin(x * 0.19 - el * 0.006) * 3;
            int hh = (int) Math.max(0, flood + wob * Math.min(1, p * 3));
            if (hh <= 0) continue;
            g.ctx.fillGradient(x, 0, x + 2, hh, argb(0.88, 0xFFE27A), argb(0.88, 0xB8860B));
            g.ctx.fill(x, Math.max(0, hh - 2), x + 2, hh, argb(0.95, 0xFFF6C0));
        }
        Random dr = new Random(g.n.start ^ 0xD71BL);
        for (int i = 0; i < 16; i++) {
            int dx = (int) (dr.nextDouble() * g.w);
            double delay = 1200 + dr.nextDouble() * 1500, maxLen = 40 + dr.nextDouble() * 50;
            double len = c01((el - delay) / 900.0) * maxLen * c01(p * 2);
            if (len < 1) continue;
            g.ctx.fill(dx, flood - 2, dx + 3, (int) (flood + len), argb(0.9, 0xE8B923));
            ellipse(g.ctx, dx + 1, (int) (flood + len), 2.5, 3.5, argb(0.95, 0xFFE27A));
        }
        for (int i = 0; i < 3; i++) {
            double bx = ((el * 0.22 + i * 140) % (g.w + 200)) - 100;
            rotRect(g, bx, g.h * 0.5, 14, g.h * 1.6, 0.5, argb(0.14 * Math.min(1, p * 3), 0xFFFFFF));
        }
        Random sr = new Random(g.n.start ^ 0x57A2L);
        for (int i = 0; i < 40; i++) {
            int sx = sr.nextInt(Math.max(1, g.w)), sy = sr.nextInt(Math.max(1, g.h));
            double tw = 0.5 + 0.5 * Math.sin(el / 160.0 + i);
            if (sy > flood - 4) continue;
            fillA(g, sx - 2, sy, sx + 3, sy + 1, 0.9 * tw, 0xFFFFFF);
            fillA(g, sx, sy - 2, sx + 1, sy + 3, 0.9 * tw, 0xFFFFFF);
        }
        int hy = (int) (g.h * 0.38);
        double q = c01((el - 1500) / 900.0);
        if (q > 0) {
            for (int i = 0; i < 12; i++) {
                double ang = el / 1500.0 + i * Math.PI / 6;
                line(g, g.cx + Math.cos(ang) * 34, hy + 27 + Math.sin(ang) * 34, g.cx + Math.cos(ang) * 130, hy + 27 + Math.sin(ang) * 130, 2, argb(0.28 * q * g.fade, 0xFFF6C0));
            }
        }
        head(g, g.cx - 27, hy, 54);
        if (q > 0) {
            fillA(g, g.cx - 27, hy, g.cx + 27, hy + 54, 0.62 * q * g.fade, 0xFFC800);
            g.ctx.fill(g.cx - 34, hy + 56, g.cx + 34, hy + 66, argb(q * g.fade, 0xB8860B));
            g.ctx.fill(g.cx - 34, hy + 56, g.cx + 34, hy + 58, argb(q * g.fade, 0xFFF6C0));
        }
        if (el > 1100) {
            double pop = PaymentHud.easeOutBack(c01((el - 1100) / 400.0));
            outlined(g, "MIDAS TOUCH", g.cx, (int) (g.h * 0.10), (float) (1.8 * pop), 0x4A2F00, 0xFFF1B0, g.fade);
            outlined(g, "+" + Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.10) + 22, (float) (3.2 * pop), 0x4A2F00, 0xFFF1B0, g.fade);
            outlined(g, "from " + g.n.name, g.cx, hy + 72, 1.3f, 0x4A2F00, 0xFFF1B0, g.fade);
        }
    }

    // ================================================================ $1B  HYPERDRIVE
    private static void hyperdrive(G g) {
        long el = g.el;
        int cx = g.cx, cy = (int) (g.h * 0.48);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.beacon.power_select", 0.8f, 0.6f);
        PaymentHud.cue(g.n, 1, el, 1200, "minecraft:entity.warden.sonic_charge", 0.7f, 1.0f);
        PaymentHud.cue(g.n, 2, el, 3500, "minecraft:entity.generic.explode", 0.7f, 1.8f);
        PaymentHud.cue(g.n, 3, el, 5200, "minecraft:ui.toast.challenge_complete", 1.0f, 1.0f);
        fillA(g, 0, 0, g.w, g.h, 0.9 * g.fade, 0x02020A);
        boolean tunnel = el >= 3500 && el < 5200;
        double v = el < 1500 ? 0.05 + el / 1500.0 * 0.15 : el < 3500 ? 0.2 + (el - 1500) / 2000.0 * 1.8 : el < 5200 ? 0 : Math.max(0.1, 1 - (el - 5200) / 700.0);
        if (!tunnel) {
            Random sr = new Random(g.n.start ^ 0x4CD5L);
            double maxR = Math.hypot(g.w, g.h) * 0.62;
            for (int i = 0; i < 170; i++) {
                double ang = sr.nextDouble() * Math.PI * 2, r0 = sr.nextDouble(), sp = 0.4 + sr.nextDouble() * 0.9;
                double pos = (r0 + el / 1000.0 * sp * 0.55) % 1.0;
                double rad = pos * pos * maxR, len = 2 + v * 70 * pos * pos;
                double ca = Math.cos(ang), sa = Math.sin(ang);
                line(g, cx + ca * (rad - len), cy + sa * (rad - len), cx + ca * rad, cy + sa * rad, pos > 0.6 ? 2 : 1,
                    argb(Math.min(1, pos * 1.6) * g.fade, pos > 0.7 ? 0xFFFFFF : 0x9FB8FF));
            }
        } else {
            for (int k = 0; k < 16; k++) {
                double t = ((k + el / 260.0) % 16) / 16.0;
                double size = 10 + t * t * Math.max(g.w, g.h) * 1.1, rot = el / 900.0 + k * 0.12;
                int col = Shows.hsv(el / 1800.0 + k * 0.05, 0.7, 1.0);
                double a = Math.min(1, t * 2.2) * g.fade;
                for (int e = 0; e < 4; e++) {
                    double th = rot + e * Math.PI / 2;
                    rotRect(g, cx + Math.cos(th) * size / 2, cy + Math.sin(th) * size / 2, size, 2 + t * 2, th + Math.PI / 2, argb(a, col));
                }
            }
            ellipse(g.ctx, cx, cy, 18 + Math.sin(el / 80.0) * 3, 18 + Math.sin(el / 80.0) * 3, argb(0.9 * g.fade, 0xFFFFFF));
        }
        if (el >= 3500 && el < 4000) fillA(g, 0, 0, g.w, g.h, 1 - (el - 3500) / 500.0, 0xFFFFFF);
        if (el >= 5200 && el < 5700) fillA(g, 0, 0, g.w, g.h, 1 - (el - 5200) / 500.0, 0xFFFFFF);
        if (el >= 500 && el < 3400 && (el / 200) % 3 != 0)
            chroma(g, "ENGAGING HYPERDRIVE", cx, (int) (g.h * 0.12), 1.5f, 1.5 + v * 2, WHITE, g.fade);
        if (tunnel) chroma(g, "LIGHT SPEED", cx, (int) (g.h * 0.12), 2.0f, 3, 0xBFD8FF, g.fade);
        if (el >= 5200) {
            long k = el - 5200;
            double pop = PaymentHud.easeOutBack(c01(k / 400.0));
            for (int i = 0; i < 4; i++) ellipse(g.ctx, cx, cy - 22, 70 - i * 14, 70 - i * 14, argb(0.10 * g.fade, 0x9FB8FF));
            head(g, cx - 26, cy - 48, 52);
            chroma(g, "+" + Stats.fmt(g.n.amount), cx, cy + 12, (float) (3.4 * pop), 1 + 2 * Math.max(0, 1 - k / 800.0), WHITE, g.fade);
            text(g, "from " + g.n.name, cx, cy + 44, 1.3f, 0xBFD8FF, g.fade);
        }
    }

    // ================================================================ $1T  REALITY SHATTER
    private static void shatter(G g) {
        long el = g.el;
        int cx = g.cx, cy = g.h / 2;
        boolean broke = el >= 1500;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.glass.hit", 0.8f, 1.4f);
        PaymentHud.cue(g.n, 1, el, 400, "minecraft:block.glass.hit", 0.8f, 1.2f);
        PaymentHud.cue(g.n, 2, el, 800, "minecraft:block.glass.hit", 0.9f, 1.0f);
        PaymentHud.cue(g.n, 3, el, 1500, "minecraft:block.glass.break", 1.0f, 0.7f);
        PaymentHud.cue(g.n, 4, el, 1500, "minecraft:entity.generic.explode", 0.7f, 0.6f);
        PaymentHud.cue(g.n, 5, el, 2200, "minecraft:ui.toast.challenge_complete", 1.0f, 0.8f);
        if (broke) {
            fillA(g, 0, 0, g.w, g.h, 0.93 * g.fade, 0x03030C);
            stars(g, 90, 0.9 * g.fade);
            double kk = c01((el - 1500) / 700.0);
            for (int i = 0; i < 18; i++) {
                double ang = el / 4000.0 + i * 2 * Math.PI / 18;
                line(g, cx, cy, cx + Math.cos(ang) * 500, cy + Math.sin(ang) * 500, 2, argb(0.18 * kk * g.fade, GOLD));
            }
            ellipse(g.ctx, cx, cy - 20, 70 * kk, 70 * kk, argb(0.25 * kk * g.fade, GOLD));
            head(g, cx - 32, cy - 52, 64);
        }
        int cols = 14, rows = 8;
        double cw = g.w / (double) cols + 1, ch = g.h / (double) rows + 1;
        Random r = new Random(g.n.start ^ 0x61A5L);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double ox = (col + 0.5) * g.w / cols, oy = (row + 0.5) * g.h / rows;
                double vx = (r.nextDouble() - 0.5) * 160, vy = -40 + r.nextDouble() * 80, vr = (r.nextDouble() - 0.5) * 6;
                double t = (el - 1500 - Math.hypot(ox - cx, oy - cy) * 1.2) / 1000.0;
                double alpha = 0.50 * g.fade;
                if (t < 0) {
                    rotRect(g, ox, oy, cw - 1, ch - 1, 0, argb(alpha, 0x0A1428));
                    rotRect(g, ox, oy, cw * 0.5, ch * 0.5, 0, argb(alpha * 0.25, 0x6AA8FF));
                } else if (t < 2.2) {
                    double dx = vx * t, dy = vy * t + 260 * t * t, rot = vr * t, a = alpha * (1 - t / 2.2);
                    rotRect(g, ox + dx, oy + dy, cw - 1, ch - 1, rot, argb(a, 0x0A1428));
                    rotRect(g, ox + dx, oy + dy, cw * 0.5, ch * 0.5, rot, argb(a * 0.35, 0x6AA8FF));
                }
            }
        }
        if (el < 1700) {
            double reach = c01(el / 900.0) * 330, ca = el < 1500 ? 1.0 : 1 - (el - 1500) / 200.0;
            Random cr = new Random(g.n.start ^ 0xC4AL);
            for (int i = 0; i < 14; i++) {
                double ang = i * 2 * Math.PI / 14 + cr.nextDouble() * 0.3, d = 0, x = cx, y = cy;
                for (int sgm = 0; sgm < 8 && d < reach; sgm++) {
                    double l = 18 + cr.nextDouble() * 34;
                    l = Math.min(l, reach - d);
                    double nx = x + Math.cos(ang) * l, ny = y + Math.sin(ang) * l;
                    line(g, x, y, nx, ny, 1, argb(0.9 * ca * g.fade, 0xFFFFFF));
                    x = nx; y = ny; d += l;
                    ang += (cr.nextDouble() - 0.5) * 0.6;
                }
            }
            fillA(g, cx - 5, cy, cx + 6, cy + 1, ca * g.fade, 0xFFFFFF);
            fillA(g, cx, cy - 5, cx + 1, cy + 6, ca * g.fade, 0xFFFFFF);
        }
        if (!broke) chroma(g, "REALITY IS BREAKING", cx, (int) (g.h * 0.12), 1.5f, 1 + 2 * c01(el / 1400.0), WHITE, g.fade);
        if (el >= 1500 && el < 2000) fillA(g, 0, 0, g.w, g.h, 1 - (el - 1500) / 500.0, 0xFFFFFF);
        if (el >= 1900) {
            long k = el - 1900;
            double pop = PaymentHud.easeOutBack(c01(k / 450.0));
            outlined(g, "+" + Stats.fmt(g.n.amount), cx, cy + 24, (float) (4.2 * pop), GOLD, 0x2A1A00, g.fade);
            text(g, g.n.name + " BROKE REALITY", cx, cy + 62, 1.4f, WHITE, g.fade);
        }
    }

    // ================================================================ helpers
    private static void head(G g, int x, int y, int size) { PlayerSkinDrawer.draw(g.ctx, g.skin, x, y, size); }

    private static void fillA(G g, int x1, int y1, int x2, int y2, double alpha, int rgb) {
        g.ctx.fill(x1, y1, x2, y2, argb(alpha, rgb));
    }

    private static void text(G g, String s, int cx, int y, float sc, int rgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        g.ctx.drawCenteredTextWithShadow(g.tr, Text.literal(s), 0, 0, targb(alpha, rgb));
        g.ms.popMatrix();
    }

    private static void textNS(G g, String s, int cx, int y, float sc, int rgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        g.ctx.drawText(g.tr, s, -g.tr.getWidth(s) / 2, 0, targb(alpha, rgb), false);
        g.ms.popMatrix();
    }

    private static void textL(G g, String s, int x, int y, float sc, int rgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) x, (float) y);
        g.ms.scale(sc, sc);
        g.ctx.drawText(g.tr, s, 0, 0, targb(alpha, rgb), true);
        g.ms.popMatrix();
    }

    private static void textR(G g, String s, int x, int y, float sc, int rgb, double alpha) {
        textL(g, s, x - (int) (g.tr.getWidth(s) * sc), y, sc, rgb, alpha);
    }

    private static void outlined(G g, String s, int cx, int y, float sc, int rgb, int outRgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        int x = -g.tr.getWidth(s) / 2, oc = targb(alpha, outRgb);
        int[][] d = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}};
        for (int[] o : d) g.ctx.drawText(g.tr, s, x + o[0], o[1], oc, false);
        g.ctx.drawText(g.tr, s, x, 0, targb(alpha, rgb), false);
        g.ms.popMatrix();
    }

    /** Neon glow: soft coloured halo behind a bright core. */
    private static void glowText(G g, String s, int cx, int y, float sc, int glowRgb, int coreRgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        int x = -g.tr.getWidth(s) / 2, gc = targb(alpha * 0.30, glowRgb);
        int[][] d = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}};
        for (int[] o : d) g.ctx.drawText(g.tr, s, x + o[0], o[1], gc, false);
        g.ctx.drawText(g.tr, s, x, 0, targb(alpha, coreRgb), false);
        g.ms.popMatrix();
    }

    /** RGB-split text: red and cyan copies pushed apart by d pixels behind the main text. */
    private static void chroma(G g, String s, int cx, int y, float sc, double d, int rgb, double alpha) {
        text(g, s, (int) (cx - d), y, sc, 0xFF2A2A, alpha * 0.8);
        text(g, s, (int) (cx + d), y, sc, 0x2AFFFF, alpha * 0.8);
        text(g, s, cx, y, sc, rgb, alpha);
    }

    private static void rainbow(G g, String s, int cx, int y, float sc) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        int x = -g.tr.getWidth(s) / 2;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            g.ctx.drawText(g.tr, ch, x, 0, targb(g.fade, Shows.hsv(g.el / 700.0 + i * 0.08, 0.85, 1.0)), true);
            x += g.tr.getWidth(ch);
        }
        g.ms.popMatrix();
    }

    private static void line(G g, double x1, double y1, double x2, double y2, double thick, int argb) {
        double dx = x2 - x1, dy = y2 - y1, len = Math.hypot(dx, dy);
        if (len < 0.5) return;
        int t = (int) Math.max(1, Math.round(thick));
        g.ms.pushMatrix();
        g.ms.translate((float) x1, (float) y1);
        g.ms.rotate((float) Math.atan2(dy, dx));
        g.ctx.fill(0, -t / 2, (int) Math.ceil(len), t - t / 2, argb);
        g.ms.popMatrix();
    }

    private static void rotRect(G g, double cx, double cy, double w, double h, double rot, int argb) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) cy);
        g.ms.rotate((float) rot);
        g.ctx.fill((int) (-w / 2), (int) (-h / 2), (int) Math.ceil(w / 2), (int) Math.ceil(h / 2), argb);
        g.ms.popMatrix();
    }

    private static void ellipse(DrawContext ctx, int cx, int cy, double rx, double ry, int argb) {
        int r = (int) Math.ceil(ry);
        for (int dy = -r; dy <= r; dy++) {
            double t = dy / ry;
            if (Math.abs(t) > 1) continue;
            int hw = (int) Math.round(rx * Math.sqrt(1 - t * t));
            if (hw <= 0) continue;
            ctx.fill(cx - hw, cy + dy, cx + hw, cy + dy + 1, argb);
        }
    }

    private static void ringDots(DrawContext ctx, int cx, int cy, double rx, double ry, int dots, double rot, int size, int argb) {
        for (int i = 0; i < dots; i++) {
            double a = rot + i * 2 * Math.PI / dots;
            int x = (int) Math.round(cx + Math.cos(a) * rx), y = (int) Math.round(cy + Math.sin(a) * ry);
            ctx.fill(x, y, x + size, y + size, argb);
        }
    }

    private static void stars(G g, int count, double alpha) {
        Random r = new Random(g.n.start ^ 0x57A5L);
        for (int i = 0; i < count; i++) {
            int x = r.nextInt(Math.max(1, g.w)), y = r.nextInt(Math.max(1, g.h));
            double ph = r.nextDouble() * 6.28, tw = 0.5 + 0.5 * Math.sin(g.el / 220.0 + ph);
            int sz = r.nextInt(4) == 0 ? 2 : 1;
            g.ctx.fill(x, y, x + sz, y + sz, argb(alpha * tw, WHITE));
        }
    }

    private static String corrupt(String s, Random r, double p) {
        StringBuilder sb = new StringBuilder();
        String junk = "#@%&?!01";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c != ' ' && r.nextDouble() < p * 0.25 ? junk.charAt(r.nextInt(junk.length())) : c);
        }
        return sb.toString();
    }

    private static int lerpRgb(int a, int b, double t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return (((int) (ar + (br - ar) * t)) << 16) | (((int) (ag + (bg - ag) * t)) << 8) | ((int) (ab + (bb - ab) * t));
    }

    private static float fit(G g, String s, float sc, int maxW) {
        int tw = Math.max(1, g.tr.getWidth(s));
        return tw * sc > maxW ? maxW / (float) tw : sc;
    }

    private static double c01(double v) { return Math.max(0, Math.min(1, v)); }

    private static int targb(double alpha, int rgb) { return (PaymentHud.clampA(alpha) << 24) | (rgb & 0xFFFFFF); }

    private static int argb(double alpha, int rgb) {
        return (Math.max(0, Math.min(255, (int) (alpha * 255))) << 24) | (rgb & 0xFFFFFF);
    }
}
