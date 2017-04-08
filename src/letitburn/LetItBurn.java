/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package letitburn;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.Timer;
import asciiPanel.AsciiPanel;
import asciiPanel.AsciiFont;

/**
 *
 * @author Robin
 */
public class LetItBurn extends JFrame implements ActionListener {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LetItBurn window = new LetItBurn();
        window.setIconImage(new ImageIcon("fire_icon.png").getImage());
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
    
    private static final int XPADDING = 5;
    private static final int YPADDING = 5;
    private static final String MAPINPUT = 
            "#############/#\n" +
            "#     |       #\n" +
            "#     #       #\n" +
            "#     #       #\n" +
            "#######       #\n" +
            "#     _       #\n" +
            "###############";
    private static final String SMOKEINPUT = 
            "1 1\n" +
            "1 2\n" +
            "1 3\n" +
            "5 6\n" +
            "2 4\n" +
            "1 1\n" +
            "1 2\n" +
            "5 5\n" +
            "5 5\n" +
            "9 1\n" +
            "7 5\n" +
            "7 1\n" +
            "9 1\n" +
            "10 1\n" +
            "9 1\n" +
            "12 5\n" +
            "11 5\n" +
            "11 4\n" +
            "11 3\n" +
            "11 2\n" +
            "9 1\n" +
            "9 1";
    private final char[][] map;
    private final int[][] smoke;
    private int smokeIndex = 0;
    private final AsciiPanel terminal;
    private int width;
    private int height;
    private List<int[]> ignitedTiles;
    
    public LetItBurn() {
        super("Let It Burn v. 1.0.0"); 
        setResizable(false);
        map = loadMap();
        smoke = loadSmoke();
        terminal = new AsciiPanel(width + (XPADDING * 2), height + (YPADDING * 2), AsciiFont.CP437_16x16);
        drawMap();
        add(terminal);
        pack();
        
        Timer timer = new Timer(1000, this);
        timer.setDelay(500);
        timer.start();
    }
    
    private void addSmoke(int x, int y) {
        ignitedTiles = new ArrayList<>();
        if (map[x][y] == ' ') {
            map[x][y] = 'S';
            igniteAround(x, y, false, false);
        } else if (map[x][y] == 'S') {
            ignite(x, y);
        } else if (map[x][y] == 'F') {
            explode(x, y);
        }
    }
    
    private void ignite(int x, int y) {
        map[x][y] = 'F';
        igniteAround(x, y);
    }
    
    private void igniteAround(int x, int y) {
        igniteAround(x, y, true, false);
    }
    
    private void igniteAround(int x, int y, boolean triggered, boolean ignore) {
        int[][] dirs = getAroundCoordinates(x, y);
        for (int[] dir : dirs) {
            x = dir[0];
            y = dir[1];
            if (IsInvalidMapCoord(x, y)) {
                continue;
            }
            if (!triggered) {
                switch (map[x][y]) {
                    case 'F':
                        igniteAround(x, y);
                        break;
                    case '_':
                    case '/':
                        if (!ignore)
                            igniteAround(x, y, false, true);
                        break;
                }
                if (map[x][y] == 'F') {
                    igniteAround(x, y);
                }
            } else {
                switch (map[x][y]) {
                    case 'S':
                        ignite(x, y);
                        break;
                    case '_':
                    case '/':
                        if (!ignore)
                            igniteAround(x, y, triggered, true);
                        break;
                }
            }
        }
    }
    
    private void explode(int x, int y) {
        int[][] dirs = getAroundCoordinates(x, y);
        for (int[] dir : dirs) {
            explodeInDir(dir[0], dir[1], x - dir[0], y - dir[1]);
        }
    }
    
    private void explodeInDir(int x, int y, int changeX, int changeY) {
        if (IsInvalidMapCoord(x, y)) {
            return;
        }
        switch (map[x][y]) {
            case 'F':
            case '/':
            case '_':
                explodeInDir(x + changeX, y + changeY, changeX, changeY);
                break;
            case '#':
                map[x][y] = '=';
                break;
            case '|':
            case '=':
                map[x][y] = '_';
                break;
            case ' ':
                ignite(x, y);
                break;
            
        }
    }
    
    private int[][] getAroundCoordinates(int x, int y) {
        int[][] dirs = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};
        return dirs;
    }
    
    private boolean IsInvalidMapCoord(int x, int y) {
        return x < 0 || x >= map.length || y < 0 || y >= map[0].length;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (smokeIndex < smoke.length) {
            addSmoke(smoke[smokeIndex][0], smoke[smokeIndex][1]);
            drawMap();
            repaint();
            smokeIndex++;
        }
    }
    
    public void repaint() {
        terminal.clear();
        drawMap();
        super.repaint();
    }
    
    private char[][] loadMap() {
        width = MAPINPUT.indexOf("\n");
        height = MAPINPUT.split("\n").length;
        char[][] map = new char[width][height];
        int x = 0;
        int y = 0;
        for (char c : MAPINPUT.toCharArray()) {
            if (c == '\n') {
                y++;
                x = 0;
            } else {
                map[x][y] = c;
                x++;
            }
        }
        return map;
    }
    
    private int[][] loadSmoke() {
        int[][] smoke = new int[SMOKEINPUT.split("\n").length][2];
        int x = 0;
        for (String s : SMOKEINPUT.split("\n")) {
            smoke[x][0] = Integer.parseInt(s.split(" ")[0]);
            smoke[x][1] = Integer.parseInt(s.split(" ")[1]);
            x++;
        }
        return smoke;
    }
    
    private void drawMap() {
        terminal.clear(' ', AsciiPanel.white, new Color(20, 119, 36));
        Color backgroundColor;
        Color foregroundColor;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                switch (map[x][y]) {
                    case '#':
                        foregroundColor = new Color(49, 10, 1);
                        backgroundColor = new Color(109, 20, 11);
                        break;
                    case 'S':
                        foregroundColor = new Color(145, 150, 146);
                        backgroundColor = new Color(204, 209, 205);
                        break;
                    case 'F':
                        foregroundColor = new Color(255, 114, 0);
                        backgroundColor = new Color(255, 0, 0);
                        break;
                    case '=':
                        foregroundColor = new Color(49, 10, 1);
                        backgroundColor = new Color(159, 40, 11);
                        break;
                    case '_':
                        foregroundColor = AsciiPanel.black;
                        backgroundColor = new Color(89, 89, 89);
                        break;
                    case '|':
                    case '/':
                    case ' ':
                    default:
                        foregroundColor = AsciiPanel.white;
                        backgroundColor = AsciiPanel.black;
                        break;
                }
                terminal.write(map[x][y], x + XPADDING, y + YPADDING, foregroundColor, backgroundColor);
            }
        }
    }
}
