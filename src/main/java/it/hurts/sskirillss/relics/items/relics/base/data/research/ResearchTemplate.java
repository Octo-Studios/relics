package it.hurts.sskirillss.relics.items.relics.base.data.research;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ResearchTemplate {
    @Builder.Default
    private Map<Integer, StarData> stars;

    @Builder.Default
    private Multimap<Integer, Integer> links;

    public List<StarData> getConnectedStars(StarData star) {
        List<StarData> connectedStars = new ArrayList<>();

        int starIndex = star.getIndex();

        if (links.containsKey(starIndex)) {
            for (Integer connectedIndex : links.get(starIndex)) {
                connectedStars.add(stars.get(connectedIndex));
            }
        }

        for (Map.Entry<Integer, Integer> entry : links.entries()) {
            if (entry.getValue().equals(starIndex)) {
                connectedStars.add(stars.get(entry.getKey()));
            }
        }

        return connectedStars;
    }

    public static class ResearchTemplateBuilder {
        private Map<Integer, StarData> stars = new HashMap<>();
        private Multimap<Integer, Integer> links = LinkedHashMultimap.create();

        public ResearchTemplateBuilder star(int index, int x, int y) {
            stars.put(index, new StarData(index, x, y));

            return this;
        }

        public ResearchTemplateBuilder link(int first, int second) {
            links.put(first > second ? first + second - (second = first) : first, second);

            return this;
        }
    }
}