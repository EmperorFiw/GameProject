
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
public class Button_ extends JButton{
   
    public Font getFont(int s){
            return new Font_all().font_Tahoma(s);
    }

    public Button_(String name,int w,int h,Color c,int s){
        setText(name);
        setPreferredSize(new Dimension(w,h));
        setFont(getFont(s));
        setBackground(c);
    }


}