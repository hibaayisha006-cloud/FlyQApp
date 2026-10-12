import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * ==============================================================================
 * flyQ — The Classic Golden Age Aviation Booking Suite
 * ==============================================================================
 * An authentic, original, and classic flight booking application
 * inspired by the Golden Age of Aviation (1930s-1950s Pan-Am & Imperial Airways).
 *
 * Color Palette:
 *  - Base:             #0B1F4A (Deep Imperial Navy)
 *  - Primary Action:   #1E90FF (Dodger / Aero Blue)
 *  - Highlights / CTA: #F5C842 (Warm Golden Amber)
 *  - Background:       #F0FBFF (Soft Ice Sky / Ivory Canvas)
 *
 * Audio:
 *  - Silent mode enabled (zero button click sounds as requested).
 *
 * Compilation & Execution:
 *  - javac FlyQApp.java && java FlyQApp
 * ==============================================================================
 */
public class FlyQApp {

    // ------------------------------------------------------------------ Palette (#0B1F4A, #1E90FF, #F5C842, #F0FBFF)
    static final Color BASE_NAVY      = new Color(0x0B, 0x1F, 0x4A); // #0B1F4A (Base)
    static final Color PRIMARY_BLUE   = new Color(0x1E, 0x90, 0xFF); // #1E90FF (Primary Action)
    static final Color HIGHLIGHT_GOLD = new Color(0xF5, 0xC8, 0x42); // #F5C842 (Highlights / CTAs)
    static final Color BG_ICE         = new Color(0xF0, 0xFB, 0xFF); // #F0FBFF (Background)
    static final Color CARD_WHITE     = new Color(0xFF, 0xFF, 0xFF); // Crisp Card White
    static final Color MUTED_TEXT     = new Color(0x56, 0x68, 0x82); // Elegant Slate Grey
    static final Color POSTAL_RED     = new Color(0xC0, 0x39, 0x2B); // Vintage Seal Red

    static final int WIN_W = 410, WIN_H = 820;
    static final long START_TIME = System.nanoTime();
    static final Random RNG = new Random();
    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    static double time() { return (System.nanoTime() - START_TIME) / 1e9; }

    static Font serif(int style, float size) {
        return new Font("Georgia", style, (int) size).deriveFont(style, size);
    }

    static Font classicDisplay(int style, float size) {
        return new Font("Palatino Linotype", style, (int) size).deriveFont(style, size);
    }

    // ------------------------------------------------------------------ Aviation Domain Model
    static final String[][] CITIES = {
            {"HYD", "Hyderabad", "Rajiv Gandhi Int'l"},
            {"BLR", "Bengaluru", "Kempegowda Int'l"},
            {"DEL", "New Delhi", "Indira Gandhi Int'l"},
            {"BOM", "Bombay / Mumbai", "Chhatrapati Shivaji"},
            {"MAA", "Madras / Chennai", "Meenambakkam Aerodrome"},
            {"COK", "Cochin / Kochi", "Nedumbassery Terminal"},
            {"CCU", "Calcutta / Kolkata", "Dum Dum Aerodrome"},
            {"GOI", "Goa", "Dabolim Coastal Airbase"}
    };

    static String cityName(String code) {
        for (String[] c : CITIES) if (c[0].equals(code)) return c[1];
        return code;
    }

    static class Flight {
        String airline, code, depTime, arrTime;
        int durationMins, stops, baseFare;
        String aircraftType;
        String travelClass;

        Flight(String airline, String code, String depTime, String arrTime, int durationMins, int stops, int baseFare, String aircraftType, String travelClass) {
            this.airline = airline;
            this.code = code;
            this.depTime = depTime;
            this.arrTime = arrTime;
            this.durationMins = durationMins;
            this.stops = stops;
            this.baseFare = baseFare;
            this.aircraftType = aircraftType;
            this.travelClass = travelClass;
        }

        String formattedDuration() {
            int h = durationMins / 60;
            int m = durationMins % 60;
            return h + "h " + (m < 10 ? "0" + m : m) + "m";
        }
    }

    static class Ticket {
        String fromCode, toCode, depTime, arrTime, flightDate;
        String airline, flightNo, aircraft, travelClass;
        String boardingGate, pnrNumber;
        List<String> passengerNames = new ArrayList<>();
        List<String> seatAssignments = new ArrayList<>();
        int totalFare;

        Ticket(String fromCode, String toCode, String depTime, String arrTime, String flightDate,
               String airline, String flightNo, String aircraft, String travelClass,
               List<String> names, List<String> seats, int totalFare) {
            this.fromCode = fromCode;
            this.toCode = toCode;
            this.depTime = depTime;
            this.arrTime = arrTime;
            this.flightDate = flightDate;
            this.airline = airline;
            this.flightNo = flightNo;
            this.aircraft = aircraft;
            this.travelClass = travelClass;
            this.passengerNames.addAll(names);
            this.seatAssignments.addAll(seats);
            this.totalFare = totalFare;

            this.boardingGate = "G-" + (char) ('A' + RNG.nextInt(5)) + (1 + RNG.nextInt(12));
            String pnrChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(pnrChars.charAt(RNG.nextInt(pnrChars.length())));
            this.pnrNumber = sb.toString();
        }
    }

    static class AppState {
        String passengerName = "";
        String passengerEmail = "";
        String fromStation = "HYD";
        String toStation = "COK";
        LocalDate departureDate = LocalDate.now().plusDays(7);
        int passengerCount = 1;
        int filterMode = 0;
        String cabinClass = "Imperial First Class";

        Flight selectedFlight;
        List<String> registeredPassengers = new ArrayList<>();
        List<String> selectedSeats = new ArrayList<>();
        String paymentOption = "Imperial Card  •••• 4821";
        int discountPercentage = 0;

        Ticket activeBoardingPass;
        final List<Ticket> upcomingTrips = new ArrayList<>();
        final List<Ticket> completedTrips = new ArrayList<>();
        final List<Flight> availableFlights = new ArrayList<>();

        AppState() {
            availableFlights.add(new Flight("Air India Classic", "AI-101", "06:45", "08:25", 100, 0, 5850, "Lockheed Constellation", "Imperial First"));
            availableFlights.add(new Flight("Vistara Sovereign", "UK-720", "09:30", "11:15", 105, 0, 6950, "Clipper Heritage", "Royal Club"));
            availableFlights.add(new Flight("IndiGo Pioneer", "6E-304", "13:15", "14:55", 100, 0, 4920, "Boeing 314 Navigator", "Classic Cabin"));
            availableFlights.add(new Flight("Akasa Horizon", "QP-882", "17:40", "19:15", 95, 0, 5140, "Douglas DC-6 Cruiser", "Club Classic"));
            availableFlights.add(new Flight("Imperial Express", "IE-219", "20:30", "22:10", 100, 0, 7250, "Aero Concorde Classic", "Imperial First"));
            availableFlights.add(new Flight("SpiceJet Vintage", "SG-441", "15:20", "18:40", 200, 1, 4150, "De Havilland Comet", "Heritage"));

            LocalDate d = LocalDate.now();
            upcomingTrips.add(new Ticket("HYD", "COK", "06:45", "08:25", d.plusDays(4).format(DATE_FMT),
                    "Air India Classic", "AI-101", "Lockheed Constellation", "Imperial First",
                    Arrays.asList("Ayisha Hiba"), Arrays.asList("1A"), 5850));
            upcomingTrips.add(new Ticket("HYD", "DEL", "09:30", "11:55", d.plusDays(12).format(DATE_FMT),
                    "Vistara Sovereign", "UK-412", "Clipper Heritage", "Royal Club",
                    Arrays.asList("Ayisha Hiba"), Arrays.asList("2B"), 6950));
            completedTrips.add(new Ticket("BOM", "HYD", "18:20", "19:45", d.minusDays(28).format(DATE_FMT),
                    "Air India Classic", "AI-502", "Lockheed Constellation", "Imperial First",
                    Arrays.asList("Ayisha Hiba"), Arrays.asList("1B"), 5850));
        }
    }

