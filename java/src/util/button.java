package util;

import java.awt.*;

public class button {

    public Rectangle button;
    private String text = "";

    private Color color;
    int mx=0,my=0;
    public button(String text, int x, int y, int w, int h, Color color){

        button = new Rectangle(x,y,w,h);
        this.text = text;
        this.color = color;
    }

    public void update(int mx, int my){
        this.mx = mx;
        this.my = my;
    }

    public void draw(Graphics g){
        if(button.intersects(mx,my,1,1)){
            g.setColor(color);
        }else {
            g.setColor(Color.LIGHT_GRAY);
        }
        g.fillRoundRect(button.x, button.y, button.width, button.height, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(button.x, button.y, button.width, button.height, 10, 10);
        g.drawString(text, button.x + 62, button.y+22);
    }

}
