package thunder.hack.features.modules.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.ChunkPos;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ChunkAnimations extends Module {
   public final Setting<Integer> animationDuration = new Setting<>("Duration", 1000, 100, 3000);
   public final Setting<ChunkAnimations.Mode> mode = new Setting<>("Mode", ChunkAnimations.Mode.UP);
   public static final Map<ChunkPos, Long> chunkRenderTimes = new HashMap<>();

   public ChunkAnimations() {
      super("ChunkAnimations", Module.Category.RENDER);
   }

   @Override
   public void onEnable() {
      chunkRenderTimes.clear();
   }

   @Override
   public void onUpdate() {
      if (mc.player != null && mc.player.age % 200 == 0) {
         long now = System.currentTimeMillis();
         chunkRenderTimes.values().removeIf(time -> now - time > (long)this.animationDuration.getValue().intValue() * 2L);
      }
   }

   public static void putChunk(ChunkPos pos) {
      if (!chunkRenderTimes.containsKey(pos)) {
         chunkRenderTimes.put(pos, System.currentTimeMillis());
      }
   }

   public double getAnimationProgress(ChunkPos pos) {
      if (!chunkRenderTimes.containsKey(pos)) {
         return 1.0;
      } else {
         long time = chunkRenderTimes.get(pos);
         long elapsed = System.currentTimeMillis() - time;
         if (elapsed >= (long)this.animationDuration.getValue().intValue()) {
            return 1.0;
         } else {
            double progress = (double)elapsed / (double)this.animationDuration.getValue().intValue();
            return 1.0 - Math.pow(1.0 - progress, 3.0);
         }
      }
   }

   public static enum Mode {
      UP,
      DOWN,
      SLIDE;
   }
}
