
package org.example;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author almacro
 */
public class Graphicizer extends Frame implements ActionListener {
    
    BufferedImage bufferedImage, bufferedImageBackup;
    Image image;
    
    Menu menu;
    MenuBar menubar;
    MenuItem menuitemOpen, menuitemSave, menuitemUndo, menuitemExit;
    Button buttonEmboss, buttonSharpen, buttonBrighten, buttonBlur, buttonReduce;
    FileDialog dialog;
    
    Panel imagePanel, buttonPanel;
    
    Graphicizer() {
        setSize(400,360);
        setTitle("The Graphicizer");
        setLayout(new BorderLayout());
        
        imagePanel = new Panel() {
            @Override
            public void paint(Graphics g) {
                if (bufferedImage != null) {
                    g.drawImage(
                            bufferedImage,
                            getSize().width / 2 - bufferedImage.getWidth() / 2,
                            getInsets().top + 20,
                            this
                    );
                }
            }
        };
        add(BorderLayout.CENTER, imagePanel);
        buttonPanel = new Panel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        
        buttonEmboss = new Button("Emboss");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(buttonEmboss, gbc);
        buttonEmboss.addActionListener(this);
        
        buttonSharpen = new Button("Sharpen");
        gbc.gridx = 1;
        buttonPanel.add(buttonSharpen, gbc);
        buttonSharpen.addActionListener(this);
        
        buttonBrighten = new Button("Brighten");
        gbc.gridx = 2;
        buttonPanel.add(buttonBrighten, gbc);
        buttonBrighten.addActionListener(this);
        
        buttonBlur = new Button("Blur");
        gbc.gridx = 3;
        buttonPanel.add(buttonBlur, gbc);
        buttonBlur.addActionListener(this);
        
        buttonReduce = new Button("Reduce");
        gbc.gridx = 4;
        buttonPanel.add(buttonReduce, gbc);
        buttonReduce.addActionListener(this);
        
        add(BorderLayout.SOUTH, buttonPanel);
        setVisible(true);
        
        menubar = new MenuBar();
        menu = new Menu("File");
        menuitemOpen = new MenuItem("Open...");
        menu.add(menuitemOpen);
        menuitemOpen.addActionListener(this);
        
        menuitemSave = new MenuItem("Save As...");
        menu.add(menuitemSave);
        menuitemSave.addActionListener(this);
        
        menuitemUndo = new MenuItem("Undo");
        menu.add(menuitemUndo);
        menuitemUndo.addActionListener(this);
        
        menuitemExit = new MenuItem("Exit");
        menu.add(menuitemExit);
        menuitemExit.addActionListener(this);
        
        menubar.add(menu);
        setMenuBar(menubar);
        
        dialog = new FileDialog(this, "File Dialog");
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
        
    @Override
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == menuitemOpen) {
            dialog.setMode(FileDialog.LOAD);
            dialog.setVisible(true);
            try {
                if (!dialog.getFile().equals("")) {
                    File input = new File(dialog.getDirectory() + dialog.getFile());
                    bufferedImage = ImageIO.read(input);
                    
                    setSize(
                        getInsets().left + getInsets().right + Math.max(400, bufferedImage.getWidth() + 60),
                        getInsets().top + getInsets().bottom + Math.max(340, bufferedImage.getHeight() + 60)
                    );
                                                   
                }
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
            imagePanel.repaint();
        }
        
        if (ev.getSource() == menuitemSave) {
            dialog.setMode(FileDialog.SAVE);
            dialog.setVisible(true);
            try {
                if (!dialog.getFile().equals("")) {
                    String outfile = dialog.getFile();
                    File outputFile = new File(dialog.getDirectory()+outfile);
                    final int len = outfile.length();
                    ImageIO.write(
                            bufferedImage, 
                            outfile.substring(len - 3, len), 
                            outputFile
                    );
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        if (ev.getSource() == buttonEmboss) {
            bufferedImageBackup = bufferedImage;
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            int[] pixels = new int[width*height];
            PixelGrabber pg = new PixelGrabber(
                    bufferedImage, 
                    0, 0,
                    width, height,
                    pixels,
                    0, width
            );
            try {
                pg.grabPixels();
            } catch(InterruptedException e) {
                System.out.println(e.getMessage());
            }
            
            for(int x = 0; x <= 1; x++) {
                for(int y = 0; y < height - 1; y++) {
                    pixels[x + y * width] = 0x88888888;
                }
            }
            for (int x = width - 2; x <= width - 1; x++) {
                for(int y = 0; y < height - 1; y++) {
                    pixels[x + y * width] = 0x88888888;
                }

            }
            for(int x = 0; x <= width - 1; x++) {
                for(int y = 0; y <= 1; y++) {
                    pixels[x + y * width] = 0x88888888;
                }
            }
            
            for (int x = 2; x < width - 1; x++) {
                for (int y = 2; y < height - 1; y++) {
                    int red = ((pixels[(x+1) + y * width + 1] & 0xFF) 
                            - (pixels[x + y * width] & 0xFF)) + 128;
                    int green = (((pixels[(x+1) + y * width + 1] & 0xFF00) / 0x100) % 0x100
                            - ((pixels[x + y * width] & 0xFF00) / 0x100) % 0x100) + 128;
                    int blue = (((pixels[(x+1) + y * width + 1] & 0xFF0000) / 0x10000) % 0x100
                            - ((pixels[x + y * width] & 0xFF0000) / 0x10000) % 0x100) + 128;
                    int avg = (red + green + blue) / 3;
                    pixels[x + y * width] = (0xff000000 | avg << 16 | avg << 8 | avg);
                    
                    image = createImage(new MemoryImageSource(
                            width, height,
                            pixels,
                            0, width
                    ));
                    bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
                    bufferedImage.createGraphics().drawImage(image, 0, 0, this);
                    imagePanel.repaint();
                }
            }
        }
        
        if (ev.getSource() == buttonSharpen) {
            bufferedImageBackup = bufferedImage;
            Kernel kernel = new Kernel(
                    3, 3, 
                    new float[]{
                        0.0f, -1.0f, 0.0f,
                        -1.0f, 5.0f, -1.0f,
                        0.0f, -1.0f, 0.0f
                    });
            ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            
            BufferedImage temp = new BufferedImage(
                    bufferedImage.getWidth(), bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            convolveOp.filter(bufferedImage, temp);
            bufferedImage = temp;
            imagePanel.repaint();
        }
        
        if(ev.getSource() == buttonBrighten) {
            bufferedImageBackup = bufferedImage;
            Kernel kernel = new Kernel(1, 1, new float[]{3});
            ConvolveOp convolveOp = new ConvolveOp(kernel);
            BufferedImage temp = new BufferedImage(
                    bufferedImage.getWidth(), bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            convolveOp.filter(bufferedImage, temp);
            bufferedImage = temp;
            imagePanel.repaint();
        }

        if (ev.getSource() == buttonBlur) {
            bufferedImageBackup = bufferedImage;
            Kernel kernel = new Kernel(
                    3, 3, 
                    new float[]{
                        0.25f, 0, 0.25f,
                        0,     0, 0,
                        0.25f, 0, 0.25f
                    });
            ConvolveOp convolveOp = new ConvolveOp(kernel);
            
            BufferedImage temp = new BufferedImage(
                    bufferedImage.getWidth(), bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            convolveOp.filter(bufferedImage, temp);
            bufferedImage = temp;
            imagePanel.repaint();
        }
        
        if (ev.getSource() == buttonReduce) {
            bufferedImageBackup = bufferedImage;
            image = bufferedImage.getScaledInstance(
                    bufferedImage.getWidth()/2, bufferedImage.getHeight()/2, 0
            );
            bufferedImage = new BufferedImage(
                    bufferedImage.getWidth()/2, bufferedImage.getHeight()/2, 
                    BufferedImage.TYPE_INT_BGR);
            bufferedImage.createGraphics().drawImage(image, 0, 0, this);
            
            setSize(getInsets().left + getInsets().right + Math.max(400, bufferedImage.getWidth() + 60),
                    getInsets().top + getInsets().bottom + Math.max(340, bufferedImage.getHeight() + 60));
            imagePanel.repaint();
        }

        if(ev.getSource() == menuitemUndo) {
            if(bufferedImageBackup != null) {
                bufferedImage = bufferedImageBackup;
                setSize(getInsets().left + getInsets().right
                        + Math.max(400, bufferedImage.getWidth() + 60),
                        getInsets().top + getInsets().bottom
                        + Math.max(340, bufferedImage.getHeight() + 60));
                imagePanel.repaint();
            }
        }
        
        if(ev.getSource() == menuitemExit) {
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        new Graphicizer();
    }
}
