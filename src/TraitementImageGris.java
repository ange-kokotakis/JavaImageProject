package mam3.ipa.projet;

import javax.media.jai.RenderedOp;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.PrintWriter;


public class TraitementImageGris extends TraitementImage {

    public TraitementImageGris(RenderedOp ropimage, String filename){
        this.filename = filename;
        IMG_WIDTH = ropimage.getWidth();
        IMG_HEIGHT = ropimage.getHeight();
        Raster r = ropimage.getData();
        DataBufferByte db = (DataBufferByte)(r.getDataBuffer());
        myGrayImage = db.getData();
        isGray = true;
    }

    public TraitementImageGris(TraitementImage image){
        filename = image.filename;
        IMG_HEIGHT = image.IMG_HEIGHT;
        IMG_WIDTH = image.IMG_WIDTH;
        myGrayImage = image.myGrayImage;
        isGray = true;

    }

    public void transformationGris(){
        System.out.println("L'image est déjà en niveau de gris");
    }

    public void histogramme(String modif){
        int[] histogramme = new int[256];
        for (int i=0; i<myGrayImage.length; i++){
            histogramme[myGrayImage[i] & 0xFF] ++;
        }
        try {
            File file = new File(filename.substring(0,filename.lastIndexOf('.')) + "-" + modif + "-h.txt");
            PrintWriter writer = new PrintWriter(file);
            for (int i=0; i<256; i++){
                writer.println(i + " " + histogramme[i]);
            }
            writer.close();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assombrissement(){
        for (int i=0; i<myGrayImage.length; i++){
            myGrayImage[i] = (byte) (Math.pow((myGrayImage[i] & 0xFF), 2)/255);
        }
    }

    public void eclairage(){
        for (int i=0; i<myGrayImage.length; i++){
            myGrayImage[i] = (byte) (Math.sqrt((myGrayImage[i] & 0xFF)*255));
        }
    }

    public void traitementConvolution(int[] matriceConvolution, boolean normalisation, int decalage){
        byte[] listeImageConvolee = new byte[myGrayImage.length];
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
                int somme = 0;
                int coefNormalisation = 0;
                for (int k=0; k<matriceConvolution.length; k++){
                    if (isCorrectIndex(indexs[k])){
                            somme += (myGrayImage[IMG_WIDTH * indexs[k][0] + indexs[k][1]] & 0xFF) * matriceConvolution[k];
                        if (normalisation) coefNormalisation += matriceConvolution[k];
                    }
                }

                if (normalisation){
                    if (coefNormalisation ==0) coefNormalisation = 1;
                    listeImageConvolee[i * IMG_WIDTH + j] = (byte) (sommeRestreinte(somme/coefNormalisation + decalage));
                }
                else listeImageConvolee[i * IMG_WIDTH + j] = (byte) (sommeRestreinte(somme + decalage));
            }
        }
        myGrayImage = listeImageConvolee;
    }

    public void negatif(){
        for (int i=0; i<myGrayImage.length; i++){
            myGrayImage[i] = (byte) (255 - (myGrayImage[i] & 0xFF));
        }
    }
}