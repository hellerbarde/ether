package org.corebounce.resman;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.corebounce.audio.Audio;
import org.corebounce.audio.AudioPanel;
import org.corebounce.io.OSC;
import org.corebounce.soundium.TabPanel;
import org.eclipse.swt.widgets.Composite;

public class Resman extends TabPanel {	
	private final Audio             audio;
	private final OSC               osc;
	private final MetaDB            db;
	private final PreviewFactory    pf;

	public Resman(Audio audio, OSC osc, MetaDB db, PreviewFactory pf) {
		super("Resman");
		this.audio = audio;
		this.osc   = osc;
		this.db    = db;
		this.pf    = pf;
	}

	@Override
	protected void fillContent(Composite panel) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		new BrowserPanel(pf, osc, db).createPartControl(panel);
		new AudioPanel(audio).createPartControl(panel);
	}
}
