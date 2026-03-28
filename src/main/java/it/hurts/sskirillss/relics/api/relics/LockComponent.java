package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class LockComponent {
    private final int unlocks;

    public static final LockComponent EMPTY = new LockComponent(0);

    public static final Codec<LockComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("breaks").forGetter(LockComponent::getUnlocks)
            ).apply(instance, LockComponent::new)
    );
}
