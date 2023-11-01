package mam3.ipa.projet;

import javax.media.jai.RenderedOp;
import java.io.File;
import java.io.PrintWriter;

public class TraitementImageRGB extends TraitementImage {

    public TraitementImageRGB(RenderedOp ropimage, String filename){
        this.filename = filename;
        IMG_WIDTH = ropimage.getWidth();
        IMG_HEIGHT = ropimage.getHeight();
        imageRGB = ropimage.getAsBufferedImage().getRGB(0,0,IMG_WIDTH,IMG_HEIGHT,null,0,IMG_WIDTH);
        isGray = false;
    }

    public void transformationGris(){ 
        myGrayImage = new byte[imageRGB.length];
        for (int i=0; i<imageRGB.length; i++){
            myGrayImage[i] = (byte) (0.21 * getRed(imageRGB[i]) + 0.72 * getGreen(imageRGB[i]) + 0.07 * getBlue(imageRGB[i]));
        }
        isGray = true;
    }

    /**
     * Méthode qui renvoie la valeur du canal rouge de pixel
     * @param pixel valeur du pixel considéré
     * @return la valeur du canal rouge de pixel
     */
    public int getRed(int pixel){
        return (pixel >> 16) & 0xFF;
    }

    /**
     * Méthode qui renvoie la valeur du canal vert de pixel
     * @param pixel valeur du pixel considéré
     * @return la valeur du canal vert de pixel
     */
    public int getGreen(int pixel){
        return (pixel >> 8) & 0xFF;
    }

    /**
     * Méthode qui renvoie la valeur du canal bleu de pixel
     * @param pixel valeur du pixel considéré
     * @return la valeur du canal bleu de pixel
     */
    public int getBlue(int pixel){
        return pixel & 0xFF;
    }

    /**
     * Méthode qui renvoie pixel en modifiant ses canaux de rouge, vert et bleu
     * @param pixel le pixel qu'on veut modifier
     * @param red valeur du canal rouge qu'on veut donner à pixel
     * @param green valeur du canal vert qu'on veut donner à pixel
     * @param blue valeur du canal bleu qu'on veut donner à pixel
     * @return la valeur de pixel qui a été modifiée
     */
    public int setRGB(int pixel, int red, int green, int blue){
        return (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }

    public void histogramme(String modif){
        int[][] histogramme = new int[256][3];
        for (int i=0; i<imageRGB.length; i++){
            histogramme[getRed(imageRGB[i])][0] ++;
            histogramme[getGreen(imageRGB[i])][1] ++;
            histogramme[getBlue(imageRGB[i])][2] ++;
        }
        try {
            File file = new File(filename.substring(0,filename.lastIndexOf('.')) + "-" + modif + "-h.txt");
            PrintWriter writer = new PrintWriter(file);
            for (int i=0; i<256; i++){
                writer.println(i + " " + histogramme[i][0] + " " + histogramme[i][1] + " " + histogramme[i][2]);
            }
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assombrissement(){
        int[] listeImageAssombri = new int[imageRGB.length];
        for (int i=0; i<imageRGB.length; i++){
            listeImageAssombri[i] = setRGB(imageRGB[i], (int) (Math.pow(getRed(imageRGB[i]), 2)/255),
                                                        (int) (Math.pow(getGreen(imageRGB[i]), 2)/255),
                                                        (int) (Math.pow(getBlue(imageRGB[i]), 2)/255));
        }
        imageRGB = listeImageAssombri;
    }

    public void eclairage(){
        int[] listeImageEclaire = new int[imageRGB.length];
        for (int i=0; i< imageRGB.length; i++){
            listeImageEclaire[i] = setRGB(imageRGB[i], (int) Math.sqrt(getRed(imageRGB[i]) * 255),
                                                       (int) Math.sqrt(getGreen(imageRGB[i]) * 255),
                                                       (int) Math.sqrt(getBlue(imageRGB[i]) * 255));
        }
        imageRGB = listeImageEclaire;
    }

    public void traitementConvolution(int[] matriceConvolution, boolean normalisation, int decalage){
        int[] listeImageConvolee = new int[imageRGB.length];
        int lencote = (int) Math.sqrt(matriceConvolution.length);
        for (int i=0; i<IMG_HEIGHT; i++){
            for (int j=0; j<IMG_WIDTH; j++){
                int[][] indexs = new int[matriceConvolution.length][2];
                for (int k=0; k<lencote; k++){
                    for (int l=0;l<lencote; l++){
                        indexs[k*lencote + l][0] = i + (k -(lencote-1)/2);
                        indexs[k*lencote + l][1] = j + (l -(lencote-1)/2);
                    }
                }
                int sommeRouge = 0;
                int sommeVert = 0;
                int sommeBleu = 0;
                int coefNormalisationRouge = 0;
                int coefNormalisationVert = 0;
                int coefNormalisationBleu = 0;
                for (int k=0; k<matriceConvolution.length; k++){
                    if (isCorrectIndex(indexs[k])){
                        sommeRouge += getRed(imageRGB[IMG_WIDTH * indexs[k][0] + indexs[k][1]]) * matriceConvolution[k];
                        sommeVert += getGreen(imageRGB[IMG_WIDTH * indexs[k][0] + indexs[k][1]]) * matriceConvolution[k];
                        sommeBleu += getBlue(imageRGB[IMG_WIDTH * indexs[k][0] + indexs[k][1]]) * matriceConvolution[k];

                        if (normalisation){
                            coefNormalisationRouge += matriceConvolution[k];
                            coefNormalisationVert += matriceConvolution[k];
                            coefNormalisationBleu += matriceConvolution[k];
                        }
                    }
                }
                if (!normalisation){
                    for (int somme : new int[] {sommeRouge, sommeVert, sommeBleu}){
                        somme = sommeRestreinte(somme);
                    }
                }
                if (coefNormalisationRouge == 0) coefNormalisationRouge = 1;
                if (coefNormalisationVert == 0) coefNormalisationVert = 1;
                if (coefNormalisationBleu == 0) coefNormalisationBleu = 1;

                listeImageConvolee[i * IMG_WIDTH + j] = setRGB(imageRGB[i * IMG_WIDTH + j], sommeRestreinte(sommeRouge/coefNormalisationRouge + decalage),
                                                                                            sommeRestreinte(sommeVert/coefNormalisationVert + decalage),
                                                                                            sommeRestreinte(sommeBleu/coefNormalisationBleu + decalage));
            }
        }
        imageRGB = listeImageConvolee;
    }

    public void negatif(){
        for (int i=0; i<imageRGB.length; i++){
            imageRGB[i] = setRGB(imageRGB[i], 255 - getRed(imageRGB[i]), 255 - getGreen(imageRGB[i]), 255 - getBlue(imageRGB[i]));
        }
    }
}