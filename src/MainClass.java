package mam3.ipa.projet;

import java.awt.image.ColorModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;

public class MainClass {

    /**
     * Crée l'image de filename avec un constructeur différent selon si elle est en 
     * niveau de gris ou en couleur
     * @param filename le chemin vers l'image
     * @return l'image de type TraitementImage
     */
    public static TraitementImage createImage(String filename){
        RenderedOp ropimage = JAI.create("fileload", filename);
        ColorModel cm = ropimage.getColorModel();
        TraitementImage image = null;
        if (cm.getColorSpace().getType() == ColorSpace.TYPE_RGB){
            image = new TraitementImageRGB(ropimage, filename);
        }
        else if (ropimage.getAsBufferedImage().getType() == BufferedImage.TYPE_BYTE_GRAY && cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
            image = new TraitementImageGris(ropimage, filename);
        }
        return image;
    }

    /**
     * Transforme convmatrix.csv en une liste qui représente une matrice de convolution
     * @param filename le chemin vers convmatrix.csv
     * @return la liste qui représente la matrice de convolution de convmatrix.csv
     */
    public static int[] getMatrice(String filename) throws IOException{
            List<String> fichier = Files.readAllLines(Paths.get(filename.substring(0,filename.lastIndexOf('/'))+"/convmatrix.csv"));
            String contenu = "";
            for (String ligne : fichier){
                contenu += ligne + ";";
            }
            String[] facteursStr = contenu.split(";");
            int[] facteurs = new int[facteursStr.length];
            for (int i=0; i<facteursStr.length; i++){
                facteurs[i] = Integer.parseInt(facteursStr[i]);
            }
            return facteurs;
    }

    public static void main(String[] args) throws IOException{ 
        String filename;

        if (args.length <= 1){
            System.out.println("Usage : java -jar traitementImage.jar [filename] [options]");
        }
        else{
            filename = args[0];
            TraitementImage image = createImage(filename);
            for (int i=1; i<args.length; i++){
                for (int j=0; j<args[i].length(); j++){
                    switch (args[i].charAt(j)) {
                        case 'g':
                            image.transformationGris();
                            image = new TraitementImageGris(image);
                            break;
                        case 'h':
                            image.histogramme(args[i].substring(0,j));
                            break;
                        case 'e':
                            image.eclairage();
                            break;
                        case 'a':
                            image.assombrissement();
                            break;
                        case 'c':
                            Boolean normalisation;
                            int decalage;
                            Scanner scNormalisation = new Scanner(System.in);
                            System.out.println("Normalisation ? [y/n]");
                            if (scNormalisation.nextLine() == "y"){
                                normalisation = true;
                            }
                            else normalisation = false;
                            Scanner scdecalage = new Scanner(System.in);
                            System.out.println("Entrez un decalage :");
                            decalage = scdecalage.nextInt();
                            image.traitementConvolution(getMatrice(filename), normalisation, decalage);
                            break;
                        case 'n':
                            image.negatif();
                            break;
                        default:
                            System.out.println("Cette option n'existe pas");
                            break;
                    }
                }
                image.save(args[i]);
            }
        }
    }
}