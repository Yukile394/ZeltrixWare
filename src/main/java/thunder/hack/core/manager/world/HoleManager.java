package thunder.hack.core.manager.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.core.manager.IManager;

public class HoleManager implements IManager {
   public static final Vec3i[] VECTOR_PATTERN = new Vec3i[]{
      new Vec3i(0, 0, 1), new Vec3i(0, 0, -1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0)
   };

   @NotNull
   public List<BlockPos> getHolePoses(@NotNull Vec3d from) {
      List<BlockPos> positions = new ArrayList<>();
      double decimalX = from.getX() - Math.floor(from.getX());
      double decimalZ = from.getZ() - Math.floor(from.getZ());
      int offX = this.calcOffset(decimalX);
      int offZ = this.calcOffset(decimalZ);
      positions.add(this.getPos(from));

      for (int x = 0; x <= Math.abs(offX); x++) {
         for (int z = 0; z <= Math.abs(offZ); z++) {
            int properX = x * offX;
            int properZ = z * offZ;
            positions.add(Objects.requireNonNull(this.getPos(from)).add(properX, 0, properZ));
         }
      }

      return positions;
   }

   @NotNull
   public List<BlockPos> getSurroundPoses(@NotNull Vec3d from) {
      BlockPos fromPos = BlockPos.ofFloored(from);
      ArrayList<BlockPos> tempOffsets = new ArrayList<>();
      double decimalX = Math.abs(from.getX()) - Math.floor(Math.abs(from.getX()));
      double decimalZ = Math.abs(from.getZ()) - Math.floor(Math.abs(from.getZ()));
      int lengthXPos = this.calcLength(decimalX, false);
      int lengthXNeg = this.calcLength(decimalX, true);
      int lengthZPos = this.calcLength(decimalZ, false);
      int lengthZNeg = this.calcLength(decimalZ, true);

      for (int x = 1; x < lengthXPos + 1; x++) {
         tempOffsets.add(this.addToPlayer(fromPos, (double)x, 0.0, (double)(1 + lengthZPos)));
         tempOffsets.add(this.addToPlayer(fromPos, (double)x, 0.0, (double)(-(1 + lengthZNeg))));
      }

      for (int x = 0; x <= lengthXNeg; x++) {
         tempOffsets.add(this.addToPlayer(fromPos, (double)(-x), 0.0, (double)(1 + lengthZPos)));
         tempOffsets.add(this.addToPlayer(fromPos, (double)(-x), 0.0, (double)(-(1 + lengthZNeg))));
      }

      for (int z = 1; z < lengthZPos + 1; z++) {
         tempOffsets.add(this.addToPlayer(fromPos, (double)(1 + lengthXPos), 0.0, (double)z));
         tempOffsets.add(this.addToPlayer(fromPos, (double)(-(1 + lengthXNeg)), 0.0, (double)z));
      }

      for (int z = 0; z <= lengthZNeg; z++) {
         tempOffsets.add(this.addToPlayer(fromPos, (double)(1 + lengthXPos), 0.0, (double)(-z)));
         tempOffsets.add(this.addToPlayer(fromPos, (double)(-(1 + lengthXNeg)), 0.0, (double)(-z)));
      }

      return tempOffsets;
   }

   @NotNull
   private BlockPos getPos(@NotNull Vec3d from) {
      return BlockPos.ofFloored(
         from.getX(),
         from.getY() - Math.floor(from.getY()) > 0.8 ? Math.floor(from.getY()) + 1.0 : Math.floor(from.getY()),
         from.getZ()
      );
   }

   public int calcOffset(double dec) {
      return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
   }

   public int calcLength(double decimal, boolean negative) {
      if (negative) {
         return decimal <= 0.3 ? 1 : 0;
      } else {
         return decimal >= 0.7 ? 1 : 0;
      }
   }

   public BlockPos addToPlayer(@NotNull BlockPos playerPos, double x, double y, double z) {
      if (playerPos.getX() < 0) {
         x = -x;
      }

      if (playerPos.getY() < 0) {
         y = -y;
      }

      if (playerPos.getZ() < 0) {
         z = -z;
      }

      return playerPos.add(BlockPos.ofFloored(x, y, z));
   }

