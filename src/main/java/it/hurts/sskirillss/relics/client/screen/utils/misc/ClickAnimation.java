package it.hurts.sskirillss.relics.client.screen.utils.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.RandomSource;

public class ClickAnimation {
    private static final RandomSource RANDOM = RandomSource.create();

    private static final double POP_IN = 0.08D;
    private static final double POP_OUT = 0.12D;
    private static final double POP_DELAY = 0.04D;
    private static final float DECAY = 0.75F;

    @Getter
    @Setter
    private float scale = 1F;
    @Getter
    @Setter
    private float rotation = 0F;

    private Tween tween;

    public void play(float overshoot, float rotationBase, int swings, double segmentDuration) {
        if (tween != null)
            tween.kill();

        tween = Tween.create().setParallel(true);

        tween.tweenMethod(this::setScale, scale, overshoot, POP_IN)
                .setEaseType(EaseType.EASE_OUT)
                .setTransitionType(TransitionType.QUAD);
        tween.tweenMethod(this::setScale, overshoot, 1F, POP_OUT)
                .setDelay(POP_DELAY)
                .setEaseType(EaseType.EASE_IN)
                .setTransitionType(TransitionType.QUAD);

        var amplitude = RANDOM.nextBoolean() ? rotationBase : -rotationBase;
        var delay = 0D;
        var lastTarget = rotation;

        for (var i = 0; i < swings; i++) {
            var nextTarget = i % 2 == 0 ? amplitude : -amplitude;

            tween.tweenMethod(this::setRotation, lastTarget, nextTarget, segmentDuration)
                    .setDelay(delay)
                    .setEaseType(i == 0 ? EaseType.EASE_OUT : EaseType.EASE_IN_OUT)
                    .setTransitionType(TransitionType.QUAD);

            lastTarget = nextTarget;
            delay += segmentDuration;
            amplitude *= DECAY;
        }

        tween.tweenMethod(this::setRotation, lastTarget, 0F, segmentDuration)
                .setDelay(delay)
                .setEaseType(EaseType.EASE_IN);

        tween.start();
    }

    public void apply(PoseStack poseStack, float centerX, float centerY) {
        poseStack.translate(centerX, centerY, 0F);
        poseStack.scale(scale, scale, 1F);
        poseStack.mulPose(Axis.ZP.rotation(rotation));
        poseStack.translate(-centerX, -centerY, 0F);
    }
}
