# Si ta hapesh projektin

Mos hap vetem `App.java`, sepse atehere IntelliJ e shfaq si file tekst/kod.

Hape keshtu:

1. Bej extract ZIP-in.
2. Hap IntelliJ IDEA.
3. Zgjidh `File > Open`.
4. Zgjidh folderin `libraria_project`.
5. Prit Maven Reload.
6. Zgjidh Run Configuration `Libraria GUI`.
7. Shtyp Run.

Nese te del `Could not find or load main class org.example.Main`, bej:

1. Mbylle projektin.
2. Hape prape me `File > Open`.
3. Zgjidh folderin kryesor `libraria_project`, jo `src` dhe jo `App.java`.
4. Pastaj `Build > Rebuild Project`.
5. Run prape `Libraria GUI`.

Main class:

```text
org.example.Main
```

Ky eshte projekt Maven sepse ka `pom.xml` dhe strukturen:

```text
src/main/java/org/example/App.java
src/main/java/org/example/Main.java
```
