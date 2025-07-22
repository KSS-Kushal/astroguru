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

//        public static final Map<String, String> PLANET_NAMES_BENGALI = Map.ofEntries(
//                Map.entry("Sun", "সূর্য"),
//                Map.entry("Moon", "চন্দ্র"),
//                Map.entry("Mars", "মঙ্গল"),
//                Map.entry("Mercury", "বুধ"),
//                Map.entry("Jupiter", "বৃহস্পতি"),
//                Map.entry("Venus", "শুক্র"),
//                Map.entry("Saturn", "শনি"),
//                Map.entry("Rahu", "রাহু"),
//                Map.entry("Ketu", "কেতু"),
//                Map.entry("Ascendant", "লগ্ন"),
//                Map.entry("Uranus", "ইউরেনাস"),
//                Map.entry("Neptune", "নেপচুন"),
//                Map.entry("Pluto", "প্লুটো")
//        );
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

    // Use List<Integer> for positions (x, y)
    private static final Map<Integer, List<Integer>> HOUSE_POSITIONS = Map.ofEntries(
            Map.entry(1, List.of(180, 130)),
            Map.entry(2, List.of(140, 90)),
            Map.entry(3, List.of(90, 50)),
            Map.entry(4, List.of(80, 130)),
            Map.entry(5, List.of(80, 210)),
            Map.entry(6, List.of(130, 250)),
            Map.entry(7, List.of(180, 250)),
            Map.entry(8, List.of(230, 250)),
            Map.entry(9, List.of(280, 210)),
            Map.entry(10, List.of(280, 130)),
            Map.entry(11, List.of(280, 50)),
            Map.entry(12, List.of(230, 90))
    );

    public static String generateSvg(Map<String, HouseData> chartData, boolean includeHouseNumbers) {
        System.out.println("In Generate SVG");
        StringBuilder svg = new StringBuilder();
        svg.append("""
            <svg width="360" height="360" xmlns="http://www.w3.org/2000/svg">
              <style>
                .box { font-family: 'Noto Sans Bengali', sans-serif; font-size: 12px; }
                .number { font-size: 10px; fill: gray; }
                .border { stroke: black; stroke-width: 1; fill: none; }
              </style>
            """);

        svg.append(drawGrid());
        svg.append(drawDiagonals());

        for (int house = 1; house <= 12; house++) {
            List<Integer> pos = HOUSE_POSITIONS.get(house);
            if (pos == null) continue;

            int x = pos.get(0);
            int y = pos.get(1);

            HouseData houseData = chartData.get(String.valueOf(house));
            if (houseData == null) continue;

            String rashi = BengaliMappings.RASHI_NAMES_BENGALI.getOrDefault(houseData.sign_no, "");
            List<String> planets = houseData.planet.stream()
                    .map(p -> BengaliMappings.PLANET_NAMES_BENGALI.getOrDefault(p.name, p.name))
                    .toList();

            System.out.println(planets);

            if (includeHouseNumbers) {
                svg.append(String.format("<text x='%d' y='%d' class='number'>%d</text>\n", x, y - 12, houseData.sign_no));
            }

//            svg.append(String.format("<text x='%d' y='%d' class='box'>%s</text>\n", x, y, rashi));
            for (int i = 0; i < planets.size(); i++) {
                svg.append(String.format(
                        "<text x='%d' y='%d' class='box'>%s</text>\n",
                        x, y + (i + 1) * 14, planets.get(i)
                ));
            }
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private static String drawGrid() {
        return """
            <rect x="80" y="50" width="200" height="200" class="border"/>
            <line x1="80" y1="130" x2="280" y2="130" class="border"/>
            <line x1="80" y1="210" x2="280" y2="210" class="border"/>
            <line x1="140" y1="50" x2="140" y2="250" class="border"/>
            <line x1="220" y1="50" x2="220" y2="250" class="border"/>
        """;
    }

    private static String drawDiagonals() {
        return """
            <line x1="80" y1="50" x2="140" y2="130" class="border"/>
            <line x1="280" y1="50" x2="220" y2="130" class="border"/>
            <line x1="80" y1="250" x2="140" y2="210" class="border"/>
            <line x1="280" y1="250" x2="220" y2="210" class="border"/>
        """;
    }
}
