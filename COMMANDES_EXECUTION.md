# Commandes pour compiler et tester MiniJava

Ce document rassemble les commandes utiles pour compiler le projet et lancer les différents tests liés aux fonctionnalités orientées objet (MiniJava). 

Assurez-vous d'ouvrir votre terminal dans le dossier `PROJET_MINI_JAVA/fr.n7.stl/` avant de lancer ces commandes.

## 1. Compiler le compilateur MiniJava

### Option A : Utiliser Ant (Méthode officielle)
Si `ant` est installé sur votre système, c'est la méthode la plus simple :
```bash
ant compile -buildfile minijava-build.xml
```
*(Vous pouvez utiliser `ant clean -buildfile minijava-build.xml` pour nettoyer le dossier)*

### Option B : Compilation Manuelle (si Ant n'est pas disponible)
Si vous préférez utiliser directement les commandes `java`/`javac` depuis le terminal :

```bash
# 1. Générer le Lexer et Parser depuis la grammaire ANTLR (uniquement si vous avez modifié les fichiers .g4)
java -cp tools/antlr-4.13.1-complete.jar org.antlr.v4.Tool -o src/fr/n7/stl/minijava/parser MiniJavaLexer.g4 MiniJavaParser.g4

# 2. Compiler toutes les sources Java du projet
find src -name "*.java" > sources.txt
javac -cp "tools/commons-text-1.9.jar:tools/antlr-4.13.1-complete.jar" -sourcepath src -d bin/cls @sources.txt
```

---

## 2. Exécuter un test (Générer le code assembleur TAM)

Une fois le compilateur MiniJava compilé, vous pouvez lui donner un fichier d'entrée (par exemple `tests-minijava/test02.mjava`). Le compilateur va l'analyser et générer un fichier `.tam` correspondant.

```bash
java -cp "bin/cls:tools/antlr-4.13.1-complete.jar:tools/commons-text-1.9.jar" fr.n7.stl.minijava.Driver tests-minijava/test02.mjava
```
*(Remplacez `test02.mjava` par le fichier que vous souhaitez tester, par exemple `test_super.mjava` ou `test_heritage.mjava`)*

---

## 3. Exécuter le programme sur la Machine Virtuelle TAM

Une fois le fichier `.tam` généré, vous devez l'exécuter dans la machine virtuelle fournie :

```bash
java -jar ../../partie1/TAM/runtam.jar tests-minijava/test02.tam
```

---

## Ligne de commande "Tout-en-un" (Compilation AST + Lancement TAM)

Pour gagner du temps lors des tests, voici une commande qui regroupe l'exécution du compilateur MiniJava et l'exécution immédiate sur la machine TAM si la compilation réussit :

```bash
TEST_FILE="test_super" # Choisissez ici votre test (test02, test_heritage, test_constructeur, test_objet...)

java -cp "bin/cls:tools/antlr-4.13.1-complete.jar:tools/commons-text-1.9.jar" fr.n7.stl.minijava.Driver tests-minijava/${TEST_FILE}.mjava && java -jar ../../partie1/TAM/runtam.jar tests-minijava/${TEST_FILE}.tam
```