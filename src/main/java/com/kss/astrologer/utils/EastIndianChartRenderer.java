package com.kss.astrologer.utils;

import com.kss.astrologer.dto.HouseData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EastIndianChartRenderer {
    // Bengali Rashi & Planet Mappings
    public static class BengaliMappings {
        public static final Map<Integer, String> RASHI_NAMES_BENGALI = Map.ofEntries(
                Map.entry(1, "মেষ"),
                Map.entry(2, "বৃষ"),
                Map.entry(3, "মিথুন"),
                Map.entry(4, "কর্কট"),
                Map.entry(5, "সিংহ"),
                Map.entry(6, "কন্যা"),
                Map.entry(7, "তুলা"),
                Map.entry(8, "বৃশ্চিক"),
                Map.entry(9, "ধনু"),
                Map.entry(10, "মকর"),
                Map.entry(11, "কুম্ভ"),
                Map.entry(12, "মীন")
        );

        public static final Map<String, String> PLANET_NAMES_BENGALI = Map.ofEntries(
                Map.entry("Sun", "সূর্য"),
                Map.entry("Moon", "চন্দ্র"),
                Map.entry("Mars", "মঙ্গল"),
                Map.entry("Mercury", "বুধ"),
                Map.entry("Jupiter", "বৃহঃ"),   // Short for বৃহস্পতি
                Map.entry("Venus", "শুক্র"),
                Map.entry("Saturn", "শনি"),
                Map.entry("Rahu", "রাহু"),
                Map.entry("Ketu", "কেতু"),
                Map.entry("Ascendant", "ল"),     // Common shorthand for লগ্ন
                Map.entry("Uranus", "ইউ"),
                Map.entry("Neptune", "নেপ"),
                Map.entry("Pluto", "প্লু")
        );
    }

    // Enhanced house positions with better alignment for East Indian style
    // Fixed house positions for proper East Indian chart layout
    // Corrected house positions for proper East Indian chart layout (360x360)
    private static final Map<Integer, HousePosition> HOUSE_POSITIONS = Map.ofEntries(
            // Top row
            Map.entry(1, new HousePosition(180, 50, "middle")),      // House 1: Top middle
            Map.entry(2, new HousePosition(90, 40, "middle")),      // House 2: Top left diagonal
            Map.entry(3, new HousePosition(20, 60, "middle")),      // House 3: Top left corner (inside triangle)
            Map.entry(12, new HousePosition(270, 40, "middle")),     // House 12: Top right diagonal
            Map.entry(11, new HousePosition(340, 60, "middle")),     // House 11: Top right corner (inside triangle)

            // Middle row
            Map.entry(4, new HousePosition(50, 180, "middle")),     // House 4: Left middle
            Map.entry(10, new HousePosition(290, 180, "middle")),    // House 10: Right middle

            // Bottom row
            Map.entry(7, new HousePosition(180, 290, "middle")),     // House 7: Bottom middle
            Map.entry(6, new HousePosition(105, 300, "middle")),     // House 6: Bottom left diagonal
            Map.entry(5, new HousePosition(25, 280, "middle")),     // House 5: Bottom left corner (inside triangle)
            Map.entry(8, new HousePosition(250, 300, "middle")),     // House 8: Bottom right diagonal
            Map.entry(9, new HousePosition(340, 280, "middle"))      // House 9: Bottom right corner (inside triangle)
    );

    // Helper class for house positioning
    private static class HousePosition {
        int x, y;
        String alignment;

        HousePosition(int x, int y, String alignment) {
            this.x = x;
            this.y = y;
            this.alignment = alignment;
        }
    }

    public static String generateSvg(Map<String, HouseData> chartData, boolean includeHouseNumbers) {
        StringBuilder svg = new StringBuilder();

        svg.append("""
                <svg width="363" height="363" xmlns="http://www.w3.org/2000/svg">
                """);

        // Draw the main chart structure
        svg.append(drawChartStructure());

        // Add content for each house
        for (int house = 1; house <= 12; house++) {
            HousePosition pos = HOUSE_POSITIONS.get(house);
            if (pos == null) continue;

            HouseData houseData = chartData.get(String.valueOf(house));
            if (houseData == null) continue;

            // Get Bengali rashi name
            String rashi = BengaliMappings.RASHI_NAMES_BENGALI.getOrDefault(houseData.sign_no, "");

            // Convert planet names to Bengali
            List<String> planets = houseData.planet.stream()
                    .map(p -> BengaliMappings.PLANET_NAMES_BENGALI.getOrDefault(p.name, p.name))
                    .toList();

            // Add house number if requested
            if (includeHouseNumbers) {
                svg.append(String.format(
                        "<text x='%d' y='%d' font-family='Arial, sans-serif' font-size='12px' fill='#333333' font-weight='bold' text-anchor='middle'>%d</text>\n",
                        pos.x, pos.y - 25, houseData.sign_no
                ));
            }

            // Add planets with better vertical spacing
            for (int i = 0; i < planets.size(); i++) {
                int planetY = pos.y + (i * 14) - 10; // Adjust starting position and spacing
                svg.append(String.format(
                        "<text x='%d' y='%d' font-family=\"'Noto Sans Bengali', Arial, sans-serif\" font-size='12px' font-weight='500' fill='#333333' text-anchor='middle'>%s</text>\n",
                        pos.x, planetY, planets.get(i)
                ));
            }
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private static String getTextAnchor(String alignment) {
        return switch (alignment) {
            case "start" -> "start";
            case "end" -> "end";
            default -> "middle";
        };
    }

    private static int getNumberOffset(String alignment) {
        return switch (alignment) {
            case "start" -> 10;
            case "end" -> -10;
            default -> 0;
        };
    }

    private static String drawChartStructure() {
        return """
            <!-- Main 360x360 rectangle -->
            <rect x="1" y="1" width="360" height="360" stroke="#FF0000" stroke-width="2" fill="#F8FF85"/>
            
            <!-- Create 3x3 grid with equal 120px sections -->
            <!-- Horizontal lines -->
            <line x1="0" y1="120" x2="360" y2="120" stroke="#FF0000" stroke-width="2" fill="#F8FF85"/>
            <line x1="0" y1="240" x2="360" y2="240" stroke="#FF0000" stroke-width="2" fill="#F8FF85"/>
            
            <!-- Vertical lines -->
            <line x1="120" y1="0" x2="120" y2="360" stroke="#FF0000" stroke-width="2" fill="#F8FF85"/>
            <line x1="240" y1="0" x2="240" y2="360" stroke="#FF0000" stroke-width="2" fill="#F8FF85"/>
            
            <!-- Diagonal lines for corner houses -->
            <line x1="0" y1="0" x2="120" y2="120" stroke="#FF0000" stroke-width="2"/>    <!-- Top-left -->
            <line x1="360" y1="0" x2="240" y2="120" stroke="#FF0000" stroke-width="2"/>   <!-- Top-right -->
            <line x1="0" y1="360" x2="120" y2="240" stroke="#FF0000" stroke-width="2"/>   <!-- Bottom-left -->
            <line x1="360" y1="360" x2="240" y2="240" stroke="#FF0000" stroke-width="2"/>  <!-- Bottom-right -->
            
            """;
    }

}
