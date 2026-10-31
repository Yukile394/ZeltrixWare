package thunder.hack.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat.DrawMode;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class BreadCrumbs extends Module {
   private final Setting<Boolean> throughWalls = new Setting<>("ThroughWalls", true);
   private final Setting<Integer> limit = new Setting<>("ListLimit", 1000, 10, 99999);
   private final List<Vec3d> positions = new CopyOnWriteArrayList<>();
   private final Setting<BreadCrumbs.Mode> lmode = new Setting<>("ColorMode", BreadCrumbs.Mode.Sync);
   private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978), v -> this.lmode.getValue() == BreadCrumbs.Mode.Custom);

   public BreadCrumbs() {
      super("BreadCrumbs", Module.Category.RENDER);
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      Render3DEngine.setupRender();
      if (this.throughWalls.getValue()) {
         RenderSystem.disableDepthTest();
      }

      RenderSystem.disableCull();
      RenderSystem.lineWidth(1.0F);
      RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);

      for (int i = 0; i < this.positions.size(); i++) {
         Vec3d vec1 = null;

         try {
            vec1 = this.positions.get(i - 1);
         } catch (Exception var8) {
         }

         Vec3d vec2 = this.positions.get(i);
         if (vec1 != null && vec2 != null) {
            Color c = this.lmode.getValue() == BreadCrumbs.Mode.Sync ? HudEditor.getColor(i) : this.color.getValue().getColorObject();
            if (i < 10) {
               c = Render2DEngine.injectAlpha(c, (int)((float)c.getAlpha() * ((float)i / 10.0F)));
            }

            MatrixStack matrices = Render3DEngine.matrixFrom(vec1.getX(), vec1.getY(), vec1.getZ());
            Render3DEngine.vertexLine(
               matrices,
               buffer,
               0.0F,
               0.0F,
               0.0F,
               (float)(vec2.getX() - vec1.getX()),
               (float)(vec2.getY() - vec1.getY()),
               (float)(vec2.getZ() - vec1.getZ()),
               c
            );
         }
      }

      Render2DEngine.endBuilding(buffer);
      RenderSystem.enableCull();
      if (this.throughWalls.getValue()) {
         RenderSystem.enableDepthTest();
      }

      Render3DEngine.endRender();
   }

   @EventHandler
   public void postSync(EventPostSync e) {
      if (this.positions.size() > this.limit.getValue()) {
         this.positions.remove(0);
      }

      this.positions.add(new Vec3d(mc.player.getX(), mc.player.getBoundingBox().minY, mc.player.getZ()));
   }

   @Override
   public void onDisable() {
      this.positions.clear();
   }

   private static enum Mode {
      Custom,
      Sync;
   }
}
