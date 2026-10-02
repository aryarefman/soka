import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RenderPoppinsArchitecture {
    public static void main(String[] args) throws Exception {
        // Load genuine Poppins fonts
        Font poppinsRegular = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Poppins-Regular.ttf"));
        Font poppinsMedium  = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Poppins-Medium.ttf"));
        Font poppinsSemi    = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Poppins-SemiBold.ttf"));
        Font poppinsBold    = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Poppins-Bold.ttf"));

        // Render at 2x resolution for retina sharpness, then save
        double scale = 2.0;
        int baseW = 530;
        int baseH = 840;
        int w = (int) (baseW * scale);
        int h = (int) (baseH * scale);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Enable top quality antialiasing and text rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Scale graphics context
        g.scale(scale, scale);

        // Pure white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, baseW, baseH);

        Color darkBlue    = new Color(15, 23, 42);    // #0f172a
        Color textMuted   = new Color(100, 116, 139); // #64748b
        Color borderColor = new Color(30, 41, 59);    // #1e293b
        Color badgeBg     = new Color(246, 247, 249); // #f6f7f9
        Color badgeText   = new Color(75, 85, 99);    // #4b5563
        Color orangeColor = new Color(225, 112, 0);   // #e17000 vibrant warm orange

        // 1. Header Title
        g.setColor(darkBlue);
        g.setFont(poppinsBold.deriveFont(22.0f));
        drawCentered(g, "Arsitektur Cloud", baseW / 2, 38);

        // Breadcrumb: "Datacenter → Host → VM → Cloudlet / Task"
        drawBreadcrumb(g, baseW / 2, 60, poppinsMedium.deriveFont(12.5f), textMuted);

        // 2. Box 1: Datacenter
        int b1W = 230;
        int b1H = 76;
        int b1X = (baseW - b1W) / 2;
        int b1Y = 94;
        drawBox(g, b1X, b1Y, b1W, b1H, borderColor);

        g.setColor(darkBlue);
        g.setFont(poppinsSemi.deriveFont(15.5f));
        drawCentered(g, "Datacenter", baseW / 2, b1Y + 33);
        g.setColor(textMuted);
        g.setFont(poppinsRegular.deriveFont(12.0f));
        drawCentered(g, "3 datacenter heterogen", baseW / 2, b1Y + 55);

        // Badge 1 & Arrow pointing down to Host row
        int badge1Y = b1Y + b1H + 20; // 190
        int badge1H = drawPill(g, "menaungi 24 host heterogen (8 host / DC)", baseW / 2, badge1Y,
                badgeBg, badgeText, poppinsRegular.deriveFont(11.5f));

        int hRow1Y = 248;
        int arrow1Top = badge1Y + badge1H / 2;
        drawArrow(g, baseW / 2, arrow1Top, baseW / 2, hRow1Y, textMuted);

        // 3. Host Boxes (3 on top row, 1 centered below)
        int hostW = 156;
        int hostH = 94;
        int hX1 = 15;
        int hX2 = 187;
        int hX3 = 359;

        drawHostCard(g, hX1, hRow1Y, hostW, hostH, "Tipe A", "\u00D74", "1.000 MIPS/core", "8 GB RAM \u00B7 10 Gbps",
                borderColor, darkBlue, textMuted, poppinsSemi, poppinsRegular);

        drawHostCard(g, hX2, hRow1Y, hostW, hostH, "Tipe B", "\u00D78", "1.500 MIPS/core", "16 GB RAM \u00B7 10 Gbps",
                borderColor, darkBlue, textMuted, poppinsSemi, poppinsRegular);

        drawHostCard(g, hX3, hRow1Y, hostW, hostH, "Tipe C", "\u00D78", "2.000 MIPS/core", "24 GB RAM \u00B7 10 Gbps",
                borderColor, darkBlue, textMuted, poppinsSemi, poppinsRegular);

        // Row 2: Tipe D
        int hRow2Y = 358;
        drawHostCard(g, hX2, hRow2Y, hostW, hostH, "Tipe D", "\u00D74", "3.000 MIPS/core", "32 GB RAM \u00B7 10 Gbps",
                borderColor, darkBlue, textMuted, poppinsSemi, poppinsRegular);

        // Badge 2 & Arrow pointing down to VM row
        int badge2Y = hRow2Y + hostH + 28; // 480
        int badge2H = drawPill2Lines(g, "VM dialokasikan ke host secara best-fit", "berdasarkan kapasitas MIPS & RAM",
                baseW / 2, badge2Y, badgeBg, badgeText, poppinsRegular.deriveFont(11.5f));

        int vmY = 538;
        int arrow2Top = badge2Y + badge2H / 2;
        drawArrow(g, baseW / 2, arrow2Top, baseW / 2, vmY, textMuted);

        // 4. VM Boxes (Small, Medium, Large)
        int vmW = 156;
        int vmH = 82;

        drawVmCard(g, hX1, vmY, vmW, vmH, "Small", "\u00D710", "1 PE \u00B7 1 GB RAM",
                borderColor, darkBlue, textMuted, poppinsSemi, poppinsRegular);

        drawVmCard(g, hX2, vmY, vmW, vmH, "Medium", "\u00D710", "2 PE \u00B7 4 GB RAM",
                borderColor, darkBlue, textMuted, poppinsSemi, poppinsRegular);

        drawVmCard(g, hX3, vmY, vmW, vmH, "Large", "\u00D710", "4 PE \u00B7 8 GB RAM",
                borderColor, darkBlue, textMuted, poppinsSemi, poppinsRegular);

        // Badge 3 & Arrow pointing down to Cloudlet box
        int badge3Y = vmY + vmH + 20; // 640
        int badge3H = drawPill(g, "setiap task dieksekusi pada 1 VM, non-preemptive", baseW / 2, badge3Y,
                badgeBg, badgeText, poppinsRegular.deriveFont(11.5f));

        int bLastY = 692;
        int arrow3Top = badge3Y + badge3H / 2;
        drawArrow(g, baseW / 2, arrow3Top, baseW / 2, bLastY, textMuted);

        // 5. Bottom Box (1.000 Cloudlet / Task)
        int bLastW = 420;
        int bLastH = 118;
        int bLastX = (baseW - bLastW) / 2;
        drawBox(g, bLastX, bLastY, bLastW, bLastH, borderColor);

        g.setColor(orangeColor);
        g.setFont(poppinsBold.deriveFont(38.0f));
        drawCentered(g, "1.000", baseW / 2, bLastY + 46);

        g.setColor(darkBlue);
        g.setFont(poppinsSemi.deriveFont(15.5f));
        drawCentered(g, "Cloudlet / Task", baseW / 2, bLastY + 73);

        g.setColor(textMuted);
        g.setFont(poppinsRegular.deriveFont(11.5f));
        drawCentered(g, "task per skenario \u00B7 bag-of-tasks, panjang bervariasi (MI)", baseW / 2, bLastY + 97);

        g.dispose();

        // Save high-resolution PNG
        File outFile = new File("c:/Users/arya4/soka/docs/images/arsitektur_cloud_3dc.png");
        ImageIO.write(img, "png", outFile);
        System.out.println("Rendered Poppins architecture diagram to: " + outFile.getAbsolutePath());
    }

    private static void drawBox(Graphics2D g, int x, int y, int w, int h, Color border) {
        RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, w, h, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(rect);
        g.setColor(border);
        g.setStroke(new BasicStroke(1.3f));
        g.draw(rect);
    }

    private static void drawHostCard(Graphics2D g, int x, int y, int w, int h,
                                     String name, String count, String spec1, String spec2,
                                     Color border, Color titleColor, Color subColor,
                                     Font titleFont, Font specFont) {
        drawBox(g, x, y, w, h, border);

        // Title row: "Tipe A" (SemiBold) + " ×4" (Regular)
        Font titleF = titleFont.deriveFont(14.5f);
        Font countF = specFont.deriveFont(12.5f);

        FontMetrics fmT = g.getFontMetrics(titleF);
        FontMetrics fmC = g.getFontMetrics(countF);
        int tw = fmT.stringWidth(name);
        int cw = fmC.stringWidth(" " + count);
        int startX = x + (w - (tw + cw)) / 2;

        g.setColor(titleColor);
        g.setFont(titleF);
        g.drawString(name, startX, y + 33);

        g.setColor(subColor);
        g.setFont(countF);
        g.drawString(" " + count, startX + tw, y + 33);

        // Specs
        g.setFont(specFont.deriveFont(11.5f));
        drawCentered(g, spec1, x + w / 2, y + 58);
        drawCentered(g, spec2, x + w / 2, y + 78);
    }

    private static void drawVmCard(Graphics2D g, int x, int y, int w, int h,
                                   String name, String count, String spec,
                                   Color border, Color titleColor, Color subColor,
                                   Font titleFont, Font specFont) {
        drawBox(g, x, y, w, h, border);

        Font titleF = titleFont.deriveFont(14.5f);
        Font countF = specFont.deriveFont(12.5f);

        FontMetrics fmT = g.getFontMetrics(titleF);
        FontMetrics fmC = g.getFontMetrics(countF);
        int tw = fmT.stringWidth(name);
        int cw = fmC.stringWidth(" " + count);
        int startX = x + (w - (tw + cw)) / 2;

        g.setColor(titleColor);
        g.setFont(titleF);
        g.drawString(name, startX, y + 34);

        g.setColor(subColor);
        g.setFont(countF);
        g.drawString(" " + count, startX + tw, y + 34);

        g.setFont(specFont.deriveFont(11.5f));
        drawCentered(g, spec, x + w / 2, y + 60);
    }

    private static int drawPill(Graphics2D g, String text, int cx, int cy, Color bg, Color textColor, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        int padX = 14;
        int padY = 5;
        int bw = tw + 2 * padX;
        int bh = fm.getHeight() + 2 * padY;
        int bx = cx - bw / 2;
        int by = cy - bh / 2;

        g.setColor(bg);
        g.fillRoundRect(bx, by, bw, bh, 6, 6);
        g.setColor(textColor);
        g.drawString(text, cx - tw / 2, cy + fm.getAscent() / 2 - 1);
        return bh;
    }

    private static int drawPill2Lines(Graphics2D g, String line1, String line2, int cx, int cy, Color bg, Color textColor, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int tw1 = fm.stringWidth(line1);
        int tw2 = fm.stringWidth(line2);
        int maxTw = Math.max(tw1, tw2);
        int padX = 16;
        int padY = 6;
        int bh = fm.getHeight() * 2 + 2 * padY;
        int bw = maxTw + 2 * padX;
        int bx = cx - bw / 2;
        int by = cy - bh / 2;

        g.setColor(bg);
        g.fillRoundRect(bx, by, bw, bh, 6, 6);

        g.setColor(textColor);
        g.drawString(line1, cx - tw1 / 2, by + padY + fm.getAscent() - 2);
        g.drawString(line2, cx - tw2 / 2, by + padY + fm.getAscent() + fm.getHeight() - 4);
        return bh;
    }

    private static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(x1, y1, x2, y2);

        int sz = 4;
        Polygon arrow = new Polygon();
        arrow.addPoint(x2, y2);
        arrow.addPoint(x2 - sz, y2 - sz * 2);
        arrow.addPoint(x2 + sz, y2 - sz * 2);
        g.fill(arrow);
    }

    // Draws "Datacenter → Host → VM → Cloudlet / Task" cleanly with geometric vector arrows
    private static void drawBreadcrumb(Graphics2D g, int cx, int cy, Font font, Color color) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        String[] parts = {"Datacenter", "Host", "VM", "Cloudlet / Task"};
        int arrowWidth = 14;
        int spacing = 8;

        int totalW = 0;
        for (int i = 0; i < parts.length; i++) {
            totalW += fm.stringWidth(parts[i]);
            if (i < parts.length - 1) {
                totalW += spacing * 2 + arrowWidth;
            }
        }

        int currX = cx - totalW / 2;
        g.setColor(color);

        for (int i = 0; i < parts.length; i++) {
            g.drawString(parts[i], currX, cy);
            currX += fm.stringWidth(parts[i]);

            if (i < parts.length - 1) {
                currX += spacing;
                // Draw vector arrow ->
                int ax1 = currX;
                int ax2 = currX + arrowWidth;
                int ay = cy - fm.getAscent() / 3;

                g.setStroke(new BasicStroke(1.2f));
                g.drawLine(ax1, ay, ax2, ay);
                // Arrowhead
                g.drawLine(ax2 - 3, ay - 3, ax2, ay);
                g.drawLine(ax2 - 3, ay + 3, ax2, ay);

                currX += arrowWidth + spacing;
            }
        }
    }

    private static void drawCentered(Graphics2D g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(text);
        g.drawString(text, x - w / 2, y);
    }
}
