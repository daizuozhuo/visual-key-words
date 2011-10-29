import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

public class Painter {
	private Vector<Word> words; // keywords to paint
	private final static String fontfile = "res/font.ttf"; // Font
	private BufferedImage img;
	Graphics2D g;
	private final static FontRenderContext context = new FontRenderContext (null, false, false);
	
	private static Point p_cen;
	private static final int max_num = 300;
	private static final int font_min = 20;
	private static final int font_max = 50;
	private static Point min_size;
	private Bound bound;
	private Shape bound_shape;
	Font font;
	Font fontBase;

	private final int height; // height of the picture
	private final int width; // width of the picture	
	private final boolean update; 
	
//	private int max(int a,int b)
//	{
//		if(a>=b)return a;
//		else return b;
//	}
	
	public Painter(Vector<Word> result, int width, int height, boolean update) 
	{
		this.width = width;
		this.height = height;
		this.update = update;
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		p_cen=new Point(width / 2, height / 2);
		min_size  = new Point(0,0);
		
		//set shape
		bound=new Bound(5, width, height);
		bound_shape=bound.get_shape();
		
		words = result;
		g = img.createGraphics();
		g.fillRect(0, 0, width, height); // Fill the picture with white
		try 
		{
			InputStream myStream = new BufferedInputStream(new FileInputStream(fontfile));
			fontBase = Font.createFont(Font.TRUETYPE_FONT, myStream);
		}
		catch (Exception ex)
		{
	        ex.printStackTrace();
	    }
	}

	public String paint()
	{
		long startTime = new Date().getTime();
		if (words.size() == 0)
		{
			return "No Keywords Found!";
		}

		int total = 0;
		int drawn = 0;
		setSize(); // Reset make is the size of the font

		//if (update) wordle.update(0, 0, width, height);
		
		for (int i = 0; i < max_num && i < words.size(); i++)
		{

			if (paintStr(i, i > 5 ? false : false) == 0)
			{
				words.remove(i);
				i--;
			}
			else
			{
				drawn ++;
			}
			System.out.println((i + 1 ) + " / " + words.size() + " done. Size: " + words.get(i).getSize());
			total++;
		}		
		for (int i = max_num; i < words.size(); i++)
		{
			// remove the rest of the array;
			words.remove(i);
			i--;
		}
		//wordle.repaint();
		System.out.println("Paint Successful!");   
		long endTime = new Date().getTime();
		return drawn + " / " + total + " drawn. \nTime used: " + (endTime - startTime) / 1000 + "." + (endTime - startTime) % 1000 + " s.";
	}
	
	public BufferedImage getImg()
	{
		return img;
	}
	
	//according to the frequency of word determine the size of font.
	private void setSize() 
	{
		int sum = 0; // The sum of all the keywords found
		for (int i = 0; i < words.size(); i++) sum += words.get(i).getCount();
		for (int i = 0; i < words.size(); i++) 
		{
			int temp = words.get(i).getCount() * 130 / sum + 150 - 5 * i; // Function to determine the font size
			if (temp < font_min) temp = font_min; // Minimum size 
			else if (temp > font_max) temp = font_max; // Maximum size
			words.get(i).setSize(temp);
		}
		
	}
	
	
	private int paintStr(int i, boolean checkNear)
	{
		// Set the font
		try {
			font = fontBase.deriveFont(Font.PLAIN, words.get(i).getSize());
	      } catch (Exception ex) {
	        ex.printStackTrace();
	      }
		g.setFont(font);
		
		// Get the bounds of the string
		Rectangle2D  bounds = g.getFont().getStringBounds (words.get(i).getStr(), context);
		// Try to find an empty space of the string
		Point position;	
		if (checkNear)
		{
			position = searchSpace(bounds, true);
			if(position == null) 
			{
				position = searchSpace(bounds, false);	
				if(position == null)
				{
					return 0;
				}
			}	
		}
		else
		{
			position = searchSpace(bounds, false);				
		}
		
		// Set the color of the string which is related to its position
//		g.setColor(new Color(
//				 max( position.x/5>220 ? 220 : position.x/5 , 100 ),
//				 max( position.y/5>220 ? 220 : position.y/5 , 100 ),
//				 max( (position.x+position.y)/10>220 ? 220 : (position.x+position.y)/10 , 100 )
//				 ));
		
		g.setColor(new Color((int)(Math.random() * 15 + 20), (int)(Math.random() * 25 + 109), (int)(Math.random() * 45 + 180), 180));
		
		// Draw the string
		int x = (int) (position.x - bounds.getMinX());
		int y = (int) (position.y - bounds.getMinY());

		//g.rotate(5, width / 2, height / 2); 
		g.drawString(words.get(i).getStr(), x, y);
		words.get(i).setPoint(x, y);
		//if(update) wordle.update(position.x, position.y, (int) (bounds.getMaxX() - bounds.getMinX()), (int) (bounds.getMaxY() - bounds.getMinY()));
		return 1;	
	}
			
