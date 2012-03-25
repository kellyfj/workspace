/*
 * The following is an analog (uses hands) clock that uses
 * buffered drawing to increase speed. In buffered drawing
 * some or all of an images is computed once and stored so
 * that it doesn't have to be redrawn later. In this program
 * the clock face is drawn only once, and the hands are
 * computed.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import javax.swing.event.*;

import java.util.*;

public class AnalogTimer extends JFrame {

    private static final long serialVersionUID = 1L;
    Clock clockFace;
    private JTextField entry;
    private DataEntry dataEntry;
    private JPanel mainPane;

    public static void main(String[] args) {
        JFrame windo = new AnalogTimer();
        windo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        windo.setVisible(true);
    }// end main

    // ========================================================== constructor

    public AnalogTimer() {

        Container content = this.getContentPane();
        content.setLayout(new BorderLayout());
        mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        clockFace = new Clock();
        
        dataEntry = new DataEntry(clockFace,this);
        mainPane.add(dataEntry);
   
        mainPane.add(clockFace);
        content.add(mainPane, BorderLayout.CENTER);
        this.setTitle("Analog Clock");
        this.pack();
       // clockFace.start();
    }// end constructor

}// end class ClockAnalogBuf



class DataEntry extends JPanel {

    protected static final boolean DEBUG = false;

    public DataEntry(final Clock c, final AnalogTimer t) {
       // this.setPreferredSize(new Dimension(100, 300));
        this.setBackground(Color.white);
        this.setForeground(Color.black);

        JLabel label = new JLabel("Time (Minutes)");
        this.add(label);
       // final JTextField time = new JTextField(6);
        final JComboBox time1 = new JComboBox(new Integer[]{1,2,3,4,5,10,15,20,25,30,40,50,60});
        this.add(time1);
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                c.start();
                Calendar now = Calendar.getInstance();
                //System.out.println("Now is : "+now.getTime());
                Integer mins = (Integer) time1.getSelectedItem();
                if(DEBUG) System.out.println(mins);
                //int minutesToRun = new Integer(t);
                c.secondsToRun = mins*60;
            }
        });
        this.add(startButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                c.stop();
            }
        });
        this.add(stopButton);
    }// end constructor
}

class Clock extends JPanel {

    //private int hours = 0;
    //private int minutes = 0;
    //private int seconds = 0;
    //private int millis = 0;
    public int secondsToRun=60;
    private int currentSeconds=0;
    private static final int spacing = 50;
    private static final float twoPi = (float) (2.0 * Math.PI);
    private static final float threePi = (float) (3.0 * Math.PI);

    // Angles for the trigonometric functions are measured in radians.
    // The following in the number of radians per sec or min.

    private static final float radPerSecMin = (float) (Math.PI / 30.0);
    private static final boolean DEBUG = false;
    private int clockSize; // height and width of clock face
    private int centerX; // x coord of middle of clock
    private int centerY; // y coord of middle of clock

    private BufferedImage clockImage;

    private javax.swing.Timer t;

    // ==================================================== Clock constructor

    public Clock() {
        this.setPreferredSize(new Dimension(300, 300));
        this.setBackground(Color.white);
        this.setForeground(Color.black);

        t = new javax.swing.Timer(1000,
        new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                update();
            }

        });

    }// end constructor


    // Replace the default update so that the plain background
    // doesn't get drawn.
    public void update() {
        this.repaint();
    }// end update

    // ================================================================ start
    public void start() {
        t.start(); // start the timer
    }

    // ================================================================= stop
    public void stop() {
        t.stop(); // start the timer
    }// end stop

    // ======================================================= paintComponent

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g); // paint background, borders

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

        // The panel may have been resized, get current dimensions

        int w = getWidth();
        int h = getHeight();

        clockSize = ((w < h) ? w : h) - 2 * spacing;

        centerX = clockSize / 2 + spacing;
        centerY = clockSize / 2 + spacing;

        // Create the clock face background image if this is the first time,

        // or if the size of the panel has changed

        if (clockImage == null || clockImage.getWidth() != w
                || clockImage.getHeight() != h) {

            clockImage = (BufferedImage) (this.createImage(w, h));
            // now get a graphics context from this image
            Graphics2D gc = clockImage.createGraphics();
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
            drawClockFace(gc);
        }

        // Now get the time and draw the hands.

        Calendar now = Calendar.getInstance();
        //hours = now.get(Calendar.HOUR);
        //minutes = now.get(Calendar.MINUTE);
        //seconds = now.get(Calendar.SECOND);
        //millis = now.get(Calendar.MILLISECOND);

        // Draw the clock face from the precomputed image

        g2.drawImage(clockImage, null, 0, 0);
        // Draw the clock hands
        //drawClockHands(g);
        currentSeconds++;
        int angle  =-360*currentSeconds/secondsToRun;
        if(DEBUG) System.out.println("Elapsed time: "+currentSeconds+" of "+secondsToRun + ". Angle = "+angle);

        drawArc(g, angle);
        
        if(currentSeconds==secondsToRun)
            this.stop();
    }// end paintComponent

    // ======================================================= drawClockHands

    private void drawClockHands(Graphics g) {
        drawArc(g, currentSeconds);
    }// end drawClockHands

    // ======================================================== drawClockFace

    private void drawClockFace(Graphics g) {

        // clock face
        g.setColor(Color.green);
        g.fillOval(spacing, spacing, clockSize, clockSize);
        g.setColor(Color.black);
        g.drawOval(spacing, spacing, clockSize, clockSize);

        // tic marks
/*
        for (int sec = 0; sec < 60; sec++) {
            int ticStart;
            if (sec % 5 == 0) {
                ticStart = size / 2 - 10;
            } else {
                ticStart = size / 2 - 5;
            }

            drawRadius(g, centerX, centerY, radPerSecMin * sec, ticStart,
                    size / 2);
        } */

    }// endmethod drawClockFace

    // =========================================================== drawRadius

    private void drawArc(Graphics g, int angle) {

                g.setColor (Color.red);  
                //g.drawLine(x + dxmin, y + dymin, x + dxmax, y + dymax);
               // g.fillArc(x, y,  x+dxmax, y+dymax, 270, (int)angle);
                g.fillArc (0, 0, 300,300, 90, angle);
            }// end drawRadius
    
}