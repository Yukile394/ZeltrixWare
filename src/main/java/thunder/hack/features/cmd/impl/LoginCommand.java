package thunder.hack.features.cmd.impl;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Optional;
import net.minecraft.command.CommandSource;
import net.minecraft.client.session.Session;
import net.minecraft.util.Uuids;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session.AccountType;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.injection.accesors.IMinecraftClient;

public class LoginCommand extends Command {
   public LoginCommand() {
      super("login");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         arg("name", StringArgumentType.word())
            .executes(
               context -> {
                  this.login((String)context.getArgument("name", String.class));
                  sendMessage(
                     (
                           ClientSettings.isRu()
                              ? "\u0410\u043a\u043a\u0430\u0443\u043d\u0442 \u0438\u0437\u043c\u0435\u043d\u0435\u043d \u043d\u0430: "
                              : "Switched account to: "
                        )
                        + mc.getSession().getUsername()
                  );
                  return 1;
               }
            )
      );
      builder.executes(
         context -> {
            sendMessage(
               ClientSettings.isRu()
                  ? "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: .login <nickname>"
                  : "Usage: .login <nickname>"
            );
            return 1;
         }
      );
   }

   public void login(String name) {
      try {
         this.setSession(new Session(name, Uuids.getOfflinePlayerUuid(name), "", Optional.empty(), Optional.empty(), AccountType.MOJANG));
      } catch (Exception var3) {
         sendMessage((ClientSettings.isRu() ? "\u041d\u0435\u0432\u0435\u0440\u043d\u043e\u0435 \u0438\u043c\u044f! " : "Incorrect username! ") + var3);
      }
   }

   public void setSession(Session session) {
      IMinecraftClient mca = (IMinecraftClient)mc;
      mca.setSessionT(session);
      mc.getGameProfile().getProperties().clear();
      UserApiService apiService = UserApiService.OFFLINE;
      mca.setUserApiService(apiService);
      mca.setSocialInteractionsManagerT(new SocialInteractionsManager(mc, apiService));
      mca.setProfileKeys(ProfileKeys.create(apiService, session, mc.runDirectory.toPath()));
      mca.setAbuseReportContextT(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
   }
}
