package thunder.hack.features.modules.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.apache.commons.lang3.StringEscapeUtils;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ChatTranslator extends Module {
   private final Setting<ChatTranslator.Lang> urLang = new Setting<>("YourLanguage", ChatTranslator.Lang.ru);
   private final Setting<ChatTranslator.Lang> outMessages = new Setting<>("OutMessages", ChatTranslator.Lang.uk);
   private List<String> exceptions = Arrays.asList("was killed by", "was destroyed by", "has joined a game", "has left the game");
   private String skip;

   public ChatTranslator() {
      super("ChatTranslator", Module.Category.CLIENT);
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive e) {
      if (!fullNullCheck()) {
         if (e.getPacket() instanceof GameMessageS2CPacket mPacket) {
            String message = mPacket.content().getString();

            for (String s : this.exceptions) {
               if (message.contains(s)) {
                  return;
               }
            }

            Managers.ASYNC.run(() -> {
               try {
                  this.sendMessage(Formatting.WHITE + this.translate(message, this.urLang.getValue().name()));
               } catch (Exception var3) {
                  var3.printStackTrace();
               }
            });
         }
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send e) {
      if (!fullNullCheck()) {
         if (e.getPacket() instanceof ChatMessageC2SPacket pac) {
            if (Objects.equals(pac.chatMessage(), this.skip)) {
               return;
            }

            if (mc.player.getMainHandStack().getItem() == Items.FILLED_MAP || mc.player.getOffHandStack().getItem() == Items.FILLED_MAP) {
               return;
            }

            if (pac.chatMessage().startsWith("/") || pac.chatMessage().startsWith(Managers.COMMAND.getPrefix())) {
               return;
            }

            Managers.ASYNC.run(() -> {
               try {
                  String outMessage = this.translate(pac.chatMessage(), this.outMessages.getValue().name());
                  if (Objects.equals(pac.chatMessage(), outMessage)) {
                     this.skip = pac.chatMessage();
                     mc.player.networkHandler.sendChatMessage(pac.chatMessage());
                  } else {
                     this.skip = outMessage;
                     mc.player.networkHandler.sendChatMessage(outMessage);
                  }
               } catch (Exception var3) {
                  var3.printStackTrace();
               }
            });
            e.cancel();
         }
      }
   }

   public String translate(String text, String to) throws UnsupportedEncodingException, MalformedURLException {
      StringBuilder response = new StringBuilder();
      URL url = new URL(
         String.format("https://translate.google.com/m?hl=en&sl=auto&tl=%s&ie=UTF-8&prev=_m&q=%s", to, URLEncoder.encode(text.trim(), StandardCharsets.UTF_8))
      );

      String line;
      try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
         while ((line = br.readLine()) != null) {
            response.append(line + "\n");
         }
      } catch (IOException var10) {
      }

      Matcher matcher = Pattern.compile("class=\"result-container\">([^<]*)<\\/div>", 8).matcher(response);
      matcher.find();
      line = matcher.group(1);
      return line != null && !line.isEmpty() ? StringEscapeUtils.unescapeHtml4(line) : "translation failed";
   }

   public static enum Lang {
      am,
      ar,
      eu,
      bn,
      bg,
      ca,
      chr,
      hr,
      cs,
      da,
      nl,
      en,
      et,
      fil,
      fi,
      fr,
      de,
      el,
      gu,
      iw,
      hi,
      hu,
      is,
      id,
      it,
      ja,
      kn,
      ko,
      lv,
      lt,
      ms,
      ml,
      mr,
      no,
      pl,
      ro,
      ru,
      sr,
      sk,
      sl,
      es,
      sw,
      sv,
      ta,
      te,
      th,
      tr,
      ur,
      uk,
      vi,
      cy,
      cn;
   }
}
