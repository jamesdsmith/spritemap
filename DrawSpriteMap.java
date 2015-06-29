import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;

public class DrawSpriteMap extends JPanel
{
	protected BufferedImage atlas;

	// These two variables are in units "tiles"
	protected int atlasTilesX;
	protected int atlasTilesY;

	boolean drawDebugLines = true;

	static final int sourceTileSize = 16;
	static final int sourceSpacing = 1;
	static final int tileSize = 32;

	int map[][] = 
	{
		{ 1,1,1,0,0,0,0,0,0,0 },
		{ 1,1,1,1,1,0,0,1,0,0 },
		{ 0,1,1,1,1,0,0,0,0,0 },
		{ 0,1,1,1,0,0,0,0,0,0 },
		{ 0,0,1,0,0,1,1,1,0,0 },
		{ 0,0,1,1,1,1,0,1,0,0 },
		{ 0,0,1,0,1,0,0,1,0,0 },
		{ 0,0,1,1,1,1,1,1,0,0 },
		{ 0,0,1,0,0,0,1,0,0,0 },
		{ 0,0,0,0,0,0,1,0,0,0 }
	};

	// Corner Positions
	protected static final byte UL = 0;
	protected static final byte UR = 1;
	protected static final byte DR = 2;
	protected static final byte DL = 3;

	// Corner Types
	protected static final byte VERTICAL = 0;
	protected static final byte HORIZONTAL = 1;
	protected static final byte CONCAVE = 2;
	protected static final byte CONVEX = 3;
	protected static final byte EMPTY = 4;

	// water tile ids
	protected int[][] waterTiles = 
	{
		{ 59, 3, 2, 115, 60 },
		{ 61, 3, 4, 114, 60 },
		{ 61, 117, 118, 57, 60 },
		{ 59, 117, 116, 58, 60 }
	};

	public DrawSpriteMap()
	{
		try
		{
			atlas = ImageIO.read( new File( "roguelikeSheet_transparent.png" ) );
			atlasTilesX = (atlas.getWidth() + 1) / (sourceTileSize + sourceSpacing);
			atlasTilesY = (atlas.getHeight() + 1) / (sourceTileSize + sourceSpacing);
		}
		catch( IOException ex )
		{
			// report error
		}

		MouseInputAdapter mouseAdapter = new MouseInputAdapter()
		{
			public void mousePressed( MouseEvent e )
			{
				int x = e.getX();
				int y = e.getY();

				int i = y / tileSize;
				int j = x / tileSize;
				//System.out.println( "i: " + i + ", j: " + j );
				map[i][j] = (e.getButton() == MouseEvent.BUTTON1) ? 1 : 0;
				repaint();
			}
		};

		addMouseListener( mouseAdapter );
		addMouseMotionListener( mouseAdapter );
	}

	public void paintComponent( Graphics g )
	{
		Graphics2D g2 = (Graphics2D)g;

		Shape oldClip = g.getClip();

		setBackground( Color.BLACK );
		super.paintComponent( g );

		if( atlas != null )
		{
			// pre-declare this stuff so we dont have to do it hundres/thousands of times
			int[] tileIds = { 62, 62, 62, 62 };
			int u = 0;
			int d = 0;
			int l = 0;
			int r = 0;

			for( int i = 0; i < map.length; ++i )
			{
				for( int j = 0; j < map[i].length; ++j )
				{
					u = Math.max( i - 1, 0 );
					d = Math.min( i + 1, map.length - 1 );
					l = Math.max( j - 1, 0 );
					r = Math.min( j + 1, map[i].length - 1 );

					int type = map[i][j];
					if( type == 1 )
					{
						tileIds[UL] = waterTiles[UL][getCornerType( j, i, u, l, type )];
						tileIds[UR] = waterTiles[UR][getCornerType( j, i, u, r, type )];
						tileIds[DR] = waterTiles[DR][getCornerType( j, i, d, r, type )];
						tileIds[DL] = waterTiles[DL][getCornerType( j, i, d, l, type )];
					}
					else
					{
						// type == 0
						tileIds[UL] = 62;
						tileIds[UR] = 62;
						tileIds[DR] = 62;
						tileIds[DL] = 62;
					}

					drawTile( g, tileIds[UL], (j * tileSize),                  (i * tileSize) );
					drawTile( g, tileIds[UR], (j * tileSize) + sourceTileSize, (i * tileSize) );
					drawTile( g, tileIds[DR], (j * tileSize) + sourceTileSize, (i * tileSize) + sourceTileSize );
					drawTile( g, tileIds[DL], (j * tileSize),                  (i * tileSize) + sourceTileSize );
				}
			}
		}

		g.setClip( oldClip );

		if( drawDebugLines )
		{
			Color oldColor = g.getColor();
			g.setColor( Color.MAGENTA );

			for( int i = 0; i < map.length; ++i )
			{
				g.drawLine( 0, i * tileSize, tileSize * map.length, i * tileSize );
			}

			for( int j = 0; j < map.length; ++j )
			{
				g.drawLine( j * tileSize, 0, j * tileSize, tileSize * map.length );
			}
			g.setColor( oldColor );
		}
	}

	protected void drawTile( Graphics g, int tileId, int x, int y )
	{
		// atlas: 57 x 31

		int tileX = tileId % atlasTilesX;
		int tileY = tileId / atlasTilesX;
		int sx = (tileX * sourceTileSize) + (tileX * sourceSpacing);
		int sy = (tileY * sourceTileSize) + (tileY * sourceSpacing);

		g.setClip( x, y, sourceTileSize, sourceTileSize );
		g.drawImage( atlas, x - sx, y - sy, this );
	}

	/*
	 * TODO: needs optimization
	 */
	protected byte getCornerType( int x, int y, int v, int h, int type )
	{
		if( map[v][x] != type )
		{
			if( map[y][h] != type )
			{
				return CONCAVE;
			}
			else
			{
				return HORIZONTAL;
			}
		}
		else
		{
			if( map[y][h] != type )
			{
				return VERTICAL;
			}
			else if( map[v][h] != type )
			{
				return CONVEX;
			}
			else
			{
				return EMPTY;
			}
		}
	}
}
