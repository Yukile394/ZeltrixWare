package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.item.Item;
import net.minecraft.command.CommandSource;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

public class ChestStealerCommand extends Command {
   public ChestStealerCommand() {
      super("cheststealer", "stealer", "rat");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
   }

   public static String getRegistered(String Name) {
      for (Block block : Registries.BLOCK) {
         if (block.getTranslationKey().replace("block.minecraft.", "").equalsIgnoreCase(Name)) {
            return block.getTranslationKey().replace("block.minecraft.", "");
         }
      }

      for (Item item : Registries.ITEM) {
         if (item.getTranslationKey().replace("item.minecraft.", "").equalsIgnoreCase(Name)) {
            return item.getTranslationKey().replace("item.minecraft.", "");
         }
      }

      return null;
   }
}