    static final AppState STATE = new AppState();

    // ------------------------------------------------------------------ Navigation
    static JFrame mainFrame;
    static JPanel cardContainer;
    static final CardLayout CARD_LAYOUT = new CardLayout();
    static final Map<String, ClassicScreen> SCREEN_REGISTRY = new LinkedHashMap<>();
    static ClassicScreen currentScreen;

    static void navigateTo(String screenName) {
        ClassicScreen s = SCREEN_REGISTRY.get(screenName);
        if (s != null) {
            s.onActivated();
            CARD_LAYOUT.show(cardContainer, screenName);
            currentScreen = s;
            s.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }

            mainFrame = new JFrame("flyQ — Classic Aviation Suite");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            cardContainer = new JPanel(CARD_LAYOUT);
            cardContainer.setPreferredSize(new Dimension(WIN_W, WIN_H));

            SCREEN_REGISTRY.put("splash", new SplashClassicScreen());
            SCREEN_REGISTRY.put("signup", new SignUpClassicScreen());
            SCREEN_REGISTRY.put("signin", new SignInClassicScreen());
            SCREEN_REGISTRY.put("home", new HomeClassicScreen());
            SCREEN_REGISTRY.put("search", new SearchClassicScreen());
            SCREEN_REGISTRY.put("booking", new BookingClassicScreen());
            SCREEN_REGISTRY.put("seatmap", new SeatMapClassicScreen());
            SCREEN_REGISTRY.put("payment", new PaymentClassicScreen());
            SCREEN_REGISTRY.put("confirmed", new ConfirmedClassicScreen());
            SCREEN_REGISTRY.put("tickets", new TicketsVaultClassicScreen());

            for (Map.Entry<String, ClassicScreen> e : SCREEN_REGISTRY.entrySet()) {
                cardContainer.add(e.getValue(), e.getKey());
            }

            mainFrame.setContentPane(cardContainer);
            mainFrame.pack();
            mainFrame.setResizable(false);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);

            navigateTo("splash");

