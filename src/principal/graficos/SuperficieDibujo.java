package principal.graficos;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import principal.controles.Raton;
import principal.maquinaestado.GestorEstados;
import principal.utilidades.Constantes;
import principal.utilidades.DibujoDebug;

public class SuperficieDibujo extends Canvas {

    private static final long serialVersionUID = -2303469561959410099L;
    
    private static SuperficieDibujo sd;
    public final Raton RATON;
    
    // Cache de fuentes para evitar instanciar fuentes en cada frame
    private static final Font FUENTE_DEBUG = new Font("SansSerif", Font.PLAIN, 9);

    private SuperficieDibujo(final int ancho, final int alto) {
        this.RATON = Constantes.RATON;
        this.setIgnoreRepaint(true);
        this.setPreferredSize(new Dimension(ancho, alto));
        this.addKeyListener(Constantes.TECLADO);
        this.addMouseListener(RATON);
        this.setFocusable(true);
        this.requestFocus();
    }

    // Corregido el typo: obtenerSuperficieDibujo
    public static SuperficieDibujo obtenerSuperficieDibujo() {
        if (sd == null) {
            sd = new SuperficieDibujo(Constantes.ANCHO_PANTALLA_COMPLETA, Constantes.ALTO_PANTALLA_COMPLETA);
        }
        return sd;
    }

    public void actualizar() {
        // Reservado para actualizar lógica propia de la superficie si fuera necesario
    }

    public void pintar(final GestorEstados ge) {
        final BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
            this.createBufferStrategy(3);
            return;
        }

        DibujoDebug.reiniciarContadorObjetos();
        
        final Graphics2D g = (Graphics2D) buffer.getDrawGraphics();

        try {
            // Aplicar render hints de rendimiento/estética
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            
            g.setFont(FUENTE_DEBUG);

            // Escalado según la resolución objetivo
            if (Constantes.FACTOR_ESCALADO_X != 1.0 || Constantes.FACTOR_ESCALADO_Y != 1.0) {
                g.scale(Constantes.FACTOR_ESCALADO_X, Constantes.FACTOR_ESCALADO_Y);
            }

            // --------- Comienzo del dibujado -----------
            
            // Vaciado/Limpieza del lienzo
            DibujoDebug.dibujarRectanguloRelleno(g, 0, 0, Constantes.ANCHO_JUEGO, Constantes.ALTO_JUEGO, Color.BLACK);

            // Dibujado de la máquina de estados (pantallas del juego)
            ge.pintar(g);

            // Capa de depuración (Debug UI)
            g.setColor(Color.GREEN);
            DibujoDebug.dibujarString(g, "APS: " + Constantes.GLOBALES.aps, 20, 35);
            DibujoDebug.dibujarString(g, "FPS: " + Constantes.GLOBALES.fps, 20, 50);
            DibujoDebug.dibujarString(g, "OPF: " + (DibujoDebug.getContadorObjetos() + 1), 20, 65);

        } finally {
            // Aseguramos que la liberación del contexto y el intercambio de buffers 
            // ocurran siempre, incluso si hay una excepción en el dibujado.
            g.dispose();
        }

        buffer.show();
        Toolkit.getDefaultToolkit().sync();
    }
}