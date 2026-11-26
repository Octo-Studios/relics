package it.hurts.sskirillss.relics.api.relics.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import it.hurts.sskirillss.relics.api.relics.ResearchComponent;
import it.hurts.sskirillss.relics.handlers.PlayerResearchHandler;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        if (!(entity instanceof ServerPlayer player))
            return;

        var playerResearch = player.getData(RelicsAttachments.PLAYER_RESEARCH);
        var key = getResearchKey();

        if (component.equals(playerResearch.getResearch().getOrDefault(key, ResearchComponent.EMPTY)))
            return;

        var map = new HashMap<>(playerResearch.getResearch());

        if (component.equals(ResearchComponent.EMPTY))
            map.remove(key);
        else
            map.put(key, component);

        PlayerResearchHandler.set(player, playerResearch.toBuilder().research(map).build());
    }

    public Multimap<Integer, Integer> getLinks() {
        return getComponent().getLinks().entrySet().stream()
                .collect(MultimapBuilder.hashKeys().arrayListValues()::build, (multimap, entry) -> multimap.putAll(Integer.parseInt(entry.getKey()), entry.getValue()), Multimap::putAll);
    }

    public void setLinks(Map<String, List<Integer>> links) {
        setComponent(getComponent().toBuilder()
                .links(links)
                .build());
    }

    public void setLinks(Multimap<Integer, Integer> links) {
        setLinks(toLinkMap(links));
    }

    private static Map<String, List<Integer>> toLinkMap(Multimap<Integer, Integer> links) {
        return links.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        entry -> new ArrayList<>(entry.getValue())
                ));
    }

    public void addLink(int from, int to) {
        var links = getLinks();

        links.put(from, to);

        setLinks(links);
    }

    public void removeLink(int from, int to) {
        var links = getLinks();

        links.remove(from, to);

        setLinks(links);
    }

    public boolean hasLock() {
        var template = abilityData.getTemplate();

        return template.getRequiredLevel() > 0 || template.getRequiredRank() > 0;
    }

    public boolean isLockOpen() {
        return !hasLock() || getComponent().isLockOpen();
    }

    public void setLockOpen(boolean open) {
        var component = getComponent();

        if (component.isLockOpen() != open)
            setComponent(component.toBuilder().lockOpen(open).build());
    }

    public boolean isResearched() {
        return abilityData.getTemplate().getResearchTemplate().getStars().isEmpty() || getComponent().isResearched();
    }

    public void setResearched(boolean researched) {
        var component = getComponent();

        setComponent(component.toBuilder().lockOpen(researched || component.isLockOpen()).researched(researched).build());
    }

    public void complete() {
        var template = this.getAbilityData().getTemplate();

        if (template == null)
            return;

        setComponent(getComponent().toBuilder()
                .links(toLinkMap(template.getResearchTemplate().getLinks()))
                .researched(true)
                .lockOpen(true)
                .build());
    }

    public void resetResearch() {
        setComponent(getComponent().toBuilder()
                .links(Map.of())
                .researched(false)
                .build());
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
