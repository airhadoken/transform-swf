package acceptance;

import java.io.File;
import java.io.IOException;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.zip.DataFormatException;

import java.awt.GraphicsEnvironment;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.flagstone.transform.factory.font.Font;
import com.flagstone.transform.movie.Background;
import com.flagstone.transform.movie.Movie;
import com.flagstone.transform.movie.Place2;
import com.flagstone.transform.movie.ShowFrame;
import com.flagstone.transform.movie.datatype.Bounds;
import com.flagstone.transform.movie.datatype.ColorTable;
import com.flagstone.transform.movie.font.DefineFont2;
import com.flagstone.transform.movie.text.DefineTextField;

public final class ShowAWTFontTest
{
	private static Set<Character> set;
	private static String alphabet;

	private static int width = 6000;
	private static int height = 1000;
	private static int margin = 400;
	private static int fontSize = 360;   
    
	private static int screenWidth = width + margin;
	private static int screenHeight = height + margin;

	@BeforeClass
	public static void setup()
	{
    	set = new LinkedHashSet<Character>();
    	alphabet = "abcdefghijklmnopqrstuvwxyz";
    	
    	for (int i=0; i<alphabet.length(); i++) {
    		set.add(alphabet.charAt(i));
    	}   	
	}
	
    @Test
    public void showAWT() throws IOException, DataFormatException
    {
        File destDir = new File("test/results/ShowAWTFontTest");
		File destFile;
    	
        if (destDir.exists() == false && destDir.mkdirs() == false) {
        	fail();
        }
        
		Font font;
    	DefineFont2 definition;
   		
        java.awt.Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

	    for (int i=0; i<fonts.length; i++) 
	    {
	    	try
	    	{
	        	font = new Font();
	        	font.decode(fonts[i]);
		    	destFile = new File(destDir, fonts[i].getFontName() + ".swf");
	            definition = font.defineFont(1, set);
	            showFont(definition, destFile);
	    	}
	    	catch (Throwable t)
	    	{
	    		System.err.println("Could not decode: "+fonts[i].getName());
	    	}
		}
    }
    
    private void showFont(DefineFont2 font, File file) throws IOException, DataFormatException
    {
        Movie movie = new Movie();

        DefineTextField text = new DefineTextField(font.getIdentifier()+1, new Bounds(0, 0, width, height), 
        				"var", alphabet);
        
        text.setUseFontGlyphs(true);
        text.setFontIdentifier(font.getIdentifier());
        text.setFontHeight(fontSize);
        text.setColor(ColorTable.black());
        
        movie.setFrameSize(new Bounds(0, 0, screenWidth, screenHeight));
        movie.setFrameRate(1.0f);
        movie.add(new Background(ColorTable.lightblue()));
        movie.add(font);
        movie.add(text);
        movie.add(new Place2(text.getIdentifier(), 1, margin , margin));
        movie.add(ShowFrame.getInstance());
        movie.encodeToFile(file.getPath());
    }
}