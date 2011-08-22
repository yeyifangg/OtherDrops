package com.gmail.zariust.bukkit.otherblocks.data;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;

import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.BlockState;
import org.bukkit.block.NoteBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NoteData implements Data {
	private Note note;

	public NoteData(BlockState state) {
		if(state instanceof NoteBlock)
			note = ((NoteBlock)state).getNote();
	}
	
	public NoteData(Note tone) {
		note = tone;
	}

	@Override
	public int getData() {
		return note.getId();
	}
	
	@Override
	public void setData(int d) {
		note = new Note((byte)d);
	}
	
	@Override
	public boolean matches(Data d) {
		if(!(d instanceof NoteData)) return false;
		return note.equals(((NoteData)d).note);
	}
	
	@Override
	public String get(Material mat) {
		String result = "";
		if(mat == Material.NOTE_BLOCK) {
			result += note.getTone();
			if(note.isSharped()) result += "#";
			result += note.getOctave();
		}
		return result;
	}

	@Override
	public void setOn(BlockState state) {
		if(!(state instanceof NoteBlock)) {
			OtherBlocks.logWarning("Tried to change a note block, but no note block was found!");
			return;
		}
		((NoteBlock)state).setNote(note);
	}

	@Override // Note blocks are not entities, so nothing to do here
	public void setOn(Entity entity, Player witness) {}

	public static Data parse(String state) {
		if(!state.matches("([A-G])(#?)([0-2]?)")) return new SimpleData();
		Note.Tone tone = Note.Tone.valueOf(state.substring(0, 1));
		if(tone == null) return new SimpleData();
		byte octave;
		if(state.matches("..?[0-2]")) octave = Byte.parseByte(state.substring(state.length() - 1));
		else octave = 1;
		Note note = new Note(octave, tone, state.contains("#"));
		return new NoteData(note);
	}
}
