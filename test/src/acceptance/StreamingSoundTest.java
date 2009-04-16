package acceptance;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;


import com.flagstone.transform.factory.sound.SoundFactory;
import com.flagstone.transform.movie.Background;
import com.flagstone.transform.movie.Movie;
import com.flagstone.transform.movie.MovieTag;
import com.flagstone.transform.movie.ShowFrame;
import com.flagstone.transform.movie.datatype.Bounds;
import com.flagstone.transform.movie.datatype.ColorTable;

import static org.junit.Assert.fail;

public class StreamingSoundTest
{
    private float framesPerSecond = 12.0f;

    protected void playSounds(File sourceDir, String[] files, File destDir) throws IOException, DataFormatException
    {
        File sourceFile;
        File destFile;  
        List<MovieTag>stream;
        
        if (!destDir.exists() && !destDir.mkdirs()) {
        	fail();
        }
        
        for (String file : files)
        {
        	sourceFile = new File(sourceDir, file);
        	destFile = new File(destDir, file.substring(0, file.lastIndexOf('.')) + ".swf");
            stream = SoundFactory.streamSound((int)framesPerSecond, sourceFile);
        	playSound(stream, destFile);
        }
    }
    
    protected void playSound(List<MovieTag>stream, File file) throws IOException, DataFormatException
    {
        Movie movie = new Movie();

        movie.setFrameSize(new Bounds(0, 0, 8000, 4000));
        movie.setFrameRate(framesPerSecond);
        movie.add(new Background(ColorTable.lightblue()));

        movie.add(stream.remove(0));

        for (Iterator<MovieTag>i=stream.iterator(); i.hasNext();)
        {
            movie.add(i.next());
            movie.add(ShowFrame.getInstance());
        }

        movie.encodeToFile(file.getPath());
    }
}