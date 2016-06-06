package ch.fhnw.pstark.woipv;
import java.awt.*;
import java.awt.event.*;

public class AwtTestFrame1b extends Panel {

	private final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	private final int line = 25;

	AwtTestFrame1b() {
		setBackground(SystemColor.control);
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		int py = 0;

		py = paintText(g2d, py, null, false);
		py = paintText(g2d, py, null, true);
		py += line;

		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, false);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, false);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_ON, false);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP, false);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB, false);
		py += line;

		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, true);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, true);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_ON, true);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP, true);
		py = paintText(g2d, py, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB, true);
		py += line;
	}

	private int paintText(Graphics2D g2d, int py, Object val, boolean aa) {
		Graphics2D dgc = g2d;
		char[] txt = ("The quick brown fox jumped over the lazy dog (" + val + ", General AA: " + aa + ")")
				.toCharArray();
		Image img = null;

		GraphicsConfiguration cfg = getGraphicsConfiguration();
		img = cfg.createCompatibleImage(getWidth(), line);
		dgc = (Graphics2D) img.getGraphics();
		dgc.setColor(getBackground());
		dgc.fillRect(0, 0, getWidth(), line);
		dgc.setColor(g2d.getColor());
		

		if (aa) {
			dgc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			dgc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		if (val != null) {
			dgc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, val);
		}
		dgc.setFont(font);

		dgc.drawChars(txt, 0, txt.length, 10, line - 5);
		
		
		Font foo = new Font("Courier", Font.BOLD, 24);
		System.out.println(foo.getStringBounds("A", dgc.getFontRenderContext()).getHeight());
		System.out.println(foo.getStringBounds("A", dgc.getFontRenderContext()).getWidth());
		
		//foo.
		
		dgc.setFont(foo);
		dgc.drawChars(txt, 0, txt.length, 10, line - 5);
		g2d.drawImage(img, 0, py, null);

		dgc.dispose();
		img.flush();

		return (py + line);
	}

	public static void main(String[] args) {
		Frame wnd = new Frame("AWT Antialiased Text Sample");

		wnd.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		wnd.add(new AwtTestFrame1b());
		wnd.setSize(new Dimension(1000, 600));
		wnd.setVisible(true);
	}

}