	private Point searchSpace(Rectangle2D bounds, boolean checkNear)
	{		
		// The bounds of the string
		int str_X = (int) (bounds.getMaxX() - bounds.getMinX());
		int str_Y = (int) (bounds.getMaxY() - bounds.getMinY());

		int loop=1;
		int step=(int)(0.1*str_Y);
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
			
			//System.out.println(str_X+" "+str_Y+" "+min_size+" "+step);
			
			if(min_size.x!=0){
				if(str_X>=min_size.x&&str_Y>=min_size.y)
				{
					break;
				}
				if(min_size.y==font_min)break;
			}
			if (isEmpty(x-str_X/2, y-str_Y/2, str_X, str_Y, checkNear))
			{
				return new Point(x-str_X/2, y-str_Y/2);
			}
			left_bound=p_cen.x-loop;
			right_bound=p_cen.x+loop;
			low_bound=p_cen.y-loop;
			up_bound=p_cen.y+loop;
			if(low_bound<=str_Y/2)low_bound=step+str_Y/2;
			if(up_bound >= height - str_Y / 2) up_bound = height - str_Y / 2;
			if(x<=left_bound)
			{
				if(y>low_bound)y=y-step;
				else x=x+step;
			}
			else if(x>=right_bound)
			{ 
				if(y<up_bound)y=y+step;
				else x=x-step;
			}
			else
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
			
			if(x<=init_X&&y<=init_Y)
			{
				init_X=left_bound-step;
				init_Y=low_bound-step;
				if(init_Y<=str_Y/2)init_Y=step+str_Y/2;
				loop=loop+step;
//				System.out.println("loop "+loop);
			}
			
		}	while (x < width - str_X / 2);	
		
		System.out.println("Error! No space available!");
		
			if (min_size.x==0) min_size.x=str_X;
			if (min_size.x>str_X) min_size.x=str_X;
			if(min_size.y==0) min_size.y=str_Y;
			if(min_size.y>str_Y) min_size.y=str_Y;
		
		
		// Some where outside the picture
		return null;
	}	
	
	private boolean isEmpty(int x, int y, int str_X, int str_Y, boolean checkNear)
	{
		if (x + str_X >= width || y + str_Y >= height) return false;
		
		int i = 0;
		int j = 0;
		
		for (j = 0; j < str_X; j += 1)
			if (!isInShape(x + j, y + j * str_Y / str_X)) return false;	

		for (j = 0; j < str_X; j += 1)
			if (!isInShape(x + str_X - j, y + j * str_Y / str_X)) return false;		
		
		i = str_Y / 2;
		for (j = 0; j < str_X; j += 1)
			if (!isInShape(x + j, y + i)) return false;	
		
		j = str_X /2;
		for (i = 0; i < str_Y; i += 1)
			if (!isInShape(x + j, y + i)) return false;		
		
		i = 0;
		for (j = 0; j < str_X; j += 1)
			if (!isInShape(x + j, y + i)) return false;
		
		i = str_Y;
		for (j = 0; j < str_X; j += 1)
			if (!isInShape(x + j, y + i)) return false;	
		
		j = 0;
		for (i = 0; i < str_Y; i += 1)
			if (!isInShape(x + j, y + i)) return false;
		
		j = str_X;
		for (i = 0; i < str_Y; i += 1)
			if (!isInShape(x + j, y + i)) return false;		
		
		//		g.drawLine(x, y, x+str_X, y);
//		g.drawLine(x, y, x, y+str_Y);
//		g.drawLine(x+str_X, y, x+str_X, y+str_Y);
//		g.drawLine(x+str_X, y+str_Y, x, y+str_Y);
		// not too far away from other words
		
		//if is the first word;
		if (!checkNear)
		{
			return true;
		}
		x -= 2;
		y -= 2;
		str_X += 4;
		str_Y += 4;
		i = 0;
		int count = 0;
		for (j = 0; j < str_X; j += 1)
			if (isNearWord(x + j, y + i)) 
			{
			count ++;
			break;
			}
		
		i = str_Y;
		for (j = 0; j < str_X; j += 1)
			if (isNearWord(x + j, y + i)) 
			{
			count ++;
			if (count == 2)
			{
//				g.drawOval(x+j,y+i, 3, 3);
				return true;
			}
			break;
			}
		
		j = 0;
		for (i = 0; i < str_Y; i += 1)
			if (isNearWord(x + j, y + i)) 
			{
			count ++;
			if (count == 2)
			{
//				g.drawOval(x+j,y+i, 3, 3);
				return true;
			}
			break;
			}
		
		j = str_X;
		for (i = 0; i < str_Y; i += 1)
			if (isNearWord(x + j, y + i)) 
			{
			count ++;
			if (count == 2)
			{
//				g.drawOval(x+j,y+i, 3, 3);
				return true;
			}
			break;
			}	

		return false;
	}

	private boolean isInShape(int a, int b)
	{
		Point s = new Point(a, b);
		Point2D d = new Point(a ,b);
		d = g.getTransform().transform(s, d);		
		if (img.getRGB((int) d.getX(), (int) d.getY()) == Color.white.getRGB() && bound_shape.contains((int) d.getX(), (int) d.getY())) 
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}
	
	private boolean isNearWord(int a, int b)
	{
		Point s = new Point(a, b);
		Point2D d = new Point(a ,b);
		d = g.getTransform().transform(s, d);	
		if (!bound_shape.contains((int) d.getX(), (int) d.getY()))
		{
			return true;
		}
		if (img.getRGB((int) d.getX(), (int) d.getY()) != Color.white.getRGB()) 
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}

	public void setBackground(BufferedImage bimg)
	{
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		if (bimg != null)
//		{
//			for(int i = 0; i < width; i++) 
//			{
//				for(int j = 0; j < height; j++) 
//				{
//					if(fimg.getRGB(i,j) == Color.white.getRGB())
//					{
//						img.setRGB(i, j, bimg.getRGB(i * bimg.getWidth() / width, j * bimg.getHeight() / height));
//					}
//					else 
//					{
//						img.setRGB(i, j, fimg.getRGB(i, j));
//					}
//				}
//			}
//		}
	}
}