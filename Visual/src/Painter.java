import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.imageio.ImageIO;

public class Painter {
	private Word[] words; // keywords to paint
	private final static String fontfile = "res/font.ttf"; // Font
	private static int height = 900; // height of the picture
	private static int width = 1600; // width of the picture
	Window window; // monitor window
	private BufferedImage img;
	Graphics g;
	private final static FontRenderContext context = new FontRenderContext (null, false, false);
	private static Point p_cen;
	private static Point min_size=new Point(0,0);
	private Bound bound;
	Shape bound_shape;
	
	public int max(int a,int b)
	{
		if(a>=b)return a;
		else return b;
	}
	public Painter(Word[] result,Window w) 
	{
		window = w;
		if(w.bimg!=null){
			height=w.bimg.getHeight();
			width=w.bimg.getWidth();
		}
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		p_cen=new Point(width/2,height/2);
		bound=new Bound(2,width,height);
		bound_shape=bound.get_shape();
		words = result;
		g = img.createGraphics();
		g.fillRect(0, 0, width, height); // Fill the picture with white
		window.set_img(img);
	}

	public void paint() throws IOException
	{
		long startTime = new Date().getTime();
		if (words.length  == 0)
		{
			System.out.println("No Keywords Found!");
			return;
		}
		System.out.println("in paint");
		reset_count(); // Reset make is the size of the font
		
		for (int i = 0; i < 200 && i < words.length; i++)
		{
			paint_str(words[i]); // paint the keywords one by one
			System.out.println((i + 1 ) + " / " + words.length + " done.");
			window.update();
		}		
		window.set_background();
		window.update();
		// Save the picture
		File outputfile = new File("res/output.gif");  
        ImageIO.write(img, "gif", outputfile);	
        
        
		System.out.println("Paint Successful!");   
		long endTime = new Date().getTime();
		System.out.println("Time used: " + (endTime - startTime) / 1000 + " s" );
	}
	//according to the frequency of word determine the size of font.
	private void reset_count() 
	{
		int sum = 0; // The sum of all the keywords found
		for (int i = 0; i < words.length; i++) sum += words[i].get_count();
		for (int i = 0; i < words.length; i++) 
		{
			int temp = words[i].get_count() * 150 / sum + 180 - 5 * i; // Function to determine the font size
			if (temp < 30) temp = 30; // Minimum size 
			else if (temp > 255) temp = 255; // Maximum size
			words[i].set_count(temp);
		}
		
	}
	
	private boolean paint_str(Word word)
	{
		//Graphics2D g2=(Graphics2D) g;
		//g2.fill(bound_shape);
		// Set the font
		Font font = new Font(fontfile, Font.BOLD, word.get_count());
		g.setFont(font);
		// Get the bounds of the string
		Rectangle2D  bounds = g.getFont().getStringBounds (word.get_str(), context);
		// Try to find an empty space of the string
		Point position = search_space(bounds);
		if(position.x>width)return false;
		
		// Set the colour of the string which is related to its position
		g.setColor(new Color(
				 max( position.x/5>220 ? 220 : position.x/5 , 100 ),
				 max( position.y/5>220 ? 220 : position.y/5 , 100 ),
				 max( (position.x+position.y)/10>220 ? 220 : (position.x+position.y)/10 , 100 )
				 ));
		
		// Draw the string
		g.drawString(word.get_str(), (int) (position.x + 5 - bounds.getMinX()), (int) (position.y + 5 - bounds.getMinY()));
		return true;	
	}
	
	private Point search_space(Rectangle2D bounds)
	{		
		// The bounds of the string
		int str_X = (int) (bounds.getMaxX() - bounds.getMinX()) + 10;
		int str_Y = (int) (bounds.getMaxY() - bounds.getMinY()) + 10;

		int loop=1;
		int step=(int)(0.05*str_Y);
		if(step<1)step=1;
		int y=p_cen.y-loop;	
		int x=p_cen.x-loop;
		// The starting position of x and y
		int init_Y = y;
		int init_X = x;
		int left_bound=0;
		int right_bound=0;
		int up_bound=0;
		int low_bound=0;
		
		do
		{
			boolean found = true;
			//System.out.println(str_X+" "+str_Y+" "+min_size+" "+step);
			
			if(min_size.x!=0){
				if(str_X>=min_size.x&&str_Y>=min_size.y)
				{
					found=false;
					break;
				}
				if(min_size.y==44)break;
			}
			for (int i = 0; i < str_Y; i++)
			{
				for (int j = 0; j < str_X; j++)
				{
					if ( !bound_shape.contains(x + j-str_X/2, y + i -str_Y/2) ) {
						found=false;
						break;
					}
					if (x + j-str_X/2 >= img.getWidth()) {
						found = false;
						break;
					}
					if (y + i -str_Y/2 >= img.getHeight()) {
						found = false;
						break;
					}
					if (img.getRGB(x + j -str_X/2, y + i -str_Y/2) != Color.white.getRGB()) 
					{
						found = false;
						i = str_Y;
						break;
					}
				}
			}
			if (found) return new Point(x-str_X/2, y-str_Y/2);
			left_bound=p_cen.x-loop;
			right_bound=p_cen.x+loop;
			low_bound=p_cen.y-loop;
			up_bound=p_cen.y+loop;
			if(low_bound<=str_Y/2)low_bound=step+str_Y/2;
			if(up_bound>=height-str_Y/2)up_bound=height-str_Y/2;
			if(x<=left_bound)
			{
				if(y>low_bound)y=y-step;
				else x=x+step;
			}else if(x>=right_bound)
			{ 
				if(y<up_bound)y=y+step;
				else x=x-step;
			}else
			{
				if(y<=low_bound)
				{
					x=x+step;
				}else if(y>=up_bound)
				{
					x=x-step;
				}else{
					x=x+step;
				}
			}
			//System.out.println(x+" "+y+" "+init_X+" "+init_Y+" "+loop);
			//System.out.println(x+" "+y);
			//System.out.println();
			if(x<=init_X&&y<=init_Y)
			{
				init_X=left_bound-step;
				init_Y=low_bound-step;
				if(init_Y<=str_Y/2)init_Y=step+str_Y/2;
				loop=loop+step;
				System.out.println("loop "+loop);
			}
			
		}	while (x<width-str_X/2);	
		
		System.out.println("Error! No space available!");
		
			if (min_size.x==0) min_size.x=str_X;
			if (min_size.x>str_X) min_size.x=str_X;
			if(min_size.y==0) min_size.y=str_Y;
			if(min_size.y>str_Y) min_size.y=str_Y;
		
		
		// Some where outside the picture
		return new Point(width + 100, height + 100);
	}	

}
