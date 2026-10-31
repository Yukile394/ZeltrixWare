# ZeltrixWare

ThunderHack Recode (Pan4ur, GPL v3) tabanlı Fabric 1.21 client modu.
Lisans: **GNU GPL v3** (`LICENSE`). Orijinal proje: https://github.com/Pan4ur/ThunderHack-Recode

## Kurulum (sırayla)

1. Bu dosyaların hepsini repoya koy.
2. Orijinal jar'ı `original/ZeltrixWare.jar` olarak repoya yükle.
3. GitHub → Actions → **Bootstrap (wrapper + decompile)** → Run workflow.
   Gradle wrapper üretir, jar'ı decompile edip yarn isimlerine çevirir ve commit'ler.
4. Actions → **Build** → Run workflow. Çıktı: Artifacts → `ZeltrixWare`.
5. `DECOMPILE_REPORT.md` içindeki sorunlu dosyaları elle düzelt.

## Hedef

| | |
|---|---|
| Minecraft | 1.21 |
| Fabric Loader | 0.15.11 |
| Fabric API | 0.102.0+1.21 |
| Yarn | 1.21+build.9 |
| Loom / Gradle | 1.7.4 / 8.8 |
| Java | 21 |
| Main | `thunder.hack.ThunderHack` |

## Yerel build

```
./gradlew build
```
