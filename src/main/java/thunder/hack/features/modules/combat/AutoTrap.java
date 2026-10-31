package thunder.hack.features.modules.combat;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.player.CombatManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.base.TrapModule;
import thunder.hack.setting.Setting;

public final class AutoTrap extends TrapModule {
   private final Setting<CombatManager.TargetBy> targetBy = new Setting<>("Target By", CombatManager.TargetBy.Distance);
   private final Setting<Boolean> targetMovingPlayers = new Setting<>("MovingPlayers", false);

   public AutoTrap() {
      super("AutoTrap", Module.Category.COMBAT);
   }

   @Override
   protected boolean needNewTarget() {
      return this.target == null
         || this.target.distanceTo(mc.player) > this.range.getValue()
         || this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0F
         || this.target.isDead();
   }

   @Nullable
   @Override
   protected PlayerEntity getTarget() {
      return Managers.COMBAT
         .getTarget(this.range.getValue(), this.targetBy.getValue(), p -> p.getVelocity().lengthSquared() < 0.08 || this.targetMovingPlayers.getValue());
   }
}
