package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.utility.world.ExplosionUtility;

public class BenchMarkCommand extends Command {
   public BenchMarkCommand() {
      super("benchmark");
   }

   @Override
   public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(
         context -> {
            new Thread(
                  () -> {
                     long time = System.currentTimeMillis();
                     BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());
                     int r = 6;

                     for (int x = playerPos.getX() - r; x <= playerPos.getX() + r; x++) {
                        for (int y = playerPos.getY() - r; y <= playerPos.getY() + r; y++) {
                           for (int z = playerPos.getZ() - r; z <= playerPos.getZ() + r; z++) {
                              float var7 = ExplosionUtility.getExplosionDamage(new Vec3d((double)x, (double)y, (double)z), mc.player, false);
                           }
                        }
                     }

                     time = System.currentTimeMillis() - time;
                     int score = (int)(216.0F / (float)time * 10000.0F);
                     sendMessage(
                        (ClientSettings.isRu() ? "\u0422\u0432\u043e\u0439 CPU \u043d\u0430\u0431\u0440\u0430\u043b: " : "Your CPU score: ")
                           + Formatting.GRAY
                           + score
                     );
                  }
               )
               .start();
            return 1;
         }
      );
   }
}
