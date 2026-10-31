package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.SearchArgumentType;
import thunder.hack.features.modules.client.ClientSettings;

public class BlockESPCommand extends Command {
   public BlockESPCommand() {
      super("blockesp");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(literal("reset").executes(context -> {
         ModuleManager.blockESP.selectedBlocks.getValue().clear();
         sendMessage(ClientSettings.isRu() ? "BlockESP \u0431\u044b\u043b \u043e\u0447\u0438\u0449\u0435\u043d!" : "BlockESP got reset.");
         mc.worldRenderer.reload();
         return 1;
      }));
      builder.then(
         literal("add")
            .then(
               arg("block", SearchArgumentType.create())
                  .executes(
                     context -> {
                        String blockName = (String)context.getArgument("block", String.class);
                        Block result = getRegisteredBlock(blockName);
                        if (result != null) {
                           ModuleManager.blockESP.selectedBlocks.getValue().add(result);
                           sendMessage(
                              Formatting.GREEN
                                 + blockName
                                 + (ClientSettings.isRu() ? " \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 BlockESP" : " added to BlockESP")
                           );
                        } else {
                           sendMessage(
                              Formatting.RED
                                 + (
                                    ClientSettings.isRu()
                                       ? "\u0422\u0430\u043a\u043e\u0433\u043e \u0431\u043b\u043e\u043a\u0430 \u043d\u0435\u0442!"
                                       : "There is no such block!"
                                 )
                           );
                        }

                        mc.worldRenderer.reload();
                        return 1;
                     }
                  )
            )
      );
      builder.then(
         literal("del")
            .then(
               arg("block", SearchArgumentType.create())
                  .executes(
                     context -> {
                        String blockName = (String)context.getArgument("block", String.class);
                        Block result = getRegisteredBlock(blockName);
                        if (result != null) {
                           ModuleManager.blockESP.selectedBlocks.getValue().remove(result);
                           sendMessage(
                              Formatting.GREEN
                                 + blockName
                                 + (ClientSettings.isRu() ? " \u0443\u0434\u0430\u043b\u0435\u043d \u0438\u0437 BlockESP" : " removed from BlockESP")
                           );
                        } else {
                           sendMessage(
                              Formatting.RED
                                 + (
                                    ClientSettings.isRu()
                                       ? "\u0422\u0430\u043a\u043e\u0433\u043e \u0431\u043b\u043e\u043a\u0430 \u043d\u0435\u0442!"
                                       : "There is no such block!"
                                 )
                           );
                        }

                        mc.worldRenderer.reload();
                        return 1;
                     }
                  )
            )
      );
      builder.executes(context -> {
         if (ModuleManager.blockESP.selectedBlocks.getValue().getItemsById().isEmpty()) {
            sendMessage("BlockESP list empty");
         } else {
            StringBuilder f = new StringBuilder("BlockESP list: ");

            for (String name : ModuleManager.blockESP.selectedBlocks.getValue().getItemsById()) {
               try {
                  f.append(name).append(", ");
               } catch (Exception var5) {
               }
            }

            sendMessage(f.toString());
         }

         return 1;
      });
   }

   public static Block getRegisteredBlock(String blockName) {
      for (Block block : Registries.BLOCK) {
         if (block.getTranslationKey().replace("block.minecraft.", "").equalsIgnoreCase(blockName.replace("block.minecraft.", ""))) {
            return block;
         }
      }

      return null;
   }
}
