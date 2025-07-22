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
            Map.entry(1, new HousePosition(220, 90, "middle")),      // House 1: Top middle
            Map.entry(2, new HousePosition(130, 80, "middle")),      // House 2: Top left diagonal
            Map.entry(3, new HousePosition(60, 100, "middle")),      // House 3: Top left corner (inside triangle)
            Map.entry(12, new HousePosition(310, 80, "middle")),     // House 12: Top right diagonal
            Map.entry(11, new HousePosition(380, 100, "middle")),     // House 11: Top right corner (inside triangle)

            // Middle row
            Map.entry(4, new HousePosition(90, 220, "middle")),     // House 4: Left middle
            Map.entry(10, new HousePosition(330, 220, "middle")),    // House 10: Right middle

            // Bottom row
            Map.entry(7, new HousePosition(220, 330, "middle")),     // House 7: Bottom middle
            Map.entry(6, new HousePosition(145, 340, "middle")),     // House 6: Bottom left diagonal
            Map.entry(5, new HousePosition(65, 320, "middle")),     // House 5: Bottom left corner (inside triangle)
            Map.entry(8, new HousePosition(290, 340, "middle")),     // House 8: Bottom right diagonal
            Map.entry(9, new HousePosition(380, 320, "middle"))      // House 9: Bottom right corner (inside triangle)
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
        System.out.println("Generating Enhanced East Indian Chart SVG");
        StringBuilder svg = new StringBuilder();

        svg.append("""
            <svg width="440" height="440" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <style>
                  .bengali-text { 
                    font-family: 'Noto Sans Bengali', Arial, sans-serif; 
                    font-size: 12px; 
                    font-weight: 500;
                    fill: #333333;
                  }
                  .house-number { 
                    font-family: Arial, sans-serif;
                    font-size: 12px; 
                    fill: #333333; 
                    font-weight: bold;
                  }
                  .chart-border { 
                    stroke: #FF0000; 
                    stroke-width: 2; 
                    fill: none; 
                  }
                  .diagonal-line {
                    stroke: #FF0000;
                    stroke-width: 2;
                  }
                </style>
              </defs>
              <rect x='40' y='40' width='360' height='360' fill='#F8FF85'/>
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
                        "<text x='%d' y='%d' class='house-number' text-anchor='middle'>%d</text>\n",
                        pos.x, pos.y - 25, house
                ));
            }

            // Add planets with better vertical spacing
            for (int i = 0; i < planets.size(); i++) {
                int planetY = pos.y + (i * 14) - 10; // Adjust starting position and spacing
                svg.append(String.format(
                        "<text x='%d' y='%d' class='bengali-text' text-anchor='middle'>%s</text>\n",
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
            <rect x="40" y="40" width="360" height="360" class="chart-border"/>
            
            <!-- Create 3x3 grid with equal 120px sections -->
            <!-- Horizontal lines -->
            <line x1="40" y1="160" x2="400" y2="160" class="chart-border"/>
            <line x1="40" y1="280" x2="400" y2="280" class="chart-border"/>
            
            <!-- Vertical lines -->
            <line x1="160" y1="40" x2="160" y2="400" class="chart-border"/>
            <line x1="280" y1="40" x2="280" y2="400" class="chart-border"/>
            
            <!-- Diagonal lines for corner houses -->
            <line x1="40" y1="40" x2="160" y2="160" class="diagonal-line"/>    <!-- Top-left -->
            <line x1="400" y1="40" x2="280" y2="160" class="diagonal-line"/>   <!-- Top-right -->
            <line x1="40" y1="400" x2="160" y2="280" class="diagonal-line"/>   <!-- Bottom-left -->
            <line x1="400" y1="400" x2="280" y2="280" class="diagonal-line"/>  <!-- Bottom-right -->
            
            """;
    }

}
