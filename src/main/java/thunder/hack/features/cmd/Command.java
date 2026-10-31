package thunder.hack.features.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.BuiltinRegistries;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.client.CommandManager;

public abstract class Command {
   protected static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
   protected static final MinecraftClient mc = MinecraftClient.getInstance();
   protected final List<String> names;
   private final String description;

   public Command(String... names) {
      this.names = Arrays.asList(names);
      this.description = "descriptions.commands." + this.names.get(0);
   }

   public abstract void executeBuild(LiteralArgumentBuilder<CommandSource> var1);

   public static void sendMessage(String message) {
      if (mc.player != null) {
         mc.player.sendMessage(Text.of(CommandManager.getClientMessage() + " " + message));
      }
   }

   @NotNull
   protected static <T> RequiredArgumentBuilder<CommandSource, T> arg(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   @NotNull
   protected static LiteralArgumentBuilder<CommandSource> literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   public void register(CommandDispatcher<CommandSource> dispatcher) {
      for (String name : this.names) {
         LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(name);
         this.executeBuild(builder);
         dispatcher.register(builder);
      }
   }

   public String getName() {
      return this.names.get(0);
   }

   public String getAliases() {
      return String.join(", ", this.names.stream().filter(n -> !n.equals(this.names.get(0))).toList());
   }

   public String getDescription() {
      return I18n.translate(this.description, new Object[0]);
   }
}
