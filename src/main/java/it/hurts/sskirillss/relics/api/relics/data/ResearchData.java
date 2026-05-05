package it.hurts.sskirillss.relics.api.relics.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import it.hurts.sskirillss.relics.api.relics.ResearchComponent;
import it.hurts.sskirillss.relics.handlers.PlayerResearchHandler;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResearchData {
    private final AbilityData abilityData;

    public ResearchData(AbilityData abilityData) {
        this.abilityData = abilityData;
    }

    public AbilityData getAbilityData() {
        return this.abilityData;
    }

    public ResearchComponent getComponent() {
        var entity = this.getAbilityData().getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player))
            return ResearchComponent.EMPTY;

        return player.getData(RelicsAttachments.PLAYER_RESEARCH)
                .getResearch()
                .getOrDefault(this.getResearchKey(), ResearchComponent.EMPTY);
    }

    public void setComponent(ResearchComponent component) {
        var entity = this.getAbilityData().getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof net.minecraft.server.level.ServerPlayer player))
            return;

        var playerResearch = player.getData(RelicsAttachments.PLAYER_RESEARCH);
        var map = new java.util.HashMap<>(playerResearch.getResearch());
        map.put(this.getResearchKey(), component);

        PlayerResearchHandler.set(player, playerResearch.toBuilder().research(map).build());
    }

    public Multimap<Integer, Integer> getLinks() {
        return getComponent().getLinks().entrySet().stream()
                .collect(MultimapBuilder.hashKeys().arrayListValues()::build, (multimap, entry) -> multimap.putAll(Integer.parseInt(entry.getKey()), entry.getValue()), Multimap::putAll);
    }

    public void setLinks(Map<String, java.util.List<Integer>> links) {
        setComponent(getComponent().toBuilder()
                .links(links)
                .build());
    }

    public void setLinks(Multimap<Integer, Integer> links) {
        setLinks(links.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> new ArrayList<>(entry.getValue())
                )));
    }

    public void addLink(int from, int to) {
        var links = getLinks();

        links.put(from, to);

        setLinks(links);
    }

    public void removeLink(int from, int to) {
        var links = getLinks();

        links.remove(from, to);

        setComponent(getComponent().toBuilder()
                .links(links.asMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> new ArrayList<>(entry.getValue())
                        )))
                .build());
    }

    public boolean isResearched() {
        if (this.getAbilityData().getTemplate().getResearchTemplate().getStars().isEmpty())
            return true;

        var entity = this.getAbilityData().getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player))
            return false;

        return player.getData(RelicsAttachments.PLAYER_RESEARCH)
                .getResearch()
                .getOrDefault(this.getResearchKey(), ResearchComponent.EMPTY)
                .isResearched();
    }

    public void setResearched(boolean researched) {
        var entity = this.getAbilityData().getAbilitiesData().getRelicData().getEntity();

        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            var key = this.getResearchKey();
            var playerResearch = player.getData(RelicsAttachments.PLAYER_RESEARCH);
            var map = new java.util.HashMap<>(playerResearch.getResearch());
            var research = map.getOrDefault(key, ResearchComponent.EMPTY);

            map.put(key, research.toBuilder().researched(researched).build());

            PlayerResearchHandler.set(player, playerResearch.toBuilder().research(map).build());
        }
    }

    private String getResearchKey() {
        var stack = this.getAbilityData().getAbilitiesData().getRelicData().getStack();
        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        return itemId + "#" + this.getAbilityData().getId();
    }

    public Multimap<Integer, Integer> getCorrectLinks() {
        Multimap<Integer, Integer> schema = this.getAbilityData().getTemplate().getResearchTemplate().getLinks();
        Multimap<Integer, Integer> links = this.getLinks();

        if (schema.isEmpty())
            return LinkedHashMultimap.create();

        Set<Pair<Integer, Integer>> bidirectionalSchema = schema.entries().stream()
                .flatMap(entry -> Stream.of(Pair.of(entry.getKey(), entry.getValue()), Pair.of(entry.getValue(), entry.getKey())))
                .collect(Collectors.toSet());

        return links.entries().stream()
                .filter(entry -> bidirectionalSchema.contains(Pair.of(entry.getKey(), entry.getValue()))
                        || bidirectionalSchema.contains(Pair.of(entry.getValue(), entry.getKey())))
                .collect(LinkedHashMultimap::create, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Multimap::putAll);
    }

    public Multimap<Integer, Integer> getIncorrectLinks() {
        Multimap<Integer, Integer> schema = this.getAbilityData().getTemplate().getResearchTemplate().getLinks();
        Multimap<Integer, Integer> links = this.getLinks();

        if (schema.isEmpty())
            return LinkedHashMultimap.create();

        Set<Pair<Integer, Integer>> bidirectionalSchema = schema.entries().stream()
                .flatMap(entry -> Stream.of(Pair.of(entry.getKey(), entry.getValue()), Pair.of(entry.getValue(), entry.getKey())))
                .collect(Collectors.toSet());

        return links.entries().stream()
                .filter(entry -> !bidirectionalSchema.contains(Pair.of(entry.getKey(), entry.getValue()))
                        && !bidirectionalSchema.contains(Pair.of(entry.getValue(), entry.getKey())))
                .collect(LinkedHashMultimap::create, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Multimap::putAll);
    }

    public double getResearchPercentage() {
        Multimap<Integer, Integer> schema = this.getAbilityData().getTemplate().getResearchTemplate().getLinks();
        Multimap<Integer, Integer> links = this.getLinks();

        if (schema.isEmpty())
            return 0D;

        Set<Pair<Integer, Integer>> bidirectionalSchema = schema.entries().stream()
                .flatMap(entry -> Stream.of(Pair.of(entry.getKey(), entry.getValue()), Pair.of(entry.getValue(), entry.getKey())))
                .collect(Collectors.toSet());

        long matchingLinks = links.entries().stream()
                .filter(entry -> bidirectionalSchema.contains(Pair.of(entry.getKey(), entry.getValue()))
                        || bidirectionalSchema.contains(Pair.of(entry.getValue(), entry.getKey())))
                .count();

        return (double) matchingLinks / schema.size();
    }

    public boolean isComplete() {
        return getResearchPercentage() >= 1D;
    }

    public int getHintPlayerExperienceCost() {
        return 50;
    }
}
