package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;

public class ModuleArgumentType implements ArgumentType<Module> {
   private static final Collection<String> EXAMPLES = Managers.MODULE.modules.stream().map(Module::getName).limit(5L).toList();

   public static ModuleArgumentType create() {
      return new ModuleArgumentType();
   }

   public Module parse(StringReader reader) throws CommandSyntaxException {
      Module module = Managers.MODULE.get(reader.readString());
      if (module == null) {
         throw new DynamicCommandExceptionType(
               name -> Text.literal(
                     ClientSettings.isRu()
                        ? "\u041c\u043e\u0434\u0443\u043b\u044f "
                           + name.toString()
                           + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442("
                        : "Module " + name.toString() + " does not exist :("
                  )
            )
            .create(reader.readString());
      } else {
         return module;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Managers.MODULE.modules.stream().map(Module::getName), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
