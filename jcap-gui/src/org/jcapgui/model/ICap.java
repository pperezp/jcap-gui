/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jcapgui.model;

import java.awt.image.BufferedImage;

/**
 *
 * @author Patricio Pérez Pinto
 */
public interface ICap {
    //weon esta wea se va a llamar cuando se genere una imagen
    //la imagen queda en captura, y size solo dice cuantas equis son
    //por ejemplo: 1x, 2x, 3x etc. es solo pa info, no es esencial
    void captura(BufferedImage captura, String size);
}
