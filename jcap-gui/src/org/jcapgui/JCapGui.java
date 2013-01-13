/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jcapgui;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import org.jcap.JCap;
import org.jcapgui.model.ICap;
import transparentWindows.AWTUtilitiesWrapper;

/**
 *
 * @author Patricio Pérez Pinto
 */
public class JCapGui extends Observable/*extiende de observable, ya que
 JCapGui avisa a los observadores cuando se captura la foto*/ implements Observer{
    private JCap cap; // para poder capturar la wea
    private Rectangle rec; // rectangulo que quiero capturar
    private Window jdialog;// el jdialog que va a servir para poder cachar que wea queri capturar
    private ICap icap;//interface que despues creo que la entederas
    private int tamanioEnEquis;// el tamaño en equis de la captura, por ejemplo, si queri la wea normal, le chantai un 1, o si queri
    //la imagen doble un 2x, y asi sucesivamente
    
    public JCapGui(Window jdialog, ICap icap) {
        // si el weon no pone la cantidad de "x", se establece en 1x (recuerda que x es el tamaño weon)
        this(jdialog ,icap, 1);
    }
    
    public JCapGui(Window jdialog, ICap icap, int tamanioEnEquis){
        this.jdialog = jdialog;
        this.jdialog.setVisible(false);
        this.addObserver(this);// añado el observador this porque esta clase implementa Observer
        
        //Cuando implementas de Observer tienes que implementar el metodo "update", que se llamará
        //cuadno yo quiera, o sea, cuando haga una captura. Como se que se quiere capturar??
        //es el mouseRelease de la wea
        this.icap = icap;
        agregarListeners();//-> acá agrego todos los listeners al jdialog, onda, el
        //mouse wea, mouse drag y weas
        this.tamanioEnEquis = tamanioEnEquis;
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof BufferedImage){//esto lo hice solamente por si despues le pongo otro object que no sea BufferedImage
            //pero por ahora siempre es BufferedImage
            BufferedImage bi = (BufferedImage)arg;//rescato la imagen. tu deci que mierda cuando se la pase?
            // esa imagen se la paso en el metodo "thisMouseReleased" qe es cuando se genera la captura weon
            //
            if(this.tamanioEnEquis != 1){//si el weon quiere una captura a mas de 1x, o sea 2 o 3 o n,
                //hay que suavizar la wea pa q los pixeles no se vean tan cuadrados
                
                //recuerda que el objeto icap, es la interface, y con esto estoy llamando al metodo que vas a implementar tu.
                //al metodo le entrego la imagen suavizada
                icap.captura(suavizarImagen(getImagenEscalada(bi, this.tamanioEnEquis)), this.tamanioEnEquis+"x");
            }else{//si el tamaño que quiere el weon es 1x, osea el origial,, no hay que
                //suavizarlo, ya que si suevizas una wea del mismo tamaño se ve borroso
                //
                icap.captura(getImagenEscalada(bi, this.tamanioEnEquis), this.tamanioEnEquis+"x");
            }
        }
    }
    
    public void comenzarCapturador(){
        /*deja el jdialog del porte de la pantalla*/
        this.jdialog.setBounds(
                new Rectangle(
                java.awt.Toolkit.getDefaultToolkit().getScreenSize()
                )
        );
        /*deja el jdialog pseudo transparente*/
        AWTUtilitiesWrapper.setWindowOpacity(this.jdialog, .2f);
        this.jdialog.setVisible(true);
    }
    
    private void agregarListeners() {
        jdialog.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {//
                thisMousePressed(evt);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                try {
                    thisMouseReleased(evt);
                } catch (AWTException ex) {
                    Logger.getLogger(JCapGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jdialog.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                thisMouseDragged(evt);
            }
        });
        jdialog.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                thisKeyReleased(evt);
            }
        });
    }

    private void thisKeyReleased(KeyEvent evt) {
        if(evt.getKeyCode() == KeyEvent.VK_ESCAPE){
            detenerCapturador();
        }
    }

    public void detenerCapturador(){
        jdialog.setVisible(false);
    }
    
    /*todo esto sucede mientras arrastras el mouse*/
    private void thisMouseDragged(MouseEvent evt) {
        Graphics g = jdialog.getGraphics();
        Graphics2D g2  = (Graphics2D)g;
        
        /*cachaña pa que se vea mejor, sin importancia relevalente*/
        g2.setColor(Color.green);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setStroke(new BasicStroke(2));
        g2.clearRect(0, 0, jdialog.getWidth(), jdialog.getHeight());
         /*cachaña pa que se vea mejor, sin importancia relevalente*/
        
        
        //aca seteo el size del rectangulo que en definitiva va a ser el rectangulo que le pase al JCap
        rec.setSize(evt.getX() - (int)rec.getX(), evt.getY() - (int)rec.getY());
        
        /*esta wea dibuja el rectangulo en el jdialog*/
        g2.fillRoundRect(
                (int)rec.getX(), 
                (int)rec.getY(), 
                (int)rec.getWidth(), 
                (int)rec.getHeight(), 0, 0);
    }
    
    /*cuando el weon levante el click, es porque quiere capturar la wea*/
    private void thisMouseReleased(MouseEvent evt) throws AWTException {
        jdialog.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); //canbio el cursor a normal
        cap = new JCap(rec);// creo el objeto para capturar y le paso el rectangulo
        this.detenerCapturador();//detengo el capturador, que es solo un jdialog.setVisible(false)
        
        //estos dos metodos son de la clase Observable y se llaman para que avisen a todos los Observadores que ha cambiado algo
        // en este caso les aviso a los observadores  que se capturo una imagen. Hay solo un observador que es esta misma clase te acordai?
        //si no te acordai lee el constructor
        setChanged();
        notifyObservers(cap.getCap());
    }

    //este el el primer click cuando quiero capturar pantalla y tengo que guardar el x y de la wea
    private void thisMousePressed(MouseEvent evt) {
        jdialog.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR)); // cambio el cursos mas cachaña
        rec = new Rectangle(new Point(evt.getX(), evt.getY())); // capturo el punto inicial y lo guardo en rec
    }

    
    //metodo para obtener una imagen escalado, ya sea mayor o menor
    //en miwea siempre es mayor a 1 asi que no hay mayor analisis
    private BufferedImage getImagenEscalada(BufferedImage im, int escala){
        BufferedImage bi = new BufferedImage(im.getWidth()*escala, im.getHeight()*escala, BufferedImage.TYPE_INT_RGB);
        Image ima = ((Image)im).getScaledInstance(bi.getWidth(), bi.getHeight(), Image.SCALE_SMOOTH);
        bi.getGraphics().drawImage(ima, 0, 0, null);
        return bi;
    }
    
    private BufferedImage suavizarImagen(BufferedImage im){
        // Define un kernel suavizador
        float ninth = 1.0f / 9.0f;
        float[] blurKernel = {
            ninth, ninth, ninth, ninth, ninth, ninth,
            ninth, ninth, ninth
        };
        
        // crea una operación de convolución a partir del kernel
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, blurKernel));
        // Filtra la imagen usando la operacion
        return op.filter(im, null);
    }
}
