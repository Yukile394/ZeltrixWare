package thunder.hack.utility.interfaces;

import java.util.List;
import thunder.hack.features.modules.combat.Aura;

public interface IEntityLiving {
   double getPrevServerX();

   double getPrevServerY();

   double getPrevServerZ();

   List<Aura.Position> getPositionHistory();
}