   public boolean isHole(BlockPos pos) {
      return this.isSingleHole(pos)
         || this.validTwoBlockIndestructible(pos)
         || this.validTwoBlockBedrock(pos)
         || this.validQuadIndestructible(pos)
         || this.validQuadBedrock(pos);
   }

   public boolean isSingleHole(BlockPos pos) {
      return this.validIndestructible(pos) || this.validBedrock(pos);
   }

   public boolean validIndestructible(@NotNull BlockPos pos) {
      return !this.validBedrock(pos)
         && (this.isIndestructible(pos.add(0, -1, 0)) || this.isBedrock(pos.add(0, -1, 0)))
         && (this.isIndestructible(pos.add(1, 0, 0)) || this.isBedrock(pos.add(1, 0, 0)))
         && (this.isIndestructible(pos.add(-1, 0, 0)) || this.isBedrock(pos.add(-1, 0, 0)))
         && (this.isIndestructible(pos.add(0, 0, 1)) || this.isBedrock(pos.add(0, 0, 1)))
         && (this.isIndestructible(pos.add(0, 0, -1)) || this.isBedrock(pos.add(0, 0, -1)))
         && this.isReplaceable(pos)
         && this.isReplaceable(pos.add(0, 1, 0))
         && this.isReplaceable(pos.add(0, 2, 0));
   }

   public boolean validBedrock(@NotNull BlockPos pos) {
      return this.isBedrock(pos.add(0, -1, 0))
         && this.isBedrock(pos.add(1, 0, 0))
         && this.isBedrock(pos.add(-1, 0, 0))
         && this.isBedrock(pos.add(0, 0, 1))
         && this.isBedrock(pos.add(0, 0, -1))
         && this.isReplaceable(pos)
         && this.isReplaceable(pos.add(0, 1, 0))
         && this.isReplaceable(pos.add(0, 2, 0));
   }

   public boolean validTwoBlockBedrock(@NotNull BlockPos pos) {
      if (!this.isReplaceable(pos)) {
         return false;
      } else {
         Vec3i addVec = this.getTwoBlocksDirection(pos);
         if (addVec == null) {
            return false;
         } else {
            BlockPos[] checkPoses = new BlockPos[]{pos, pos.add(addVec)};

            for (BlockPos checkPos : checkPoses) {
               BlockPos downPos = checkPos.down();
               if (!this.isBedrock(downPos)) {
                  return false;
               }

               for (Vec3i vec : VECTOR_PATTERN) {
                  BlockPos reducedPos = checkPos.add(vec);
                  if (!this.isBedrock(reducedPos) && !reducedPos.equals(pos) && !reducedPos.equals(pos.add(addVec))) {
                     return false;
                  }
               }
            }

            return true;
         }
      }
   }

   public boolean validTwoBlockIndestructible(@NotNull BlockPos pos) {
      if (!this.isReplaceable(pos)) {
         return false;
      } else {
         Vec3i addVec = this.getTwoBlocksDirection(pos);
         if (addVec == null) {
            return false;
         } else {
            BlockPos[] checkPoses = new BlockPos[]{pos, pos.add(addVec)};
            boolean wasIndestrictible = false;

            for (BlockPos checkPos : checkPoses) {
               BlockPos downPos = checkPos.down();
               if (this.isIndestructible(downPos)) {
                  wasIndestrictible = true;
               } else if (!this.isBedrock(downPos)) {
                  return false;
               }

               for (Vec3i vec : VECTOR_PATTERN) {
                  BlockPos reducedPos = checkPos.add(vec);
                  if (this.isIndestructible(reducedPos)) {
                     wasIndestrictible = true;
                  } else if (!this.isBedrock(reducedPos) && !reducedPos.equals(pos) && !reducedPos.equals(pos.add(addVec))) {
                     return false;
                  }
               }
            }

            return wasIndestrictible;
         }
      }
   }

   @Nullable
   private Vec3i getTwoBlocksDirection(BlockPos pos) {
      for (Vec3i vec : VECTOR_PATTERN) {
         if (this.isReplaceable(pos.add(vec))) {
            return vec;
         }
      }

      return null;
   }

