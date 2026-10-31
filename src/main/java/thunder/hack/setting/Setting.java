package thunder.hack.setting;

import java.util.function.Predicate;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.EnumConverter;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.setting.impl.SettingGroup;

public class Setting<T> {
   private final String name;
   private final T defaultValue;
   private T value;
   private T plannedValue;
   private T min;
   private T max;
   public Setting<?> group = null;
   private boolean hasRestriction;
   private Predicate<T> visibility;
   private Module module;

   public Setting(String name, T defaultValue) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.value = defaultValue;
      this.plannedValue = defaultValue;
   }

   public Setting(String name, T defaultValue, T min, T max) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.value = defaultValue;
      this.min = min;
      this.max = max;
      this.plannedValue = defaultValue;
      this.hasRestriction = true;
   }

   public Setting(String name, T defaultValue, T min, T max, Predicate<T> visibility) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.value = defaultValue;
      this.min = min;
      this.max = max;
      this.plannedValue = defaultValue;
      this.visibility = visibility;
      this.hasRestriction = true;
   }

   public Setting(String name, T defaultValue, Predicate<T> visibility) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.value = defaultValue;
      this.visibility = visibility;
      this.plannedValue = defaultValue;
   }

   public static Enum get(Enum clazz) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot invoke "org.jetbrains.java.decompiler.struct.gen.VarType.isGeneric()" because "newRet" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.getInferredExprType(InvocationExprent.java:634)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:1063)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.toJava(InvocationExprent.java:902)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.wrapOperandString(FunctionExprent.java:745)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.wrapOperandString(FunctionExprent.java:714)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.toJava(FunctionExprent.java:610)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.wrapOperandString(FunctionExprent.java:745)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.wrapOperandString(FunctionExprent.java:714)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.toJava(FunctionExprent.java:614)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.wrapOperandString(FunctionExprent.java:745)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.toJava(FunctionExprent.java:589)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement.toJava(DoStatement.java:136)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:833)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1283)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokestatic thunder/hack/setting/impl/EnumConverter.currentEnum (Ljava/lang/Enum;)I
      // 04: istore 1
      // 05: bipush 0
      // 06: istore 2
      // 07: iload 2
      // 08: aload 0
      // 09: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 0c: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 0f: checkcast [Ljava/lang/Enum;
      // 12: arraylength
      // 13: if_icmpge 35
      // 16: aload 0
      // 17: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 1a: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 1d: checkcast [Ljava/lang/Enum;
      // 20: iload 2
      // 21: aaload
      // 22: astore 3
      // 23: iload 2
      // 24: iload 1
      // 25: bipush 1
      // 26: iadd
      // 27: if_icmpeq 2d
      // 2a: goto 2f
      // 2d: aload 3
      // 2e: areturn
      // 2f: iinc 2 1
      // 32: goto 07
      // 35: aload 0
      // 36: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 39: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 3c: checkcast [Ljava/lang/Enum;
      // 3f: bipush 0
      // 40: aaload
      // 41: areturn
   }

   public String getName() {
      return this.name;
   }

   public T getValue() {
      return this.value;
   }

   public void setValue(T value) {
      this.setValueSilent(value);
      ThunderHack.EVENT_BUS.post(new EventSetting(this));
   }

   public void setValueSilent(T value) {
      this.setPlannedValue(value);
      if (this.hasRestriction) {
         if (((Number)this.min).floatValue() > ((Number)value).floatValue()) {
            this.setPlannedValue(this.min);
         }

         if (((Number)this.max).floatValue() < ((Number)value).floatValue()) {
            this.setPlannedValue(this.max);
         }
      }

      this.value = this.plannedValue;
   }

   public float getPow2Value() {
      if (this.value instanceof Float) {
         return (Float)this.value * (Float)this.value;
      } else {
         return this.value instanceof Integer ? (float)((Integer)this.value * (Integer)this.value) : 0.0F;
      }
   }

   public void setPlannedValue(T value) {
      this.plannedValue = value;
   }

   public T getMin() {
      return this.min;
   }

   public void setMin(T min) {
      this.min = min;
   }

   public T getMax() {
      return this.max;
   }

   public void setMax(T max) {
      this.max = max;
   }

   public Module getModule() {
      return this.module;
   }

   public void setModule(Module module) {
      this.module = module;
   }

   public String currentEnumName() {
      return EnumConverter.getProperName((Enum)this.value);
   }

   public String[] getModes() {
      return EnumConverter.getNames((Enum)this.value);
   }

   public void setEnum(Enum mod) {
      this.plannedValue = (T)mod;
   }

   public void increaseEnum() {
      this.plannedValue = (T)EnumConverter.increaseEnum((Enum)this.value);
      this.value = this.plannedValue;
      ThunderHack.EVENT_BUS.post(new EventSetting(this));
   }

   public void setEnumByNumber(int id) {
      this.plannedValue = (T)EnumConverter.setEnumInt((Enum<?>)this.value, id);
      this.value = this.plannedValue;
      ThunderHack.EVENT_BUS.post(new EventSetting(this));
   }

   public boolean isNumberSetting() {
      return this.value instanceof Double
         || this.value instanceof Integer
         || this.value instanceof Short
         || this.value instanceof Long
         || this.value instanceof Float;
   }

   public boolean isInteger() {
      return this.value instanceof Integer;
   }

   public boolean isFloat() {
      return this.value instanceof Float;
   }

   public boolean isEnumSetting() {
      return this.value.getClass().isEnum();
   }

   public boolean isBindSetting() {
      return this.value instanceof Bind;
   }

   public boolean isStringSetting() {
      return this.value instanceof String;
   }

   public boolean isItemSelectSetting() {
      return this.value instanceof ItemSelectSetting;
   }

   public boolean isPositionSetting() {
      return this.value instanceof PositionSetting;
   }

   public T getDefaultValue() {
      return this.defaultValue;
   }

   public boolean hasRestriction() {
      return this.hasRestriction;
   }

   public Setting<T> addToGroup(Setting<?> group) {
      this.group = group;
      return this;
   }

   public boolean isVisible() {
      if (this.group != null) {
         if (this.group.getValue() instanceof BooleanSettingGroup bp && !bp.isExtended()) {
            return false;
         }

         if (this.group.getValue() instanceof SettingGroup p && !p.isExtended()) {
            return false;
         }
      }

      return this.visibility == null ? true : this.visibility.test(this.getValue());
   }

   public boolean is(T v) {
      return this.value == v;
   }

   public boolean not(T v) {
      return this.value != v;
   }
}