            // Animation timer (30 FPS)
            new javax.swing.Timer(33, e -> {
                if (currentScreen != null) currentScreen.repaint();
            }).start();
        });
    }

    // ------------------------------------------------------------------ Drawing Helpers
    static Graphics2D initAA(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    static void textL(Graphics2D g, String s, Font f, Color c, float x, float y) {
        g.setFont(f); g.setColor(c); g.drawString(s, x, y);
    }
    static void textR(Graphics2D g, String s, Font f, Color c, float xr, float y) {
        g.setFont(f); g.setColor(c);
        g.drawString(s, xr - g.getFontMetrics().stringWidth(s), y);
    }
    static void textC(Graphics2D g, String s, Font f, Color c, float cx, float y) {
        g.setFont(f); g.setColor(c);
        g.drawString(s, cx - g.getFontMetrics().stringWidth(s) / 2f, y);
    }

    static void drawGuillocheWave(Graphics2D g, double x, double y, double width, double height, Color col) {
        g.setColor(col);
        g.setStroke(new BasicStroke(0.8f));
        Path2D p1 = new Path2D.Double();
        Path2D p2 = new Path2D.Double();
        boolean first = true;
        for (double px = 0; px <= width; px += 2.0) {
            double u = px / 16.0;
            double py1 = y + Math.sin(u) * height * 0.45;
            double py2 = y + Math.sin(u + Math.PI / 2) * height * 0.35;
            if (first) {
                p1.moveTo(x + px, py1);
                p2.moveTo(x + px, py2);
                first = false;
            } else {
                p1.lineTo(x + px, py1);
                p2.lineTo(x + px, py2);
            }
        }
        g.draw(p1);
        g.draw(p2);
    }

    static void drawClassicFrame(Graphics2D g, double x, double y, double w, double h, double arc, Color fill, Color border) {
        RoundRectangle2D outer = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
        if (fill != null) {
            g.setColor(fill);
            g.fill(outer);
        }
        if (border != null) {
            g.setColor(border);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(outer);
            RoundRectangle2D inner = new RoundRectangle2D.Double(x + 3.5, y + 3.5, w - 7, h - 7, Math.max(0, arc - 4), Math.max(0, arc - 4));
            g.setColor(new Color(border.getRed(), border.getGreen(), border.getBlue(), 80));
            g.setStroke(new BasicStroke(0.8f));
            g.draw(inner);
        }
    }

    static void drawWingedInsignia(Graphics2D g, double cx, double cy, double scale, Color col) {
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.scale(scale, scale);
        g.setColor(col);

        g.setStroke(new BasicStroke(1.8f));
        g.draw(new Ellipse2D.Double(-12, -12, 24, 24));
        g.draw(new Ellipse2D.Double(-8, -8, 16, 16));

        Path2D star = new Path2D.Double();
        star.moveTo(0, -18); star.lineTo(2.5, -4); star.lineTo(18, 0); star.lineTo(2.5, 4);
        star.lineTo(0, 18); star.lineTo(-2.5, 4); star.lineTo(-18, 0); star.lineTo(-2.5, -4);
        star.closePath();
        g.fill(star);

        for (int i = 0; i < 3; i++) {
            Path2D wingL = new Path2D.Double();
            double yOff = -4 + i * 5;
            double len = 42 - i * 8;
            wingL.moveTo(-10, yOff);
            wingL.curveTo(-22, yOff - 6, -34, yOff - 4, -len, yOff - 2);
            wingL.curveTo(-32, yOff + 2, -20, yOff + 4, -10, yOff + 2);
            wingL.closePath();
            g.fill(wingL);

            Path2D wingR = new Path2D.Double();
            wingR.moveTo(10, yOff);
            wingR.curveTo(22, yOff - 6, 34, yOff - 4, len, yOff - 2);
            wingR.curveTo(32, yOff + 2, 20, yOff + 4, 10, yOff + 2);
            wingR.closePath();
            g.fill(wingR);
        }

        g.setTransform(old);
    }

    static void drawClassicStreamliner(Graphics2D g, double x, double y, double s, double angle, double time) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);
        g.scale(s, s);

        Path2D farWing = new Path2D.Double();
        farWing.moveTo(28, -6);
        farWing.lineTo(-12, -40);
        farWing.lineTo(8, -40);
        farWing.lineTo(58, -6);
        farWing.closePath();
        g.setColor(new Color(0x6D, 0x82, 0xA2));
        g.fill(farWing);

        g.setColor(HIGHLIGHT_GOLD);
        g.fill(new RoundRectangle2D.Double(-116, -34, 14, 28, 4, 4));
        g.setColor(BASE_NAVY);
        g.fill(new RoundRectangle2D.Double(-114, -32, 10, 24, 3, 3));

        Path2D fuselage = new Path2D.Double();
        fuselage.moveTo(-112, -8);
        fuselage.curveTo(-60, -18, 40, -18, 80, -10);
        fuselage.curveTo(116, -6, 126, 2, 128, 6);
        fuselage.curveTo(124, 14, 90, 18, 50, 16);
        fuselage.curveTo(-40, 16, -90, 12, -112, 4);
        fuselage.closePath();

        g.setPaint(new GradientPaint(0, -18, Color.WHITE, 0, 18, new Color(0xD8, 0xE9, 0xF8)));
        g.fill(fuselage);

        Shape clip = g.getClip();
        g.clip(fuselage);
        g.setColor(PRIMARY_BLUE);
        g.fill(new Rectangle2D.Double(-120, 2, 260, 4.5));
        g.setColor(HIGHLIGHT_GOLD);
        g.fill(new Rectangle2D.Double(-120, 6.5, 260, 2));
        g.setClip(clip);

        g.setColor(BASE_NAVY);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(fuselage);

        for (int i = 0; i < 11; i++) {
            g.fill(new Ellipse2D.Double(-76 + i * 13.5, -6.5, 6, 7.5));
        }

        Path2D cockpit = new Path2D.Double();
        cockpit.moveTo(88, -8);
        cockpit.lineTo(108, -5);
        cockpit.lineTo(112, 1);
        cockpit.lineTo(92, 1);
        cockpit.closePath();
        g.setColor(BASE_NAVY);
        g.fill(cockpit);

        Path2D nearWing = new Path2D.Double();
        nearWing.moveTo(34, 4);
        nearWing.lineTo(-24, 56);
        nearWing.lineTo(4, 56);
        nearWing.lineTo(76, 6);
        nearWing.closePath();
        g.setPaint(new GradientPaint(0, 4, Color.WHITE, 0, 56, new Color(0xC0, 0xD4, 0xEB)));
        g.fill(nearWing);
        g.setColor(BASE_NAVY);
        g.draw(nearWing);

        double[] engineX = {18, 52};
        double[] engineY = {22, 10};
        for (int e = 0; e < 2; e++) {
            double ex = engineX[e], ey = engineY[e];
            g.setColor(new Color(0xD8, 0xE6, 0xF5));
            g.fill(new RoundRectangle2D.Double(ex, ey, 42, 15, 8, 8));
            g.setColor(BASE_NAVY);
            g.draw(new RoundRectangle2D.Double(ex, ey, 42, 15, 8, 8));
            g.setColor(HIGHLIGHT_GOLD);
            g.fill(new Ellipse2D.Double(ex + 36, ey + 4, 8, 7));

            g.setColor(new Color(30, 144, 255, 140));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Ellipse2D.Double(ex + 38, ey - 14, 4, 43));
        }

        g.setTransform(old);
    }

    static void drawSkyScene(Graphics2D g, int w, int h, double t) {
        g.setPaint(new LinearGradientPaint(0, 0, 0, h,
                new float[]{0f, 0.5f, 1f},
                new Color[]{BG_ICE, new Color(0xE3, 0xF4, 0xFD), new Color(0xD0, 0xEC, 0xFB)}));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(30, 144, 255, 25));
        g.setStroke(new BasicStroke(0.6f));
        for (int y = 60; y < h; y += 90) g.drawLine(0, y, w, y);
        for (int x = 40; x < w; x += 110) g.drawLine(x, 0, x, h);

        for (int i = 0; i < 5; i++) {
            double cx = ((i * 140 + t * (12 + i * 3)) % (w + 260)) - 130;
            double cy = 110 + (i % 3) * 65;
            double scale = 0.65 + (i % 3) * 0.25;
            g.setColor(new Color(255, 255, 255, 140));
            double[][] puffs = {{0, 0, 110, 38}, {35, -20, 85, 48}, {75, -8, 95, 42}, {-30, 8, 80, 30}};
            for (double[] p : puffs) {
                g.fill(new Ellipse2D.Double(cx + p[0] * scale, cy + p[1] * scale, p[2] * scale, p[3] * scale));
            }
        }
    }

    // ------------------------------------------------------------------ UI Components
    static class FlatPanel extends JPanel {
        FlatPanel() { setOpaque(false); }
        FlatPanel(LayoutManager lm) { super(lm); setOpaque(false); }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
    }

    static class ScrollColumn extends FlatPanel implements Scrollable {
        ScrollColumn() { setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 22; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 100; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    static JScrollPane classicScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return sp;
    }

    static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    static FlatPanel classicField(String title, JComponent comp) {
        FlatPanel p = new FlatPanel(new BorderLayout(0, 4));
        JLabel titleLbl = label(title, serif(Font.BOLD, 10), BASE_NAVY);
        p.add(titleLbl, BorderLayout.NORTH);

        FlatPanel box = new FlatPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = initAA(g0);
                drawClassicFrame(g, 0.5, 0.5, getWidth() - 1, getHeight() - 1, 10, CARD_WHITE, new Color(0x1E, 0x90, 0xFF, 140));
                g.dispose();
            }
        };
        box.setPreferredSize(new Dimension(100, 42));
        box.setBorder(new EmptyBorder(2, 12, 2, 10));

        comp.setFont(serif(Font.PLAIN, 14));
        comp.setForeground(BASE_NAVY);
        if (comp instanceof JTextComponent) {
            comp.setOpaque(false);
            comp.setBorder(null);
            ((JTextComponent) comp).setCaretColor(PRIMARY_BLUE);
        } else if (comp instanceof JComboBox) {
            comp.setBackground(CARD_WHITE);
            comp.setBorder(null);
        }
        box.add(comp, BorderLayout.CENTER);
        p.add(box, BorderLayout.CENTER);
        return p;
    }

    /** Buttons styled with CTA #F5C842 and Primary #1E90FF */
    static class ClassicButton extends JButton {
        boolean isHighlightCTA;

        ClassicButton(String text, boolean isHighlightCTA) {
            super(text);
            this.isHighlightCTA = isHighlightCTA;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(serif(Font.BOLD, 14));
            setForeground(isHighlightCTA ? BASE_NAVY : Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(320, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = initAA(g0);
            int w = getWidth(), h = getHeight();
            boolean hot = getModel().isRollover();

            g.setColor(new Color(11, 31, 74, 30));
            g.fill(new RoundRectangle2D.Double(2, 3, w - 4, h - 3, 14, 14));

            Shape shape = new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 14, 14);
            if (isHighlightCTA) {
                g.setPaint(new GradientPaint(0, 0, hot ? new Color(0xFF, 0xDC, 0x73) : HIGHLIGHT_GOLD, 0, h, new Color(0xE0, 0xB0, 0x2A)));
            } else {
                g.setPaint(new GradientPaint(0, 0, hot ? new Color(0x43, 0xA2, 0xFF) : PRIMARY_BLUE, 0, h, new Color(0x10, 0x74, 0xD4)));
            }
            g.fill(shape);

            g.setColor(isHighlightCTA ? BASE_NAVY : new Color(255, 255, 255, 160));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);

            g.dispose();
            super.paintComponent(g0);
        }
    }

    static class RoundActionButton extends JButton {
        int iconType;
        int diameter;

        RoundActionButton(int iconType, int diameter) {
            this.iconType = iconType;
            this.diameter = diameter;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Dimension d = new Dimension(diameter + 10, diameter + 10);
            setPreferredSize(d); setMaximumSize(d); setMinimumSize(d);
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = initAA(g0);
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
            double r = diameter / 2.0;

            g.setPaint(new RadialGradientPaint(new Point2D.Double(cx - 2, cy - 2), (float) r,
                    new float[]{0f, 1f},
                    new Color[]{PRIMARY_BLUE, BASE_NAVY}));
            g.fill(new Ellipse2D.Double(cx - r, cy - r, diameter, diameter));

            g.setColor(HIGHLIGHT_GOLD);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Ellipse2D.Double(cx - r, cy - r, diameter, diameter));

            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (iconType == 0) { // Back chevron
                Path2D p = new Path2D.Double();
                p.moveTo(cx + 4, cy - 7); p.lineTo(cx - 3, cy); p.lineTo(cx + 4, cy + 7);
                g.draw(p);
            } else { // Launch / Next arrow
                g.draw(new Line2D.Double(cx - 8, cy, cx + 8, cy));
                Path2D p = new Path2D.Double();
                p.moveTo(cx + 2, cy - 6); p.lineTo(cx + 8, cy); p.lineTo(cx + 2, cy + 6);
                g.draw(p);
            }
            g.dispose();
        }
    }

    static class ClassicNavigationDeck extends JPanel {
        int selectedIndex;

        ClassicNavigationDeck(int selectedIndex) {
            this.selectedIndex = selectedIndex;
            setOpaque(false);
            setPreferredSize(new Dimension(WIN_W, 66));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int slot = e.getX() / (getWidth() / 4);
                    if (slot == 0) navigateTo("home");
                    else if (slot == 1) navigateTo("search");
                    else if (slot == 2) navigateTo("tickets");
                    else {
                        int choice = JOptionPane.showConfirmDialog(mainFrame,
                                "Passenger: " + (STATE.passengerName.isEmpty() ? "Voyager" : STATE.passengerName) + "\nEmail: " + STATE.passengerEmail
                                        + "\nSaloon: " + STATE.cabinClass + "\n\nSign out?",
                                "Aviation Registry Profile", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) navigateTo("signin");
                    }
                }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = initAA(g0);
            int w = getWidth(), h = getHeight();

            Shape deck = new RoundRectangle2D.Double(0, 0, w, h + 30, 24, 24);
            g.setColor(BASE_NAVY);
            g.fill(deck);

            g.setColor(HIGHLIGHT_GOLD);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(0, 1, w, 1);

            String[] titles = {"Hangar", "Voyages", "Manifest", "Officer"};
            for (int i = 0; i < 4; i++) {
                double cx = w / 8.0 + i * (w / 4.0), cy = 24;
                boolean active = (i == selectedIndex);

                if (active) {
                    g.setColor(PRIMARY_BLUE);
                    g.fill(new RoundRectangle2D.Double(cx - 22, cy - 14, 44, 28, 12, 12));
                }

                Color col = active ? Color.WHITE : HIGHLIGHT_GOLD;
                g.setColor(col);
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                if (i == 0) {
                    Path2D hPath = new Path2D.Double();
                    hPath.moveTo(cx - 8, cy + 2); hPath.lineTo(cx, cy - 7); hPath.lineTo(cx + 8, cy + 2);
                    g.draw(hPath);
                    g.draw(new Rectangle2D.Double(cx - 5, cy + 2, 10, 7));
                } else if (i == 1) {
                    drawWingedInsignia(g, cx, cy, 0.42, col);
                } else if (i == 2) {
                    g.draw(new RoundRectangle2D.Double(cx - 9, cy - 6, 18, 12, 3, 3));
                    g.drawLine((int) cx - 2, (int) cy - 6, (int) cx - 2, (int) cy + 6);
                } else {
                    g.draw(new Ellipse2D.Double(cx - 4, cy - 9, 8, 8));
                    g.draw(new Arc2D.Double(cx - 8, cy, 16, 12, 0, 180, Arc2D.OPEN));
                }

                textC(g, titles[i], serif(Font.BOLD, 10), active ? HIGHLIGHT_GOLD : new Color(0xC0, 0xD4, 0xEB), (float) cx, 54);
            }
            g.dispose();
        }
    }

    static abstract class ClassicScreen extends JPanel {
        ClassicScreen() { setLayout(new BorderLayout()); setOpaque(false); }
        void onActivated() { }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = initAA(g0);
            drawSkyScene(g, getWidth(), getHeight(), time());
            double u = ((time() * 0.04) % 1.0);
            double px = -160 + u * (getWidth() + 320);
            double py = getHeight() * 0.42 + Math.sin(u * Math.PI * 2) * 8;
            drawClassicStreamliner(g, px, py, 0.46, -0.04, time());
            g.dispose();
        }
    }

    // ================================================================== 1. SPLASH SCREEN
    static class SplashClassicScreen extends ClassicScreen {
        SplashClassicScreen() {
            FlatPanel south = new FlatPanel();
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.setBorder(new EmptyBorder(0, 0, 46, 0));

            // Direct route: Leads to user details registration page
            RoundActionButton enterBtn = new RoundActionButton(1, 64);
            enterBtn.setAlignmentX(0.5f);
            enterBtn.addActionListener(e -> navigateTo("signup"));

            JLabel promptLbl = label("COMMENCE VOYAGE", serif(Font.BOLD, 12), BASE_NAVY);
            promptLbl.setAlignmentX(0.5f);

            south.add(enterBtn);
            south.add(Box.createVerticalStrut(8));
            south.add(promptLbl);
            add(south, BorderLayout.SOUTH);
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = initAA(g0);
            int w = getWidth();
            double crestY = 210;

            drawWingedInsignia(g, w / 2.0, crestY - 34, 1.0, BASE_NAVY);

            Font brandFont = classicDisplay(Font.BOLD, 54);
            g.setFont(brandFont);
            FontMetrics fm = g.getFontMetrics();
            int bw = fm.stringWidth("flyQ");
            float lx = (w - bw) / 2f;

            g.setColor(BASE_NAVY);
            g.drawString("fly", lx, (float) crestY + 36);
            g.setColor(PRIMARY_BLUE);
            g.drawString("Q", lx + fm.stringWidth("fly"), (float) crestY + 36);

            drawGuillocheWave(g, w / 2.0 - 110, crestY + 48, 220, 10, HIGHLIGHT_GOLD);

            textC(g, "ROYAL TRANSIT & AERO NAVIGATION", serif(Font.BOLD, 10), BASE_NAVY, w / 2f, (float) crestY + 76);
            textC(g, "Established MCMXXVIII  •  Imperial Flagship Service", serif(Font.ITALIC, 11), MUTED_TEXT, w / 2f, (float) crestY + 94);

            g.dispose();
        }
    }

    // ================================================================== 2. USER DETAILS SCREEN (AFTER SPLASH)
    static class SignUpClassicScreen extends ClassicScreen {
        JTextField nameField = new JTextField("");
        JTextField emailField = new JTextField("");
        JPasswordField passField = new JPasswordField("");

        SignUpClassicScreen() {
            FlatPanel content = new FlatPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(new EmptyBorder(22, 28, 14, 28));

            FlatPanel banner = new FlatPanel() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = initAA(g0);
                    drawClassicFrame(g, 0, 0, getWidth(), getHeight(), 16, BASE_NAVY, HIGHLIGHT_GOLD);
                    textL(g, "Passenger Registration", serif(Font.BOLD, 18), HIGHLIGHT_GOLD, 18, 32);
                    textL(g, "Please enter your personal details", serif(Font.ITALIC, 11), Color.WHITE, 18, 50);
                    g.dispose();
                }
            };
            banner.setPreferredSize(new Dimension(340, 72));
            content.add(banner);
            content.add(Box.createVerticalStrut(14));

            content.add(classicField("PASSENGER FULL NAME", nameField));
            content.add(Box.createVerticalStrut(8));
            content.add(classicField("OFFICIAL EMAIL ADDRESS", emailField));
            content.add(Box.createVerticalStrut(8));
            content.add(classicField("SECURITY CIPHER / PASSWORD", passField));
            content.add(Box.createVerticalStrut(14));

            ClassicButton submitBtn = new ClassicButton("Submit & Proceed to Hangar →", true);
            submitBtn.setAlignmentX(0f);
            submitBtn.addActionListener(e -> {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                STATE.passengerName = name.isEmpty() ? "Honored Aviator" : name;
                STATE.passengerEmail = email;
                navigateTo("home");
            });
            content.add(submitBtn);
            content.add(Box.createVerticalStrut(12));

            // Sign In option in this page
            JButton signInLink = new JButton("Already have an account? Sign In Here →");
            signInLink.setFont(serif(Font.BOLD, 12));
            signInLink.setForeground(PRIMARY_BLUE);
            signInLink.setBorderPainted(false);
            signInLink.setContentAreaFilled(false);
            signInLink.setFocusPainted(false);
            signInLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            signInLink.setAlignmentX(0f);
            signInLink.addActionListener(e -> navigateTo("signin"));
            content.add(signInLink);

            for (Component c : content.getComponents()) if (c instanceof JComponent) ((JComponent) c).setAlignmentX(0f);
            add(content, BorderLayout.CENTER);
        }
    }

    // ================================================================== 3. SIGN IN SCREEN
    static class SignInClassicScreen extends ClassicScreen {
        JTextField emailField = new JTextField("");
        JPasswordField passField = new JPasswordField("");

        SignInClassicScreen() {
            FlatPanel content = new FlatPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(new EmptyBorder(22, 28, 14, 28));

            FlatPanel banner = new FlatPanel() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = initAA(g0);
                    drawClassicFrame(g, 0, 0, getWidth(), getHeight(), 16, BASE_NAVY, HIGHLIGHT_GOLD);
                    textL(g, "Welcome Aboard", serif(Font.BOLD, 18), HIGHLIGHT_GOLD, 18, 32);
                    textL(g, "Verification of Registered Aviator", serif(Font.ITALIC, 11), Color.WHITE, 18, 50);
                    g.dispose();
                }
            };
            banner.setPreferredSize(new Dimension(340, 72));
            content.add(banner);
            content.add(Box.createVerticalStrut(16));

            content.add(classicField("REGISTERED EMAIL", emailField));
            content.add(Box.createVerticalStrut(8));
            content.add(classicField("SECURITY CIPHER", passField));
            content.add(Box.createVerticalStrut(14));

            ClassicButton signInBtn = new ClassicButton("Authorize & Enter Hangar →", true);
            signInBtn.setAlignmentX(0f);
            signInBtn.addActionListener(e -> {
                String email = emailField.getText().trim();
                if (!email.isEmpty()) STATE.passengerEmail = email;
                if (STATE.passengerName.isEmpty()) STATE.passengerName = "Captain Aviator";
                navigateTo("home");
            });
            content.add(signInBtn);

            for (Component c : content.getComponents()) if (c instanceof JComponent) ((JComponent) c).setAlignmentX(0f);
            add(content, BorderLayout.CENTER);
        }
    }

    // ================================================================== 4. HOME SCREEN
    static class HomeClassicScreen extends ClassicScreen {
        JComboBox<String> fromBox = new JComboBox<>();
        JComboBox<String> toBox = new JComboBox<>();
        JTextField dateField = new JTextField();
        JComboBox<String> paxBox = new JComboBox<>(new String[]{"1 Passenger", "2 Passengers", "3 Passengers", "4 Passengers"});
        FlatPanel recentTripsList = new FlatPanel();

        HomeClassicScreen() {
            for (String[] c : CITIES) {
                fromBox.addItem(c[0] + " — " + c[1]);
                toBox.addItem(c[0] + " — " + c[1]);
            }

            FlatPanel content = new FlatPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(new EmptyBorder(16, 24, 10, 24));

            FlatPanel top = new FlatPanel(new BorderLayout());
            FlatPanel logoBox = new FlatPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            logoBox.add(label("fly", classicDisplay(Font.BOLD, 26), BASE_NAVY));
            logoBox.add(label("Q", classicDisplay(Font.BOLD, 26), PRIMARY_BLUE));
            top.add(logoBox, BorderLayout.WEST);
            top.add(label("Saloon: " + STATE.cabinClass, serif(Font.ITALIC, 11), MUTED_TEXT), BorderLayout.EAST);
            content.add(top);
            content.add(Box.createVerticalStrut(8));

            FlatPanel banner = new FlatPanel() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = initAA(g0);
                    drawClassicFrame(g, 0, 0, getWidth(), getHeight(), 16, BASE_NAVY, HIGHLIGHT_GOLD);
                    textL(g, "Grand Trans-Continental Flights", serif(Font.BOLD, 15), HIGHLIGHT_GOLD, 16, 28);
                    String displayName = STATE.passengerName.isEmpty() ? "Honored Voyager" : STATE.passengerName;
                    textL(g, "Voyages Cataloged for " + displayName, serif(Font.ITALIC, 11), Color.WHITE, 16, 46);
                    g.dispose();
                }
            };
            banner.setPreferredSize(new Dimension(350, 64));
            content.add(banner);
            content.add(Box.createVerticalStrut(10));

            FlatPanel bookingCard = new FlatPanel() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = initAA(g0);
                    drawClassicFrame(g, 0, 0, getWidth(), getHeight(), 18, CARD_WHITE, new Color(0x1E, 0x90, 0xFF, 120));
                    g.dispose();
                }
            };
            bookingCard.setLayout(new BoxLayout(bookingCard, BoxLayout.Y_AXIS));
            bookingCard.setBorder(new EmptyBorder(12, 14, 12, 14));

            bookingCard.add(classicField("DEPARTURE AERODROME", fromBox));
            bookingCard.add(Box.createVerticalStrut(6));
            bookingCard.add(classicField("DESTINATION AERODROME", toBox));
            bookingCard.add(Box.createVerticalStrut(6));

            FlatPanel grid2 = new FlatPanel(new GridLayout(1, 2, 8, 0));
            grid2.add(classicField("DATE", dateField));
            grid2.add(classicField("VOYAGERS", paxBox));
            bookingCard.add(grid2);
            bookingCard.add(Box.createVerticalStrut(10));

            ClassicButton searchBtn = new ClassicButton("Search Imperial Flights →", true);
            searchBtn.setAlignmentX(0f);
            searchBtn.addActionListener(e -> {
                STATE.fromStation = CITIES[fromBox.getSelectedIndex()][0];
                STATE.toStation = CITIES[toBox.getSelectedIndex()][0];
                STATE.passengerCount = paxBox.getSelectedIndex() + 1;
                navigateTo("search");
            });
            bookingCard.add(searchBtn);

            for (Component c : bookingCard.getComponents()) if (c instanceof JComponent) ((JComponent) c).setAlignmentX(0f);
            content.add(bookingCard);
            content.add(Box.createVerticalStrut(10));

            content.add(label("Upcoming Voyages in Logbook", serif(Font.BOLD, 14), BASE_NAVY));
            content.add(Box.createVerticalStrut(6));

            recentTripsList.setLayout(new BoxLayout(recentTripsList, BoxLayout.Y_AXIS));
            content.add(recentTripsList);

            content.add(Box.createVerticalGlue());
            for (Component c : content.getComponents()) if (c instanceof JComponent) ((JComponent) c).setAlignmentX(0f);

            add(content, BorderLayout.CENTER);
            add(new ClassicNavigationDeck(0), BorderLayout.SOUTH);
        }

        @Override void onActivated() {
            super.onActivated();
            dateField.setText(STATE.departureDate.format(DATE_FMT));
            fromBox.setSelectedIndex(0);
            toBox.setSelectedIndex(5);
            recentTripsList.removeAll();

            for (Ticket t : STATE.upcomingTrips) {
                FlatPanel card = new FlatPanel() {
                    @Override protected void paintComponent(Graphics g0) {
                        Graphics2D g = initAA(g0);
                        drawClassicFrame(g, 0, 0, getWidth(), getHeight(), 12, CARD_WHITE, new Color(0x0B, 0x1F, 0x4A, 60));
                        textL(g, t.fromCode + " → " + t.toCode, serif(Font.BOLD, 15), BASE_NAVY, 14, 24);
                        textR(g, t.flightDate, serif(Font.PLAIN, 11), MUTED_TEXT, getWidth() - 14, 24);
                        textL(g, t.airline + " (" + t.flightNo + ")", serif(Font.ITALIC, 11), MUTED_TEXT, 14, 42);
                        textR(g, "PNR: " + t.pnrNumber, serif(Font.BOLD, 11), POSTAL_RED, getWidth() - 14, 42);
                        g.dispose();
                    }
                };
                card.setPreferredSize(new Dimension(350, 54));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        STATE.activeBoardingPass = t;
                        navigateTo("confirmed");
                    }
                });
                recentTripsList.add(card);
                recentTripsList.add(Box.createVerticalStrut(6));
            }
            recentTripsList.revalidate();
            recentTripsList.repaint();
        }
    }

    // ================================================================== 5. SEARCH SCREEN
    static class SearchClassicScreen extends ClassicScreen {
        ScrollColumn flightList = new ScrollColumn();
        JScrollPane scrollPane;

        SearchClassicScreen() {
            FlatPanel topBar = new FlatPanel(new BorderLayout(10, 0));
            topBar.setBorder(new EmptyBorder(16, 22, 6, 22));
            RoundActionButton backBtn = new RoundActionButton(0, 36);
            backBtn.addActionListener(e -> navigateTo("home"));
            topBar.add(backBtn, BorderLayout.WEST);
            topBar.add(label("Imperial Flight Timetable", classicDisplay(Font.BOLD, 18), BASE_NAVY), BorderLayout.CENTER);

            FlatPanel header = new FlatPanel() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = initAA(g0);
                    int w = getWidth(), h = getHeight();
                    drawClassicFrame(g, 0, 0, w, h, 16, BASE_NAVY, HIGHLIGHT_GOLD);
                    textL(g, STATE.fromStation, classicDisplay(Font.BOLD, 26), Color.WHITE, 18, 38);
                    textR(g, STATE.toStation, classicDisplay(Font.BOLD, 26), Color.WHITE, w - 18, 38);
                    textL(g, cityName(STATE.fromStation), serif(Font.PLAIN, 10), HIGHLIGHT_GOLD, 18, 54);
                    textR(g, cityName(STATE.toStation), serif(Font.PLAIN, 10), HIGHLIGHT_GOLD, w - 18, 54);

                    g.setColor(HIGHLIGHT_GOLD);
                    g.drawLine(85, 34, w - 85, 34);
                    textC(g, STATE.departureDate.format(DATE_FMT) + "  •  " + STATE.passengerCount + " Voyagers", serif(Font.ITALIC, 11), Color.WHITE, w / 2f, 72);
                    g.dispose();
                }
            };
            header.setPreferredSize(new Dimension(350, 84));

            FlatPanel north = new FlatPanel();
            north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
            north.setBorder(new EmptyBorder(0, 22, 6, 22));
            north.add(topBar);
            north.add(header);
            add(north, BorderLayout.NORTH);

            flightList.setBorder(new EmptyBorder(4, 22, 16, 22));
            scrollPane = classicScroll(flightList);
            add(scrollPane, BorderLayout.CENTER);
        }

        void refreshSchedule() {
            flightList.removeAll();
            for (Flight f : STATE.availableFlights) {
                FlatPanel card = new FlatPanel() {
                    @Override protected void paintComponent(Graphics g0) {
                        Graphics2D g = initAA(g0);
                        int w = getWidth(), h = getHeight();
                        drawClassicFrame(g, 0, 0, w, h, 14, CARD_WHITE, new Color(0x1E, 0x90, 0xFF, 120));

                        textL(g, f.airline, serif(Font.BOLD, 15), BASE_NAVY, 16, 26);
                        textR(g, "₹" + String.format("%,d", f.baseFare), classicDisplay(Font.BOLD, 18), POSTAL_RED, w - 16, 26);
                        textL(g, f.aircraftType + "  •  " + f.code, serif(Font.ITALIC, 10), MUTED_TEXT, 16, 42);

                        textL(g, f.depTime, classicDisplay(Font.BOLD, 20), BASE_NAVY, 16, 70);
                        textR(g, f.arrTime, classicDisplay(Font.BOLD, 20), BASE_NAVY, w - 16, 70);

                        textC(g, f.formattedDuration(), serif(Font.PLAIN, 10), MUTED_TEXT, w / 2f, 60);
                        textC(g, f.stops == 0 ? "Non-Stop" : "1 Port", serif(Font.ITALIC, 10), MUTED_TEXT, w / 2f, 76);

                        g.dispose();
                    }
                };
                card.setPreferredSize(new Dimension(350, 94));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        STATE.selectedFlight = f;
                        navigateTo("booking");
                    }
                });
                flightList.add(card);
                flightList.add(Box.createVerticalStrut(8));
            }
            flightList.revalidate();
            flightList.repaint();
        }

        @Override void onActivated() {
            super.onActivated();
            refreshSchedule();
        }
    }

    // ================================================================== 6. BOOKING SCREEN
    static class BookingClassicScreen extends ClassicScreen {
        ScrollColumn column = new ScrollColumn();
        JScrollPane scrollPane;
        List<JTextField[]> inputs = new ArrayList<>();

        BookingClassicScreen() {
            FlatPanel topBar = new FlatPanel(new BorderLayout(10, 0));
            topBar.setBorder(new EmptyBorder(16, 22, 6, 22));
            RoundActionButton backBtn = new RoundActionButton(0, 36);
            backBtn.addActionListener(e -> navigateTo("search"));
            topBar.add(backBtn, BorderLayout.WEST);
            topBar.add(label("Passenger Manifest Register", classicDisplay(Font.BOLD, 18), BASE_NAVY), BorderLayout.CENTER);
            add(topBar, BorderLayout.NORTH);

            column.setBorder(new EmptyBorder(4, 22, 8, 22));
            scrollPane = classicScroll(column);
            add(scrollPane, BorderLayout.CENTER);

            FlatPanel south = new FlatPanel();
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.setBorder(new EmptyBorder(4, 22, 20, 22));

            ClassicButton seatBtn = new ClassicButton("Select Cabin Berths & Seats →", true);
            seatBtn.setAlignmentX(0f);
            seatBtn.addActionListener(e -> {
                List<String> valid = new ArrayList<>();
                for (JTextField[] f : inputs) {
                    valid.add(f[0].getText().trim().isEmpty() ? "Voyager" : f[0].getText().trim());
                }
                STATE.registeredPassengers = valid;
                navigateTo("seatmap");
            });
            south.add(seatBtn);
            add(south, BorderLayout.SOUTH);
        }

        @Override void onActivated() {
            super.onActivated();
            column.removeAll();
            inputs.clear();

            for (int i = 0; i < STATE.passengerCount; i++) {
                column.add(label("VOYAGER " + (i + 1), serif(Font.BOLD, 11), BASE_NAVY));
                column.add(Box.createVerticalStrut(4));

                JTextField nameField = new JTextField(i == 0 && !STATE.passengerName.isEmpty() ? STATE.passengerName : "");
                JTextField idField = new JTextField("");

                FlatPanel g1 = new FlatPanel(new GridLayout(1, 2, 8, 0));
                g1.add(classicField("NAME", nameField));
                g1.add(classicField("PASSPORT / ID", idField));
                column.add(g1);
                column.add(Box.createVerticalStrut(10));
                inputs.add(new JTextField[]{nameField, idField});
            }
            column.revalidate();
            column.repaint();
        }
    }

    // ================================================================== 7. SEAT MAP SCREEN
    static class SeatMapClassicScreen extends ClassicScreen {
        Map<String, JToggleButton> seats = new LinkedHashMap<>();

        SeatMapClassicScreen() {
            FlatPanel topBar = new FlatPanel(new BorderLayout(10, 0));
            topBar.setBorder(new EmptyBorder(16, 22, 6, 22));
            RoundActionButton backBtn = new RoundActionButton(0, 36);
            backBtn.addActionListener(e -> navigateTo("booking"));
            topBar.add(backBtn, BorderLayout.WEST);
            topBar.add(label("Cabin Seating Chart", classicDisplay(Font.BOLD, 18), BASE_NAVY), BorderLayout.CENTER);
            add(topBar, BorderLayout.NORTH);

            FlatPanel center = new FlatPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(6, 22, 10, 22));

            FlatPanel cabin = new FlatPanel() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = initAA(g0);
                    drawClassicFrame(g, 10, 0, getWidth() - 20, getHeight(), 24, CARD_WHITE, BASE_NAVY);
                    textC(g, "• FORWARD CABIN •", serif(Font.BOLD, 10), PRIMARY_BLUE, getWidth() / 2f, 32);
                    g.dispose();
                }
            };
            cabin.setPreferredSize(new Dimension(350, 380));
            cabin.setLayout(new GridLayout(5, 5, 8, 8));
            cabin.setBorder(new EmptyBorder(46, 28, 20, 28));

            String[] rows = {"1", "2", "3", "4", "5"};
            String[] cols = {"A", "B", "AISLE", "C", "D"};

            for (String r : rows) {
                for (String c : cols) {
                    if (c.equals("AISLE")) {
                        JLabel a = label(r, serif(Font.BOLD, 12), BASE_NAVY);
                        a.setHorizontalAlignment(SwingConstants.CENTER);
                        cabin.add(a);
                    } else {
                        String code = r + c;
                        JToggleButton b = new JToggleButton(code);
                        b.setFont(serif(Font.BOLD, 11));
                        b.setFocusPainted(false);
                        b.setBackground(CARD_WHITE);
                        b.setForeground(BASE_NAVY);
                        seats.put(code, b);
                        cabin.add(b);
                    }
                }
            }
            center.add(cabin);
            add(center, BorderLayout.CENTER);

            FlatPanel south = new FlatPanel();
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.setBorder(new EmptyBorder(4, 22, 20, 22));

            ClassicButton payBtn = new ClassicButton("Confirm Berths & Proceed to Ledger →", true);
            payBtn.setAlignmentX(0.5f);
            payBtn.addActionListener(e -> {
                List<String> chosen = new ArrayList<>();
                for (Map.Entry<String, JToggleButton> ent : seats.entrySet()) {
                    if (ent.getValue().isSelected()) chosen.add(ent.getKey());
                }
                if (chosen.size() != STATE.passengerCount) {
                    JOptionPane.showMessageDialog(mainFrame, "Please pick " + STATE.passengerCount + " berths.", "Seat Reservation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                STATE.selectedSeats = chosen;
                navigateTo("payment");
            });
            south.add(payBtn);
            add(south, BorderLayout.SOUTH);
        }

        @Override void onActivated() {
            super.onActivated();
            for (JToggleButton b : seats.values()) b.setSelected(false);
            String[] auto = {"1A", "1B", "1C", "1D"};
            for (int i = 0; i < Math.min(STATE.passengerCount, auto.length); i++) {
                if (seats.containsKey(auto[i])) seats.get(auto[i]).setSelected(true);
            }
        }
    }

    // ================================================================== 8. PAYMENT SCREEN
    static class PaymentClassicScreen extends ClassicScreen {
        JLabel totalLbl = label("", classicDisplay(Font.BOLD, 18), POSTAL_RED);

        PaymentClassicScreen() {
            FlatPanel topBar = new FlatPanel(new BorderLayout(10, 0));
            topBar.setBorder(new EmptyBorder(16, 22, 6, 22));
            RoundActionButton backBtn = new RoundActionButton(0, 36);
            backBtn.addActionListener(e -> navigateTo("seatmap"));
            topBar.add(backBtn, BorderLayout.WEST);
            topBar.add(label("Imperial Treasury & Payment", classicDisplay(Font.BOLD, 18), BASE_NAVY), BorderLayout.CENTER);

            FlatPanel center = new FlatPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(4, 22, 10, 22));
            center.add(topBar);

            FlatPanel ledgerCard = new FlatPanel(new GridLayout(4, 2, 0, 6)) {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = initAA(g0);
                    drawClassicFrame(g, 0, 0, getWidth(), getHeight(), 16, CARD_WHITE, BASE_NAVY);
                    g.dispose();
                }
            };
            ledgerCard.setBorder(new EmptyBorder(14, 18, 14, 18));
            ledgerCard.setPreferredSize(new Dimension(350, 130));

            ledgerCard.add(label("Base Airfare", serif(Font.PLAIN, 12), BASE_NAVY));
            ledgerCard.add(label("Included", serif(Font.PLAIN, 12), MUTED_TEXT));
            ledgerCard.add(label("Airport Dues (12%)", serif(Font.PLAIN, 12), BASE_NAVY));
            ledgerCard.add(label("Standard", serif(Font.PLAIN, 12), MUTED_TEXT));
            ledgerCard.add(label("Berth Reservation", serif(Font.PLAIN, 12), BASE_NAVY));
            ledgerCard.add(label("Guaranteed", serif(Font.PLAIN, 12), MUTED_TEXT));
            ledgerCard.add(label("TOTAL PAYABLE", serif(Font.BOLD, 14), BASE_NAVY));
            ledgerCard.add(totalLbl);
            center.add(ledgerCard);
            center.add(Box.createVerticalStrut(14));

            ClassicButton pay = new ClassicButton("Authorize Payment & Issue Pass →", true);
            pay.setAlignmentX(0.5f);
            pay.addActionListener(e -> {
                Flight f = STATE.selectedFlight;
                int total = f.baseFare * STATE.passengerCount;
                Ticket tk = new Ticket(STATE.fromStation, STATE.toStation, f.depTime, f.arrTime,
                        STATE.departureDate.format(DATE_FMT), f.airline, f.code, f.aircraftType,
                        STATE.cabinClass, STATE.registeredPassengers, STATE.selectedSeats, total);
                STATE.activeBoardingPass = tk;
                STATE.upcomingTrips.add(0, tk);
                navigateTo("confirmed");
            });
            center.add(pay);
            add(center, BorderLayout.CENTER);
        }

        @Override void onActivated() {
            super.onActivated();
            if (STATE.selectedFlight != null) {
                int total = STATE.selectedFlight.baseFare * STATE.passengerCount;
                totalLbl.setText("₹" + String.format("%,d", total));
            }
        }
    }

    // ================================================================== 9. BOARDING PASS SCREEN
    static class ConfirmedClassicScreen extends ClassicScreen {
        ScrollColumn passColumn = new ScrollColumn();

        ConfirmedClassicScreen() {
            FlatPanel top = new FlatPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.setBorder(new EmptyBorder(18, 22, 6, 22));

            JLabel title = label("VOYAGE CONFIRMED", classicDisplay(Font.BOLD, 20), BASE_NAVY);
            title.setAlignmentX(0.5f);
            top.add(title);
            add(top, BorderLayout.NORTH);

            passColumn.setBorder(new EmptyBorder(4, 22, 10, 22));
            add(classicScroll(passColumn), BorderLayout.CENTER);

            FlatPanel south = new FlatPanel();
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.setBorder(new EmptyBorder(4, 22, 22, 22));

            ClassicButton viewTickets = new ClassicButton("Open Ticket Vault & Archives →", true);
            viewTickets.setAlignmentX(0.5f);
            viewTickets.addActionListener(e -> navigateTo("tickets"));
            south.add(viewTickets);
            add(south, BorderLayout.SOUTH);
        }

        @Override void onActivated() {
            super.onActivated();
            passColumn.removeAll();
            Ticket t = STATE.activeBoardingPass;
            if (t == null) return;

            for (int i = 0; i < t.passengerNames.size(); i++) {
                final int idx = i;
                final String pName = t.passengerNames.get(idx);
                final String pSeat = t.seatAssignments.size() > idx ? t.seatAssignments.get(idx) : "1A";

                FlatPanel pass = new FlatPanel() {
                    @Override protected void paintComponent(Graphics g0) {
                        Graphics2D g = initAA(g0);
                        int w = getWidth(), h = getHeight();

                        drawClassicFrame(g, 0, 0, w, h, 16, CARD_WHITE, BASE_NAVY);
                        drawGuillocheWave(g, 14, 24, w - 28, 6, HIGHLIGHT_GOLD);

                        textL(g, t.fromCode, classicDisplay(Font.BOLD, 26), BASE_NAVY, 18, 52);
                        textR(g, t.toCode, classicDisplay(Font.BOLD, 26), BASE_NAVY, w - 18, 52);

                        // Perforation notches
                        g.setColor(BG_ICE);
                        g.fill(new Ellipse2D.Double(-8, 85, 16, 16));
                        g.fill(new Ellipse2D.Double(w - 8, 85, 16, 16));

                        g.setColor(PRIMARY_BLUE);
                        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 4}, 0));
                        g.drawLine(14, 93, w - 14, 93);

                        textL(g, "PASSENGER: " + pName, serif(Font.BOLD, 12), BASE_NAVY, 18, 120);
                        textR(g, "SEAT: " + pSeat, serif(Font.BOLD, 12), POSTAL_RED, w - 18, 120);
                        textL(g, "GATE: " + t.boardingGate + "  •  DEP: " + t.depTime, serif(Font.PLAIN, 11), MUTED_TEXT, 18, 140);
                        textR(g, "PNR: " + t.pnrNumber, serif(Font.BOLD, 11), BASE_NAVY, w - 18, 140);

                        g.dispose();
                    }
                };
                pass.setPreferredSize(new Dimension(350, 160));
                passColumn.add(pass);
                passColumn.add(Box.createVerticalStrut(10));
            }
            passColumn.revalidate();
            passColumn.repaint();
        }
    }

    // ================================================================== 10. TICKETS VAULT
    static class TicketsVaultClassicScreen extends ClassicScreen {
        ScrollColumn vaultList = new ScrollColumn();

        TicketsVaultClassicScreen() {
            FlatPanel top = new FlatPanel(new BorderLayout());
            top.setBorder(new EmptyBorder(18, 22, 8, 22));
            top.add(label("The Imperial Ticket Vault", classicDisplay(Font.BOLD, 18), BASE_NAVY), BorderLayout.WEST);
            add(top, BorderLayout.NORTH);

            vaultList.setBorder(new EmptyBorder(4, 22, 14, 22));
            add(classicScroll(vaultList), BorderLayout.CENTER);
            add(new ClassicNavigationDeck(2), BorderLayout.SOUTH);
        }

        @Override void onActivated() {
            super.onActivated();
            vaultList.removeAll();
            for (Ticket t : STATE.upcomingTrips) {
                FlatPanel card = new FlatPanel() {
                    @Override protected void paintComponent(Graphics g0) {
                        Graphics2D g = initAA(g0);
                        drawClassicFrame(g, 0, 0, getWidth(), getHeight(), 12, CARD_WHITE, BASE_NAVY);
                        textL(g, t.fromCode + " → " + t.toCode, classicDisplay(Font.BOLD, 16), BASE_NAVY, 14, 26);
                        textR(g, t.flightDate, serif(Font.PLAIN, 11), MUTED_TEXT, getWidth() - 14, 26);
                        textL(g, t.airline + " (" + t.flightNo + ")", serif(Font.ITALIC, 11), MUTED_TEXT, 14, 46);
                        textR(g, "PNR: " + t.pnrNumber, serif(Font.BOLD, 11), POSTAL_RED, getWidth() - 14, 46);
                        g.dispose();
                    }
                };
                card.setPreferredSize(new Dimension(350, 58));
                vaultList.add(card);
                vaultList.add(Box.createVerticalStrut(8));
            }
            vaultList.revalidate();
            vaultList.repaint();
        }
    }
}