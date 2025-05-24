package it.hurts.sskirillss.relics.client.models.items.base;

import com.google.common.collect.ImmutableList;
import it.hurts.sskirillss.relics.client.models.items.utils.ModelSide;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;

public class SidedCurioModel extends CurioModel {
    @Getter
    @Setter
    private int slot;

    public SidedCurioModel(Item item) {
        super(item);
    }

    public ModelSide getSide() {
        return slot % 2 == 0 ? ModelSide.RIGHT : ModelSide.LEFT;
    }

    @Nonnull
    @Override
    protected Iterable<ModelPart> headParts() {
        return getItem() instanceof IRenderableCurio renderable ? renderable.headParts().stream().filter(entry -> !entry.startsWith(getSide().getOpposite().getId())).map(this::getById).toList() : ImmutableList.of();
    }

    @Nonnull
    @Override
    protected Iterable<ModelPart> bodyParts() {
        return getItem() instanceof IRenderableCurio renderable ? renderable.bodyParts().stream().filter(entry -> !entry.startsWith(getSide().getOpposite().getId())).map(this::getById).toList() : ImmutableList.of();
    }
}