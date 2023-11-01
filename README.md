# Java Image Project

Projet scolaire de traitement d'image, permet de modifier des images au format png.

### Utilisation :

Compiler le code
```shell
javac -cp "lib/*" -d . src/*
```

compresser le tout dans une archive
```shell
jar cfm traitementImage.jar META-INF/MANIFEST.MF *
```

Puis pour l'exécuter :
```shell
java -jar traitementImage.jar [filename] [options]
```

Les differentes options sont :

a : pour assombrir l'image\
e : pour éclairer l'image\
h : pour créer l'histogramme de l'image\
g : pour passer l'image en niveau de gris\
c : pour réaliser un traitement par convolution à l'aide de la matrice `convmatrix.csv`

exemple de `convmatrix.csv` :\
-1;-1;-1\
-1;8;-1\
-1;-1;-1