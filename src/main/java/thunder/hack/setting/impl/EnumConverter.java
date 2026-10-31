package thunder.hack.setting.impl;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class EnumConverter extends Converter<Enum, JsonElement> {
   private final Class<? extends Enum> clazz;

   public EnumConverter(Class<? extends Enum> clazz) {
      this.clazz = clazz;
   }

   public static int currentEnum(Enum clazz) {
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
      // 00: bipush 0
      // 01: istore 1
      // 02: iload 1
      // 03: aload 0
      // 04: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 07: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 0a: checkcast [Ljava/lang/Enum;
      // 0d: arraylength
      // 0e: if_icmpge 37
      // 11: aload 0
      // 12: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 15: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 18: checkcast [Ljava/lang/Enum;
      // 1b: iload 1
      // 1c: aaload
      // 1d: astore 2
      // 1e: aload 2
      // 1f: invokevirtual java/lang/Enum.name ()Ljava/lang/String;
      // 22: aload 0
      // 23: invokevirtual java/lang/Enum.name ()Ljava/lang/String;
      // 26: invokevirtual java/lang/String.equalsIgnoreCase (Ljava/lang/String;)Z
      // 29: ifne 2f
      // 2c: goto 31
      // 2f: iload 1
      // 30: ireturn
      // 31: iinc 1 1
      // 34: goto 02
      // 37: bipush -1
      // 38: ireturn
   }

   public static Enum increaseEnum(Enum clazz) {
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

   public static Enum<?> setEnumInt(@NotNull Enum<?> clazz, int id) {
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
      // 00: bipush 0
      // 01: istore 2
      // 02: iload 2
      // 03: aload 0
      // 04: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 07: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 0a: checkcast [Ljava/lang/Enum;
      // 0d: arraylength
      // 0e: if_icmpge 2e
      // 11: aload 0
      // 12: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 15: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 18: checkcast [Ljava/lang/Enum;
      // 1b: iload 2
      // 1c: aaload
      // 1d: astore 3
      // 1e: iload 2
      // 1f: iload 1
      // 20: if_icmpeq 26
      // 23: goto 28
      // 26: aload 3
      // 27: areturn
      // 28: iinc 2 1
      // 2b: goto 02
      // 2e: aload 0
      // 2f: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 32: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 35: checkcast [Ljava/lang/Enum;
      // 38: bipush 0
      // 39: aaload
      // 3a: areturn
   }

   public static String[] getNames(Enum clazz) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot invoke "org.jetbrains.java.decompiler.struct.gen.VarType.isGeneric()" because "newRet" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.getInferredExprType(InvocationExprent.java:634)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.getInferredExprType(InvocationExprent.java:500)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.getInferredExprType(InvocationExprent.java:339)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.getInferredExprType(InvocationExprent.java:339)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.getInferredExprType(FunctionExprent.java:243)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:966)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent.toJava(ExitExprent.java:86)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:895)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:90)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1283)
      //
      // Bytecode:
      // 00: aload 0
      // 01: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 04: invokevirtual java/lang/Class.getEnumConstants ()[Ljava/lang/Object;
      // 07: checkcast [Ljava/lang/Enum;
      // 0a: invokestatic java/util/Arrays.stream ([Ljava/lang/Object;)Ljava/util/stream/Stream;
      // 0d: invokedynamic apply ()Ljava/util/function/Function; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ (Ljava/lang/Object;)Ljava/lang/Object;, java/lang/Enum.name ()Ljava/lang/String;, (Ljava/lang/Enum;)Ljava/lang/String; ]
      // 12: invokeinterface java/util/stream/Stream.map (Ljava/util/function/Function;)Ljava/util/stream/Stream; 2
      // 17: invokedynamic apply ()Ljava/util/function/IntFunction; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ (I)Ljava/lang/Object;, thunder/hack/setting/impl/EnumConverter.lambda$getNames$0 (I)[Ljava/lang/String;, (I)[Ljava/lang/String; ]
      // 1c: invokeinterface java/util/stream/Stream.toArray (Ljava/util/function/IntFunction;)[Ljava/lang/Object; 2
      // 21: checkcast [Ljava/lang/String;
      // 24: areturn
   }

   public static String getProperName(Enum clazz) {
      return clazz.name();
   }

   public JsonElement doForward(Enum anEnum) {
      return new JsonPrimitive(anEnum.toString());
   }

   public Enum doBackward(JsonElement jsonElement) {
      try {
         return Enum.valueOf(this.clazz, jsonElement.getAsString());
      } catch (IllegalArgumentException var3) {
         return null;
      }
   }
}
