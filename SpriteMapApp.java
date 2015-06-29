import java.awt.*;
import javax.swing.*;

public class SpriteMapApp extends JFrame
{
	public static void main( String[] args )
	{
		SpriteMapApp w = new SpriteMapApp();
		w.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		w.setVisible( true );
	}

	public SpriteMapApp()
	{
		super( "Sprite Map Editor" );
		DrawSpriteMap panel = new DrawSpriteMap();

		Dimension size = new Dimension( 320, 320 );
		panel.setPreferredSize( size );
		panel.setMinimumSize( size );

		add( panel );
		pack();
	}
}