   public boolean validQuadIndestructible(@NotNull BlockPos pos) {
      List<BlockPos> checkPoses = this.getQuadDirection(pos);
      if (checkPoses == null) {
         return false;
      } else {
         boolean wasIndestrictible = false;

         for (BlockPos checkPos : checkPoses) {
            BlockPos downPos = checkPos.down();
            if (this.isIndestructible(downPos)) {
               wasIndestrictible = true;
            } else if (!this.isBedrock(downPos)) {
               return false;
            }

            for (Vec3i vec : VECTOR_PATTERN) {
               BlockPos reducedPos = checkPos.add(vec);
               if (this.isIndestructible(reducedPos)) {
                  wasIndestrictible = true;
               } else if (!this.isBedrock(reducedPos) && !checkPoses.contains(reducedPos)) {
                  return false;
               }
            }
         }

         return wasIndestrictible;
      }
   }

   public boolean validQuadBedrock(@NotNull BlockPos pos) {
      List<BlockPos> checkPoses = this.getQuadDirection(pos);
      if (checkPoses == null) {
         return false;
      } else {
         for (BlockPos checkPos : checkPoses) {
            BlockPos downPos = checkPos.down();
            if (!this.isBedrock(downPos)) {
               return false;
            }

            for (Vec3i vec : VECTOR_PATTERN) {
               BlockPos reducedPos = checkPos.add(vec);
               if (!this.isBedrock(reducedPos) && !checkPoses.contains(reducedPos)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   @Nullable
   private List<BlockPos> getQuadDirection(@NotNull BlockPos pos) {
      List<BlockPos> dirList = new ArrayList<>();
      dirList.add(pos);
      if (!this.isReplaceable(pos)) {
         return null;
      } else {
         if (this.isReplaceable(pos.add(1, 0, 0)) && this.isReplaceable(pos.add(0, 0, 1)) && this.isReplaceable(pos.add(1, 0, 1))) {
            dirList.add(pos.add(1, 0, 0));
            dirList.add(pos.add(0, 0, 1));
            dirList.add(pos.add(1, 0, 1));
         }

         if (this.isReplaceable(pos.add(-1, 0, 0))
            && this.isReplaceable(pos.add(0, 0, -1))
            && this.isReplaceable(pos.add(-1, 0, -1))) {
            dirList.add(pos.add(-1, 0, 0));
            dirList.add(pos.add(0, 0, -1));
            dirList.add(pos.add(-1, 0, -1));
         }

         if (this.isReplaceable(pos.add(1, 0, 0)) && this.isReplaceable(pos.add(0, 0, -1)) && this.isReplaceable(pos.add(1, 0, -1))) {
            dirList.add(pos.add(1, 0, 0));
            dirList.add(pos.add(0, 0, -1));
            dirList.add(pos.add(1, 0, -1));
         }

         if (this.isReplaceable(pos.add(-1, 0, 0)) && this.isReplaceable(pos.add(0, 0, 1)) && this.isReplaceable(pos.add(-1, 0, 1))) {
            dirList.add(pos.add(-1, 0, 0));
            dirList.add(pos.add(0, 0, 1));
            dirList.add(pos.add(-1, 0, 1));
         }

         return dirList.size() != 4 ? null : dirList;
      }
   }

   private boolean isIndestructible(BlockPos bp) {
      return mc.world == null
         ? false
         : mc.world.getBlockState(bp).getBlock() == Blocks.OBSIDIAN
            || mc.world.getBlockState(bp).getBlock() == Blocks.NETHERITE_BLOCK
            || mc.world.getBlockState(bp).getBlock() == Blocks.CRYING_OBSIDIAN
            || mc.world.getBlockState(bp).getBlock() == Blocks.RESPAWN_ANCHOR;
   }

   private boolean isBedrock(BlockPos bp) {
      return mc.world == null ? false : mc.world.getBlockState(bp).getBlock() == Blocks.BEDROCK;
   }

   private boolean isReplaceable(BlockPos bp) {
      return mc.world == null ? false : mc.world.getBlockState(bp).isReplaceable();
   }
}
