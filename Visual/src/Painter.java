import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
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
	private final static int height = 900; // height of the picture
	private final static int width = 1600; // width of the picture
	private BufferedImage img = new BufferedImage(1600, 900, BufferedImage.TYPE_INT_ARGB);
	Graphics g = img.createGraphics();
	private final static FontRenderContext context = new FontRenderContext (null, false, false);
	
	public Painter(Word[] result) 
	{
		words = result;
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.fillRect(0, 0, 1600, 900); // Fill the picture with white
	}

	public void paint() throws IOException
	{
		long startTime = new Date().getTime();
		if (words.length  == 0)
		{
			System.out.println("No Keywords Found!");
			return;
		}
		
		reset_count(); // Reset make is the size of the font
		
		for (int i = 0; i < 150 && i < words.length; i++)
		{
			paint_str(words[i]); // paint the keywords one by one
			System.out.println((i + 1 ) + " / " + words.length + " done.");
		}		
		
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
			int temp = words[i].get_count() * 150 / sum + 200 - 5 * i; // Function to determine the font size
			if (temp < 30) temp = 30; // Minimum size 
			else if (temp > 255) temp = 255; // Maximum size
			words[i].set_count(temp);
		}
		
	}

	private void paint_str(Word word)
	{
		// Set the font
		Font font = new Font(fontfile, Font.BOLD, word.get_count());
		g.setFont(font);
		
		// Set the color of the string
		g.setColor(new Color(word.get_count() / 4 ,255 - word.get_count(),255 - word.get_count()));
		
		// Get the bounds of the string
		Rectangle2D  bounds = g.getFont().getStringBounds (word.get_str(), context);
		
		// Try to find an empty space of the string
		Point position = search_space(bounds);
		
		// Draw the string
		g.drawString(word.get_str(), (int) (position.x - bounds.getMinX()), (int) (position.y - bounds.getMinY()));
		
//		// The bounds of the string
//		g.drawRect(position.x, position.y, 
//				(int) (bounds.getMaxX() - bounds.getMinX()),
//				(int) (bounds.getMaxY() - bounds.getMinY()));		
	}
	
	private Point search_space(Rectangle2D bounds)
	{		
		// The bounds of the string
		int str_X = (int) (bounds.getMaxX() - bounds.getMinX());
		int str_Y = (int) (bounds.getMaxY() - bounds.getMinY());

		// A random position to start searching
		int x = (int) (Math.random() * (width - str_X));
		int y = (int) (Math.random() * (height - str_Y));
		
		// The starting position of x and y
		int init_Y = y;
		int init_X = x;
		
		do
		{
			boolean found = true;
			for (int i = 0; i < str_Y; i++)
			{
				for (int j = 0; j < str_X; j++)
				{
					if (img.getRGB(x + j, y + i) != Color.white.getRGB()) 
					{
						found = false;
						i = str_Y;
						break;
					}
				}
			}
			if (found) return new Point(x, y);
			x++;
			if (x >= width - str_X)
			{
				x = 0;
				y++;
				if (y >= height - str_Y) y = 0;
			}
		}	while (y != init_Y || x != init_X);	
		
		System.out.println("Error! No space available!");
		
		// Some where outside the picture
		return new Point(width + 100, height + 100);
	}	

}