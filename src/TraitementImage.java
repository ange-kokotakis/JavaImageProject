package mam3.ipa.projet;

import javax.media.jai.JAI;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import javax.media.jai.RasterFactory;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.Point;

public abstract class TraitementImage {
    protected int IMG_WIDTH;
    protected int IMG_HEIGHT;
    protected String filename;
    protected boolean isGray;
    protected int[] imageRGB;
    protected byte[] myGrayImage;

    /**
     * Modifie imageRGB pour le mettre en niveau de gris, si appliqué à une image en niv de gris
     * renvoie "l'image est déjà en niveau de gris"
     */
    public abstract void transformationGris();

    /**
     * Crée l'histogramme de l'image et l'enregistre avec modif à la fin de son nom
     * @param modif chaine de caractère qui représente les modification appliquées à l'image
     */
    public abstract void histogramme(String modif);

    /**
     * Modifie l'image pour l'assombrir
     */
    public abstract void assombrissement();

    /**
     * Modifie l'image pour l'éclairer
     */
    public abstract void eclairage();

    /**
     * Applique la matrice de convolution matriceConvolution à l'image, on peut choisir d'appliquer une normalisation
     * et/ou un décalage
     * @param matriceConvolution matrice de convolution à appliquer à l'image
     * @param normalisation booléen qui représente si on veut faire la normalisation ou non
     * @param decalage nombre qui représente le décalage qu'on veut appliquer
     */
    public abstract void traitementConvolution(int[] matriceConvolution, boolean normalisation, int decalage);

    /**
     * Modifie l'image pour la transformer en son negatif
     */
    public abstract void negatif();
    
    /**
     * Fonction qui retourne vrai si l'index est correct et faux sinon
     * @param index index dont on veut savoir si il est correct ou non
     * @return booléen qui représente si l'index est correct ou non
     */
    public boolean isCorrectIndex(int[] index){
        if (0<=index[0] && 0<=index[1] && index[0]<IMG_HEIGHT && index[1]<IMG_WIDTH){
            return true;
        }
        return false;
    }

    /**
     * Fonction qui restreint somme entre 0 et 255
     * @param somme un nombre à restreindre
     * @return somme qui est restreinte à [0, 255]
     */
    public int sommeRestreinte(int somme){
        if (somme < 0) return 0;
        if (somme > 255) return 255;
        return somme;
    }

    /**
     * Sauvegarde l'image (différement selon si elle est en niveau de gris ou en couleur)
     * en rajoutant à la fin du nom du fichier modif
     * @param modif chaine de caractère qui représente les modifications appliquées à l'image
     */
    public void save(String modif){
        if (isGray){
            SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE,IMG_WIDTH,IMG_HEIGHT,1);
            BufferedImage image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
            image.setData(Raster.createRaster(sm, new DataBufferByte(myGrayImage, myGrayImage.length), new Point()));
            JAI.create("filestore",image,filename.substring(0,filename.lastIndexOf('.')) + "-" + modif + ".png","PNG");
        }
        else{
            DataBufferInt dataBuffer = new DataBufferInt(imageRGB, imageRGB.length);
            ColorModel colorModel = new DirectColorModel(32,0xFF0000,0xFF00,0xFF,0xFF000000);
            WritableRaster raster = Raster.createPackedRaster(dataBuffer, IMG_WIDTH, IMG_HEIGHT, IMG_WIDTH, ((DirectColorModel) colorModel).getMasks(), null);
            BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
            JAI.create("filestore", image, filename.substring(0,filename.lastIndexOf('.')) + "-" + modif + ".png", "png");
        }
    }
}
